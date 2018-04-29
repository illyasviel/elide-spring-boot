/*
 * Copyright (c) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.illyasviel.elide.spring.boot.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yahoo.elide.Elide;
import com.yahoo.elide.ElideSettingsBuilder;
import com.yahoo.elide.annotation.OnCreatePostCommit;
import com.yahoo.elide.annotation.OnCreatePreCommit;
import com.yahoo.elide.annotation.OnCreatePreSecurity;
import com.yahoo.elide.core.DataStore;
import com.yahoo.elide.core.EntityDictionary;
import com.yahoo.elide.core.filter.dialect.RSQLFilterDialect;
import com.yahoo.elide.functions.LifeCycleHook;
import com.yahoo.elide.jsonapi.JsonApiMapper;
import com.yahoo.elide.security.checks.Check;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import javax.persistence.EntityManager;
import org.atteo.classindex.ClassIndex;
import org.hibernate.ScrollMode;
import org.illyasviel.elide.spring.boot.Account;
import org.illyasviel.elide.spring.boot.AccountOnCreatePostCommit;
import org.illyasviel.elide.spring.boot.AccountOnCreatePreCommit;
import org.illyasviel.elide.spring.boot.annotation.ElideCheck;
import org.illyasviel.elide.spring.boot.annotation.ElideHook;
import org.illyasviel.elide.spring.boot.datastore.SpringHibernateDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author olOwOlo
 */
@Configuration
@EnableConfigurationProperties(ElideProperties.class)
@ConditionalOnWebApplication
@AutoConfigureAfter({HibernateJpaAutoConfiguration.class, WebMvcAutoConfiguration.class})
public class ElideAutoConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(ElideAutoConfiguration.class);

  @Bean
  @ConditionalOnMissingBean
  public Elide elide(PlatformTransactionManager txManager,
      AutowireCapableBeanFactory beanFactory,
      ApplicationContext context,
      EntityManager entityManager,
      ObjectMapper objectMapper,
      ElideProperties elideProperties) {
    ConcurrentHashMap<String, Class<? extends Check>> checks = new ConcurrentHashMap<>();

    // scan checks
    for (Class<?> clazz : ClassIndex.getAnnotated(ElideCheck.class)) {
      ElideCheck elideCheck = clazz.getAnnotation(ElideCheck.class);
      if (Check.class.isAssignableFrom(clazz)) {
        logger.debug("Register Elide Check [{}] with expression [{}]",
            clazz.getCanonicalName(), elideCheck.value());
        checks.put(elideCheck.value(), clazz.asSubclass(Check.class));
      }
    }

    EntityDictionary entityDictionary = new EntityDictionary(checks);
    RSQLFilterDialect rsqlFilterDialect = new RSQLFilterDialect(entityDictionary);

    // scan life cycle hooks
    for (Class<?> clazz : ClassIndex.getAnnotated(ElideHook.class)) {
      if (LifeCycleHook.class.isAssignableFrom(clazz)) {
        ElideHook elideHook = clazz.getAnnotation(ElideHook.class);
        // get generic entity type
        Class<?> entity = null;
        for (Type genericInterface : clazz.getGenericInterfaces()) {
          if (genericInterface instanceof ParameterizedType
              && ((ParameterizedType) genericInterface).getRawType().equals(LifeCycleHook.class)) {
            Type[] genericTypes = ((ParameterizedType) genericInterface).getActualTypeArguments();
            entity = (Class<?>) genericTypes[0];
          }
        }
        if (entity == null) {
          throw new RuntimeException("entity is null, this should not be thrown");
        }

        if (elideHook.fieldOrMethodName().equals("")) {
          entityDictionary.bindTrigger(entity, elideHook.lifeCycle(),
              (LifeCycleHook) context.getBean(clazz));
        } else {
          entityDictionary.bindTrigger(entity, elideHook.lifeCycle(),
              elideHook.fieldOrMethodName(), (LifeCycleHook) context.getBean(clazz));
        }

        // temp: log
        AccountOnCreatePreCommit bean = ((AccountOnCreatePreCommit) context.getBean(clazz));
        bean.logString();

        logger.debug("getBean: {}", bean);
        logger.debug("getTriggers: {}", entityDictionary.getTriggers(Account.class, OnCreatePreCommit.class, ""));
        // end temp

        logger.debug("Register Elide Function Hook: bindTrigger({}, {}, \"{}\", {})",
            entity.getCanonicalName(),
            elideHook.lifeCycle().getSimpleName(),
            //clazz.getCanonicalName(),
            elideHook.fieldOrMethodName(),
            bean.getClass().getCanonicalName());

      } else {
        throw new RuntimeException("ElideHook class must implements LifeCycleHook<T>");
      }
    }

    // temp: bind some triggers
    entityDictionary.bindTrigger(Account.class, OnCreatePostCommit.class, new AccountOnCreatePostCommit());
    entityDictionary.bindTrigger(Account.class, OnCreatePreSecurity.class,
        (a, b, c) -> logger.debug("OnCreatePreSecurity lambda execute"));
    // end temp

    DataStore springDataStore = new SpringHibernateDataStore(txManager, beanFactory, entityManager,
        true, ScrollMode.FORWARD_ONLY);

    return new Elide(new ElideSettingsBuilder(springDataStore)
        .withJsonApiMapper(new JsonApiMapper(entityDictionary, objectMapper))
        .withEntityDictionary(entityDictionary)
        .withJoinFilterDialect(rsqlFilterDialect)
        .withSubqueryFilterDialect(rsqlFilterDialect)
        .withDefaultPageSize(elideProperties.getDefaultPageSize())
        .withDefaultMaxPageSize(elideProperties.getMaxPageSize())
        .build());
  }
}

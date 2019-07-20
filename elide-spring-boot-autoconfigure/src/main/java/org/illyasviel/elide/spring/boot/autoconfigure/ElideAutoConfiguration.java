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
 * Elide AutoConfiguration.
 * 
 * @author olOwOlo
 */
@Configuration
@EnableConfigurationProperties(ElideProperties.class)
@ConditionalOnWebApplication
@AutoConfigureAfter({ HibernateJpaAutoConfiguration.class, WebMvcAutoConfiguration.class })
public class ElideAutoConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(ElideAutoConfiguration.class);

  /**
   * Configure the Elide instance automatically.
   */
  @Bean
  @ConditionalOnMissingBean
  public Elide elide(PlatformTransactionManager txManager, AutowireCapableBeanFactory beanFactory,
      ApplicationContext context, EntityManager entityManager, ObjectMapper objectMapper,
      ElideProperties elideProperties) {
    ConcurrentHashMap<String, Class<? extends Check>> checks = new ConcurrentHashMap<>();

    // scan checks
    scanChecks(checks);

    EntityDictionary entityDictionary = new EntityDictionary(checks);
    RSQLFilterDialect rsqlFilterDialect = new RSQLFilterDialect(entityDictionary);

    DataStore springDataStore = new SpringHibernateDataStore(txManager, beanFactory, entityManager, elideProperties,
        true, ScrollMode.FORWARD_ONLY);

    Elide elide = new Elide(new ElideSettingsBuilder(springDataStore).withJsonApiMapper(new JsonApiMapper(objectMapper))
        .withEntityDictionary(entityDictionary).withJoinFilterDialect(rsqlFilterDialect)
        .withSubqueryFilterDialect(rsqlFilterDialect).withDefaultPageSize(elideProperties.getDefaultPageSize())
        .withDefaultMaxPageSize(elideProperties.getMaxPageSize())
        .withReturnErrorObjects(elideProperties.isReturnErrorObjects()).build());

    // scan life cycle hooks
    scanLifeCycleHook(entityDictionary, context);

    return elide;
  }

  /**
   * Side effect: populate checks.
   */
  private void scanChecks(ConcurrentHashMap<String, Class<? extends Check>> checks) {
    for (Class<?> clazz : ClassIndex.getAnnotated(ElideCheck.class)) {
      ElideCheck elideCheck = clazz.getAnnotation(ElideCheck.class);
      if (Check.class.isAssignableFrom(clazz)) {
        logger.debug("Register Elide Check [{}] with expression [{}]", clazz.getCanonicalName(), elideCheck.value());
        checks.put(elideCheck.value(), clazz.asSubclass(Check.class));
      } else {
        throw new RuntimeException(
            "The class[" + clazz.getCanonicalName() + "] being annotated with @ElideCheck must be a Check.");
      }
    }
  }

  /**
   * Side effect: bind triggers on entityDictionary.
   */
  private void scanLifeCycleHook(EntityDictionary entityDictionary, ApplicationContext context) {
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
          entityDictionary.bindTrigger(entity, elideHook.lifeCycle(), (LifeCycleHook) context.getBean(clazz));
        } else {
          entityDictionary.bindTrigger(entity, elideHook.lifeCycle(), elideHook.fieldOrMethodName(),
              (LifeCycleHook) context.getBean(clazz));
        }

        logger.debug("Register Elide Function Hook: bindTrigger({}, {}, \"{}\", {})", entity.getCanonicalName(),
            elideHook.lifeCycle().getSimpleName(), elideHook.fieldOrMethodName(), clazz.getCanonicalName());

      } else {
        throw new RuntimeException("The class[" + clazz.getCanonicalName()
            + "] being annotated with @ElideHook must implements LifeCycleHook<T>.");
      }
    }
  }
}

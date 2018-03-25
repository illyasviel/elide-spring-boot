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

package org.illyasviel.elide.spring.boot.datastore;

import com.google.common.base.Preconditions;
import com.yahoo.elide.core.DataStore;
import com.yahoo.elide.core.DataStoreTransaction;
import com.yahoo.elide.core.EntityDictionary;
import java.util.Objects;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import org.hibernate.ScrollMode;
import org.hibernate.Session;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * @author olOwOlo
 */
public class SpringHibernateDataStore implements DataStore {
  protected final PlatformTransactionManager txManager;
  protected final EntityManager entityManager;
  protected final boolean isScrollEnabled;
  protected final ScrollMode scrollMode;
  protected final HibernateTransactionSupplier transactionSupplier;

  /**
   * Constructor.
   *
   * @param txManager Spring PlatformTransactionManager
   * @param entityManager EntityManager
   * @param isScrollEnabled Whether or not scrolling is enabled on driver
   * @param scrollMode Scroll mode to use for scrolling driver
   */
  public SpringHibernateDataStore(PlatformTransactionManager txManager,
      EntityManager entityManager, boolean isScrollEnabled, ScrollMode scrollMode) {
    this(txManager, entityManager, isScrollEnabled, scrollMode, SpringHibernateTransaction::new);
  }

  /**
   * Constructor.
   *
   * Useful for extending the store and relying on existing code
   * to instantiate custom hibernate transaction.
   *
   * @param txManager Spring PlatformTransactionManager
   * @param entityManager EntityManager factory
   * @param isScrollEnabled Whether or not scrolling is enabled on driver
   * @param scrollMode Scroll mode to use for scrolling driver
   * @param transactionSupplier Supplier for transaction
   */
  protected SpringHibernateDataStore(PlatformTransactionManager txManager,
      EntityManager entityManager,
      boolean isScrollEnabled,
      ScrollMode scrollMode,
      HibernateTransactionSupplier transactionSupplier) {
    this.txManager = txManager;
    this.entityManager = entityManager;
    this.isScrollEnabled = isScrollEnabled;
    this.scrollMode = scrollMode;
    this.transactionSupplier = transactionSupplier;
  }

  @Override
  public void populateEntityDictionary(EntityDictionary dictionary) {
    Set<EntityType<?>> entities = entityManager.getMetamodel().getEntities();
    /* bind all entities */
    entities.stream()
        .map(EntityType::getJavaType)
        .filter(Objects::nonNull)
        .forEach(mappedClass -> {
          try {
            // Ignore this result. We are just checking to see if it throws an exception meaning that
            // provided class was _not_ an entity.
            dictionary.lookupEntityClass(mappedClass);
            // Bind if successful
            dictionary.bindEntity(mappedClass);
          } catch (IllegalArgumentException e)  {
            // Ignore this entity
            // Turns out that hibernate may include non-entity types in this list when using things
            // like envers. Since they are not entities, we do not want to bind them into the entity
            // dictionary
          }
        });
  }

  @Override
  public DataStoreTransaction beginTransaction() {
    // begin a spring transaction
    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
    def.setName("elide transaction");
    def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    TransactionStatus txStatus = txManager.getTransaction(def);

    Session session = entityManager.unwrap(Session.class);
    Preconditions.checkNotNull(session);

    return transactionSupplier.get(session, txManager, txStatus, isScrollEnabled, scrollMode);
  }

  /**
   * Functional interface for describing a method to supply a custom Hibernate transaction.
   */
  @FunctionalInterface
  public interface HibernateTransactionSupplier {
    SpringHibernateTransaction get(Session session, PlatformTransactionManager txManager,
        TransactionStatus txStatus, boolean isScrollEnabled, ScrollMode scrollMode);
  }
}

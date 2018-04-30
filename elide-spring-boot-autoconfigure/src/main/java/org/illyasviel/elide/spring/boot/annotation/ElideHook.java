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

package org.illyasviel.elide.spring.boot.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.atteo.classindex.IndexAnnotated;
import org.springframework.stereotype.Component;

/**
 * A convenience annotation that help you register elide function hook.
 * <br><br>
 * Example: <br>
 * <pre>
 * <code>@ElideHook(lifeCycle = OnUpdatePreCommit.class)</code>
 * public class AccountUpdatePreCommit implements{@literal LifeCycleHook<Account>} {
 *
 *   <code>@Autowired</code>
 *   private SomeComponent someComponent;
 *
 *   <code>@Override</code>
 *   public void execute(Account account, RequestScope requestScope,
 *      {@literal Optional<ChangeSpec>} changes) {
 *     // do something.
 *   }
 * }
 * </pre>
 *
 * <b>NOTE: </b> The class you annotated must implements {@literal LifeCycleHook<T>},
 * otherwise a RuntimeException is thrown.
 *
 * @author olOwOlo
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Component
@IndexAnnotated
public @interface ElideHook {

  /**
   * Define the life cycle phase(OnReadPostCommit.class, OnUpdatePreSecurity.class, etc).
   * @return OnXXX.class
   */
  Class<? extends Annotation> lifeCycle();

  /**
   * @return The name of the field or method
   */
  String fieldOrMethodName() default "";
}

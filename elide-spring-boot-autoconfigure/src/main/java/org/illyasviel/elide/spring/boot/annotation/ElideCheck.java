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

import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.atteo.classindex.IndexAnnotated;

/**
 * A convenience annotation that help you register elide check
 * <br><br>
 * Example: <br>
 * <pre>
 * <code>@ElideCheck("i am an expression")</code>
 * public static class{@literal Inline<Post>} extends{@literal OperationCheck<Post>} {
 *   <code>@Override</code>
 *   public boolean ok(Post object, RequestScope requestScope,{@literal Optional<ChangeSpec>} changeSpec) {
 *     return false;
 *   }
 * }
 * </pre>
 *
 * @author olOwOlo
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@IndexAnnotated
public @interface ElideCheck {

  /**
   * The expression which will be used for
   * {@link ReadPermission#expression()},
   * {@link UpdatePermission#expression()},
   * {@link CreatePermission#expression()},
   * {@link DeletePermission#expression()}
   * @return The expression you want to defined.
   */
  String value();
}

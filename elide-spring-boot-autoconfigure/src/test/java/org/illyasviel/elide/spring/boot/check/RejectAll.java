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

package org.illyasviel.elide.spring.boot.check;

import com.yahoo.elide.security.ChangeSpec;
import com.yahoo.elide.security.RequestScope;
import com.yahoo.elide.security.checks.CommitCheck;
import com.yahoo.elide.security.checks.OperationCheck;
import java.util.Optional;
import org.illyasviel.elide.spring.boot.annotation.ElideCheck;

/**
 * @author olOwOlo
 */
public class RejectAll {

  public static final String INLINE_REJECT = "inline reject";
  public static final String AT_COMMIT_REJECT = "at commit reject";
  public static final String WRONG_CLASS_TYPE = "this should not be registered";

  @ElideCheck(INLINE_REJECT)
  public static class Inline<Post> extends OperationCheck<Post> {
    @Override
    public boolean ok(Post object, RequestScope requestScope, Optional<ChangeSpec> changeSpec) {
      return false;
    }
  }

  @ElideCheck(AT_COMMIT_REJECT)
  public static class AtCommit<Post> extends CommitCheck<Post> {
    @Override
    public boolean ok(Post post, RequestScope requestScope, Optional<ChangeSpec> changeSpec) {
      return false;
    }
  }

  @ElideCheck(WRONG_CLASS_TYPE)
  public static class other {

  }
}

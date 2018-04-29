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

package org.illyasviel.elide.spring.boot;

import com.yahoo.elide.annotation.OnCreatePreCommit;
import com.yahoo.elide.functions.LifeCycleHook;
import com.yahoo.elide.security.ChangeSpec;
import com.yahoo.elide.security.RequestScope;
import java.util.Optional;
import org.illyasviel.elide.spring.boot.annotation.ElideHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author olOwOlo
 */
@ElideHook(lifeCycle = OnCreatePreCommit.class)
public class AccountOnCreatePreCommit implements LifeCycleHook<Account> {

  private static final Logger logger = LoggerFactory.getLogger(AccountOnCreatePreCommit.class);

  @Override
  public void execute(Account account, RequestScope requestScope,
      Optional<ChangeSpec> changes) {
    logger.debug("AccountOnCreatePreCommit LifeCycleHook execute");
  }

  public void logString() {
    logger.debug("AccountOnCreatePreCommit bean can work!");
  }
}

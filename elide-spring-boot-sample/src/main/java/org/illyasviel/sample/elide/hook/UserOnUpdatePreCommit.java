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

package org.illyasviel.sample.elide.hook;

import com.yahoo.elide.annotation.OnUpdatePreCommit;
import com.yahoo.elide.functions.LifeCycleHook;
import com.yahoo.elide.security.ChangeSpec;
import com.yahoo.elide.security.RequestScope;
import java.util.Optional;
import org.illyasviel.elide.spring.boot.annotation.ElideHook;
import org.illyasviel.sample.elide.domain.User;
import org.illyasviel.sample.elide.util.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author olOwOlo
 */
@ElideHook(lifeCycle = OnUpdatePreCommit.class, fieldOrMethodName = "password")
public class UserOnUpdatePreCommit implements LifeCycleHook<User> {

  private static final Logger logger = LoggerFactory.getLogger(UserOnUpdatePreCommit.class);

  private final PasswordEncoder passwordEncoder;

  @Autowired
  public UserOnUpdatePreCommit(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void execute(User user, RequestScope requestScope, Optional<ChangeSpec> changes) {
    logger.info("User: OnUpdatePreCommit(\"password\") function hook execute!");
    user.setEncodedPassword(passwordEncoder.encode(user.getPassword()));
  }
}

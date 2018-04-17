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

package org.illyasviel.sample.elide.util;

import com.google.common.hash.Hashing;
import java.nio.charset.Charset;
import org.springframework.stereotype.Component;

/**
 * Just an example, the spring's
 * <code>PasswordEncoderFactories.createDelegatingPasswordEncoder()<code/>
 * is a better choice.
 *
 * @author olOwOlo
 */
@Component
public class PasswordEncoder {

  public String encode(String raw) {
    return Hashing.sha512().hashString(raw, Charset.forName("UTF-8")).toString();
  }
}

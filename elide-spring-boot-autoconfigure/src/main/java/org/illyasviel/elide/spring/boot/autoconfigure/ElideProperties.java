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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ElideProperties.
 * @author olOwOlo
 */
@Data
@ConfigurationProperties(prefix = "elide")
public class ElideProperties {

  private String prefix = "/api";
  private int defaultPageSize = 20;
  private int maxPageSize = 100;
  /**
   * Allow inject bean in entity model class.
   */
  private boolean springDependencyInjection = true;
  /**
   * Return error object array instead of error string array.
   */
  private boolean returnErrorObjects = false;
  private MvcProperties mvc;

  @Data
  public static class MvcProperties {

    private boolean enable = true;
    private boolean get = true;
    private boolean post = true;
    private boolean patch = true;
    private boolean delete = true;
  }
}

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

package org.illyasviel.sample.elide.domain;

import static org.illyasviel.sample.elide.check.UserCheck.REJECT_ALL;

import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.OnCreatePreSecurity;
import com.yahoo.elide.annotation.ReadPermission;
import javax.inject.Inject;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.illyasviel.sample.elide.util.PasswordEncoder;

/**
 * @author olOwOlo
 */
@Setter
@NoArgsConstructor
@Table(name = "users")
@Entity
@Include(rootLevel = true)
public class User {

  private Integer id;
  private String username;
  private String password;
  private String encodedPassword;

  @Id
  @GeneratedValue
  public Integer getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  @NotNull
  @ReadPermission(expression = REJECT_ALL)
  public String getPassword() {
    return password;
  }

  public String getEncodedPassword() {
    return encodedPassword;
  }

  /*
   * All annotations supported by spring are ok. e.g. `@Autowired`
   */
  @Inject
  private PasswordEncoder passwordEncoder;

  @OnCreatePreSecurity
  public void onCreatePreSecurity() {
    setEncodedPassword(passwordEncoder.encode(getPassword()));
  }
}

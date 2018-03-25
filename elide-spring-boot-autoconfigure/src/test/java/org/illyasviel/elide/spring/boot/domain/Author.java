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

package org.illyasviel.elide.spring.boot.domain;

import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.SharePermission;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author olOwOlo
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Entity
@Include(rootLevel = true)
@SharePermission
public class Author {

  private Integer id;
  private String firstName;
  private String lastName;
  private Short age;
  private Set<Book> books;

  @Id
  @GeneratedValue(generator = "author_g", strategy = GenerationType.SEQUENCE)
  @SequenceGenerator(name = "author_g", sequenceName = "author_sequence")
  public Integer getId() {
    return id;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public Short getAge() {
    return age;
  }

  @OneToMany(mappedBy = "author")
  public Set<Book> getBooks() {
    return books;
  }
}

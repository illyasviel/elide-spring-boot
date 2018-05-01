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

package org.illyasviel.elide.spring.boot.exception;

import com.yahoo.elide.core.exceptions.HttpStatusException;
import org.springframework.http.HttpStatus;

/**
 * 422, "Unprocessable Entity".
 *
 * @author olOwOlo
 */
public class UnprocessableEntityException extends HttpStatusException {

  private static final long serialVersionUID = 1L;

  public UnprocessableEntityException(String message) {
    super(HttpStatus.UNPROCESSABLE_ENTITY.value(), message);
  }

  public UnprocessableEntityException(String message, Throwable e) {
    super(HttpStatus.UNPROCESSABLE_ENTITY.value(), message, e, null);
  }
}

/*
 * -\-\-
 * hamcrest-jackson
 * --
 * Copyright (C) 2017 Spotify AB
 * --
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -/-/-
 */

package com.spotify.hamcrest.javaslang;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;

import javaslang.control.Try;
import org.hamcrest.StringDescription;
import org.junit.Test;

public class IsSuccessfulTryTest {
  StringDescription description = new StringDescription();
  IsSuccessfulTry<Integer> sut = new IsSuccessfulTry<>(is(42));

  @Test
  public void testDescription() throws Exception {
    sut.describeTo(description);
    assertThat(description.toString(), is("Try with value that is <42>"));
  }

  @Test
  public void testMismatchDescriptionFailedTry() throws Exception {
    sut.describeMismatch(Try.failure(new NullPointerException()), description);
    assertThat(description.toString(),
        is("was failed try with exception <" + new NullPointerException() + ">"));
  }

  @Test
  public void testMismatchDescriptionDifferentValue() throws Exception {
    sut.describeMismatch(Try.success(0), description);
    assertThat(description.toString(), is("was a Try with value that was <0>"));
  }

  @Test
  public void testNoMismatchDescriptionIsMatches() throws Exception {
    sut.describeMismatch(Try.success(42), description);
    assertThat(description.toString(), is(emptyString()));
  }
}
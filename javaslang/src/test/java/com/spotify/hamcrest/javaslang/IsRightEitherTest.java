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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEmptyString.emptyString;

import javaslang.control.Either;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Test;

public class IsRightEitherTest {
  StringDescription description = new StringDescription();
  Matcher<Either<?, String>> sut = new IsRightEither<>(is("hi"));

  @Test
  public void testDescription() throws Exception {
    sut.describeTo(description);
    assertThat(description.toString(), is("a right Either that is \"hi\""));
  }

  @Test
  public void testMismatchDescriptionLeft() throws Exception {
    sut.describeMismatch(Either.left(42), description);
    assertThat(description.toString(), is("was left with value <42>"));
  }

  @Test
  public void testMismatchDescriptionDifferentValue() throws Exception {
    sut.describeMismatch(Either.right("not hi"), description);
    assertThat(description.toString(), is("was right with value that was \"not hi\""));
  }

  @Test
  public void testNoMismatchDescriptionIsMatches() throws Exception {
    sut.describeMismatch(Either.right("hi"), description);
    assertThat(description.toString(), is(emptyString()));
  }
}
/*-
 * -\-\-
 * hamcrest-pojo
 * --
 * Copyright (C) 2016 Spotify AB
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

package com.spotify.hamcrest.pojo;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.spotify.hamcrest.util.DescriptionUtils;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class IsPojo<A> extends TypeSafeDiagnosingMatcher<A> {

  private final Class<A> cls;
  private final ImmutableMap<String, Matcher<?>> methodMatchers;

  private IsPojo(final Class<A> cls, final ImmutableMap<String, Matcher<?>> methodMatchers) {
    this.cls = Objects.requireNonNull(cls);
    this.methodMatchers = Objects.requireNonNull(methodMatchers);
  }

  public static <A> IsPojo<A> pojo(Class<A> cls) {
    return new IsPojo<>(cls, ImmutableMap.of());
  }

  public IsPojo<A> where(String methodName, Matcher<?> returnValueMatcher) {
    final ImmutableMap<String, Matcher<?>> newMethodMatchers =
        ImmutableMap.<String, Matcher<?>>builder()
            .putAll(methodMatchers)
            .put(methodName, returnValueMatcher)
            .build();

    return new IsPojo<>(cls, newMethodMatchers);
  }

  public IsPojo<A> withProperty(String property, Matcher<?> valueMatcher) {
    return where("get" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, property),
                 valueMatcher);
  }

  @Override
  protected boolean matchesSafely(A item, Description mismatchDescription) {
    if (!cls.isInstance(item)) {
      mismatchDescription.appendText("not an instance of " + cls.getName());
      return false;
    }

    final Map<String, Consumer<Description>> mismatches = new LinkedHashMap<>();

    for (Map.Entry<String, Matcher<?>> methodMatcher : methodMatchers.entrySet()) {
      final String methodName = methodMatcher.getKey();
      final Matcher<?> matcher = methodMatcher.getValue();

      final Object returnValue;
      try {
        returnValue = cls.getMethod(methodName).invoke(item);
        if (!matcher.matches(returnValue)) {
          mismatches.put(methodName, d -> matcher.describeMismatch(returnValue, d));
        }
      } catch (IllegalAccessException e) {
        // This only happens if the method has been removed from the class after the code was
        // compiled, so very unlikely...
        mismatches.put(methodName, d -> d.appendText("was not accessible"));
      } catch (InvocationTargetException e) {
        final Throwable cause = e.getCause();
        mismatches.put(methodName,
            d -> d.appendText("threw an exception: ")
                .appendText(cause.getClass().getCanonicalName())
                .appendText(": ")
                .appendText(cause.getMessage()));
      } catch (NoSuchMethodException e) {
        mismatches.put(methodName, d -> d.appendText("did not exist"));
      }
    }

    if (!mismatches.isEmpty()) {
      mismatchDescription.appendText(cls.getSimpleName()).appendText(" ");
      DescriptionUtils.describeNestedMismatches(
          methodMatchers.keySet(),
          mismatchDescription,
          mismatches,
          IsPojo::describeMethod);
      return false;
    }

    return true;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText(cls.getSimpleName()).appendText(" {\n");

    for (Map.Entry<String, Matcher<?>> methodMatcher : methodMatchers.entrySet()) {
      final String methodName = methodMatcher.getKey();
      final Matcher<?> matcher = methodMatcher.getValue();
      description.appendText("  ").appendText(methodName).appendText("(): ");

      Description innerDescription = new StringDescription();
      matcher.describeTo(innerDescription);

      indentDescription(description, innerDescription);
    }
    description.appendText("}");
  }


  private static void describeMethod(String name, Description description) {
    description.appendText(name).appendText("()");
  }

  private void indentDescription(Description description, Description innerDescription) {
    description
        .appendText(
            Joiner.on("\n  ").join(Splitter.on('\n').split(innerDescription.toString())))
        .appendText("\n");
  }
}

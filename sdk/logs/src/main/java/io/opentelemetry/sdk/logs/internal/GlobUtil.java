/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.sdk.logs.internal;

import io.opentelemetry.sdk.logs.funcitonal.Predicate;
import java.util.regex.Pattern;

public final class GlobUtil {

  private GlobUtil() {}

  /**
   * Return a predicate that returns {@code true} if a string matches the {@code globPattern}.
   *
   * <p>{@code globPattern} may contain the wildcard characters {@code *} and {@code ?} with the
   * following matching criteria:
   *
   * <ul>
   *   <li>{@code *} matches 0 or more instances of any character
   *   <li>{@code ?} matches exactly one instance of any character
   * </ul>
   */
  public static Predicate<String> toGlobPatternPredicate(final String globPattern) {
    // Match all
    if (globPattern.equals("*")) {
      return new Predicate<String>() {
        @Override
        public boolean test(String s) {
          return true;
        }
      };
    }

    // If globPattern contains '*' or '?', convert it to a regex and return corresponding predicate
    for (int i = 0; i < globPattern.length(); i++) {
      char c = globPattern.charAt(i);
      if (c == '*' || c == '?') {
        final Pattern pattern = toRegexPattern(globPattern);
        return new Predicate<String>() {
          @Override
          public boolean test(String s) {
            return pattern.matcher(s).matches();
          }
        };
      }
    }

    // Exact match, ignoring case
    return new Predicate<String>() {
      @Override
      public boolean test(String s) {
        return globPattern.equalsIgnoreCase(s);
      }
    };
  }

  /**
   * Transform the {@code globPattern} to a regex by converting {@code *} to {@code .*}, {@code ?}
   * to {@code .}, and escaping other regex special characters.
   */
  private static Pattern toRegexPattern(String globPattern) {
    int tokenStart = -1;
    StringBuilder patternBuilder = new StringBuilder();
    for (int i = 0; i < globPattern.length(); i++) {
      char c = globPattern.charAt(i);
      if (c == '*' || c == '?') {
        if (tokenStart != -1) {
          patternBuilder.append(Pattern.quote(globPattern.substring(tokenStart, i)));
          tokenStart = -1;
        }
        if (c == '*') {
          patternBuilder.append(".*");
        } else {
          // c == '?'
          patternBuilder.append(".");
        }
      } else {
        if (tokenStart == -1) {
          tokenStart = i;
        }
      }
    }
    if (tokenStart != -1) {
      patternBuilder.append(Pattern.quote(globPattern.substring(tokenStart)));
    }
    return Pattern.compile(patternBuilder.toString());
  }
}

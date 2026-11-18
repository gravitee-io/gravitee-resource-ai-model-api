/*
 * Copyright © 2015 The Gravitee team (http://gravitee.io)
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
package io.gravitee.resource.ai_model.api.decision;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import java.util.Collections;
import java.util.LinkedHashMap;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AnswerTest {

    @Nested
    class Choice {

        @Test
        void should_refuse_an_answer_without_choice() {
            assertThatThrownBy(() -> new Answer.Choice(null, 0.9, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("choice");
        }

        @Test
        void should_read_missing_probabilities_as_none() {
            assertThat(new Answer.Choice("billing", 0.9, null).probabilities()).isEmpty();
        }

        @Test
        void should_accept_probabilities_given_as_an_unmodifiable_map() {
            var probabilities = new LinkedHashMap<String, Double>();
            probabilities.put("triage", 0.1);
            probabilities.put("billing", 0.9);

            assertThat(
                new Answer.Choice("billing", 0.9, Collections.unmodifiableSequencedMap(probabilities)).probabilities()
            ).containsExactly(entry("triage", 0.1), entry("billing", 0.9));
        }

        @Test
        void should_not_follow_changes_of_the_probabilities_given() {
            var probabilities = new LinkedHashMap<String, Double>();
            probabilities.put("billing", 0.9);
            var answer = new Answer.Choice("billing", 0.9, probabilities);

            probabilities.put("triage", 0.1);

            assertThat(answer.probabilities()).containsOnlyKeys("billing");
            assertThatThrownBy(() -> answer.probabilities().put("triage", 0.1)).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void should_refuse_a_missing_probability() {
            var probabilities = new LinkedHashMap<String, Double>();
            probabilities.put("billing", null);

            assertThatThrownBy(() -> new Answer.Choice("billing", 0.9, probabilities))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("probability cannot be null");
        }
    }
}

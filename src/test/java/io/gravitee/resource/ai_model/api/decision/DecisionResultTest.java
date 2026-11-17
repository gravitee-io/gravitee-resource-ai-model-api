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

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class DecisionResultTest {

    @Nested
    class Answers {

        @Test
        void should_keep_the_answers_in_the_order_given_and_not_follow_their_changes() {
            var answers = new LinkedHashMap<String, Answer>();
            answers.put("z", new Answer.Noul(0.2));
            answers.put("a", new Answer.Score(1, 0.8));
            var result = new DecisionResult("model", answers, null);

            answers.put("b", new Answer.Noul(0.5));

            assertThat(result.answers().keySet()).containsExactly("z", "a");
            assertThatThrownBy(() -> result.answers().put("b", new Answer.Noul(0.5))).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void should_refuse_a_result_without_answers() {
            assertThatThrownBy(() -> new DecisionResult("model", null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("answers");
        }

        @Test
        void should_refuse_a_missing_answer() {
            var answers = new LinkedHashMap<String, Answer>();
            answers.put("urgent", null);

            assertThatThrownBy(() -> new DecisionResult("model", answers, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("The answer [urgent] is missing");
        }

        @Test
        void should_refuse_an_answer_without_key() {
            assertThatThrownBy(() -> new DecisionResult("model", new LinkedHashMap<>(Map.of(" ", new Answer.Noul(0.5))), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("answer needs a key");
        }
    }
}

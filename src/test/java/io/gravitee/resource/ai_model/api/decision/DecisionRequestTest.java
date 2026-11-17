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

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("unchecked")
class DecisionRequestTest {

    @Nested
    class Context {

        @Test
        void should_accept_a_context_made_of_json_values() {
            var nested = new LinkedHashMap<String, Object>();
            nested.put("missing", null);
            Map<String, Object> context = Map.of("text", "hello", "count", 3, "flag", true, "items", List.of(1.5, "a", nested));

            assertThat(DecisionRequest.of(context, "urgent", new Question.Noul("Is it urgent?")).context()).isEqualTo(context);
        }

        @Test
        void should_accept_a_context_made_of_vertx_json() {
            var context = new JsonObject()
                .put("text", "hello")
                .put("items", new JsonArray().add(1).add(new JsonObject().put("flag", true)));

            var request = DecisionRequest.of(context, "urgent", new Question.Noul("Is it urgent?"));

            assertThat(request.context()).isInstanceOf(Map.class);
            assertThat(Json.encode(request.context())).isEqualTo(context.encode());
        }

        @Test
        void should_not_follow_changes_of_the_context_given() {
            var items = new ArrayList<Object>(List.of("a"));
            var context = new LinkedHashMap<String, Object>();
            context.put("items", items);
            var request = DecisionRequest.of(context, "urgent", new Question.Noul("Is it urgent?"));

            context.put("other", new Object());
            items.add(new Object());

            assertThat(request.context()).isEqualTo(Map.of("items", List.of("a")));
            assertThatThrownBy(() -> ((Map<String, Object>) request.context()).put("other", 1)).isInstanceOf(
                UnsupportedOperationException.class
            );
        }

        @Test
        void should_refuse_a_request_without_context() {
            assertThatThrownBy(() -> DecisionRequest.of(null, "urgent", new Question.Noul("Is it urgent?")))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("context");
        }

        @Test
        void should_refuse_a_context_holding_something_else_than_json_values() {
            Map<String, Object> context = Map.of("items", List.of("a", new Object()));

            assertThatThrownBy(() -> DecisionRequest.of(context, "urgent", new Question.Noul("Is it urgent?")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("context.items[1]");
        }

        @Test
        void should_refuse_a_vertx_json_context_holding_something_else_than_json_values() {
            var context = new JsonObject().put("items", new JsonArray().add("a").add(new Object()));

            assertThatThrownBy(() -> DecisionRequest.of(context, "urgent", new Question.Noul("Is it urgent?")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("context.items[1]");
        }

        @Test
        void should_refuse_a_context_with_a_key_that_is_not_a_text() {
            assertThatThrownBy(() -> DecisionRequest.of(Map.of(1, "one"), "urgent", new Question.Noul("Is it urgent?")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("keys must be texts");
        }

        @ParameterizedTest
        @ValueSource(doubles = { Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY })
        void should_refuse_a_number_that_is_not_finite(double number) {
            assertThatThrownBy(() -> DecisionRequest.of(Map.of("score", number), "urgent", new Question.Noul("Is it urgent?")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("context.score");
        }

        @ParameterizedTest
        @ValueSource(floats = { Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY })
        void should_refuse_a_float_that_is_not_finite(float number) {
            assertThatThrownBy(() -> DecisionRequest.of(Map.of("score", number), "urgent", new Question.Noul("Is it urgent?")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("context.score");
        }
    }

    @Nested
    class Questions {

        @Test
        void should_keep_the_questions_in_the_order_given() {
            var questions = new LinkedHashMap<String, Question>();
            questions.put("z", new Question.Noul("Is it urgent?"));
            questions.put("a", new Question.Score("How severe?", List.of("routine", "outage")));

            assertThat(new DecisionRequest("context", questions).questions().keySet()).containsExactly("z", "a");
        }

        @Test
        void should_not_follow_changes_of_the_questions_given() {
            var questions = new LinkedHashMap<String, Question>();
            questions.put("urgent", new Question.Noul("Is it urgent?"));
            var request = new DecisionRequest("context", questions);

            questions.put("severe", new Question.Noul("Is it severe?"));

            assertThat(request.questions()).containsOnlyKeys("urgent");
        }

        @Test
        void should_refuse_a_request_without_question() {
            assertThatThrownBy(() -> new DecisionRequest("context", new LinkedHashMap<>()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one question");
        }

        @Test
        void should_refuse_a_missing_question() {
            var questions = new LinkedHashMap<String, Question>();
            questions.put("urgent", null);

            assertThatThrownBy(() -> new DecisionRequest("context", questions))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("urgent");
        }

        @Test
        void should_refuse_a_question_without_key() {
            var questions = new LinkedHashMap<String, Question>();
            questions.put(" ", new Question.Noul("Is it urgent?"));

            assertThatThrownBy(() -> new DecisionRequest("context", questions))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("key");
        }
    }
}

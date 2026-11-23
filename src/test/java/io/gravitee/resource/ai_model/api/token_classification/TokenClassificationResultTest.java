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
package io.gravitee.resource.ai_model.api.token_classification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.gravitee.resource.ai_model.api.ModelInvokeException;
import io.gravitee.resource.ai_model.api.token_classification.TokenClassificationResult.Span;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class TokenClassificationResultTest {

    private static final Span EMAIL = new Span("EMAIL", "EMAIL_ADDRESS", 0.98, 12, 28);
    private static final Span PERSON = new Span("PERSON", "PER", 0.71, 0, 8);

    @Nested
    class Spans {

        @Test
        void should_keep_the_spans_of_each_input_in_the_order_given_and_not_follow_their_changes() {
            var first = new ArrayList<>(List.of(EMAIL));
            var spans = new ArrayList<List<Span>>(List.of(first, List.of()));
            var result = new TokenClassificationResult("model", spans, null);

            first.add(PERSON);
            spans.add(List.of(PERSON));

            assertThat(result.spans()).containsExactly(List.of(EMAIL), List.of());
            assertThatThrownBy(() -> result.spans().add(List.of())).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> result.spans().getFirst().add(PERSON)).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void should_refuse_a_result_without_spans() {
            assertThatThrownBy(() -> new TokenClassificationResult("model", null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("spans");
        }

        @Test
        void should_refuse_an_input_without_spans() {
            assertThatThrownBy(() -> new TokenClassificationResult("model", Arrays.asList(List.of(EMAIL), null), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("The spans of the input [1] are missing");
        }

        @Test
        void should_refuse_a_missing_span() {
            assertThatThrownBy(() -> new TokenClassificationResult("model", List.of(Arrays.asList(EMAIL, null)), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("A span of the input [0] is missing");
        }

        @Test
        void should_accept_a_result_without_model_nor_usage() {
            var result = new TokenClassificationResult(null, List.of(List.of()), null);

            assertThat(result.model()).isNull();
            assertThat(result.usage()).isNull();
        }
    }

    @Nested
    class SpansFor {

        private final TokenClassificationRequest request = new TokenClassificationRequest(List.of("first", "second"));

        @Test
        void should_give_the_spans_of_each_input_of_the_request() {
            var result = new TokenClassificationResult("model", List.of(List.of(EMAIL), List.of()), null);

            assertThat(result.spansFor(request)).containsExactly(List.of(EMAIL), List.of());
        }

        @Test
        void should_fail_when_the_result_answers_fewer_inputs_than_the_request_holds() {
            var result = new TokenClassificationResult("model", List.of(List.of(EMAIL)), null);

            assertThatThrownBy(() -> result.spansFor(request))
                .isInstanceOf(ModelInvokeException.class)
                .hasMessageContaining("answered for 1 texts, not for the 2 it was given");
        }

        @Test
        void should_fail_when_the_result_answers_more_inputs_than_the_request_holds() {
            var result = new TokenClassificationResult("model", List.of(List.of(), List.of(), List.of()), null);

            assertThatThrownBy(() -> result.spansFor(request)).isInstanceOf(ModelInvokeException.class);
        }

        @Test
        void should_fail_without_a_request() {
            var result = new TokenClassificationResult("model", List.of(), null);

            assertThatThrownBy(() -> result.spansFor(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("request");
        }
    }

    @Nested
    class ASpan {

        @Test
        void should_refuse_a_span_without_category() {
            assertThatThrownBy(() -> new Span(null, "PER", 0.9, 0, 4))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("A span needs a category");
            assertThatThrownBy(() -> new Span(" ", "PER", 0.9, 0, 4))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("A span needs a category");
        }

        @Test
        void should_refuse_a_span_without_label() {
            assertThatThrownBy(() -> new Span("PERSON", null, 0.9, 0, 4))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("A span needs a label");
            assertThatThrownBy(() -> new Span("PERSON", " ", 0.9, 0, 4))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("A span needs a label");
        }

        @Test
        void should_refuse_a_span_starting_before_its_input() {
            assertThatThrownBy(() -> new Span("PERSON", "PER", 0.9, -1, 4))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid span [-1, 4)");
        }

        @Test
        void should_refuse_an_empty_span() {
            assertThatThrownBy(() -> new Span("PERSON", "PER", 0.9, 4, 4))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid span [4, 4)");
        }

        @Test
        void should_refuse_a_span_ending_before_it_starts() {
            assertThatThrownBy(() -> new Span("PERSON", "PER", 0.9, 4, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid span [4, 2)");
        }

        @Test
        void should_not_check_the_score_which_the_implementation_checks() {
            assertThat(new Span("PERSON", "PER", 1.4, 0, 4).score()).isEqualTo(1.4);
        }
    }
}

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class TokenClassificationRequestTest {

    @Nested
    class Inputs {

        @Test
        void should_keep_the_inputs_in_the_order_given_and_not_follow_their_changes() {
            var inputs = new ArrayList<>(List.of("second", "first"));
            var request = new TokenClassificationRequest(inputs);

            inputs.add("third");

            assertThat(request.inputs()).containsExactly("second", "first");
            assertThatThrownBy(() -> request.inputs().add("third")).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void should_accept_an_empty_input() {
            assertThat(new TokenClassificationRequest(List.of("")).inputs()).containsExactly("");
        }

        @Test
        void should_refuse_a_request_without_inputs() {
            assertThatThrownBy(() -> new TokenClassificationRequest(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one input");
            assertThatThrownBy(() -> new TokenClassificationRequest(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one input");
        }

        @Test
        void should_refuse_a_null_input() {
            assertThatThrownBy(() -> new TokenClassificationRequest(Arrays.asList("text", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("An input cannot be null");
        }
    }

    @Nested
    class Of {

        @Test
        void should_classify_one_input() {
            assertThat(TokenClassificationRequest.of("text").inputs()).containsExactly("text");
        }

        @Test
        void should_refuse_a_null_input() {
            assertThatThrownBy(() -> TokenClassificationRequest.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("An input cannot be null");
        }
    }
}

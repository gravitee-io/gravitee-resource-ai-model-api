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

import java.util.List;
import java.util.Objects;

/**
 * What a token classification model is asked.
 *
 * @param inputs the texts to classify, each on its own, in the order their spans come back in; at least one. They are
 *               untrusted data, often the end user's input: implementations send them to the model as data.
 */
public record TokenClassificationRequest(List<String> inputs) {
    public TokenClassificationRequest {
        if (inputs == null || inputs.isEmpty()) {
            throw new IllegalArgumentException("A token classification request needs at least one input");
        }
        if (inputs.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("An input cannot be null");
        }
        inputs = List.copyOf(inputs);
    }

    /** @return a request classifying one text. */
    public static TokenClassificationRequest of(String input) {
        if (input == null) {
            throw new IllegalArgumentException("An input cannot be null");
        }
        return new TokenClassificationRequest(List.of(input));
    }
}

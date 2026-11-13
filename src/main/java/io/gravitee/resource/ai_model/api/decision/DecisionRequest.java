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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * What a decision model is asked.
 *
 * @param context what the questions are about. Sent to the model as JSON: a text, or any structure of maps, lists,
 *                texts, numbers and booleans.
 * @param questions the questions, each under the key its answer comes back under; at least one.
 */
public record DecisionRequest(Object context, Map<String, Question> questions) {
    public DecisionRequest {
        Objects.requireNonNull(context, "context");
        if (questions == null || questions.isEmpty()) {
            throw new IllegalArgumentException("A decision request needs at least one question");
        }
        questions.forEach((key, question) -> {
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("A question needs a key");
            }
            if (question == null) {
                throw new IllegalArgumentException("The question [" + key + "] is missing");
            }
        });
        questions = Collections.unmodifiableMap(new LinkedHashMap<>(questions));
    }

    /** @return a request asking one question, under the given key. */
    public static DecisionRequest of(Object context, String key, Question question) {
        return new DecisionRequest(context, Map.of(key, question));
    }
}

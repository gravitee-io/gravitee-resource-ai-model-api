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
 * What a decision model answered.
 *
 * @param model the model that answered, as it names itself.
 * @param answers the answer to each question of the request, under the key of the question.
 * @param usage what the call consumed; {@code null} when the model does not tell.
 */
public record DecisionResult(String model, Map<String, Answer> answers, Usage usage) {
    public DecisionResult {
        Objects.requireNonNull(answers, "answers");
        answers = Collections.unmodifiableMap(new LinkedHashMap<>(answers));
    }

    /**
     * @param inputTokens the tokens of the request.
     * @param outputTokens the tokens of the answer.
     */
    public record Usage(long inputTokens, long outputTokens) {}
}

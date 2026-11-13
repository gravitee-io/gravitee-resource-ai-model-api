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

/** The answer of a decision model to one {@link Question}, of the shape of that question. */
public sealed interface Answer permits Answer.Noul, Answer.Choice, Answer.Score {
    /**
     * The answer to a {@link Question.Noul}.
     *
     * @param probability the probability of yes, from 0 (no) to 1 (yes).
     */
    record Noul(double probability) implements Answer {}

    /**
     * The answer to a {@link Question.Choice}.
     *
     * @param choice the choice picked, one of the question.
     * @param confidence how sure the model is of the choice picked, from 0 to 1; it is the source of truth for that
     *                   choice, even when {@code probabilities} gives it another value.
     * @param probabilities the probability of each choice of the question; empty when the model gives none.
     */
    record Choice(String choice, double confidence, Map<String, Double> probabilities) implements Answer {
        public Choice {
            if (probabilities == null) {
                probabilities = Map.of();
            } else if (probabilities.containsValue(null)) {
                throw new IllegalArgumentException("A choice probability cannot be null");
            } else {
                probabilities = Collections.unmodifiableMap(new LinkedHashMap<>(probabilities));
            }
        }
    }

    /**
     * The answer to a {@link Question.Score}.
     *
     * @param score the position on the scale of the question, from 0, its lowest level, to
     *              {@code levels().size() - 1}, its highest level; it may fall between two levels.
     * @param confidence how sure the model is of it, from 0 to 1.
     */
    record Score(double score, double confidence) implements Answer {}
}

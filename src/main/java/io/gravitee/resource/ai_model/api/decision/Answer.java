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
import java.util.Objects;
import java.util.SequencedMap;

/** The answer of a decision model to one {@link Question}, of the shape of that question. */
public sealed interface Answer permits Answer.Noul, Answer.Choice, Answer.Score {
    /**
     * @return why the model answered so, in natural language; {@code null} when the model gives none. Free model
     *         output, which the context can steer: treat it as untrusted before logging it or returning it to a caller.
     */
    String reason();

    /**
     * The answer to a {@link Question.Noul}.
     *
     * @param probability the probability of yes, from 0 (no) to 1 (yes).
     * @param reason why the model answered so; {@code null} when the model gives none. See {@link Answer#reason()}.
     */
    record Noul(double probability, String reason) implements Answer {
        /**
         * An answer without reason.
         *
         * @param probability the probability of yes, from 0 (no) to 1 (yes).
         */
        public Noul(double probability) {
            this(probability, null);
        }
    }

    /**
     * The answer to a {@link Question.Choice}.
     *
     * @param choice the choice picked, one of the question.
     * @param confidence how sure the model is of the choice picked, from 0 to 1; it is the source of truth for that
     *                   choice, even when {@code probabilities} gives it another value.
     * @param probabilities the probability of each choice of the question, in the order of its choices; empty when the model gives none.
     * @param reason why the model answered so; {@code null} when the model gives none. See {@link Answer#reason()}.
     */
    record Choice(String choice, double confidence, SequencedMap<String, Double> probabilities, String reason) implements Answer {
        public Choice {
            Objects.requireNonNull(choice, "choice");
            if (probabilities == null) {
                probabilities = Collections.unmodifiableSequencedMap(new LinkedHashMap<>());
            } else if (probabilities.values().stream().anyMatch(Objects::isNull)) {
                throw new IllegalArgumentException("A choice probability cannot be null");
            } else {
                probabilities = Collections.unmodifiableSequencedMap(new LinkedHashMap<>(probabilities));
            }
        }

        /**
         * An answer without reason.
         *
         * @param choice the choice picked, one of the question.
         * @param confidence how sure the model is of the choice picked, from 0 to 1.
         * @param probabilities the probability of each choice of the question; empty when the model gives none.
         */
        public Choice(String choice, double confidence, SequencedMap<String, Double> probabilities) {
            this(choice, confidence, probabilities, null);
        }
    }

    /**
     * The answer to a {@link Question.Score}.
     *
     * @param score the position on the scale of the question, from 0, its lowest level, to
     *              {@code levels().size() - 1}, its highest level; it may fall between two levels.
     * @param confidence how sure the model is of it, from 0 to 1.
     * @param reason why the model answered so; {@code null} when the model gives none. See {@link Answer#reason()}.
     */
    record Score(double score, double confidence, String reason) implements Answer {
        /**
         * An answer without reason.
         *
         * @param score the position on the scale of the question.
         * @param confidence how sure the model is of it, from 0 to 1.
         */
        public Score(double score, double confidence) {
            this(score, confidence, null);
        }
    }
}

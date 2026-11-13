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
import java.util.List;
import java.util.Map;

/**
 * One question asked to a decision model. Its shape sets the shape of the {@link Answer}: a {@link Noul} is answered
 * by an {@link Answer.Noul}, a {@link Choice} by an {@link Answer.Choice}, a {@link Score} by an {@link Answer.Score}.
 */
public sealed interface Question permits Question.Noul, Question.Choice, Question.Score {
    /** @return the question itself, in natural language. */
    String instructions();

    /**
     * A yes or no question, answered by the probability of yes.
     *
     * @param instructions the question.
     * @param whenTrue what makes the answer yes; optional.
     * @param whenFalse what makes the answer no; optional.
     */
    record Noul(String instructions, String whenTrue, String whenFalse) implements Question {
        public Noul {
            requireInstructions(instructions);
        }

        /**
         * A yes or no question, without saying what makes the answer yes or no.
         *
         * @param instructions the question.
         */
        public Noul(String instructions) {
            this(instructions, null, null);
        }
    }

    /**
     * A question answered by one of the given choices.
     *
     * @param instructions the question.
     * @param choices the possible answers, each with what it stands for (may be {@code null}); at least two.
     */
    record Choice(String instructions, Map<String, String> choices) implements Question {
        public Choice {
            requireInstructions(instructions);
            if (choices == null || choices.size() < 2) {
                throw new IllegalArgumentException("A choice question needs at least two choices");
            }
            if (
                choices
                    .keySet()
                    .stream()
                    .anyMatch(choice -> choice == null || choice.isBlank())
            ) {
                throw new IllegalArgumentException("A choice cannot be blank");
            }
            choices = Collections.unmodifiableMap(new LinkedHashMap<>(choices));
        }
    }

    /**
     * A question answered by a position on a scale.
     *
     * @param instructions the question.
     * @param levels what each level of the scale stands for, from the lowest, level 0, to the highest; at least two.
     */
    record Score(String instructions, List<String> levels) implements Question {
        public Score {
            requireInstructions(instructions);
            if (levels == null || levels.size() < 2) {
                throw new IllegalArgumentException("A score question needs at least two levels");
            }
            if (levels.stream().anyMatch(level -> level == null || level.isBlank())) {
                throw new IllegalArgumentException("A level cannot be blank");
            }
            levels = List.copyOf(levels);
        }
    }

    private static void requireInstructions(String instructions) {
        if (instructions == null || instructions.isBlank()) {
            throw new IllegalArgumentException("A question needs instructions");
        }
    }
}

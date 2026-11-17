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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class QuestionTest {

    @Nested
    class Noul {

        @Test
        void should_refuse_a_question_without_instructions() {
            assertThatThrownBy(() -> new Question.Noul(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("instructions");
        }
    }

    @Nested
    class Choice {

        @Test
        void should_refuse_a_choice_question_without_instructions() {
            assertThatThrownBy(() -> new Question.Choice(" ", new LinkedHashMap<>(Map.of("billing", "Payments", "triage", "Other"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("instructions");
        }

        @Test
        void should_refuse_a_choice_question_with_a_single_choice() {
            assertThatThrownBy(() -> new Question.Choice("Which team?", new LinkedHashMap<>(Map.of("billing", "Payments"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least two choices");
        }

        @Test
        void should_refuse_a_blank_choice() {
            var choices = new LinkedHashMap<String, String>();
            choices.put("billing", "Payments");
            choices.put("", "Nothing");

            assertThatThrownBy(() -> new Question.Choice("Which team?", choices))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("choice cannot be blank");
        }

        @Test
        void should_accept_a_choice_without_description() {
            var choices = new LinkedHashMap<String, String>();
            choices.put("billing", "Payments");
            choices.put("triage", null);

            assertThat(new Question.Choice("Which team?", choices).choices()).containsEntry("triage", null);
        }

        @Test
        void should_keep_the_choices_in_the_order_given_and_not_follow_their_changes() {
            var choices = new LinkedHashMap<String, String>();
            choices.put("triage", "Other");
            choices.put("billing", "Payments");
            var question = new Question.Choice("Which team?", choices);

            choices.put("sales", "Offers");

            assertThat(question.choices().keySet()).containsExactly("triage", "billing");
            assertThatThrownBy(() -> question.choices().put("sales", "Offers")).isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    class Score {

        @Test
        void should_refuse_a_score_question_without_instructions() {
            assertThatThrownBy(() -> new Question.Score(" ", List.of("routine", "outage")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("instructions");
        }

        @Test
        void should_refuse_a_score_question_with_a_single_level() {
            assertThatThrownBy(() -> new Question.Score("How severe?", List.of("routine")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least two levels");
        }

        @Test
        void should_refuse_a_missing_level() {
            var levels = new ArrayList<String>();
            levels.add("routine");
            levels.add(null);

            assertThatThrownBy(() -> new Question.Score("How severe?", levels))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("level cannot be blank");
        }

        @Test
        void should_refuse_a_blank_level() {
            assertThatThrownBy(() -> new Question.Score("How severe?", List.of("routine", " ")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("level cannot be blank");
        }

        @Test
        void should_not_follow_changes_of_the_levels_given() {
            var levels = new ArrayList<>(List.of("routine", "outage"));
            var question = new Question.Score("How severe?", levels);

            levels.add("disaster");

            assertThat(question.levels()).containsExactly("routine", "outage");
            assertThatThrownBy(() -> question.levels().add("disaster")).isInstanceOf(UnsupportedOperationException.class);
        }
    }
}

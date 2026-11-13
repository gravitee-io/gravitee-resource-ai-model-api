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
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class DecisionRequestTest {

    @Test
    void should_refuse_a_request_without_question() {
        assertThatThrownBy(() -> new DecisionRequest("context", Map.of()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("at least one question");
    }

    @Test
    void should_keep_the_questions_in_the_order_given() {
        var questions = new LinkedHashMap<String, Question>();
        questions.put("z", new Question.Noul("Is it urgent?"));
        questions.put("a", new Question.Score("How severe?", List.of("routine", "outage")));

        assertThat(new DecisionRequest("context", questions).questions().keySet()).containsExactly("z", "a");
    }

    @Test
    void should_refuse_a_question_without_instructions() {
        assertThatThrownBy(() -> new Question.Noul(" "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("instructions");
    }

    @Test
    void should_refuse_a_request_without_context() {
        assertThatThrownBy(() -> DecisionRequest.of(null, "urgent", new Question.Noul("Is it urgent?")))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("context");
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

    @Test
    void should_not_follow_changes_of_the_questions_given() {
        var questions = new LinkedHashMap<String, Question>();
        questions.put("urgent", new Question.Noul("Is it urgent?"));
        var request = new DecisionRequest("context", questions);

        questions.put("severe", new Question.Noul("Is it severe?"));

        assertThat(request.questions()).containsOnlyKeys("urgent");
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
    void should_refuse_a_missing_level() {
        var levels = new ArrayList<String>();
        levels.add("routine");
        levels.add(null);

        assertThatThrownBy(() -> new Question.Score("How severe?", levels))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("level cannot be blank");
    }

    @Test
    void should_read_missing_probabilities_as_none() {
        assertThat(new Answer.Choice("billing", 0.9, null).probabilities()).isEmpty();
    }

    @Test
    void should_refuse_a_missing_probability() {
        var probabilities = new LinkedHashMap<String, Double>();
        probabilities.put("billing", null);

        assertThatThrownBy(() -> new Answer.Choice("billing", 0.9, probabilities))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("probability cannot be null");
    }

    @Test
    void should_refuse_a_result_without_answers() {
        assertThatThrownBy(() -> new DecisionResult("model", null, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("answers");
    }

    @Test
    void should_refuse_a_choice_question_with_a_single_choice() {
        assertThatThrownBy(() -> new Question.Choice("Which team?", Map.of("billing", "Payments")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("at least two choices");
    }

    @Test
    void should_refuse_a_score_question_with_a_single_level() {
        assertThatThrownBy(() -> new Question.Score("How severe?", List.of("routine")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("at least two levels");
    }

    @Test
    void should_accept_a_choice_without_description() {
        var choices = new LinkedHashMap<String, String>();
        choices.put("billing", "Payments");
        choices.put("triage", null);

        assertThat(new Question.Choice("Which team?", choices).choices()).containsEntry("triage", null);
    }
}

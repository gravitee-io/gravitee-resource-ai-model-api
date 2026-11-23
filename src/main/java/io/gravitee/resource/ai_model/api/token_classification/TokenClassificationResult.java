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

import io.gravitee.resource.ai_model.api.ModelInvokeException;
import io.gravitee.resource.ai_model.api.Usage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * What a token classification model found.
 *
 * @param model the model that answered, as it names itself; {@code null} when the model does not tell.
 * @param spans the spans found in each input of the request, in the order of the inputs: one list per input, empty
 *              when the model found nothing in it. The spans of an input are entities, not tokens: an implementation
 *              whose model tags tokens ({@code B-PERSON}, {@code I-PERSON}) merges them into the entities they make.
 *              They come in the order of their {@code start}, and may overlap, a zero-shot model finding the same
 *              name as a {@code person} and its first name as a {@code first_name} for instance: a consumer resolves
 *              the overlaps. Read them through {@link #spansFor(TokenClassificationRequest)}, which checks that the
 *              result answers every input of the request.
 * @param usage what the call consumed; {@code null} when the model does not tell.
 */
public record TokenClassificationResult(String model, List<List<Span>> spans, Usage usage) {
    public TokenClassificationResult {
        Objects.requireNonNull(spans, "spans");
        var copy = new ArrayList<List<Span>>(spans.size());
        for (int i = 0; i < spans.size(); i++) {
            List<Span> found = spans.get(i);
            if (found == null) {
                throw new IllegalArgumentException("The spans of the input [" + i + "] are missing");
            }
            if (found.stream().anyMatch(Objects::isNull)) {
                throw new IllegalArgumentException("A span of the input [" + i + "] is missing");
            }
            copy.add(List.copyOf(found));
        }
        spans = Collections.unmodifiableList(copy);
    }

    /**
     * @param request the request this result answers.
     * @return the spans of each input of the request, in the order of its inputs.
     * @throws ModelInvokeException when the result does not hold one list of spans for each input of the request: a
     *                              consumer that went through the spans alone would leave the inputs without an answer
     *                              unchecked.
     */
    public List<List<Span>> spansFor(TokenClassificationRequest request) {
        Objects.requireNonNull(request, "request");
        if (spans.size() != request.inputs().size()) {
            throw new ModelInvokeException(
                "The token classification model answered for " +
                    spans.size() +
                    " texts, not for the " +
                    request.inputs().size() +
                    " it was given"
            );
        }
        return spans;
    }

    /**
     * A passage of an input the model labelled.
     *
     * @param category the category of the passage in the {@link AiTokenClassificationModelResource#catalog() catalog}
     *                 of the resource, the one its label is mapped to.
     * @param label the label the model gave the passage, as the model names it; for logs and reports, consumers decide
     *              on the category.
     * @param score how sure the model is of the label, from 0 to 1.
     * @param start the index of the first character of the passage in the input, as {@link String#substring(int, int)}
     *              counts it.
     * @param end the index after its last character; a passage is never empty.
     */
    public record Span(String category, String label, double score, int start, int end) {
        public Span {
            if (category == null || category.isBlank()) {
                throw new IllegalArgumentException("A span needs a category");
            }
            if (label == null || label.isBlank()) {
                throw new IllegalArgumentException("A span needs a label");
            }
            if (start < 0 || end <= start) {
                throw new IllegalArgumentException("Invalid span [" + start + ", " + end + ")");
            }
        }
    }
}

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
import io.reactivex.rxjava3.core.Single;

/**
 * A resource giving access to a token classification model: a model that finds passages in a text and labels each of
 * them, the entities of a named entity recognition model or the personal data of a PII model for instance.
 * <p>
 * The labels are those of the model, and differ from one model to another. The resource maps them to the categories
 * of a {@link #catalog() catalog}, a vocabulary its consumers know, so that a consumer is written once for every model
 * of that catalog.
 * <p>
 * It is an interface, which a resource implements alongside the resource class it extends, an
 * {@link io.gravitee.resource.api.AbstractConfigurableResource} for instance: it has no lifecycle of its own, the
 * implementation owns what reaches the model and releases it on stop.
 */
public interface AiTokenClassificationModelResource {
    /**
     * @return the id of the catalog the categories of the spans belong to, one of the package
     *         {@link io.gravitee.resource.ai_model.api.catalog}. A consumer that reads the categories of one catalog
     *         checks this id first: the same category name may mean something else in another catalog.
     */
    String catalog();

    /**
     * Asks the model to classify the texts of the request, each on its own.
     *
     * @param request the texts.
     * @return the entities found in every text of the request, in the order of their start in each text; they may
     *         overlap. Fails with a {@link ModelInvokeException} when the model cannot be reached, does not answer in
     *         time, or answers something else than what was asked: implementations check the answer of the model (a
     *         result for each text, spans within their text, a score from 0 to 1, a category of the
     *         {@link #catalog() catalog}) before returning it, and never return a partial result.
     */
    Single<TokenClassificationResult> classify(TokenClassificationRequest request);
}

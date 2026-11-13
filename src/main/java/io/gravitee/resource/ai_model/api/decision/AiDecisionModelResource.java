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

import io.gravitee.resource.ai_model.api.ModelInvokeException;
import io.gravitee.resource.api.AbstractConfigurableResource;
import io.gravitee.resource.api.ResourceConfiguration;
import io.reactivex.rxjava3.core.Single;

/**
 * A resource giving access to a decision model: a model that answers questions about a context with a decision and
 * how sure it is of it, rather than with generated text.
 *
 * @param <C> the configuration of the resource.
 */
public abstract class AiDecisionModelResource<C extends ResourceConfiguration> extends AbstractConfigurableResource<C> {

    /**
     * Asks the model the questions of the request about its context.
     *
     * @param request the context and the questions.
     * @return the answer to every question of the request. Fails with a {@link ModelInvokeException} when the model
     *         cannot be reached, does not answer in time, or answers something else than what was asked: implementations
     *         check the answers of the model (a known key and shape for each question, probabilities and confidences
     *         from 0 to 1, a choice among those of the question, a score within the scale) before returning them.
     */
    public abstract Single<DecisionResult> decide(DecisionRequest request);
}

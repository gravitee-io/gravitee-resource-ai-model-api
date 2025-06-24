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
package io.gravitee.resource.ai_model.api;

import io.gravitee.resource.ai_model.api.model.PromptInput;
import io.gravitee.resource.api.AbstractConfigurableResource;
import io.gravitee.resource.api.ResourceConfiguration;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
public abstract class AiTextModelResource<C extends ResourceConfiguration, MODEL_RESULT, RESOURCE_RESULT>
    extends AbstractConfigurableResource<C>
    implements ApplicationContextAware {

    protected Vertx vertx;
    protected ApplicationContext applicationContext;
    protected InferenceServiceClient<PromptInput, MODEL_RESULT> inferenceServiceClient;

    @Override
    public void doStart() throws Exception {
        super.doStart();
        vertx = applicationContext.getBean(Vertx.class);
        inferenceServiceClient = buildInferenceServiceClient();

        fetchModel();
    }

    @Override
    public void doStop() throws Exception {
        super.doStop();
        inferenceServiceClient
            .stopModel()
            .subscribe(
                address -> log.debug("Model [{}] at address [{}] stopped", getModelName(), address),
                throwable -> log.error("Model {} stopped", getModelName(), throwable)
            );
    }

    protected abstract String getModelName();

    public abstract Single<RESOURCE_RESULT> invokeModel(PromptInput input);

    protected abstract InferenceServiceClient<PromptInput, MODEL_RESULT> buildInferenceServiceClient();

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    protected void fetchModel() {
        inferenceServiceClient
            .loadModel()
            .subscribe(
                address -> log.debug("Loaded model [{}] at address [{}]", getModelName(), address),
                t -> log.error("Failed to load model", t)
            );
    }
}

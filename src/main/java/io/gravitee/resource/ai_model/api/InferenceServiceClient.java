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

import static io.gravitee.inference.api.Constants.MODEL_ADDRESS_KEY;
import static io.gravitee.inference.api.Constants.SERVICE_INFERENCE_MODELS_ADDRESS;
import static io.gravitee.inference.api.service.InferenceAction.STOP;

import io.gravitee.inference.api.service.InferenceAction;
import io.gravitee.inference.api.service.InferenceRequest;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.eventbus.Message;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class InferenceServiceClient<INPUT, RESULT> {

    private final Vertx vertx;

    private String modelAddress = null;

    protected InferenceServiceClient(Vertx vertx) {
        this.vertx = vertx;
    }

    protected abstract RESULT handleResult(Message<Buffer> message);

    protected abstract InferenceRequest buildInferRequest(INPUT sentence);

    public Single<RESULT> infer(INPUT input) {
        return getModelAddress()
            .flatMap(address ->
                vertx
                    .eventBus()
                    .<Buffer>request(address, Json.encodeToBuffer(buildInferRequest(input)))
                    .map(this::handleResult)
                    .doOnError(throwable -> log.error(throwable.getMessage(), throwable))
            );
    }

    private Single<String> getModelAddress() {
        return Maybe.just(modelAddress).switchIfEmpty(Single.error(new ModelInvokeException("Model address could not be resolved")));
    }

    public Single<String> loadModel(Map<String, Object> config) {
        var request = new InferenceRequest(InferenceAction.START, config);
        return vertx
            .eventBus()
            .<Buffer>request(SERVICE_INFERENCE_MODELS_ADDRESS, Json.encodeToBuffer(request))
            .map(bufferMessage -> bufferMessage.body().toString())
            .doOnSuccess(address -> modelAddress = address)
            .doOnError(throwable -> log.error(throwable.getMessage(), throwable));
    }

    public Single<String> stopModel() {
        return vertx
            .eventBus()
            .<Buffer>request(SERVICE_INFERENCE_MODELS_ADDRESS, Json.encodeToBuffer(buildStopRequest()))
            .map(bufferMessage -> bufferMessage.body().toString())
            .doOnSuccess(address -> modelAddress = null)
            .doOnError(throwable -> log.error(throwable.getMessage(), throwable));
    }

    protected InferenceRequest buildStopRequest() {
        return new InferenceRequest(STOP, Map.of(MODEL_ADDRESS_KEY, modelAddress));
    }
}

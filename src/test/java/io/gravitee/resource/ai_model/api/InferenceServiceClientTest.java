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

import static io.gravitee.inference.api.Constants.SERVICE_INFERENCE_MODELS_ADDRESS;
import static org.assertj.core.api.Assertions.assertThat;

import io.gravitee.inference.api.service.InferenceRequest;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.eventbus.Message;
import java.util.Map;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(VertxExtension.class)
class InferenceServiceClientTest {

    @Nested
    class LoadModelTest {

        @Test
        void single_should_be_in_error_when_bad_response(Vertx vertx, VertxTestContext context) {
            var cut = buildInferenceClient(vertx);

            vertx
                .eventBus()
                .consumer(SERVICE_INFERENCE_MODELS_ADDRESS)
                .handler(message -> message.reply("bad response"));

            cut
                .loadModel()
                .subscribe(
                    str -> context.failNow("must fail"),
                    err -> {
                        context.verify(() -> assertThat(err).isInstanceOf(ModelLifeCycleException.class));
                        context.completeNow();
                    }
                );
        }

        @Test
        void single_should_the_content_buffer_as_string(Vertx vertx, VertxTestContext context) {
            var cut = buildInferenceClient(vertx);

            vertx
                .eventBus()
                .consumer(SERVICE_INFERENCE_MODELS_ADDRESS)
                .handler(message -> message.reply(Buffer.buffer("response")));

            cut.loadModel().subscribe(str -> context.verify(() -> assertThat(str).isEqualTo("response")).completeNow(), context::failNow);
        }

        private static <A, B> InferenceServiceClient<A, B> buildInferenceClient(Vertx vertx) {
            return new InferenceServiceClient<>(io.vertx.rxjava3.core.Vertx.newInstance(vertx), Map.of()) {
                @Override
                protected InferenceRequest buildInferRequest(A sentence) {
                    return null;
                }

                @Override
                protected B handleResult(Message<Buffer> message) {
                    return null;
                }
            };
        }
    }
}

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

import io.gravitee.resource.ai_model.api.model.ModelFileType;
import io.gravitee.resource.ai_model.api.model.PromptInput;
import io.gravitee.resource.api.AbstractConfigurableResource;
import io.gravitee.resource.api.ResourceConfiguration;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
public abstract class AiTextModelResource<C extends ResourceConfiguration, MODEL_RESULT, RESOURCE_RESULT>
    extends AbstractConfigurableResource<C>
    implements ApplicationContextAware {

    private static final String GRAVITEE_HOME = "gravitee.home";
    private static final String GRAVITEE_HOME_PATH = System.getProperty(GRAVITEE_HOME);

    protected Vertx vertx;
    protected ApplicationContext applicationContext;
    protected ModelFetcher modelFetcher;
    protected InferenceServiceClient<PromptInput, MODEL_RESULT> inferenceServiceClient;

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        vertx = applicationContext.getBean(Vertx.class);
        modelFetcher = buildModelFetcher();
        inferenceServiceClient = buildInferenceServiceClient();

        fetchModel();
    }

    public abstract Single<RESOURCE_RESULT> invokeModel(PromptInput input);

    protected abstract ModelFetcher buildModelFetcher();

    protected abstract InferenceServiceClient<PromptInput, MODEL_RESULT> buildInferenceServiceClient();

    protected abstract Map<String, Object> getModelConfiguration(Map<ModelFileType, String> modelFileMap);

    protected abstract String getModelId();

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    protected void fetchModel() {
        modelFetcher
            .fetchModel()
            .flatMap(modelFileMap -> inferenceServiceClient.loadModel(getModelConfiguration(modelFileMap)))
            .subscribe(
                address -> log.debug("Loaded model [{}] at address [{}]", getModelId(), address),
                t -> log.error("Failed to load model", t)
            );
    }

    protected Path getFileDirectory() throws IOException {
        Path directory = Path.of(GRAVITEE_HOME_PATH + "/models/" + getModelId());
        try {
            return Files.createDirectories(directory);
        } catch (FileAlreadyExistsException faee) {
            log.debug("{} already exists, skip directory creation", directory);
            return directory;
        } catch (Exception e) {
            log.warn("Failed to create directory, creating temp directory", e);
            return Files.createTempDirectory(getModelId().replace("/", "-"));
        }
    }
}

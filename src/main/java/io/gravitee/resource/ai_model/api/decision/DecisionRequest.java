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

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedMap;

/**
 * What a decision model is asked.
 *
 * @param context what the questions are about: untrusted data, often the end user's input. Implementations send it
 *                to the model as data, apart from the instructions of the questions, never inside them. It is sent
 *                as JSON (as Vert.x {@code Json.encode} does): a text, or any structure of maps with text keys
 *                ({@link JsonObject} included), lists ({@link JsonArray} included), texts, finite numbers, booleans
 *                and {@code null} values. Copied into unmodifiable maps and lists, in their order: a {@link JsonObject}
 *                comes back as a map, a {@link JsonArray} as a list.
 * @param questions the questions, each under the key its answer comes back under, in the
 *                  order they are asked; at least one.
 */
public record DecisionRequest(Object context, SequencedMap<String, Question> questions) {
    public DecisionRequest {
        Objects.requireNonNull(context, "context");
        context = copyJson(context, "context");
        if (questions == null || questions.isEmpty()) {
            throw new IllegalArgumentException("A decision request needs at least one question");
        }
        questions.forEach((key, question) -> {
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("A question needs a key");
            }
            if (question == null) {
                throw new IllegalArgumentException("The question [" + key + "] is missing");
            }
        });
        questions = Collections.unmodifiableSequencedMap(new LinkedHashMap<>(questions));
    }

    private static Object copyJson(Object value, String path) {
        return switch (value) {
            case null -> null;
            case String text -> text;
            case Double number when !Double.isFinite(number) -> throw notFinite(path);
            case Float number when !Float.isFinite(number) -> throw notFinite(path);
            case Number number -> number;
            case Boolean bool -> bool;
            case JsonObject json -> copyJson(json.getMap(), path);
            case JsonArray json -> copyJson(json.getList(), path);
            case Map<?, ?> map -> {
                var copy = new LinkedHashMap<String, Object>();
                map.forEach((key, child) -> {
                    if (!(key instanceof String name)) {
                        throw new IllegalArgumentException("The context keys must be texts, found [" + key + "] in " + path);
                    }
                    copy.put(name, copyJson(child, path + "." + name));
                });
                yield Collections.unmodifiableSequencedMap(copy);
            }
            case List<?> list -> {
                var copy = new ArrayList<>(list.size());
                for (int i = 0; i < list.size(); i++) {
                    copy.add(copyJson(list.get(i), path + "[" + i + "]"));
                }
                yield Collections.unmodifiableList(copy);
            }
            default -> throw new IllegalArgumentException(
                "The context can only hold texts, numbers, booleans, maps and lists, found a " + value.getClass().getName() + " in " + path
            );
        };
    }

    private static IllegalArgumentException notFinite(String path) {
        return new IllegalArgumentException("The context numbers must be finite, found one in " + path);
    }

    /** @return a request asking one question, under the given key. */
    public static DecisionRequest of(Object context, String key, Question question) {
        var questions = new LinkedHashMap<String, Question>();
        questions.put(key, question);
        return new DecisionRequest(context, questions);
    }
}

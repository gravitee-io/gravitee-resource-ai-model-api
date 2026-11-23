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
package io.gravitee.resource.ai_model.api.catalog;

import java.util.Optional;

/**
 * The catalog of personally identifiable information: the categories a resource maps the labels of a PII model to,
 * and the ones its consumers act on. It is data only: no resource contract depends on it.
 * <p>
 * A category travels as its {@link Category#name() name}. Renaming or removing one breaks the configurations that
 * name it: only add to it.
 * <p>
 * Every plugin bundles its own copy of this API: a resource built on a newer copy may answer a category an older
 * consumer does not know, which {@link #category(String)} answers as empty. A consumer handles it failing closed: it
 * treats it as personal data, redacting it for instance, rather than letting it through.
 */
public final class PiiCatalog {

    /** The id of the catalog, the one a resource mapping to it answers as its catalog. */
    public static final String ID = "pii";

    private PiiCatalog() {}

    /**
     * @param name the name of a category, as a span carries it.
     * @return the category of that name; empty when the catalog has none, {@code null} included.
     */
    public static Optional<Category> category(String name) {
        if (name == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Category.valueOf(name));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /** A category of personally identifiable information. */
    public enum Category {
        /** The names of individuals, with their titles and prefixes. */
        PERSON,

        /** Companies, public or private organizations, and institutions. */
        ORGANIZATION,

        /** Physical addresses, coordinates, and geographic locations. */
        LOCATION,

        /** Email addresses. */
        EMAIL,

        /** Telephone and fax numbers, country codes included. */
        PHONE,

        /** Network identifiers such as IP addresses, MAC addresses, and URLs. */
        NETWORK_IDENTIFIER,

        /** Hardware identifiers such as IMEI or serial numbers. */
        DEVICE_IDENTIFIER,

        /** Bank accounts, credit card numbers, IBANs, routing numbers, and financial records. */
        FINANCIAL_ACCOUNT,

        /** Cryptocurrency wallet addresses. */
        CRYPTOCURRENCY_WALLET,

        /** Identifiers issued by a government: social security number, passport, driver's license. */
        GOVERNMENT_ID,

        /** Vehicle identification numbers and license plates. */
        VEHICLE_ID,

        /** Security credentials such as passwords, API keys, and secrets. */
        CREDENTIAL,

        /** Personal attributes such as age, date of birth, or gender. */
        DEMOGRAPHIC,

        /** Any other sensitive data that fits no other category. */
        MISCELLANEOUS,
    }
}

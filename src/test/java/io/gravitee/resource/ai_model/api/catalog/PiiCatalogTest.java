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

import static org.assertj.core.api.Assertions.assertThat;

import io.gravitee.resource.ai_model.api.catalog.PiiCatalog.Category;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PiiCatalogTest {

    @Test
    void should_keep_its_id() {
        assertThat(PiiCatalog.ID).isEqualTo("pii");
    }

    /** The configurations of the plugins name the categories: one renamed or removed breaks them. */
    @Test
    void should_keep_the_names_of_its_categories() {
        assertThat(Arrays.stream(Category.values()).map(Category::name)).containsExactly(
            "PERSON",
            "ORGANIZATION",
            "LOCATION",
            "EMAIL",
            "PHONE",
            "NETWORK_IDENTIFIER",
            "DEVICE_IDENTIFIER",
            "FINANCIAL_ACCOUNT",
            "CRYPTOCURRENCY_WALLET",
            "GOVERNMENT_ID",
            "VEHICLE_ID",
            "CREDENTIAL",
            "DEMOGRAPHIC",
            "MISCELLANEOUS"
        );
    }

    @Test
    void should_find_a_category_by_its_name() {
        assertThat(PiiCatalog.category("EMAIL")).contains(Category.EMAIL);
    }

    @Test
    void should_find_no_category_for_a_name_it_does_not_have() {
        assertThat(PiiCatalog.category("email")).isEmpty();
        assertThat(PiiCatalog.category("TOXICITY")).isEmpty();
        assertThat(PiiCatalog.category(null)).isEmpty();
    }
}

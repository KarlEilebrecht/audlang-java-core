//@formatter:off
/*
 * ProcessContextTest
 * Copyright 2025 Karl Eilebrecht
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"):
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//@formatter:on

package de.calamanari.adl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class ProcessContextTest {

    @Test
    void testBasics() {

        Flag flag = new Flag() {

            private static final long serialVersionUID = 4724838361411854983L;
            // test flag
        };

        Flag flag2 = new Flag() {

            private static final long serialVersionUID = 5724838361411854222L;
            // test flag
        };

        ProcessContext ctx = ProcessContext.empty();

        assertEquals(Collections.emptySet(), ctx.getGlobalFlags());
        assertEquals(Collections.emptyMap(), ctx.getGlobalVariables());

        ctx.getGlobalVariables().put("a", "xyc");
        ctx.getGlobalFlags().add(flag);

        assertTrue(ctx.getGlobalVariables().containsKey("a"));
        assertTrue(flag.check(ctx.getGlobalFlags()));

        assertTrue(flag.check(flag2, flag));

    }

}

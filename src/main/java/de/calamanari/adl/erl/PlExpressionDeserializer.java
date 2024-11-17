//@formatter:off
/*
 * PlExpressionDeserializer
 * Copyright 2024 Karl Eilebrecht
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

package de.calamanari.adl.erl;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * This custom JACKSON de-serializer solves the expression sub types when reading JSON back into memory.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class PlExpressionDeserializer extends JsonDeserializer<PlExpression<?>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlExpressionDeserializer.class);

    @Override
    public PlExpression<?> deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        ObjectCodec codec = parser.getCodec();
        JsonNode node = codec.readTree(parser);
        try {
            return codec.treeToValue(node, resolveExpressionType(parser, node));
        }
        catch (JsonProcessingException ex) {
            LOGGER.error("""
                    Error during JSON-deserialization of {}
                    The reason might be that you are trying to de-serialize a JSON generated from a CoreExpression.
                    Be aware that these formats are incompatible.
                    """, node);
            throw ex;
        }
    }

    /**
     * Determines the class to use by inspection of the given node.
     * 
     * @param parser
     * @param node
     * @return concrete class to be used for deserialization
     * @throws JsonMappingException
     */
    @SuppressWarnings("java:S1452")
    static final Class<? extends PlExpression<?>> resolveExpressionType(JsonParser parser, JsonNode node) throws JsonMappingException {
        if (node.has("combi_type")) {
            return PlCombinedExpression.class;
        }
        else if (node.has("curb_delegate")) {
            return PlCurbExpression.class;
        }
        else if (node.has("arg_name")) {
            return PlMatchExpression.class;
        }
        else if (node.has("delegate")) {
            return PlNegationExpression.class;
        }
        else if (node.has("set_type")) {
            return PlSpecialSetExpression.class;
        }
        else {
            throw new JsonMappingException(parser, "Could not determine concrete type of presentation layer expression: " + node.asText());
        }

    }

}

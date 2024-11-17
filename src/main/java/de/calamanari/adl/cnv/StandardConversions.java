//@formatter:off
/*
 * StandardConversions
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

package de.calamanari.adl.cnv;

import java.util.Optional;
import java.util.function.Function;

import de.calamanari.adl.AudlangExpression;
import de.calamanari.adl.ConversionException;
import de.calamanari.adl.FormatStyle;
import de.calamanari.adl.TimeOut;
import de.calamanari.adl.erl.AudlangParseResult;
import de.calamanari.adl.erl.PlExpression;
import de.calamanari.adl.erl.PlExpressionBuilder;
import de.calamanari.adl.irl.CoreExpression;
import de.calamanari.adl.irl.biceps.CoreExpressionOptimizer;

//@formatter:off
/**
 * This class contains convenient shorthand functions to perform typical expression conversions.
 * <p/>
 * Usage examples:
 * <ul>
 * <li>Convert an <b><code>expressionString</code></b> into an {@link AudlangParseResult} which will either contain a parsed {@link PlExpression} or an error message:<pre>
 *      AuslangParseResult pr;
 *      pr = Optional.of(expressionString)
 *              .map(StandardConversions.parse)
 *              .get();
 * </pre></li>
 * <li>Convert an <b><code>expressionString</code></b> into a {@link PlExpression}:<pre>
 *      PlExpression<?> expr;
 *      expr = Optional.of(expressionString)
 *              .map(StandardConversions.parse)
 *              .map(StandardConversions.toPlExpression())
 *              .get();
 * </pre></li>
 * <li>Convert a {@link PlExpression} <b><code>plExpression</code></b> into a {@link CoreExpression}:<pre>
 *      CoreExpression expr;
 *      expr = Optional.of(plExpression)
 *              .map(StandardConversions.plToCoreExpression())
 *              .get();
 * </pre></li>
 * <li>Convert an <b><code>expressionString</code></b> right into a {@link CoreExpression}:<pre>
 *      CoreExpression expr;
 *      expr = Optional.of(expressionString)
 *              .map(StandardConversions.parse)
 *              .map(StandardConversions.toCoreExpression())
 *              .get();
 * </pre></li>
 * <li>Re-map the arguments of a {@link CoreExpression} <b><code>coreExpr</code></b> :<pre>
 *      expr = Optional.of(expr)
 *              .map(StandardConversions.mapCoreArguments(mapping, false)
 *              .get();
 * </pre></li>
 * <li>Convert a core expression <b><code>coreExpr</code></b> back to a {@link PlExpression} and pretty-print it as string:<pre>
 *      String expr;
 *      expr = Optional.of(coreExpr)
 *              .map(StandardConversions.coreToPlExpression())
 *              .map(StandardConversions.prettyPrint())
 *              .get();
 * </pre></li>
 * </ul>
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
//@formatter:on
public class StandardConversions {

    /**
     * @return function to parse a string expression into an {@link AudlangParseResult}
     */
    public static Function<String, AudlangParseResult> parse() {
        return PlExpressionBuilder::stringToExpression;
    }

    @SuppressWarnings("java:S1452")
    public static Function<AudlangExpression<?, ?>, String> asString() {
        return AudlangExpression::toString;
    }

    @SuppressWarnings("java:S1452")
    public static Function<AudlangExpression<?, ?>, String> prettyPrint() {
        return expression -> expression.format(FormatStyle.PRETTY_PRINT);
    }

    /**
     * @return function to extract the presentation layer expression from an {@link AudlangParseResult} if available or to otherwise throw an
     *         {@link ConversionException}
     */
    @SuppressWarnings("java:S1452")
    public static Function<AudlangParseResult, PlExpression<?>> toPlExpression() {
        return result -> assertValid().apply(result).getResultExpression();
    }

    /**
     * The returned function converts and optimizes the expression with default settings, see {@link TimeOut#createDefaultTimeOut(String)}
     * 
     * @return function to convert a presentation layer expression into the corresponding {@link CoreExpression}
     */
    @SuppressWarnings("java:S1452")
    public static Function<PlExpression<?>, CoreExpression> plToCoreExpression() {
        return plExpression -> new PlToCoreExpressionConverter(new CoreExpressionOptimizer()).convert(plExpression);
    }

    /**
     * The returned function converts a {@link CoreExpression} back into a presentation layer expression and restores some higher language features.
     * 
     * @return function to convert a {@link CoreExpression} into a {@link PlExpression}
     */
    @SuppressWarnings("java:S1452")
    public static Function<CoreExpression, PlExpression<?>> coreToPlExpression() {
        return new CoreToPlExpressionConverter()::convert;
    }

    /**
     * Shorthand for first parsing the presentation layer expression followed by a conversion into a {@link CoreExpression}
     * 
     * @return function to extract a {@link CoreExpression} from an {@link AudlangParseResult} if available or to otherwise throw an {@link ConversionException}
     */
    public static Function<AudlangParseResult, CoreExpression> toCoreExpression() {
        return result -> plToCoreExpression().apply(assertValid().apply(result).getResultExpression());
    }

    /**
     * Creates a mapping function based on the given mapping to replace the arguments and values in {@link PlExpression}.
     * <p/>
     * The provided mapping must be bijective and structure-preserving (see {@link ArgNameValueMapper#isBijective()} resp.
     * {@link ArgNameValueMapper#isArgumentStructurePreserving()}).
     * 
     * @param mapping settings how to map argument names and values
     * @param lazy if true all unresolvable arguments names and values will be kept as is rather than throwing a {@link MappingNotFoundException}
     * @return argument name and value function
     * @throws IncompatibleMappingException if the mapping does not fulfill the requirements
     */
    @SuppressWarnings("java:S1452")
    public static Function<PlExpression<?>, PlExpression<?>> mapPlArguments(ArgNameValueMapping mapping, boolean lazy) {
        if (lazy) {
            return new MappingPlExpressionConverter(new DefaultArgNameValueMapper(mapping, DummyArgNameValueMapper.getInstance()))::convert;
        }
        else {
            return new MappingPlExpressionConverter(new DefaultArgNameValueMapper(mapping))::convert;
        }
    }

    /**
     * Creates a mapping function based on the given mapping to replace the arguments and values in {@link CoreExpression}.
     * <p/>
     * This method covers bijective as well as non-bijective (not reversible) mappings. Depending on the provided mapping you can replace names with IDs or even
     * adjust the data model. E.g., <code>color = blue AND shape = circle</code> could be mapped to <code>arg156 = 1 AND arg91 = 1</code>.
     * 
     * @param mapping settings how to map argument names and values
     * @param lazy if true all unresolvable arguments names and values will be kept as is rather than throwing a {@link MappingNotFoundException}
     * @return argument name and value function
     */
    public static Function<CoreExpression, CoreExpression> mapCoreArguments(ArgNameValueMapping mapping, boolean lazy) {
        if (lazy) {
            return new MappingCoreExpressionConverter(new DefaultArgNameValueMapper(mapping, DummyArgNameValueMapper.getInstance()))::convert;
        }
        else {
            return new MappingCoreExpressionConverter(new DefaultArgNameValueMapper(mapping))::convert;
        }
    }

    /**
     * @return function to either pass-through the given result or throw an {@link ConversionException} if the result is in error-state
     */
    public static Function<AudlangParseResult, AudlangParseResult> assertValid() {
        return result -> {
            if (result.isError()) {
                throw new ConversionException("Invalid parse result, cause: " + result.getErrorMessage());
            }
            return result;
        };
    }

    /**
     * Shorthand for quickly parsing an expression String into a {@link CoreExpression}, mainly for testing
     * 
     * @param expr not null
     * @return parsed {@link CoreExpression}
     * @throws ConversionException in case of an error
     */
    public static CoreExpression parseCoreExpression(String expr) {
        if (expr == null) {
            throw new ConversionException("Cannot convert expr=null.");
        }

        // The optional below will always be present (or earlier an exception will be thrown)
        // @formatter:off
        return Optional.of(expr)
                .map(parse())
                .map(toPlExpression())
                .map(plToCoreExpression())
                .orElseThrow(() -> new ConversionException("Unexpected missing value after parsing: " + expr));
        // @formatter:on
    }

    /**
     * Shorthand for quickly parsing a presentation layer expression, mainly for testing
     * 
     * @param expr not null
     * @return parsed {@link PlExpression}
     * @throws ConversionException in case of an error
     */
    @SuppressWarnings("java:S1452")
    public static PlExpression<?> parsePlExpression(String expr) {
        if (expr == null) {
            throw new ConversionException("Cannot convert expr=null.");
        }

        // The optional below will always be present (or earlier an exception will be thrown)
        // @formatter:off
        return Optional.of(expr)
                .map(parse())
                .map(toPlExpression())
                .orElseThrow(() -> new ConversionException("Unexpected missing value after parsing: " + expr));
        // @formatter:on
    }

    private StandardConversions() {
        // no instances
    }

}

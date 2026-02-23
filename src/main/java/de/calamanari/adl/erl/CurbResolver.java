//@formatter:off
/*
 * CurbResolutionUtils
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.calamanari.adl.CombinedExpressionType;
import de.calamanari.adl.SpecialSetType;
import de.calamanari.adl.erl.PlCurbExpression.PlCurbOperator;

import static de.calamanari.adl.VariationUtils.computeNumberOfSubLists;
import static de.calamanari.adl.VariationUtils.createSubLists;

/**
 * The {@link CurbResolver} utility resolves {@link PlCurbExpression}s (recursively) into basic Audlang expressions.
 * <p>
 * See also <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#43-curbed-or">§4.3 Audlang
 * Specification</a>
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class CurbResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(CurbResolver.class);

    /**
     * Limits the execution of the resolver, aborts if the resolution runs into combinatoric explosion
     */
    private static final int MAX_NUMBER_OF_SUBLIST = 2_000;

    /**
     * Represents the case that there is no minimum number of expressions to be fulfilled (for better readability)
     */
    private static final int NO_LOWER_LIMIT = 0;

    /**
     * Represents the case that there is no maximum number of expressions to be fulfilled (for better readability)
     */
    private static final int NO_UPPER_LIMIT = -1;

    /**
     * Represents the case that the upper limit cannot be fulfilled (e.g. &lt;0)
     */
    private static final int INVALID_UPPER_LIMIT = -2;

    /**
     * Minimum number of conditions in the curbed OR that MUST be fulfilled
     */
    private final int minRequired;

    /**
     * Maximum number of conditions in the curbed OR that is ALLOWED to be fulfilled
     */
    private final int maxAllowed;

    /**
     * The members of the curbed OR
     */
    private final List<PlExpression<?>> expressions;

    /**
     * All the members negated
     */
    private final List<PlExpression<?>> expressionsNegated;

    /**
     * The expression to be resolved
     */
    private final PlCurbExpression curbExpression;

    /**
     * Resolves the given curbed OR into a (potentially very large) expression composed of simple expressions.
     * 
     * @param curbExpression to be resolved
     * @return new expression composed of simple expressions
     */
    @SuppressWarnings("java:S1452")
    public static PlExpression<?> resolve(PlCurbExpression curbExpression) {
        if (curbExpression.operator() == PlCurbOperator.NOT_EQUALS) {
            return handleNotEqualsBound(curbExpression);
        }
        else {
            return new CurbResolver(curbExpression).createResolution();
        }
    }

    /**
     * @param curbExpression
     * @return resolution
     */
    private static PlExpression<?> handleNotEqualsBound(PlCurbExpression curbExpression) {
        int memberCount = curbExpression.curbDelegate().members().size();
        if (curbExpression.bound() == 0) {
            return new CurbResolver(new PlCurbExpression(curbExpression.curbDelegate(), PlCurbOperator.GREATER_THAN, 0, null)).createResolution();
        }
        else if (curbExpression.bound() == memberCount) {
            return new CurbResolver(new PlCurbExpression(curbExpression.curbDelegate(), PlCurbOperator.LESS_THAN, memberCount, null)).createResolution();
        }
        else if (curbExpression.bound() > memberCount) {
            return new PlSpecialSetExpression(SpecialSetType.ALL, null);
        }
        else {
            PlExpression<?> leftResolution = new CurbResolver(
                    new PlCurbExpression(curbExpression.curbDelegate(), PlCurbOperator.LESS_THAN, curbExpression.bound(), null)).createResolution();
            PlExpression<?> rightResolution = new CurbResolver(
                    new PlCurbExpression(curbExpression.curbDelegate(), PlCurbOperator.GREATER_THAN, curbExpression.bound(), null)).createResolution();
            return new PlCombinedExpression(CombinedExpressionType.OR, Arrays.asList(leftResolution, rightResolution), null);
        }
    }

    /**
     * @param curbExpression
     * @return minimum number of members that MUST be fulfilled
     */
    private static int computeRequiredNumberOfFulfilledConditions(PlCurbExpression curbExpression) {
        switch (curbExpression.operator()) {
        case PlCurbOperator.EQUALS, PlCurbOperator.GREATER_THAN_OR_EQUALS:
            return curbExpression.bound();
        case PlCurbOperator.GREATER_THAN:
            // avoid overflow
            return curbExpression.bound() == Integer.MAX_VALUE ? curbExpression.bound() : curbExpression.bound() + 1;
        // $CASES-OMITTED$
        default:
            return NO_LOWER_LIMIT;
        }
    }

    /**
     * 
     * @param curbExpression
     * @return maximum number of members that is ALLOWED to be fulfilled
     */
    private static int computeAllowedNumberOfFulfilledConditions(PlCurbExpression curbExpression) {
        switch (curbExpression.operator()) {
        case PlCurbOperator.EQUALS, PlCurbOperator.LESS_THAN_OR_EQUALS:
            return curbExpression.bound();
        case PlCurbOperator.LESS_THAN:
            // avoid ambiguity
            return curbExpression.bound() == 0 ? INVALID_UPPER_LIMIT : curbExpression.bound() - 1;
        // $CASES-OMITTED$
        default:
            // no limit
            return NO_UPPER_LIMIT;
        }
    }

    protected CurbResolver(PlCurbExpression curbExpression) {

        this.curbExpression = curbExpression;

        this.expressions = curbExpression.curbDelegate().members().stream().map(PlExpression::resolveHigherLanguageFeatures)
                .collect(Collectors.toCollection(ArrayList<PlExpression<?>>::new));
        this.expressionsNegated = curbExpression.curbDelegate().members().stream()
                .map(e -> new PlNegationExpression(e, false, null).resolveHigherLanguageFeatures())
                .collect(Collectors.toCollection(ArrayList<PlExpression<?>>::new));

        this.minRequired = computeRequiredNumberOfFulfilledConditions(curbExpression);
        int limit = computeAllowedNumberOfFulfilledConditions(curbExpression);
        if (limit >= expressions.size()) {
            LOGGER.trace("Ignoring upper bound (irrelevant): minRequired={} and maxAllowed={} on {} members ({})", minRequired, limit, expressions.size(),
                    expressions);
            this.maxAllowed = NO_UPPER_LIMIT;
        }
        else {
            this.maxAllowed = limit;
        }

    }

    /**
     * System protection: Throws an exception if the execution would otherwise exceed system limits.
     * <p>
     * In case of combinatoric explosion the algorithm can <i>run away</i> causing insane runtime or worst-case {@link OutOfMemoryError}s.
     * 
     * @param supplyList
     * @param targetSize
     * @throws CurbComplexityException if the expression is too complex to be resolved
     */
    private void assertFeasibleComplexity(List<PlExpression<?>> supplyList, int targetSize) {
        int count = computeNumberOfSubLists(supplyList, targetSize);
        if (count < 0 || count > MAX_NUMBER_OF_SUBLIST) {
            throw new CurbComplexityException("The given CURB-expression's exceeds system limits: " + this.curbExpression);
        }
    }

    /**
     * @return the plain expression after resolving the curb into simpler expressions
     */
    @SuppressWarnings("java:S1452")
    protected PlExpression<?> createResolution() {
        if (maxAllowed == INVALID_UPPER_LIMIT || (maxAllowed > 0 && maxAllowed < minRequired) || (minRequired > expressions.size())) {
            LOGGER.trace("Returning NONE (cannot be fulfilled) for minRequired={} and maxAllowed={} on {} members ({})", minRequired, maxAllowed,
                    expressions.size(), expressions);
            return new PlSpecialSetExpression(SpecialSetType.NONE, null);
        }
        if (minRequired == NO_LOWER_LIMIT && maxAllowed == NO_UPPER_LIMIT) {
            LOGGER.trace("Returning ALL (always fulfilled) for minRequired={} and maxAllowed={} on {} members ({})", minRequired, maxAllowed,
                    expressions.size(), expressions);
            return new PlSpecialSetExpression(SpecialSetType.ALL, null);
        }
        PlExpression<?> res = null;
        if (minRequired == expressions.size()) {
            // we need all of them to be true
            res = new PlCombinedExpression(CombinedExpressionType.AND, expressions, null);
        }
        else if (minRequired == 1 && maxAllowed == NO_UPPER_LIMIT) {
            // any condition must be true
            res = new PlCombinedExpression(CombinedExpressionType.OR, expressions, null);
        }
        else if (maxAllowed == NO_UPPER_LIMIT) {
            // there is no upper limit
            assertFeasibleComplexity(expressions, minRequired);
            res = createAggregatedCondition(createSubLists(expressions, minRequired));
        }
        else {
            // complex case with upper limit
            res = resolveConsiderMaxAllowedConditions();
        }
        LOGGER.trace("Returning {} for minRequired={} and maxAllowed={} on {} members ({})", res, minRequired, maxAllowed, expressions.size(), expressions);
        return res;
    }

    /**
     * Here we deal with the complex scenario that we must deal with a number of required conditions but also with an upper limit of allowed conditions at the
     * same time.
     * 
     * @return resolution
     */
    private PlExpression<?> resolveConsiderMaxAllowedConditions() {
        PlExpression<?> res = null;

        // count conditions that MUST be FALSE
        int minRequiredNegations = expressions.size() - maxAllowed;

        if (minRequired == NO_LOWER_LIMIT) {
            // consider all possible combinations of all the negative conditions, because none of the positive conditions is mandatory
            assertFeasibleComplexity(expressionsNegated, minRequiredNegations);
            res = createAggregatedCondition(createSubLists(expressionsNegated, minRequiredNegations));
        }
        else {
            assertFeasibleComplexity(expressions, minRequired);
            List<List<PlExpression<?>>> requiredExpressionsList = createSubLists(expressions, minRequired);
            for (List<PlExpression<?>> requiredExpressions : requiredExpressionsList) {
                List<PlExpression<?>> othersNegated = allOtherExpressionsNegated(requiredExpressions);
                if (maxAllowed == minRequired) {
                    // the remaining conditions MUST all be false
                    requiredExpressions.addAll(othersNegated);
                }
                else {
                    // SOME of the remaining conditions MUST be false
                    assertFeasibleComplexity(othersNegated, minRequiredNegations);
                    requiredExpressions.add(createAggregatedCondition(createSubLists(othersNegated, minRequiredNegations)));
                }
            }
            res = createAggregatedCondition(requiredExpressionsList);
        }

        return res;
    }

    /**
     * @param currentList
     * @return a list of all other expressions from the initial list in negated form (complement)
     */
    private List<PlExpression<?>> allOtherExpressionsNegated(List<PlExpression<?>> currentList) {
        List<PlExpression<?>> res = new ArrayList<>();
        for (int i = 0; i < expressions.size(); i++) {
            PlExpression<?> candidate = expressions.get(i);
            if (!currentList.contains(candidate)) {
                res.add(expressionsNegated.get(i));
            }
        }
        return res;
    }

    /**
     * Each sublist of the given list represents a set of conditions that must be fulfilled (logical) AND.
     * <p>
     * This method returns a single OR composed of these sub-ANDs.
     * 
     * @param subConditionLists list with condition lists
     * @return OR-expressions
     */
    private PlExpression<?> createAggregatedCondition(List<List<PlExpression<?>>> subConditionLists) {

        List<PlExpression<?>> orMemberList = new ArrayList<>(subConditionLists.size());
        for (List<PlExpression<?>> subConditionList : subConditionLists) {
            if (subConditionList.size() == 1) {
                orMemberList.add(subConditionList.get(0));
            }
            else {
                orMemberList.add(new PlCombinedExpression(CombinedExpressionType.AND, subConditionList, null));
            }
        }
        if (orMemberList.size() == 1) {
            return orMemberList.get(0);
        }
        else {
            return new PlCombinedExpression(CombinedExpressionType.OR, orMemberList, null);
        }
    }

}

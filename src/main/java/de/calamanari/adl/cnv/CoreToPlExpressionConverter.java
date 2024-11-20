//@formatter:off
/*
 * CoreToPlExpressionConverter
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.calamanari.adl.CombinedExpressionType;
import de.calamanari.adl.FormatStyle;
import de.calamanari.adl.erl.PlCombinedExpression;
import de.calamanari.adl.erl.PlCurbExpression;
import de.calamanari.adl.erl.PlExpression;
import de.calamanari.adl.erl.PlMatchExpression;
import de.calamanari.adl.erl.PlMatchOperator;
import de.calamanari.adl.erl.PlNegationExpression;
import de.calamanari.adl.erl.PlOperand;
import de.calamanari.adl.erl.PlSpecialSetExpression;
import de.calamanari.adl.irl.CombinedExpression;
import de.calamanari.adl.irl.CoreExpression;
import de.calamanari.adl.irl.MatchExpression;
import de.calamanari.adl.irl.NegationExpression;
import de.calamanari.adl.irl.Operand;
import de.calamanari.adl.irl.SpecialSetExpression;

/**
 * The {@link CoreToPlExpressionConverter} takes an internal representation layer expression and produces a corresponding {@link PlExpression}.
 * <p>
 * Certain advanced language features which are only available on the presentation layer can be recreated, others like the {@link PlCurbExpression} cannot be
 * created from a plain {@link CoreExpression}.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class CoreToPlExpressionConverter extends AbstractCoreExpressionConverter<PlExpression<?>, DefaultConversionContext<PlExpression<?>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreToPlExpressionConverter.class);

    /**
     * Special comparator for a nicer member order after converting an expression
     */
    public static final Comparator<PlExpression<?>> MEMBER_PRETTY_ORDER_COMPARATOR = (left, right) -> {
        int res = 0;
        if (left instanceof PlMatchExpression leftMatch && right instanceof PlMatchExpression rightMatch) {
            res = compareMatches(leftMatch, rightMatch);
        }
        else if (left instanceof PlNegationExpression leftNeg && leftNeg.delegate() instanceof PlMatchExpression leftMatch
                && right instanceof PlMatchExpression rightMatch) {
            res = compareMatches(leftMatch, rightMatch);
            if (res == 0) {
                // negative after positive
                res = 1;
            }
        }
        else if (left instanceof PlMatchExpression leftMatch && right instanceof PlNegationExpression rightNeg
                && rightNeg.delegate() instanceof PlMatchExpression rightMatch) {
            res = compareMatches(leftMatch, rightMatch);
            if (res == 0) {
                // positive before negative
                res = -1;
            }
        }
        else if (left instanceof PlNegationExpression leftNeg && leftNeg.delegate() instanceof PlMatchExpression leftMatch
                && right instanceof PlNegationExpression rightNeg && rightNeg.delegate() instanceof PlMatchExpression rightMatch) {
            res = compareMatches(leftMatch, rightMatch);
        }
        else if (left.getClass() != right.getClass() && (left instanceof PlSpecialSetExpression || right instanceof PlCombinedExpression)) {
            // special sets in front, combined expressions to the end
            res = -1;
        }
        else if (left.getClass() != right.getClass() && (right instanceof PlSpecialSetExpression || left instanceof PlCombinedExpression)) {
            // special sets in front, combined expressions to the end
            res = 1;
        }
        if (res == 0) {
            // order lexicographically
            res = left.compareTo(right);
        }
        return res;

    };

    /**
     * @param leftMatch
     * @param rightMatch
     * @return
     */
    private static int compareMatches(PlMatchExpression leftMatch, PlMatchExpression rightMatch) {
        int res;
        res = leftMatch.argName().compareTo(rightMatch.argName());
        if (res == 0 && !leftMatch.operator().isMatchNegation() && rightMatch.operator().isMatchNegation()) {
            // positive before negative
            res = -1;
        }
        else if (res == 0 && leftMatch.operator().isMatchNegation() && !rightMatch.operator().isMatchNegation()) {
            // negative after positive
            res = 1;
        }
        else if (res == 0 && leftMatch.operator().isStrictMatchNegation() && !rightMatch.operator().isStrictMatchNegation()) {
            // strict negation before non-strict negation
            res = -1;
        }
        else if (res == 0 && !leftMatch.operator().isStrictMatchNegation() && rightMatch.operator().isStrictMatchNegation()) {
            // non-strict negation after strict negation
            res = 1;
        }
        else if (res == 0) {
            // order by operation enum
            res = Integer.compare(leftMatch.operator().ordinal(), rightMatch.operator().ordinal());
        }
        return res;
    }

    public CoreToPlExpressionConverter() {
        super(DefaultConversionContext::new);
    }

    @Override
    public void handleMatchExpression(MatchExpression expression) {
        getParentContext().members().add(createPlMatchExpression(expression));
    }

    @Override
    public void exitCombinedExpression(CombinedExpression expression) {
        getParentContext().members().add(new PlCombinedExpression(expression.combiType(), getContext().members(), null));
    }

    @Override
    public void exitNegationExpression(NegationExpression expression) {
        if (getContext().members().size() == 1 && getContext().members().get(0) instanceof PlMatchExpression delegate) {
            switch (delegate.operator()) {
            case EQUALS:
                getParentContext().members().add(new PlMatchExpression(delegate.argName(), PlMatchOperator.STRICT_NOT_EQUALS, delegate.operands(), null));
                break;
            case LESS_THAN, GREATER_THAN:
                getParentContext().members().add(new PlNegationExpression(delegate, true, null));
                break;
            case CONTAINS:
                getParentContext().members().add(new PlMatchExpression(delegate.argName(), PlMatchOperator.STRICT_NOT_CONTAINS, delegate.operands(), null));
                break;
            case IS_UNKNOWN:
                getParentContext().members().add(new PlMatchExpression(delegate.argName(), PlMatchOperator.IS_NOT_UNKNOWN, Collections.emptyList(), null));
                break;
            // $CASES-OMITTED$
            default:
                throw new IllegalStateException("Unexpected operator to be negated, given: " + getContext().members().get(0));
            }
        }
        else {
            throw new IllegalStateException(
                    "Unexpected type of expression to be negated, expected: single PlMatchExpression, given: " + getContext().members());
        }
    }

    @Override
    public void handleSpecialSetExpression(SpecialSetExpression expression) {
        getParentContext().members().add(new PlSpecialSetExpression(expression.setType(), null));
    }

    @Override
    protected CoreExpression prepareRootExpression() {
        CoreExpression root = super.prepareRootExpression();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Prepared expression for conversion: \n{}", root.format(FormatStyle.PRETTY_PRINT));
        }
        return root;
    }

    @Override
    protected PlExpression<?> finishResult() {
        PlExpression<?> before = getRootContext().members().get(0);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("finishResult BEFORE: {}", before);
        }

        PlExpression<?> res = before;

        res = removeRedundantStrictness(res, false);
        res = recreateCombinedOperators(res);
        res = recreateAnyOfCombinations(res);

        // this time we run the aggressive mode to reduce the number of STRICTs to a minimum for better readability
        res = removeRedundantStrictness(res, true);

        res = applyPrettyMemberOrder(res);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("finishResult AFTER: {}{}", ((res != before) ? "*" : " "), res);
        }

        return res;
    }

    /**
     * @param expression
     * @return corresponding {@link CoreExpression} for the given presentation layer expression
     */
    private PlMatchExpression createPlMatchExpression(MatchExpression expression) {
        switch (expression.operator()) {
        case IS_UNKNOWN:
            return new PlMatchExpression(expression.argName(), PlMatchOperator.IS_UNKNOWN, null);
        case EQUALS:
            return new PlMatchExpression(expression.argName(), PlMatchOperator.EQUALS, convertMatchOperand(expression.operand()), null);
        case CONTAINS:
            return new PlMatchExpression(expression.argName(), PlMatchOperator.CONTAINS, convertMatchOperand(expression.operand()), null);
        case LESS_THAN:
            return new PlMatchExpression(expression.argName(), PlMatchOperator.LESS_THAN, convertMatchOperand(expression.operand()), null);
        case GREATER_THAN:
            return new PlMatchExpression(expression.argName(), PlMatchOperator.GREATER_THAN, convertMatchOperand(expression.operand()), null);
        // $CASES-OMITTED$
        default:
            throw new IllegalStateException("Unexpected operator: " + expression);
        }
    }

    /**
     * @param operand
     * @return converted operator suitable for {@link PlMatchExpression}s
     */
    private static PlOperand convertMatchOperand(Operand operand) {
        return new PlOperand(operand.value(), operand.isReference(), null);
    }

    /**
     * Takes the root freshly built from a CoreExpression and re-creates the non-strict matches.
     * <p>
     * The method eliminates the STRICTs where possible and afterwards cleans-up the redundant IS-UNKNOWNs.<br>
     * It is crucial to call this method <i>before</i> any other grouping (e.g., ANY OF) because it can only handle simple matches.
     * 
     * @param rootExpression to be updated
     * @param aggressive if true we consider STRICT neighbors as non-unknown indicators (can impede grouping)
     * @return rootExpression or updated root
     */
    private PlExpression<?> removeRedundantStrictness(PlExpression<?> rootExpression, boolean aggressive) {
        return removeRedundantStrictness(rootExpression, Collections.emptyList(), Collections.emptyList(), aggressive);
    }

    /**
     * Recursively walks through the expression and applies the IS-UNKNOWN collected from <i>parent ORs</i> to the negations to check where we can turn a strict
     * negation into a non-strict one because the expression would be true anyway.
     * 
     * @param expression to be analyzed
     * @param parentIsUnknowns already collected outer IS-UNKNOWNs
     * @param isNotUnknowns names of arguments we know from outer conditions that they are not unknown
     * @param aggressive if true we consider STRICT neighbors as non-unknown indicators (can impede grouping)
     * @return expression or update
     */
    private PlExpression<?> removeRedundantStrictness(PlExpression<?> expression, List<String> parentIsUnknowns, List<String> isNotUnknowns,
            boolean aggressive) {

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("removeRedundantStrictness BEFORE: {}", expression);
        }
        PlExpression<?> update = expression;

        switch (expression) {
        case PlCombinedExpression cmb when cmb.combiType() == CombinedExpressionType.AND:
            update = removeRedundantStrictFromAnd(cmb, parentIsUnknowns, isNotUnknowns, aggressive);
            break;
        case PlCombinedExpression cmb when cmb.combiType() == CombinedExpressionType.OR:
            update = removeRedundantStrictFromOr(cmb, parentIsUnknowns, isNotUnknowns, aggressive);
            break;
        default:

        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("removeRedundantStrictness AFTER: {}{}", ((update != expression) ? "*" : " "), update);
        }

        return update;

    }

    /**
     * Analyzes an OR that could still contain STRICT negations which might be turned into non-STRICT negations
     * 
     * @param expression to be analyzed
     * @param parentIsUnknowns already collected outer IS-UNKNOWNs
     * @param isNotUnknowns names of arguments we know from outer conditions that they are not unknown
     * @param aggressive if true we consider STRICT neighbors as non-unknown indicators (can impede grouping)
     * @return expression or update
     */
    private PlExpression<?> removeRedundantStrictFromOr(PlCombinedExpression expression, List<String> parentIsUnknowns, List<String> isNotUnknowns,
            boolean aggressive) {
        PlExpression<?> res = expression;
        List<PlExpression<?>> members = new ArrayList<>(expression.childExpressions());
        List<String> localIsUnknowns = new ArrayList<>();
        boolean modified = false;
        for (PlExpression<?> member : members) {
            if (isUnknown(member)) {
                localIsUnknowns.add(argNameOf(member));
            }
        }
        List<String> combinedIsUnknowns = new ArrayList<>(parentIsUnknowns.size() + localIsUnknowns.size());
        combinedIsUnknowns.addAll(parentIsUnknowns);
        combinedIsUnknowns.addAll(localIsUnknowns);
        for (int idx = 0; idx < members.size(); idx++) {
            PlExpression<?> member = members.get(idx);
            PlExpression<?> memberUpd = member;
            if (member instanceof PlCombinedExpression cmb) {
                memberUpd = removeRedundantStrictness(cmb, combinedIsUnknowns, isNotUnknowns, aggressive);
            }
            else if (isStrictIntraNegatedMatch(member)) {
                memberUpd = removeRedundantStrictIfApplicable((PlMatchExpression) member, parentIsUnknowns, localIsUnknowns, isNotUnknowns);
            }
            else if (isStrictNegatedMatch(member)) {
                memberUpd = removeRedundantStrictIfApplicable((PlNegationExpression) member, parentIsUnknowns, localIsUnknowns, isNotUnknowns);
            }
            if (memberUpd != member) {
                members.set(idx, memberUpd);
                modified = true;
            }
        }
        modified = removeRedundantIsUnknownsFromOr(members) || modified;
        if (modified) {
            res = recreateCombinedExpression(expression.combiType(), members, Collections.emptyList());
        }
        return res;
    }

    /**
     * This final optimization runs on OR-combinations to detect redundant IS-UNKNOWN
     * <p>
     * Example I: <code>a IS UNKNOWN OR a != 1</code> collapses to <code>a != 1</code> because <code>a IS UNKNOWN</code> <i>is covered</i> by the non-strict
     * negation <code>a != 1</code>.<br>
     * Example II: <code>a IS UNKNOWN OR (a != 1 AND a != 2)</code> collapses to <code>(a != 1 AND a != 2)</code> because <code>a IS UNKNOWN</code> <i>is
     * covered</i> by the non-strict members of the AND.
     * 
     * @param members to be updated
     * @return true if the members were updated
     */
    private boolean removeRedundantIsUnknownsFromOr(List<PlExpression<?>> members) {
        int sizeBefore = members.size();
        for (int idx = members.size() - 1; idx > -1; idx--) {
            PlExpression<?> member = members.get(idx);
            if (isUnknown(member)) {
                String argName = argNameOf(member);
                for (int idxOther = 0; idxOther < members.size(); idxOther++) {
                    if (idxOther != idx && checkIsUnknownCoveredBy(argName, members.get(idxOther))) {
                        members.remove(idx);
                        break;
                    }
                }
            }
        }
        return members.size() != sizeBefore;
    }

    /**
     * Tests whether the candidate <i>covers</i> the case that argName IS UNKNOWN.
     * <p>
     * Example: <code>a != 1</code> <i>implicitly covers</i> <code>a IS UNKNOWN</code>
     * 
     * @param argName
     * @param candidate
     * @return true if the candidate covers the IS-UNKNOWN case for the given argName
     */
    private boolean checkIsUnknownCoveredBy(String argName, PlExpression<?> candidate) {
        boolean res = false;
        if (candidate instanceof PlCombinedExpression cmb && cmb.combiType() == CombinedExpressionType.AND) {
            res = checkIsUnknownCoveredByAnd(argName, cmb);
        }
        else if (candidate instanceof PlCombinedExpression cmb && cmb.combiType() == CombinedExpressionType.OR) {
            res = checkIsUnknownCoveredByOr(argName, cmb);
        }
        else if (isNonStrictIntraNegatedMatch(candidate)) {
            String memberArgName = argNameOf(candidate);
            String memberReferencedArgName = isReferenceMatch(candidate) ? referencedArgNameOf(candidate) : null;
            res = argName.equals(memberArgName) || argName.equals(memberReferencedArgName);
        }
        else if (isNonStrictNegatedMatch(candidate)) {
            PlNegationExpression neg = (PlNegationExpression) candidate;
            PlMatchExpression match = (PlMatchExpression) neg.delegate();
            String memberArgName = argNameOf(match);
            String memberReferencedArgName = isReferenceMatch(match) ? referencedArgNameOf(match) : null;
            res = argName.equals(memberArgName) || argName.equals(memberReferencedArgName);
        }
        return res;
    }

    /**
     * Tests whether the AND-expression <i>covers</i> the case that argName IS UNKNOWN.
     * <p>
     * Example 1: <code>a != 1 AND a != 2</code> <i>implicitly covers</i> <code>a IS UNKNOWN</code><br>
     * Example 2: <code>a != 1 AND b = 2</code> <i>does <b>not</b> implicitly cover</i> <code>a IS UNKNOWN</code>
     * 
     * @param argName
     * @param candidate
     * @return true if the AND covers the IS-UNKNOWN case for the given argName
     */
    private boolean checkIsUnknownCoveredByAnd(String argName, PlCombinedExpression andExpression) {
        for (PlExpression<?> member : andExpression.childExpressions()) {
            if (!checkIsUnknownCoveredBy(argName, member)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests whether the OR-expression <i>covers</i> the case that argName IS UNKNOWN.
     * <p>
     * Example 1: <code>a != 1 OR a != 2</code> <i>implicitly covers</i> <code>a IS UNKNOWN</code><br>
     * Example 2: <code>a != 1 OR b = 2</code> <i>implicitly covers</i> <code>a IS UNKNOWN</code>
     * 
     * @param argName
     * @param candidate
     * @return true if the AND covers the IS-UNKNOWN case for the given argName
     */
    private boolean checkIsUnknownCoveredByOr(String argName, PlCombinedExpression orExpression) {
        for (PlExpression<?> member : orExpression.childExpressions()) {
            if (checkIsUnknownCoveredBy(argName, member)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Analyzes an AND that could still contain STRICT negations which might be turned into non-STRICT negations
     * 
     * @param expression to be analyzed
     * @param parentIsUnknowns already collected outer IS-UNKNOWNs
     * @param isNotUnknowns names of arguments we know from outer conditions that they are not unknown
     * @param aggressive if true we consider STRICT neighbors as non-unknown indicators (can impede grouping)
     * @return expression or update
     */
    private PlExpression<?> removeRedundantStrictFromAnd(PlCombinedExpression expression, List<String> parentIsUnknowns, List<String> isNotUnknowns,
            boolean aggressive) {
        PlExpression<?> res = expression;
        List<PlExpression<?>> members = new ArrayList<>(expression.childExpressions());
        boolean modified = false;

        // The loop below runs backwards because in a scenario with multiple STRICT AND-members related to the same variable
        // we want to keep the left outer one only for better readability
        // e.g.: STRICT a != 0 AND a STRICT NOT CONTAINS "foo" collapses to STRICT a != 0 AND a NOT CONTAINS "foo"
        for (int idx = members.size() - 1; idx > -1; idx--) {
            PlExpression<?> member = members.get(idx);
            PlExpression<?> memberUpd = member;
            if (member instanceof PlCombinedExpression cmb) {
                memberUpd = removeRedundantStrictness(cmb, parentIsUnknowns, deriveIsNotUnknownsForAnd(members, idx, isNotUnknowns, aggressive), aggressive);
            }
            else if (isStrictIntraNegatedMatch(member)) {
                memberUpd = removeRedundantStrictIfApplicable((PlMatchExpression) member, parentIsUnknowns, Collections.emptyList(),
                        deriveIsNotUnknownsForAnd(members, idx, isNotUnknowns, aggressive));
            }
            else if (isStrictNegatedMatch(member)) {
                memberUpd = removeRedundantStrictIfApplicable((PlNegationExpression) member, parentIsUnknowns, Collections.emptyList(),
                        deriveIsNotUnknownsForAnd(members, idx, isNotUnknowns, aggressive));
            }
            if (memberUpd != member) {
                members.set(idx, memberUpd);
                modified = true;
            }
        }
        if (modified) {
            res = new PlCombinedExpression(expression.combiType(), members, expression.comments());
        }
        return res;
    }

    /**
     * This method takes the given is-not-unknowns and adds is-not-unknowns according to the <i>neighbors</i> within an AND.
     * <p>
     * Example: <code>a = 1 AND a STRICT NOT CONTAINS "foo"</code>, here we <i>know</i> from <code>a = 1</code> that a cannot be UNKNOWN anyway, so the STRICT
     * is unnecessary and the expression can be simplified to <code>a = 1 AND a NOT CONTAINS "foo"</code> with better readability.
     * 
     * @param members
     * @param skipIdx
     * @param isNotUnknowns
     * @param aggressive if true we consider STRICT neighbors as non-unknown indicators (can impede grouping)
     * @return list with argument names we know they are not unknown
     */
    private List<String> deriveIsNotUnknownsForAnd(List<PlExpression<?>> members, int skipIdx, List<String> isNotUnknowns, boolean aggressive) {
        List<String> res = new ArrayList<>(isNotUnknowns);
        for (int idx = 0; idx < members.size(); idx++) {
            if (idx != skipIdx) {
                res.addAll(collectIsNotUnknowns(members.get(idx), aggressive));
            }
        }
        return res;
    }

    /**
     * Takes a previously identified match (STRICT negation) to turn it into a non-STRICT negation if the collected IS-UNKNOWNs are applicable.
     * 
     * @param candidate strictly negated match
     * @param parentIsUnknowns IS-UNKNOWNs inherited from outer ORs
     * @param localIsUnknowns IS-UNKNOWNs sitting side-by-side within an enclosing OR
     * @param isNotUnknowns names of arguments we know from outer conditions that they are not unknown
     */
    private PlMatchExpression removeRedundantStrictIfApplicable(PlMatchExpression candidate, List<String> parentIsUnknowns, List<String> localIsUnknowns,
            List<String> isNotUnknowns) {
        PlMatchExpression res = candidate;
        String argName = argNameOf(candidate);
        String referencedArgName = isReferenceMatch(candidate) ? referencedArgNameOf(candidate) : null;
        if ((isNotUnknowns.contains(argName) || parentIsUnknowns.contains(argName) || localIsUnknowns.contains(argName)) && (referencedArgName == null
                || isNotUnknowns.contains(referencedArgName) || parentIsUnknowns.contains(referencedArgName) || localIsUnknowns.contains(referencedArgName))) {
            res = new PlMatchExpression(candidate.argName(), removeStrictness(candidate.operator()), candidate.operands(), candidate.comments(), null);
        }
        return res;
    }

    /**
     * Takes a previously identified negated match (STRICT negation of a wrapped match) to turn it into a non-STRICT negation if the collected IS-UNKNOWNs are
     * applicable.
     * 
     * @param candidate strict negation of a match
     * @param parentIsUnknowns IS-UNKNOWNs inherited from outer ORs
     * @param localIsUnknowns IS-UNKNOWNs sitting side-by-side within an enclosing OR
     * @param isNotUnknowns names of arguments we know from outer conditions that they are not unknown
     */
    private PlNegationExpression removeRedundantStrictIfApplicable(PlNegationExpression candidate, List<String> parentIsUnknowns, List<String> localIsUnknowns,
            List<String> isNotUnknowns) {
        PlNegationExpression res = candidate;
        String argName = argNameOf(candidate.delegate());
        String referencedArgName = isReferenceMatch(candidate.delegate()) ? referencedArgNameOf(candidate.delegate()) : null;
        if ((isNotUnknowns.contains(argName) || parentIsUnknowns.contains(argName) || localIsUnknowns.contains(argName)) && (referencedArgName == null
                || isNotUnknowns.contains(referencedArgName) || parentIsUnknowns.contains(referencedArgName) || localIsUnknowns.contains(referencedArgName))) {
            res = new PlNegationExpression(candidate.delegate(), false, candidate.comments(), null);
        }
        return res;
    }

    /**
     * @param operator
     * @return corresponding non-STRICT operator for the given STRICT operator
     */
    private PlMatchOperator removeStrictness(PlMatchOperator operator) {
        switch (operator) {
        case STRICT_NOT_ANY_OF:
            return PlMatchOperator.NOT_ANY_OF;
        case STRICT_NOT_BETWEEN:
            return PlMatchOperator.NOT_BETWEEN;
        case STRICT_NOT_CONTAINS:
            return PlMatchOperator.NOT_CONTAINS;
        case STRICT_NOT_CONTAINS_ANY_OF:
            return PlMatchOperator.NOT_CONTAINS_ANY_OF;
        case STRICT_NOT_EQUALS:
            return PlMatchOperator.NOT_EQUALS;
        // $CASES-OMITTED$
        default:
            throw new IllegalStateException("Unexpected operator: " + operator);

        }
    }

    /**
     * Applies a couple of policies to re-combine the higher language features like less-then-or-equals or BETWEEN
     * 
     * @param rootExpression
     * @return root or replacement
     */
    private PlExpression<?> recreateCombinedOperators(PlExpression<?> rootExpression) {

        boolean modifiedInRun = false;

        do {
            modifiedInRun = false;
            if (rootExpression instanceof PlCombinedExpression cmb) {
                PlExpression<?> rootUpd = recreateCombinedOperatorsInCombinedExpression(cmb);
                modifiedInRun = (rootUpd != rootExpression);
                rootExpression = rootUpd;
            }
        } while (modifiedInRun);
        return rootExpression;
    }

    /**
     * Runs the policy check recursively (rebuild bottom-up)
     * 
     * @param candidate
     * @return candidate or replacement
     */
    private PlExpression<?> recreateCombinedOperatorsInCombinedExpression(PlCombinedExpression candidate) {

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("recreateCombinedOperatorsInCombinedExpression BEFORE: {}", candidate);
        }

        boolean modified = false;
        PlExpression<?> res = candidate;
        List<PlExpression<?>> members = new ArrayList<>(candidate.childExpressions());
        List<PlExpression<?>> consumedMembers = new ArrayList<>();
        for (int idx = 0; idx < members.size(); idx++) {
            modified = recreateCombinedOperatorsInCombinedExpression(candidate.combiType(), members, idx, consumedMembers) || modified;
        }
        if (modified) {
            res = recreateCombinedExpression(candidate.combiType(), members, consumedMembers);
        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("recreateCombinedOperatorsInCombinedExpression AFTER: {}{}", (modified ? "*" : " "), res);
        }

        return res;
    }

    /**
     * Runs the check for a single member (one of potentially two members to be combined)
     * 
     * @param parentType
     * @param members
     * @param idx
     * @param consumedMembers here we collect members which got obsolete due to combination (for final removal)
     * @return true if the members were modified
     */
    private boolean recreateCombinedOperatorsInCombinedExpression(CombinedExpressionType parentType, List<PlExpression<?>> members, int idx,
            List<PlExpression<?>> consumedMembers) {
        boolean modified = false;
        PlExpression<?> member = members.get(idx);
        if (member instanceof PlCombinedExpression cmb) {
            PlExpression<?> memberUpd = recreateCombinedOperatorsInCombinedExpression(cmb);
            if (memberUpd != member) {
                modified = true;
                members.set(idx, memberUpd);
            }
        }
        else if (parentType == CombinedExpressionType.AND) {
            for (OperatorCombinationPolicy policy : OperatorCombinationPolicy.values()) {
                modified = policy.applyInAndParent(members, idx, consumedMembers) || modified;
            }
        }
        else {
            for (OperatorCombinationPolicy policy : OperatorCombinationPolicy.values()) {
                modified = policy.applyInOrParent(members, idx, consumedMembers) || modified;
            }
        }
        return modified;
    }

    /**
     * Applies a couple of policies to re-combine the higher language features related to ANY-OF
     * 
     * @param rootExpression
     * @return root or replacement
     */
    private PlExpression<?> recreateAnyOfCombinations(PlExpression<?> rootExpression) {
        if (rootExpression instanceof PlCombinedExpression cmb) {
            PlExpression<?> rootUpd = recreateAnyOfCombinationsInCombinedExpression(cmb);
            rootExpression = rootUpd;
        }
        return rootExpression;
    }

    /**
     * Runs the recreation of (NOT) (CONTAINS) ANY OFs on an OR resp. AND
     * 
     * @param candidate
     * @return replacement or the candidate if there was no change
     */
    private PlExpression<?> recreateAnyOfCombinationsInCombinedExpression(PlCombinedExpression candidate) {

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("recreateAnyOfCombinationsInCombinedExpression BEFORE: {}", candidate);
        }

        boolean modified = false;
        PlExpression<?> res = candidate;
        List<PlExpression<?>> members = new ArrayList<>(candidate.childExpressions());
        List<PlExpression<?>> consumedMembers = new ArrayList<>();
        for (int idx = 0; idx < members.size(); idx++) {
            modified = recreateAnyOfCombinationsInCombinedExpression(candidate.combiType(), members, idx, consumedMembers) || modified;
        }
        if (modified) {
            res = recreateCombinedExpression(candidate.combiType(), members, consumedMembers);
        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("recreateAnyOfCombinationsInCombinedExpression AFTER: {}{}", (modified ? "*" : " "), res);
        }

        return res;

    }

    /**
     * Runs the check for a single member against the remainder
     * 
     * @param parentType
     * @param members
     * @param idx
     * @param consumedMembers here we collect members which got obsolete due to combination (for final removal)
     * @return true if the members were modified
     */
    private boolean recreateAnyOfCombinationsInCombinedExpression(CombinedExpressionType parentType, List<PlExpression<?>> members, int idx,
            List<PlExpression<?>> consumedMembers) {
        boolean modified = false;
        PlExpression<?> member = members.get(idx);
        if (member instanceof PlCombinedExpression cmb) {
            PlExpression<?> memberUpd = recreateAnyOfCombinationsInCombinedExpression(cmb);
            if (memberUpd != member) {
                modified = true;
                members.set(idx, memberUpd);
            }
        }
        else if (parentType == CombinedExpressionType.AND) {
            for (AnyOfCombinationPolicy policy : AnyOfCombinationPolicy.values()) {
                modified = policy.applyInAndParent(members, idx, consumedMembers) || modified;
            }
        }
        else {
            for (AnyOfCombinationPolicy policy : AnyOfCombinationPolicy.values()) {
                modified = policy.applyInOrParent(members, idx, consumedMembers) || modified;
            }
        }
        return modified;
    }

    /**
     * Polishes the final expression by sorting the members on each levels with {@link #MEMBER_PRETTY_ORDER_COMPARATOR}
     * 
     * @param expression
     * @return updated expression or expression if not changed
     */
    private PlExpression<?> applyPrettyMemberOrder(PlExpression<?> expression) {
        PlExpression<?> res = expression;
        if (expression instanceof PlCombinedExpression cmb) {

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("applyPrettyMemberOrder BEFORE: {}", cmb);
            }

            List<PlExpression<?>> members = new ArrayList<>(expression.childExpressions());
            for (int idx = 0; idx < members.size(); idx++) {
                members.set(idx, applyPrettyMemberOrder(members.get(idx)));
            }
            Collections.sort(members, MEMBER_PRETTY_ORDER_COMPARATOR);
            if (!members.equals(expression.childExpressions())) {
                res = new PlCombinedExpression(cmb.combiType(), members, cmb.comments());
            }

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("applyPrettyMemberOrder AFTER: {}{}", ((res != expression) ? "*" : " "), res);
            }

        }
        return res;
    }

    /**
     * Either re-creates the AND resp. OR
     * 
     * @param combiType
     * @param members
     * @param skipMembers members to be removed from the members list if present
     * @return re-created combined expression or the only member left
     */
    private PlExpression<?> recreateCombinedExpression(CombinedExpressionType combiType, List<PlExpression<?>> members, List<PlExpression<?>> skipMembers) {
        for (int idx = members.size() - 1; idx > -1; idx--) {
            if (skipMembers.contains(members.get(idx))) {
                members.remove(idx);
            }
        }
        if (members.size() == 1) {
            return members.get(0);
        }
        else {
            return new PlCombinedExpression(combiType, members, null);
        }
    }

    /**
     * @param match
     * @return argument name of a match
     */
    private String argNameOf(PlExpression<?> match) {
        return ((PlMatchExpression) match).argName();
    }

    /**
     * @param match
     * @return referenced argument name for a reference match
     */
    private String referencedArgNameOf(PlExpression<?> match) {
        return ((PlMatchExpression) match).operands().get(0).value();
    }

    /**
     * @param candidate
     * @return true if the candidate is a reference match with a single argument
     */
    private boolean isReferenceMatch(PlExpression<?> candidate) {
        return (candidate instanceof PlMatchExpression match && match.operands().size() == 1 && match.operands().get(0).isReference());
    }

    /**
     * @param candidate
     * @return true if the candidate is an IS-UNKNOWN check
     */
    private boolean isUnknown(PlExpression<?> candidate) {
        return (candidate instanceof PlMatchExpression match && match.operator() == PlMatchOperator.IS_UNKNOWN);
    }

    /**
     * @param candidate
     * @return true if this is strict a negation expression with a real match as its delegate
     */
    private boolean isStrictNegatedMatch(PlExpression<?> candidate) {
        return (candidate instanceof PlNegationExpression neg && neg.isStrict() && neg.delegate() instanceof PlMatchExpression match
                && !match.operands().isEmpty());
    }

    /**
     * @param candidate
     * @return true if this is non-strict a negation expression with a real match as its delegate
     */
    private boolean isNonStrictNegatedMatch(PlExpression<?> candidate) {
        return (candidate instanceof PlNegationExpression neg && !neg.isStrict() && neg.delegate() instanceof PlMatchExpression match
                && !match.operands().isEmpty());
    }

    /**
     * @param candidate
     * @return true if this is strict intra-negated match expression (e.g., <code>STRICT a != b</code>)
     */
    private boolean isStrictIntraNegatedMatch(PlExpression<?> candidate) {
        return (candidate instanceof PlMatchExpression match && match.operator().isStrictMatchNegation());
    }

    /**
     * @param candidate
     * @return true if this is non-strict intra-negated match expression (e.g., <code>a != b</code>)
     */
    private boolean isNonStrictIntraNegatedMatch(PlExpression<?> candidate) {
        return (candidate instanceof PlMatchExpression match && match.operator().isMatchNegation() && !match.operator().isStrictMatchNegation());
    }

    /**
     * Checks the given expression if we can assume any IS-NOT-UNKNOWNs from it
     * <p>
     * Example: <code>a = 1</code> <i>implies</i> that <code>a IS NOT UNKNOWN</code>
     * 
     * @param candidate
     * @param aggressive if true we consider STRICT neighbors as non-unknown indicators (can impede grouping)
     * @return list with arguments we can assume that they are NOT UNKNOWN
     */
    private List<String> collectIsNotUnknowns(PlExpression<?> candidate, boolean aggressive) {
        List<String> res = Collections.emptyList();
        if (candidate instanceof PlMatchExpression match && matchImpliesArgumentIsNotUnknown(match, aggressive)) {
            if (isReferenceMatch(candidate)) {
                res = Arrays.asList(argNameOf(candidate), referencedArgNameOf(candidate));
            }
            else {
                res = Arrays.asList(argNameOf(candidate));
            }
        }
        else if (candidate instanceof PlCombinedExpression cmb && cmb.combiType() == CombinedExpressionType.AND) {
            res = new ArrayList<>();
            for (PlExpression<?> member : cmb.childExpressions()) {
                res.addAll(collectIsNotUnknowns(member, aggressive));
            }
        }
        else if (candidate instanceof PlCombinedExpression cmb && cmb.combiType() == CombinedExpressionType.OR) {
            res = collectIsNotUnknownsInOrParent(cmb, aggressive);
        }
        return res;
    }

    /**
     * Performs the collection within an OR. The result is the <i>overlap</i> of all member results.
     * 
     * @param candidate
     * @param aggressive if true we consider STRICT neighbors as non-unknown indicators (can impede grouping)
     * @return list with arguments we can assume that they are NOT UNKNOWN
     */
    private List<String> collectIsNotUnknownsInOrParent(PlCombinedExpression candidate, boolean aggressive) {
        List<String> res = new ArrayList<>();
        List<PlExpression<?>> members = candidate.childExpressions();
        for (int idx = 0; idx < members.size(); idx++) {
            List<String> memberIsNotUnknowns = collectIsNotUnknowns(members.get(idx), aggressive);
            if (idx == 0) {
                res.addAll(memberIsNotUnknowns);
            }
            else {
                for (int mergeIdx = res.size() - 1; mergeIdx > -1; mergeIdx--) {
                    if (!memberIsNotUnknowns.contains(res.get(mergeIdx))) {
                        res.remove(mergeIdx);
                    }
                }
            }
            if (res.isEmpty()) {
                return Collections.emptyList();
            }
        }
        return res;
    }

    /**
     * @param match
     * @param aggressive if true we consider STRICT neighbors as non-unknown indicators (can impede grouping)
     * @return true if the given match implies that the related argument cannot be unknown
     */
    private boolean matchImpliesArgumentIsNotUnknown(PlMatchExpression match, boolean aggressive) {
        PlMatchOperator operator = match.operator();
        return (operator == PlMatchOperator.IS_NOT_UNKNOWN || operator == PlMatchOperator.EQUALS || operator == PlMatchOperator.ANY_OF
                || operator == PlMatchOperator.CONTAINS || operator == PlMatchOperator.CONTAINS_ANY_OF || operator == PlMatchOperator.LESS_THAN
                || operator == PlMatchOperator.GREATER_THAN || (aggressive && operator.isStrictMatchNegation()));
    }

}

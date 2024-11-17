//@formatter:off
/*
 * PlNegationExpression
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

import static de.calamanari.adl.FormatUtils.appendNegationExpressionMultiLine;
import static de.calamanari.adl.FormatUtils.appendNegationExpressionSingleLine;
import static de.calamanari.adl.erl.PlComment.Position.AFTER_EXPRESSION;
import static de.calamanari.adl.erl.PlComment.Position.BEFORE_EXPRESSION;
import static de.calamanari.adl.erl.PlComment.Position.C1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import de.calamanari.adl.AudlangField;
import de.calamanari.adl.AudlangValidationException;
import de.calamanari.adl.CombinedExpressionType;
import de.calamanari.adl.FormatStyle;
import de.calamanari.adl.FormatUtils.FormatInfo;
import de.calamanari.adl.SpecialSetType;
import de.calamanari.adl.Visit;
import de.calamanari.adl.erl.PlMatchExpression.PlMatchOperator;

/**
 * {@link PlNegationExpression} represents a logical NOT on the Audlang presentation layer.
 * <p/>
 * See also <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#5-negation">§5</a> Audlang Spec
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public record PlNegationExpression(PlExpression<?> delegate, @JsonInclude(JsonInclude.Include.NON_DEFAULT) boolean isStrict,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) List<PlComment> comments, String inline) implements PlExpression<PlNegationExpression> {

    private static final Set<PlComment.Position> VALID_COMMENT_POSITIONS = Collections
            .unmodifiableSet(new LinkedHashSet<>(Arrays.asList(BEFORE_EXPRESSION, AFTER_EXPRESSION)));

    private static final Set<PlComment.Position> VALID_COMMENT_POSITIONS_STRICT = Collections
            .unmodifiableSet(new LinkedHashSet<>(Arrays.asList(BEFORE_EXPRESSION, C1, AFTER_EXPRESSION)));

    /**
     * @param delegate any Audlang expression to be negated
     * @param isStrict true if this NOT shall be strict
     * @param comments optional comments, may be null
     * @param inline this is an internal value computed by the constructor, no matter what you specify here, it will be ignored
     */
    public PlNegationExpression(@JsonDeserialize(using = PlExpressionDeserializer.class) PlExpression<?> delegate, boolean isStrict, List<PlComment> comments,
            @SuppressWarnings("java:S1172") String inline) {
        if (delegate == null) {
            throw new AudlangValidationException("The delegate of a negation must not be null.");
        }

        List<PlComment> tempComments = (comments == null ? new ArrayList<>() : new ArrayList<>(comments));
        if (!CommentUtils.verifyCommentsApplicable(tempComments, (isStrict ? VALID_COMMENT_POSITIONS_STRICT : VALID_COMMENT_POSITIONS))) {
            throw new AudlangValidationException(String.format("Invalid comment position (expected: %s), given: delegate=%s, isStrict=%s, comments=%s",
                    (isStrict ? VALID_COMMENT_POSITIONS_STRICT : VALID_COMMENT_POSITIONS), delegate, isStrict, tempComments));
        }

        this.delegate = moveIndistinguishableTrailingCommentsToDelegate(tempComments, delegate);
        this.isStrict = isStrict;
        this.comments = Collections.unmodifiableList(tempComments);
        this.inline = format(FormatStyle.INLINE);
    }

    private static PlExpression<?> moveIndistinguishableTrailingCommentsToDelegate(List<PlComment> comments, PlExpression<?> delegate) {

        if (!comments.isEmpty()) {

            List<PlComment> commentsBefore = comments.stream().filter(cmn -> cmn.position() == BEFORE_EXPRESSION).toList();
            List<PlComment> commentsAfter = comments.stream().filter(cmn -> cmn.position() == AFTER_EXPRESSION).toList();

            if (!commentsAfter.isEmpty()) {
                List<PlComment> joinedComments = new ArrayList<>();
                joinedComments.addAll(delegate.allDirectComments());
                joinedComments.addAll(commentsAfter);
                delegate = delegate.withComments(joinedComments);
                comments.clear();
                comments.addAll(commentsBefore);
            }
        }

        return delegate;

    }

    /**
     * @param delegate any Auslang expression to be negated
     * @param isStrict true if this NOT shall be strict
     * @param comments optional comments, may be null
     */
    public PlNegationExpression(PlExpression<?> delegate, boolean isStrict, List<PlComment> comments) {
        this(delegate, isStrict, comments, null);
    }

    @Override
    public List<PlExpression<?>> childExpressions() {
        return Collections.unmodifiableList(Arrays.asList(delegate));
    }

    @Override
    public void collectFieldsInternal(Map<String, AudlangField.Builder> fieldMap) {
        delegate.collectFieldsInternal(fieldMap);
    }

    @Override
    public void collectAllComments(List<PlComment> result) {
        result.addAll(comments);
        delegate.collectAllComments(result);
    }

    @Override
    public void accept(PlExpressionVisitor visitor) {
        visitor.visit(this, Visit.ENTER);
        delegate.accept(visitor);
        visitor.visit(this, Visit.EXIT);
    }

    @Override
    public List<PlComment> allDirectComments() {
        return comments;
    }

    @Override
    public PlNegationExpression stripComments() {
        PlExpression<?> updatedDelegate = delegate.stripComments();
        if (updatedDelegate == delegate && comments.isEmpty()) {
            return this;
        }
        else {
            return new PlNegationExpression(delegate.stripComments(), isStrict, null);
        }
    }

    @Override
    public PlNegationExpression withComments(List<PlComment> comments) {
        if ((comments == null && this.comments.isEmpty()) || this.comments.equals(comments)) {
            return this;
        }
        else {
            if (comments == null) {
                comments = Collections.emptyList();
            }
            List<PlComment> commentsOther = comments.stream().filter(Predicate.not(cmn -> cmn.position() == AFTER_EXPRESSION)).toList();
            List<PlComment> commentsAfter = comments.stream().filter(cmn -> cmn.position() == AFTER_EXPRESSION).toList();

            List<PlComment> joinedComments = new ArrayList<>();
            joinedComments.addAll(delegate.allDirectComments().stream().filter(Predicate.not(cmn -> cmn.position() == AFTER_EXPRESSION)).toList());
            joinedComments.addAll(commentsAfter);

            PlExpression<?> updDelegate = delegate.withComments(joinedComments);

            if (updDelegate.equals(delegate) && commentsOther.equals(this.comments)) {
                return this;
            }
            return new PlNegationExpression(delegate.withComments(joinedComments), isStrict, commentsOther);
        }
    }

    @Override
    public PlExpression<?> resolveHigherLanguageFeatures() {

        // @formatter:off
        if (isStrict 
                && ((delegate instanceof PlMatchExpression match && match.operator() == PlMatchOperator.IS_NOT_UNKNOWN)
                        || (delegate instanceof PlNegationExpression neg
                                && !neg.isStrict()
                                && neg.delegate() instanceof PlMatchExpression subMatch
                                && subMatch.operator() == PlMatchOperator.IS_UNKNOWN))) {
            // @formatter:off
            
            // special case §5.2 Audlang spec: 
            // STRICT NOT arg IS NOT UNKNOWN := NONE (exclusion of unknowns)
            return new PlSpecialSetExpression(SpecialSetType.NONE, null);
        }
        else if (delegate instanceof PlCurbExpression curb) {
            // apply the NOT to the bound condition (not to the resolved curb)
            return new PlCurbExpression(curb.curbDelegate(), curb.operator().negate(), curb.bound(), null).resolveHigherLanguageFeatures();
        }

        return resolveHigherLanguageFeaturesInternal();
    }

    /**
     * First resolves the higher language features on the delegate, evaluates the result and processes then type-specific
     * @return resolved expression without any higher language features
     */
    @SuppressWarnings("java:S6880")
    private PlExpression<?> resolveHigherLanguageFeaturesInternal() {
        
        // inner-outer resolution (apply the NOT to the already resolved delegate)
        PlExpression<?> resolvedDelegate = delegate.resolveHigherLanguageFeatures();

        // Not using switch here because of an OpenJDK bug in the switch-statement-implementation
        // that lead to JVM-crash that could only be fixed by reverting the code below to classic IF-ELSE 
        // Symptom: java.lang.VerifyError: Inconsistent stackmap frames at branch target
        // See https://bugs.openjdk.org/browse/JDK-8332934

        PlExpression<?> res = null;
        if (resolvedDelegate instanceof PlNegationExpression neg) {
            res = resolveHigherLanguageFeatures(neg);
        }
        else if (resolvedDelegate instanceof PlCombinedExpression cmb) {
            res = resolveHigherLanguageFeatures(cmb);
        }
        else if (resolvedDelegate instanceof PlMatchExpression match) {
            res = resolveHigherLanguageFeatures(match);
        }
        else if (resolvedDelegate instanceof PlSpecialSetExpression spc) {
            res = resolveHigherLanguageFeatures(spc);
        }
        else {
            throw new IllegalStateException("BUG: Unexpected expression type: " + this.getClass().getName());
        }
        return res == this ? this : res;
    }

    private PlExpression<?> resolveHigherLanguageFeatures(PlSpecialSetExpression spc) {
        if (spc.setType() == SpecialSetType.ALL) {
            return new PlSpecialSetExpression(SpecialSetType.NONE, null);
        }
        else {
            return new PlSpecialSetExpression(SpecialSetType.ALL, null);
        }
    }

    private PlExpression<?> resolveHigherLanguageFeatures(PlMatchExpression match) {

        // Important to understand: At this point the given match is already resolved, so many of the operators we don't need to care about

        switch (match.operator()) {
        case IS_UNKNOWN:
            return new PlMatchExpression(match.argName(), PlMatchOperator.IS_NOT_UNKNOWN, null).resolveHigherLanguageFeatures();
        case EQUALS:
            return new PlMatchExpression(match.argName(), isStrict ? PlMatchOperator.STRICT_NOT_EQUALS : PlMatchOperator.NOT_EQUALS, match.operands(), null)
                    .resolveHigherLanguageFeatures();
        case CONTAINS:
            return new PlMatchExpression(match.argName(), isStrict ? PlMatchOperator.STRICT_NOT_CONTAINS : PlMatchOperator.NOT_CONTAINS, match.operands(), null)
                    .resolveHigherLanguageFeatures();
        case LESS_THAN, GREATER_THAN:
            return handleNotLessThanGreaterThanCase(match);
        // $CASES-OMITTED$
        default:
            throw new IllegalStateException("BUG: At this point there should not be any match of this type: " + match);
        }
    }

    /**
     * Takes care of the less than and greater than which don't have intra-negations in the language, so we need to apply the rules here
     * 
     * @param match
     * @return resolved expression
     */
    private PlExpression<?> handleNotLessThanGreaterThanCase(PlMatchExpression match) {
        if (this.isStrict) {
            return new PlNegationExpression(match, true, null);
        }
        else {
            // A non strict NOT must consider the argument to be null, and in case of a reference match the other argument to be null

            List<PlExpression<?>> orMembers = new ArrayList<>();
            orMembers.add(new PlNegationExpression(match, true, null));
            orMembers.add(new PlMatchExpression(match.argName(), PlMatchOperator.IS_UNKNOWN, null));
            PlOperand operand = match.operands().get(0);
            if (operand.isReference()) {
                orMembers.add(new PlMatchExpression(operand.value(), PlMatchOperator.IS_UNKNOWN, null));
            }
            return new PlCombinedExpression(CombinedExpressionType.OR, orMembers, null);
        }
    }

    private PlExpression<?> resolveHigherLanguageFeatures(PlNegationExpression neg) {
        if (neg.delegate instanceof PlMatchExpression matchDelegate) {
            if (this.isStrict || matchDelegate.operator() == PlMatchOperator.IS_UNKNOWN) {
                // we know this is a match, where a strict not of results in the original match
                return matchDelegate;
            }
            else {
                // special case (§5 Audlang Spec): NOT STRICT NOT (arg=1) means: arg=1 OR arg IS UNKNOWN

                List<PlExpression<?>> orMembers = new ArrayList<>();
                orMembers.add(matchDelegate);
                orMembers.add(new PlMatchExpression(matchDelegate.argName(), PlMatchOperator.IS_UNKNOWN, null));

                // we know here that the given negation and thus the contained match is already resolved,
                // so it is safe to assume a single operand
                PlOperand operand = matchDelegate.operands().get(0);
                if (operand.isReference()) {
                    orMembers.add(new PlMatchExpression(operand.value(), PlMatchOperator.IS_UNKNOWN, null));
                }
                return new PlCombinedExpression(CombinedExpressionType.OR, orMembers, null);
            }
        }
        else {
            throw new IllegalStateException("BUG: At this point there should not be any remaining negations on aggregations anymore, given: " + neg);
        }
    }

    private PlExpression<?> resolveHigherLanguageFeatures(PlCombinedExpression cmb) {
        // @formatter:off
        return new PlCombinedExpression(cmb.combiType().switchType(), 
                                        cmb.members().stream().map(m -> new PlNegationExpression(m, isStrict, null))
                                            .collect(Collectors.toCollection(ArrayList<PlExpression<?>>::new)), 
                                        null).resolveHigherLanguageFeatures();
        // @formatter:on
    }

    @Override
    public void appendMultiLine(StringBuilder sb, FormatStyle style, int level) {
        appendNegationExpressionMultiLine(sb, delegate, isStrict, comments, new FormatInfo(style, level));
    }

    @Override
    public void appendSingleLine(StringBuilder sb, FormatStyle style, int level) {
        appendNegationExpressionSingleLine(sb, delegate, isStrict, comments, new FormatInfo(style, level));
    }

    @Override
    public String toString() {
        return inline;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PlNegationExpression pln && pln.inline.equals(this.inline);
    }

    @Override
    public int hashCode() {
        return inline.hashCode();
    }

}

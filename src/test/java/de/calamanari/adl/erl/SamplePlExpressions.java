//@formatter:off
/*
 * SamplePlExpressions
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import de.calamanari.adl.CombinedExpressionType;
import de.calamanari.adl.SpecialSetType;
import de.calamanari.adl.erl.PlComment.Position;
import de.calamanari.adl.erl.PlCurbExpression.PlCurbOperator;

import static de.calamanari.adl.erl.PlComment.Position.AFTER_EXPRESSION;
import static de.calamanari.adl.erl.PlComment.Position.AFTER_OPERAND;
import static de.calamanari.adl.erl.PlComment.Position.BEFORE_EXPRESSION;
import static de.calamanari.adl.erl.PlComment.Position.BEFORE_OPERAND;
import static de.calamanari.adl.erl.PlComment.Position.C1;
import static de.calamanari.adl.erl.PlComment.Position.C2;
import static de.calamanari.adl.erl.PlComment.Position.C3;
import static de.calamanari.adl.erl.PlComment.Position.C4;
import static de.calamanari.adl.erl.PlComment.Position.C5;
import static de.calamanari.adl.erl.PlComment.Position.C6;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class SamplePlExpressions {

    public static final List<PlComment> COMMENT_BEFORE_EXPR = Arrays.asList(new PlComment("/* comment before expression */", BEFORE_EXPRESSION));
    public static final List<PlComment> SHORT_COMMENT_BEFORE_EXPR = Arrays.asList(new PlComment("/* comment BE */", BEFORE_EXPRESSION));
    public static final List<PlComment> EMPTY_COMMENT_BEFORE_EXPR = Arrays.asList(new PlComment("/**/", BEFORE_EXPRESSION));
    public static final List<PlComment> TWO_COMMENTS_BEFORE_EXPR = Arrays.asList(new PlComment("/* comment 1 before expression */", BEFORE_EXPRESSION),
            new PlComment("/* comment 2 before expression */", Position.BEFORE_EXPRESSION));
    public static final List<PlComment> MULTI_LINE_COMMENT_BEFORE_EXPR = Arrays
            .asList(new PlComment("/* comment VERY LONG VERY LONG VERY LONG VERY LONG before expression */", BEFORE_EXPRESSION));
    public static final List<PlComment> TWO_MULTI_LINE_COMMENTS_BEFORE_EXPR = Arrays.asList(
            new PlComment("/* comment 1 VERY LONG VERY LONG VERY LONG VERY LONG before expression */", BEFORE_EXPRESSION),
            new PlComment("/* comment 2 VERY LONG VERY LONG VERY LONG VERY LONG before expression */", Position.BEFORE_EXPRESSION));

    public static final List<PlComment> COMMENT_AFTER_EXPR = Arrays.asList(new PlComment("/* comment after expression */", AFTER_EXPRESSION));
    public static final List<PlComment> SHORT_COMMENT_AFTER_EXPR = Arrays.asList(new PlComment("/* comment AE */", AFTER_EXPRESSION));
    public static final List<PlComment> TWO_COMMENTS_AFTER_EXPR = Arrays.asList(new PlComment("/* comment 1 after expression */", AFTER_EXPRESSION),
            new PlComment("/* comment 2 after expression */", AFTER_EXPRESSION));
    public static final List<PlComment> TWO_COMMENTS_BEFORE_AND_AFTER_EXPR = Arrays
            .asList(new PlComment("/* comment 1 before expression */", BEFORE_EXPRESSION), new PlComment("/* comment 2 after expression */", AFTER_EXPRESSION));
    public static final List<PlComment> MULTI_LINE_COMMENT_AFTER_EXPR = Arrays
            .asList(new PlComment("/* comment VERY LONG VERY LONG VERY LONG VERY LONG after expression */", AFTER_EXPRESSION));
    public static final List<PlComment> TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION = Arrays.asList(
            new PlComment("/* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */", AFTER_EXPRESSION),
            new PlComment("/* comment 2 after expression */", Position.AFTER_EXPRESSION));

    public static final List<PlComment> TWO_MULTI_LINE_COMMENTS_BEFORE_AND_AFTER_EXPR = Arrays.asList(
            new PlComment("/* comment 1 VERY LONG VERY LONG VERY LONG VERY LONG before expression */", BEFORE_EXPRESSION),
            new PlComment("/* comment 2 VERY LONG VERY LONG VERY LONG VERY LONG after expression */", AFTER_EXPRESSION));

    public static final List<PlComment> COMMENT_BEFORE_OPERAND = Arrays.asList(new PlComment("/* comment before operand */", BEFORE_OPERAND));
    public static final List<PlComment> EMPTY_COMMENT_BEFORE_OPERAND = Arrays.asList(new PlComment("/**/", BEFORE_OPERAND));
    public static final List<PlComment> TWO_COMMENTS_BEFORE_OPERAND = Arrays.asList(new PlComment("/* comment 1 before operand */", BEFORE_OPERAND),
            new PlComment("/* comment 2 before operand */", Position.BEFORE_OPERAND));
    public static final List<PlComment> MULTI_LINE_COMMENT_BEFORE_OPERAND = Arrays
            .asList(new PlComment("/* comment VERY LONG VERY LONG VERY LONG VERY LONG before operand */", BEFORE_EXPRESSION));
    public static final List<PlComment> TWO_MULTI_LINE_COMMENTS_BEFORE_OPERAND = Arrays.asList(
            new PlComment("/* comment 1 VERY LONG VERY LONG VERY LONG VERY LONG before operand */", BEFORE_EXPRESSION),
            new PlComment("/* comment 2 VERY LONG VERY LONG VERY LONG VERY LONG before operand */", Position.BEFORE_EXPRESSION));

    public static final List<PlComment> COMMENT_AFTER_OPERAND = Arrays.asList(new PlComment("/* comment after operand */", AFTER_OPERAND));
    public static final List<PlComment> TWO_COMMENTS_AFTER_OPERAND = Arrays.asList(new PlComment("/* comment 1 after operand */", AFTER_OPERAND),
            new PlComment("/* comment 2 after operand */", AFTER_OPERAND));
    public static final List<PlComment> TWO_COMMENTS_BEFORE_AND_AFTER_OPERAND = Arrays.asList(new PlComment("/* comment 1 before operand */", BEFORE_OPERAND),
            new PlComment("/* comment 2 after operand */", AFTER_OPERAND));
    public static final List<PlComment> MULTI_LINE_COMMENT_AFTER_OPERAND = Arrays
            .asList(new PlComment("/* comment VERY LONG VERY LONG VERY LONG VERY LONG after operand */", AFTER_EXPRESSION));

    public static final List<PlComment> TWO_COMMENTS_ONE_MULTI_LINE_AFTER_OPERAND = Arrays.asList(
            new PlComment("/* comment 1 VERY LONG VERY LONG VERY LONG VERY LONG after operand */", AFTER_OPERAND),
            new PlComment("/* comment 2 after operand */", Position.AFTER_OPERAND));

    public static final List<PlComment> TWO_MULTI_LINE_COMMENTS_BEFORE_AND_AFTER_OPERAND = Arrays.asList(
            new PlComment("/* comment 1 VERY LONG VERY LONG VERY LONG VERY LONG after operand */", BEFORE_EXPRESSION),
            new PlComment("/* comment 2 VERY LONG VERY LONG VERY LONG VERY LONG after operand */", AFTER_EXPRESSION));

    public static final PlOperand VALUE_A = new PlOperand("A", false, null);
    public static final PlOperand VALUE_B = new PlOperand("B", false, null);
    public static final PlOperand VALUE_C = new PlOperand("C", false, null);
    public static final PlOperand VALUE_D = new PlOperand("D", false, null);
    public static final PlOperand VALUE_WITH_SPACE = new PlOperand("Foo Bar", false, null);
    public static final PlOperand VALUE_WITH_TAB = new PlOperand("Top\tTopic", false, null);

    public static final PlOperand REF_R1 = new PlOperand("R1", true, null);
    public static final PlOperand REF_R2 = new PlOperand("R2", true, null);
    public static final PlOperand REF_R3 = new PlOperand("R3", true, null);
    public static final PlOperand REF_R4 = new PlOperand("R4", true, null);
    public static final PlOperand REF_WITH_SPACE = new PlOperand("Ref 4", true, null);
    public static final PlOperand REF_WITH_TAB = new PlOperand("Ref\t5", true, null);

    public static final List<PlOperand> VALUE_LIST_SINGLE = Arrays.asList(VALUE_A);
    public static final List<PlOperand> VALUE_LIST_SHORT = Arrays.asList(VALUE_A, VALUE_B, VALUE_C);
    public static final List<PlOperand> VALUE_LIST_LONG = Arrays.asList(VALUE_A, VALUE_B, VALUE_C, VALUE_D, VALUE_WITH_SPACE, VALUE_WITH_TAB);

    public static final List<PlOperand> REF_LIST_SINGLE = Arrays.asList(REF_R1);
    public static final List<PlOperand> REF_LIST_SHORT = Arrays.asList(REF_R1, REF_R2, REF_R3);
    public static final List<PlOperand> REF_LIST_LONG = Arrays.asList(REF_R1, REF_R2, REF_R3, REF_R4, REF_WITH_SPACE, REF_WITH_TAB);

    public static final PlSpecialSetExpression ALL = new PlSpecialSetExpression(SpecialSetType.ALL, null);
    public static final PlSpecialSetExpression NONE = new PlSpecialSetExpression(SpecialSetType.NONE, null);

    public static final List<PlComment> SINGLE_COMMENT_C1 = comments(C1, "/* comment C1 */");
    public static final List<PlComment> SINGLE_COMMENT_C2 = comments(C2, "/* comment C2 */");
    public static final List<PlComment> SINGLE_COMMENT_C3 = comments(C3, "/* comment C3 */");
    public static final List<PlComment> SINGLE_COMMENT_C4 = comments(C4, "/* comment C4 */");
    public static final List<PlComment> SINGLE_COMMENT_C5 = comments(C5, "/* comment C5 */");
    public static final List<PlComment> SINGLE_COMMENT_C6 = comments(C6, "/* comment C6 */");

    public static final PlCombinedExpression and(PlExpression<?>... members) {
        return new PlCombinedExpression(CombinedExpressionType.AND, Arrays.asList(members), null);
    }

    public static final PlCombinedExpression or(PlExpression<?>... members) {
        return new PlCombinedExpression(CombinedExpressionType.OR, Arrays.asList(members), null);
    }

    public static final PlNegationExpression not(PlExpression<?> delegate) {
        return new PlNegationExpression(delegate, false, null);
    }

    public static final PlNegationExpression strictNot(PlExpression<?> delegate) {
        return new PlNegationExpression(delegate, true, null);
    }

    public static final PlMatchExpression match(String argName, PlMatchOperator operator, List<PlOperand> operands) {
        return new PlMatchExpression(argName, operator, operands, null);
    }

    public static final PlMatchExpression match(String argName, PlMatchOperator operator, PlOperand operand) {
        return new PlMatchExpression(argName, operator, operand, null);
    }

    public static final PlMatchExpression match(String argName, PlMatchOperator op, String value) {
        return new PlMatchExpression(argName, op, Arrays.asList(new PlOperand(value, false, null)), null);
    }

    public static final PlCurbExpression curb(PlCurbOperator operator, int bound, PlExpression<?>... members) {
        return new PlCurbExpression(or(members), operator, bound, null);
    }

    public static final PlComment comment(Position position, String comment) {
        return new PlComment(comment, position);
    }

    public static final List<PlComment> comments(Position position, String... comments) {
        if (comments == null || comments.length == 0) {
            return Collections.emptyList();
        }
        return Stream.of(comments).map(c -> new PlComment(c, position)).toList();
    }

    public static final List<PlComment> comments(PlComment... comments) {
        if (comments == null || comments.length == 0) {
            return Collections.emptyList();
        }
        return Arrays.asList(comments);
    }

    @SafeVarargs
    public static final List<PlComment> comments(List<PlComment>... comments) {
        if (comments == null || comments.length == 0) {
            return Collections.emptyList();
        }
        return Stream.of(comments).flatMap(List::stream).toList();
    }

    public static final PlOperand vop(String value) {
        return new PlOperand(value, false, null);
    }

    public static final PlOperand rop(String reference) {
        return new PlOperand(reference, true, null);
    }

    public static final List<PlOperand> lop(PlOperand... operands) {
        return Arrays.asList(operands);
    }

    private SamplePlExpressions() {
        // util
    }

}

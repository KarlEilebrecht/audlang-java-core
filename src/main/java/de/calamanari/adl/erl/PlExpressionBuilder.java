//@formatter:off
/*
 * PlExpressionBuilder
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

import static de.calamanari.adl.erl.PlComment.Position.AFTER_EXPRESSION;
import static de.calamanari.adl.erl.PlComment.Position.BEFORE_EXPRESSION;
import static de.calamanari.adl.erl.PlComment.Position.BEFORE_OPERAND;
import static de.calamanari.adl.erl.PlComment.Position.C1;
import static de.calamanari.adl.erl.PlComment.Position.C2;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.calamanari.adl.AudlangMessage;
import de.calamanari.adl.CombinedExpressionType;
import de.calamanari.adl.CommonErrors;
import de.calamanari.adl.SpecialSetType;
import de.calamanari.adl.antlr.AudlangBaseListener;
import de.calamanari.adl.antlr.AudlangLexer;
import de.calamanari.adl.antlr.AudlangParser;
import de.calamanari.adl.antlr.AudlangParser.AllExpressionContext;
import de.calamanari.adl.antlr.AudlangParser.AndExpressionContext;
import de.calamanari.adl.antlr.AudlangParser.ArgNameContext;
import de.calamanari.adl.antlr.AudlangParser.ArgRefContext;
import de.calamanari.adl.antlr.AudlangParser.ArgValueContext;
import de.calamanari.adl.antlr.AudlangParser.BracedExpressionContext;
import de.calamanari.adl.antlr.AudlangParser.CmpAnyOfContext;
import de.calamanari.adl.antlr.AudlangParser.CmpBetweenContext;
import de.calamanari.adl.antlr.AudlangParser.CmpContainsAnyOfContext;
import de.calamanari.adl.antlr.AudlangParser.CmpContainsContext;
import de.calamanari.adl.antlr.AudlangParser.CmpEqualsContext;
import de.calamanari.adl.antlr.AudlangParser.CmpExpressionContext;
import de.calamanari.adl.antlr.AudlangParser.CmpGreaterThanContext;
import de.calamanari.adl.antlr.AudlangParser.CmpGreaterThanOrEqualsContext;
import de.calamanari.adl.antlr.AudlangParser.CmpInnerNotContext;
import de.calamanari.adl.antlr.AudlangParser.CmpIsNotUnknownContext;
import de.calamanari.adl.antlr.AudlangParser.CmpIsUnknownContext;
import de.calamanari.adl.antlr.AudlangParser.CmpLessThanContext;
import de.calamanari.adl.antlr.AudlangParser.CmpLessThanOrEqualsContext;
import de.calamanari.adl.antlr.AudlangParser.CmpNotEqualsContext;
import de.calamanari.adl.antlr.AudlangParser.CmpStrictInnerNotContext;
import de.calamanari.adl.antlr.AudlangParser.CmpStrictNotEqualsContext;
import de.calamanari.adl.antlr.AudlangParser.CommentContext;
import de.calamanari.adl.antlr.AudlangParser.CurbBoundContext;
import de.calamanari.adl.antlr.AudlangParser.CurbEqualsContext;
import de.calamanari.adl.antlr.AudlangParser.CurbExpressionContext;
import de.calamanari.adl.antlr.AudlangParser.CurbGreaterThanContext;
import de.calamanari.adl.antlr.AudlangParser.CurbGreaterThanOrEqualsContext;
import de.calamanari.adl.antlr.AudlangParser.CurbLessThanContext;
import de.calamanari.adl.antlr.AudlangParser.CurbLessThanOrEqualsContext;
import de.calamanari.adl.antlr.AudlangParser.CurbNotEqualsContext;
import de.calamanari.adl.antlr.AudlangParser.NoneExpressionContext;
import de.calamanari.adl.antlr.AudlangParser.NotExpressionContext;
import de.calamanari.adl.antlr.AudlangParser.OrExpressionContext;
import de.calamanari.adl.antlr.AudlangParser.QueryContext;
import de.calamanari.adl.antlr.AudlangParser.SnippetListItemContext;
import de.calamanari.adl.antlr.AudlangParser.SpaceAfterAnyContext;
import de.calamanari.adl.antlr.AudlangParser.SpaceAfterArgNameContext;
import de.calamanari.adl.antlr.AudlangParser.SpaceAfterBetweenContext;
import de.calamanari.adl.antlr.AudlangParser.SpaceAfterCombinerContext;
import de.calamanari.adl.antlr.AudlangParser.SpaceAfterContainsContext;
import de.calamanari.adl.antlr.AudlangParser.SpaceAfterCurbContext;
import de.calamanari.adl.antlr.AudlangParser.SpaceAfterCurbedOrContext;
import de.calamanari.adl.antlr.AudlangParser.SpaceAfterExpressionContext;
import de.calamanari.adl.antlr.AudlangParser.SpaceAfterIsContext;
import de.calamanari.adl.antlr.AudlangParser.SpaceAfterListItemContext;
import de.calamanari.adl.antlr.AudlangParser.SpaceAfterNotContext;
import de.calamanari.adl.antlr.AudlangParser.SpaceAfterOfContext;
import de.calamanari.adl.antlr.AudlangParser.SpaceAfterOperatorContext;
import de.calamanari.adl.antlr.AudlangParser.SpaceAfterStrictContext;
import de.calamanari.adl.antlr.AudlangParser.SpaceBeforeCombinerContext;
import de.calamanari.adl.antlr.AudlangParser.SpaceBeforeExpressionContext;
import de.calamanari.adl.antlr.AudlangParser.SpaceBeforeListItemContext;
import de.calamanari.adl.antlr.AudlangParser.StrictNotExpressionContext;
import de.calamanari.adl.antlr.AudlangParser.ValueListItemContext;
import de.calamanari.adl.antlr.AudlangParser.ValueOrRefListItemContext;
import de.calamanari.adl.erl.CommentUtils.PhysicalCommentPosition;
import de.calamanari.adl.erl.PlComment.Position;
import de.calamanari.adl.erl.PlCurbExpression.PlCurbOperator;
import de.calamanari.adl.util.AdlTextUtils;

/**
 * The {@link PlExpressionBuilder} is the concrete ANTLR-listener implementation for parsing textual representations of Audlang expressions. It supports the
 * full language including comments.
 * <p>
 * See also: <a href=
 * "https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#audience-definition-language-specification">Audience
 * Definition Language Specification</a>
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class PlExpressionBuilder extends AudlangBaseListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlExpressionBuilder.class);

    /**
     * the result object which will be filled by the this builder
     */
    private AudlangParseResult parseResult = new AudlangParseResult();

    /**
     * root data collector (starting level)
     */
    protected DataCollector rootCollector = null;

    /**
     * the collector stack allows us to collect data independently from the position in the tree, it avoids confusing state mix while parsing
     */
    protected final Deque<DataCollector> stack = new ArrayDeque<>();

    /**
     * Apart from unexpected runtime exceptions this listener catches
     */
    private final ANTLRErrorListener errorListener = new ProtocolErrorListener();

    /**
     * Names of the rules (order corresponding to parser (for rule name resolution if required, debugging)
     */
    protected String[] ruleNames;

    /**
     * Names of the tokens in order, this is required for detecting comment positions
     */
    protected String[] tokenNames;

    /**
     * Then currently parsed input string (debugging)
     */
    protected String source;

    /**
     * This central processing method takes a string to parse it. The method wraps the boilerplate code to setup and trigger the ANTLR-parser.
     * 
     * @param source expression string to be parsed
     * @return result either with an expression or an error description
     */
    public static AudlangParseResult stringToExpression(String source) {

        AudlangParseResult res = new AudlangParseResult();
        res.setSource(source);

        if (source == null || source.isBlank()) {
            res.setError(true);
            res.setErrorMessage("Source must not be null or blank.");
            res.getUserMessages().add(AudlangMessage.msg(CommonErrors.ERR_1000_PARSE_FAILED));
        }
        else {

            try {
                PlExpressionBuilder expressionBuilder = new PlExpressionBuilder();

                AudlangLexer lexer = new AudlangLexer((CharStream) null);
                lexer.removeErrorListeners();
                lexer.addErrorListener(expressionBuilder.getErrorListener());

                CharStream inputCharStream = CharStreams.fromString(source);

                lexer.setInputStream(inputCharStream);
                CommonTokenStream allTokens = new CommonTokenStream(lexer);

                allTokens.fill();

                AudlangParser parser = new AudlangParser(null);

                parser.setBuildParseTree(true);
                parser.removeErrorListeners();
                parser.addErrorListener(expressionBuilder.getErrorListener());
                parser.addParseListener(expressionBuilder);

                expressionBuilder.initialize(source, determineTokenNames(parser, allTokens), parser.getRuleNames());

                // we obtain the reference before parsing to have it in case of errors
                res = expressionBuilder.getParseResult();

                parser.setTokenStream(allTokens);

                parser.query();

            }
            catch (OutOfMemoryError err) {
                throw err;
            }
            catch (RuntimeException ex) {
                String msg = String.format("Unexpected error while parsing: %s", ex);
                if (!res.isError()) {
                    // always report the first error, not the follow-up
                    res.setError(true);
                    res.setErrorMessage(msg);
                    res.getUserMessages().add(0, AudlangMessage.msg(CommonErrors.ERR_1000_PARSE_FAILED));
                }
                LOGGER.debug(msg, ex);
            }

        }
        return res;
    }

    /**
     * Obtains human-readable token names corresponding to the grammar
     * 
     * @param parser to transpate tokens
     * @param allTokens
     * @return list of token names (grammar names) in index order
     */
    private static String[] determineTokenNames(AudlangParser parser, CommonTokenStream allTokens) {
        Vocabulary vocabulary = parser.getVocabulary();

        List<Token> tokens = allTokens.getTokens();
        String[] tokenNames = new String[tokens.size()];
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            String tokenName = vocabulary.getSymbolicName(token.getType());
            if (tokenName == null) {
                tokenName = vocabulary.getLiteralName(token.getType());
            }
            if (tokenName == null) {
                tokenName = token.toString();
            }

            tokenNames[i] = tokenName;
        }
        return tokenNames;
    }

    protected PlExpressionBuilder() {
        // for potential sub-classing
    }

    @Override
    public void exitArgName(ArgNameContext ctx) {
        String nameOrRef = decodePlainText(ctx.getText());

        if (here().nameIsReference) {
            here().matchOperandList.add(new PlOperand(nameOrRef, true, null));
        }
        else {
            here().argName = nameOrRef;
        }

    }

    @Override
    public void exitArgValue(ArgValueContext ctx) {
        String value = decodePlainText(ctx.getText());
        here().matchOperandList.add(new PlOperand(value, false, null));
    }

    @Override
    public void enterArgRef(ArgRefContext ctx) {
        here().nameIsReference = true;
    }

    private void handleAfterOperand() {
        List<PlComment> operandComments = new ArrayList<>();
        List<PlComment> parentComments = new ArrayList<>();
        for (PlComment comment : here().comments) {
            if (comment.position() == BEFORE_OPERAND || comment.position() == Position.AFTER_OPERAND) {
                operandComments.add(comment);
            }
            else {
                parentComments.add(comment);
            }
        }
        here().comments = parentComments;
        List<PlOperand> operandList = here().matchOperandList;
        PlOperand operand = operandList.remove(operandList.size() - 1);
        operandList.add(operand.withComments(operandComments));
    }

    @Override
    public void exitSnippetListItem(SnippetListItemContext ctx) {
        handleAfterOperand();
    }

    @Override
    public void exitValueListItem(ValueListItemContext ctx) {
        handleAfterOperand();
    }

    @Override
    public void exitValueOrRefListItem(ValueOrRefListItemContext ctx) {
        handleAfterOperand();
    }

    @Override
    public void enterCmpAnyOf(CmpAnyOfContext ctx) {
        switch (here().innerType) {
        case PLAIN:
            here().matchOperator = PlMatchOperator.ANY_OF;
            break;
        case NOT:
            here().matchOperator = PlMatchOperator.NOT_ANY_OF;
            break;
        case STRICT_NOT:
            here().matchOperator = PlMatchOperator.STRICT_NOT_ANY_OF;
        }
    }

    @Override
    public void enterCmpBetween(CmpBetweenContext ctx) {
        switch (here().innerType) {
        case PLAIN:
            here().matchOperator = PlMatchOperator.BETWEEN;
            break;
        case NOT:
            here().matchOperator = PlMatchOperator.NOT_BETWEEN;
            break;
        case STRICT_NOT:
            here().matchOperator = PlMatchOperator.STRICT_NOT_BETWEEN;
        }
    }

    @Override
    public void enterCmpContains(CmpContainsContext ctx) {
        switch (here().innerType) {
        case PLAIN:
            here().matchOperator = PlMatchOperator.CONTAINS;
            break;
        case NOT:
            here().matchOperator = PlMatchOperator.NOT_CONTAINS;
            break;
        case STRICT_NOT:
            here().matchOperator = PlMatchOperator.STRICT_NOT_CONTAINS;
        }
    }

    @Override
    public void enterCmpContainsAnyOf(CmpContainsAnyOfContext ctx) {
        switch (here().innerType) {
        case PLAIN:
            here().matchOperator = PlMatchOperator.CONTAINS_ANY_OF;
            break;
        case NOT:
            here().matchOperator = PlMatchOperator.NOT_CONTAINS_ANY_OF;
            break;
        case STRICT_NOT:
            here().matchOperator = PlMatchOperator.STRICT_NOT_CONTAINS_ANY_OF;
        }
    }

    @Override
    public void enterCmpEquals(CmpEqualsContext ctx) {
        if (here().matchOperator == null) {
            here().matchOperator = PlMatchOperator.EQUALS;
        }
    }

    @Override
    public void enterCmpExpression(CmpExpressionContext ctx) {
        List<PlComment> lookAheadComments = here().lookAheadComments;
        stack.push(new DataCollector(ItemType.MATCH));
        here().comments.addAll(lookAheadComments);
        lookAheadComments.clear();
    }

    @Override
    public void exitCmpExpression(CmpExpressionContext ctx) {
        moveBeforeOperandComments();
        DataCollector coll = stack.pop();
        here().childExpressions.add(new PlMatchExpression(coll.argName, coll.matchOperator, coll.matchOperandList, coll.comments));
    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("EXIT rule: {}", ruleNames[ctx.getRuleIndex()]);
        }
    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("ENTER rule: {}", ruleNames[ctx.getRuleIndex()]);
        }
    }

    @Override
    public void enterCmpGreaterThan(CmpGreaterThanContext ctx) {
        here().matchOperator = PlMatchOperator.GREATER_THAN;
    }

    @Override
    public void enterCmpGreaterThanOrEquals(CmpGreaterThanOrEqualsContext ctx) {
        here().matchOperator = PlMatchOperator.GREATER_THAN_OR_EQUALS;
    }

    @Override
    public void enterCmpLessThan(CmpLessThanContext ctx) {
        here().matchOperator = PlMatchOperator.LESS_THAN;
    }

    @Override
    public void enterCmpLessThanOrEquals(CmpLessThanOrEqualsContext ctx) {
        here().matchOperator = PlMatchOperator.LESS_THAN_OR_EQUALS;
    }

    @Override
    public void enterCmpNotEquals(CmpNotEqualsContext ctx) {
        if (here().matchOperator == null) {
            here().matchOperator = PlMatchOperator.NOT_EQUALS;
        }
    }

    @Override
    public void enterCmpStrictNotEquals(CmpStrictNotEqualsContext ctx) {
        here().matchOperator = PlMatchOperator.STRICT_NOT_EQUALS;
    }

    @Override
    public void enterCmpIsUnknown(CmpIsUnknownContext ctx) {
        here().matchOperator = PlMatchOperator.IS_UNKNOWN;
    }

    @Override
    public void enterCmpIsNotUnknown(CmpIsNotUnknownContext ctx) {
        here().matchOperator = PlMatchOperator.IS_NOT_UNKNOWN;
    }

    @Override
    public void enterCmpInnerNot(CmpInnerNotContext ctx) {
        if (here().innerType != InnerType.STRICT_NOT) {
            here().innerType = InnerType.NOT;
        }
    }

    @Override
    public void enterCmpStrictInnerNot(CmpStrictInnerNotContext ctx) {
        here().innerType = InnerType.STRICT_NOT;
    }

    @Override
    public void exitAllExpression(AllExpressionContext ctx) {
        here().childExpressions.add(new PlSpecialSetExpression(SpecialSetType.ALL, null));
    }

    @Override
    public void exitNoneExpression(NoneExpressionContext ctx) {
        here().childExpressions.add(new PlSpecialSetExpression(SpecialSetType.NONE, null));
    }

    @Override
    public void enterBracedExpression(BracedExpressionContext ctx) {
        List<PlComment> lookAheadComments = here().lookAheadComments;
        stack.push(new DataCollector(ItemType.EXTRA_BRACES));
        here().comments.addAll(lookAheadComments);
        lookAheadComments.clear();
    }

    @Override
    public void exitBracedExpression(BracedExpressionContext ctx) {
        moveCommentsToLastChild();
        handleLookBackComments();
        DataCollector coll = stack.pop();
        here().childExpressions.add(coll.getLastChildExpression());
    }

    @Override
    public void enterAndExpression(AndExpressionContext ctx) {
        List<PlComment> lookAheadComments = here().lookAheadComments;
        stack.push(new DataCollector(ItemType.AND));
        here().comments.addAll(lookAheadComments);
        lookAheadComments.clear();
    }

    @Override
    public void exitAndExpression(AndExpressionContext ctx) {
        moveCommentsToLastChild();
        handleLookBackComments();
        DataCollector coll = stack.pop();
        PlCombinedExpression combinedExpression = new PlCombinedExpression(CombinedExpressionType.AND, coll.childExpressions, null);
        here().childExpressions.add(combinedExpression);
    }

    @Override
    public void enterOrExpression(OrExpressionContext ctx) {
        if (here().itemType == ItemType.CURB) {
            stack.push(new DataCollector(ItemType.OR));
        }
        else {
            List<PlComment> lookAheadComments = here().lookAheadComments;
            stack.push(new DataCollector(ItemType.OR));
            here().comments.addAll(lookAheadComments);
            lookAheadComments.clear();
        }

    }

    @Override
    public void exitOrExpression(OrExpressionContext ctx) {
        moveCommentsToLastChild();
        handleLookBackComments();
        DataCollector coll = stack.pop();
        PlCombinedExpression combinedExpression = new PlCombinedExpression(CombinedExpressionType.OR, coll.childExpressions, here().lookAheadComments);
        here().childExpressions.add(combinedExpression);
    }

    @Override
    public void enterNotExpression(NotExpressionContext ctx) {
        List<PlComment> lookAheadComments = here().lookAheadComments;
        stack.push(new DataCollector(ItemType.NOT));
        here().comments.addAll(lookAheadComments);
        lookAheadComments.clear();
    }

    @Override
    public void exitNotExpression(NotExpressionContext ctx) {
        List<PlComment> delegateComments = new ArrayList<>(here().lookAheadComments);
        DataCollector coll = stack.pop();
        PlExpression<?> delegate = coll.getLastChildExpression();
        delegateComments.addAll(delegate.allDirectComments());
        delegate = delegate.withComments(delegateComments);
        here().childExpressions.add(new PlNegationExpression(delegate, false, coll.comments));
    }

    @Override
    public void enterStrictNotExpression(StrictNotExpressionContext ctx) {
        List<PlComment> lookAheadComments = here().lookAheadComments;
        stack.push(new DataCollector(ItemType.STRICT_NOT));
        here().comments.addAll(lookAheadComments);
        lookAheadComments.clear();
    }

    @Override
    public void exitStrictNotExpression(StrictNotExpressionContext ctx) {
        DataCollector coll = stack.pop();
        PlNegationExpression negation = coll.getLastChildExpression();
        coll.comments.addAll(negation.allDirectComments());
        negation = new PlNegationExpression(negation.delegate(), true, coll.comments);
        here().childExpressions.add(negation);
    }

    @Override
    public void enterCurbExpression(CurbExpressionContext ctx) {
        List<PlComment> lookAheadComments = here().lookAheadComments;
        stack.push(new DataCollector(ItemType.CURB));
        here().comments.addAll(lookAheadComments);
        lookAheadComments.clear();
    }

    @Override
    public void exitCurbExpression(CurbExpressionContext ctx) {
        DataCollector coll = stack.pop();
        PlCombinedExpression innerOr = coll.getLastChildExpression();
        PlCurbExpression curb = new PlCurbExpression(innerOr, coll.curbOperator, coll.curbBound, coll.comments);
        here().childExpressions.add(curb);
    }

    @Override
    public void enterCurbEquals(CurbEqualsContext ctx) {
        here().curbOperator = PlCurbOperator.EQUALS;
    }

    @Override
    public void enterCurbNotEquals(CurbNotEqualsContext ctx) {
        here().curbOperator = PlCurbOperator.NOT_EQUALS;
    }

    @Override
    public void enterCurbGreaterThan(CurbGreaterThanContext ctx) {
        here().curbOperator = PlCurbOperator.GREATER_THAN;
    }

    @Override
    public void enterCurbGreaterThanOrEquals(CurbGreaterThanOrEqualsContext ctx) {
        here().curbOperator = PlCurbOperator.GREATER_THAN_OR_EQUALS;
    }

    @Override
    public void enterCurbLessThan(CurbLessThanContext ctx) {
        here().curbOperator = PlCurbOperator.LESS_THAN;
    }

    @Override
    public void enterCurbLessThanOrEquals(CurbLessThanOrEqualsContext ctx) {
        here().curbOperator = PlCurbOperator.LESS_THAN_OR_EQUALS;
    }

    @Override
    public void exitCurbBound(CurbBoundContext ctx) {

        int bound = -1;

        String parsedBound = ctx.getText();
        try {
            bound = Integer.parseInt(parsedBound);
        }
        catch (NumberFormatException | NullPointerException _) {
            // handled by value check below
        }

        if (bound < 0) {
            String msg = String.format("The bound value of a CURB is out of range (bound must reflect the number of conditions to be met."
                    + " Realistically, it should be a small number >= 0), given: %s", parsedBound);
            logError(msg);
            throw new ParseCancellationException(msg);
        }

        here().curbBound = bound;
    }

    @Override
    public void exitComment(CommentContext ctx) {

        switch (here().itemType) {
        case ItemType.CURB: {
            processIntraCurbComment(ctx);
            break;
        }
        case ItemType.NOT, ItemType.STRICT_NOT: {
            processIntraNotComment(ctx);
            break;
        }
        case ItemType.EXTRA_BRACES: {
            if (here().nextCommentPosition == AFTER_EXPRESSION) {
                processTrailingExtraBraceComment(ctx);
            }
            else {
                processIntraCombinedComment(ctx);
            }
            break;
        }
        case ItemType.AND, ItemType.OR: {
            processIntraCombinedComment(ctx);
            break;
        }
        case ItemType.ROOT: {
            processRootComment(ctx);
            break;
        }
        // $CASES-OMITTED$
        default: {
            here().comments.add(new PlComment(ctx.getText(), here().nextCommentPosition));
        }
        }

    }

    @Override
    public void enterSpaceAfterAny(SpaceAfterAnyContext ctx) {
        here().nextCommentPosition = CommentUtils.translatePhysicalToRelativeCommentPosition(PhysicalCommentPosition.AFTER_ANY, here().matchOperator);
    }

    @Override
    public void enterSpaceAfterArgName(SpaceAfterArgNameContext ctx) {
        here().nextCommentPosition = (here().nextCommentPosition == C1 ? C2 : C1);
    }

    @Override
    public void enterSpaceAfterBetween(SpaceAfterBetweenContext ctx) {
        here().nextCommentPosition = CommentUtils.translatePhysicalToRelativeCommentPosition(PhysicalCommentPosition.AFTER_BETWEEN, here().matchOperator);
    }

    @Override
    public void enterSpaceAfterCombiner(SpaceAfterCombinerContext ctx) {
        here().nextCommentPosition = BEFORE_EXPRESSION;
    }

    @Override
    public void enterSpaceAfterContains(SpaceAfterContainsContext ctx) {
        here().nextCommentPosition = CommentUtils.translatePhysicalToRelativeCommentPosition(PhysicalCommentPosition.AFTER_CONTAINS, here().matchOperator);
    }

    @Override
    public void enterSpaceAfterCurb(SpaceAfterCurbContext ctx) {
        here().nextCommentPosition = BEFORE_EXPRESSION;
    }

    @Override
    public void enterSpaceAfterCurbedOr(SpaceAfterCurbedOrContext ctx) {
        here().nextCommentPosition = AFTER_EXPRESSION;
    }

    @Override
    public void exitSpaceAfterCurbedOr(SpaceAfterCurbedOrContext ctx) {

        PlExpression<?> innerOr = here().childExpressions.remove(0);

        List<PlComment> parentComments = new ArrayList<>();

        List<PlComment> orComments = new ArrayList<>(innerOr.allDirectComments());
        for (PlComment comment : here().comments) {
            if (comment.position() == AFTER_EXPRESSION) {
                orComments.add(comment);
            }
            else {
                parentComments.add(comment);
            }
        }
        here().comments.clear();
        here().comments.addAll(parentComments);
        innerOr = innerOr.withComments(orComments);
        here().childExpressions.add(innerOr);

    }

    @Override
    public void enterSpaceAfterExpression(SpaceAfterExpressionContext ctx) {
        here().nextCommentPosition = AFTER_EXPRESSION;
    }

    @Override
    public void enterSpaceAfterIs(SpaceAfterIsContext ctx) {
        here().nextCommentPosition = CommentUtils.translatePhysicalToRelativeCommentPosition(PhysicalCommentPosition.AFTER_IS, here().matchOperator);
    }

    @Override
    public void enterSpaceAfterListItem(SpaceAfterListItemContext ctx) {
        here().nextCommentPosition = Position.AFTER_OPERAND;
    }

    @Override
    public void enterSpaceAfterNot(SpaceAfterNotContext ctx) {
        if (here().itemType == ItemType.NOT) {
            here().nextCommentPosition = BEFORE_EXPRESSION;
        }
        else {
            Position commentPosition = here().nextCommentPosition;
            if (commentPosition == null) {
                here().nextCommentPosition = C1;
            }
            else {
                here().nextCommentPosition = commentPosition.next();
            }
        }
    }

    @Override
    public void enterSpaceAfterOf(SpaceAfterOfContext ctx) {
        here().nextCommentPosition = CommentUtils.translatePhysicalToRelativeCommentPosition(PhysicalCommentPosition.AFTER_OF, here().matchOperator);
    }

    @Override
    public void enterSpaceAfterOperator(SpaceAfterOperatorContext ctx) {
        if (here().itemType == ItemType.CURB) {
            here().nextCommentPosition = C1;
        }
        else {
            here().nextCommentPosition = CommentUtils.translatePhysicalToRelativeCommentPosition(PhysicalCommentPosition.AFTER_OPERATOR, here().matchOperator);
        }
    }

    @Override
    public void enterSpaceAfterStrict(SpaceAfterStrictContext ctx) {
        Position commentPosition = here().nextCommentPosition;
        if (commentPosition == null) {
            here().nextCommentPosition = C1;
        }
        else {
            here().nextCommentPosition = commentPosition.next();
        }
    }

    @Override
    public void enterSpaceBeforeCombiner(SpaceBeforeCombinerContext ctx) {
        here().nextCommentPosition = AFTER_EXPRESSION;
    }

    @Override
    public void exitSpaceBeforeCombiner(SpaceBeforeCombinerContext ctx) {
        moveCommentsToLastChild();
        handleLookBackComments();
    }

    @Override
    public void enterSpaceBeforeExpression(SpaceBeforeExpressionContext ctx) {
        here().nextCommentPosition = BEFORE_EXPRESSION;
    }

    @Override
    public void enterSpaceBeforeListItem(SpaceBeforeListItemContext ctx) {
        here().nextCommentPosition = BEFORE_OPERAND;
    }

    @Override
    public void enterQuery(QueryContext ctx) {
        if (this.rootCollector != null) {
            throw new ParseCancellationException("Expression builder is already in use, (re-)initialize before reuse!");
        }
        if (this.ruleNames == null) {
            throw new ParseCancellationException("Expression builder must be initialized with the rule names of the parser!");
        }
        if (this.tokenNames == null) {
            throw new ParseCancellationException("Expression builder must be initialized with the token names of the parser!");
        }
        if (this.source == null) {
            throw new ParseCancellationException("Expression builder must be initialized with a source string!");
        }
        rootCollector = new DataCollector(ItemType.ROOT);
        stack.push(rootCollector);

    }

    @Override
    public void exitQuery(QueryContext ctx) {

        if (!parseResult.isError() && here() != rootCollector) {
            logError(String.format("Unexpected end of query at line: %s, charPositionInLine: %s, text: %s", ctx.getStart().getLine(),
                    ctx.getStart().getCharPositionInLine(), ctx.getText()));
        }
        else if (!parseResult.isError()) {
            moveCommentsToLastChild();
            handleLookBackComments();
            parseResult.setResultExpression(here().getLastChildExpression());
        }

        stack.pop();

    }

    /**
     * @return error listener which MUST be registered before parsing to ensure {@link #getParseResult()} can correctly report problems
     */
    protected ANTLRErrorListener getErrorListener() {
        return errorListener;
    }

    /**
     * @return instance that either contains the parsed expression or error information
     */
    protected AudlangParseResult getParseResult() {
        return parseResult;
    }

    /**
     * This methods (re-)initializes the builder for the next parsing round
     * 
     * @param source text to be parsed
     * @param tokenNames obtained from parser
     * @param ruleNames obtained from lexer/parser
     */
    protected void initialize(String source, String[] tokenNames, String[] ruleNames) {
        this.source = source;
        this.parseResult = new AudlangParseResult();
        this.parseResult.setSource(source);
        this.rootCollector = null;
        this.stack.clear();
        this.tokenNames = tokenNames;
        this.ruleNames = ruleNames;
    }

    /**
     * When a level is closed there is just one child but the related comments sit on the parent level. <br>
     * This method attaches the comments to the produced child expression.
     */
    private void moveCommentsToLastChild() {
        DataCollector coll = here();
        PlExpression<?> childExpression = coll.childExpressions.remove(coll.childExpressions.size() - 1);
        List<PlComment> beforeComments = coll.comments.stream().filter(com -> com.position() == BEFORE_EXPRESSION).toList();
        List<PlComment> afterComments = coll.comments.stream().filter(com -> com.position() == AFTER_EXPRESSION).toList();
        List<PlComment> combinedComments = new ArrayList<>();
        combinedComments.addAll(beforeComments);
        combinedComments.addAll(childExpression.allDirectComments());
        combinedComments.addAll(afterComments);
        childExpression = childExpression.withComments(combinedComments);
        coll.comments.clear();
        coll.childExpressions.add(childExpression);
    }

    /**
     * This method deals with the collected operand comments (distinguish them from the expression comments and attach)
     */
    private void moveBeforeOperandComments() {
        if (here().matchOperandList.isEmpty() || here().comments.isEmpty()) {
            return;
        }
        List<PlComment> operandComments = new ArrayList<>();
        List<PlComment> exprComments = new ArrayList<>();
        for (PlComment comment : here().comments) {
            if (comment.position() == BEFORE_OPERAND) {
                operandComments.add(comment);
            }
            else {
                exprComments.add(comment);
            }
        }
        here().comments = exprComments;
        PlOperand operand = here().matchOperandList.get(0);
        operandComments.addAll(operand.allDirectComments());
        operand = operand.withComments(operandComments);
        here().matchOperandList.set(0, operand);
    }

    /**
     * Braces on AND/OR are optional, so we need to distinguish comments after a combined expression (AND/OR) <br>
     * vs. comments after the last element of the combined expression (if there was no brace in-between)
     * 
     * @param child to attach look-back comments to
     * @return modified child
     */
    private PlCombinedExpression processLookBackCommentsOnCombinedMember(PlCombinedExpression child) {

        List<PlComment> leftAlignedLookBackComments = here().leftAlignedLookBackComments;
        List<PlComment> unalignedLookBackComments = here().lookBackComments.stream().filter(Predicate.not(leftAlignedLookBackComments::contains)).toList();

        List<PlComment> childComments = new ArrayList<>(child.comments());
        childComments.addAll(unalignedLookBackComments);

        List<PlExpression<?>> subChildren = new ArrayList<>(child.members());
        PlExpression<?> subChild = subChildren.get(subChildren.size() - 1);
        List<PlComment> subChildComments = new ArrayList<>(subChild.allDirectComments());
        subChildComments.addAll(leftAlignedLookBackComments);

        subChild = subChild.withComments(subChildComments);
        subChildren.set(subChildren.size() - 1, subChild);

        return new PlCombinedExpression(child.combiType(), subChildren, childComments);

    }

    /**
     * This method places comments collected after an expression according to their affinity to the previous expression.
     */
    private void handleLookBackComments() {
        PlExpression<?> child = here().getLastChildExpression();
        if (!here().lookBackComments.isEmpty()) {

            if (child instanceof PlCombinedExpression cmb) {
                child = processLookBackCommentsOnCombinedMember(cmb);
                here().childExpressions.set(here().childExpressions.size() - 1, child);
            }
            else {
                List<PlComment> childComments = new ArrayList<>(child.allDirectComments());
                childComments.addAll(here().lookBackComments);
                here().lookBackComments.clear();
                child = child.withComments(childComments);
                here().childExpressions.set(here().childExpressions.size() - 1, child);
            }
            here().lookBackComments.clear();
            here().leftAlignedLookBackComments.clear();
        }
    }

    /**
     * Comment on root level
     * 
     * @param ctx
     */
    private void processRootComment(CommentContext ctx) {
        if (here().nextCommentPosition == AFTER_EXPRESSION) {
            PlComment plComment = new PlComment(ctx.getText(), AFTER_EXPRESSION);
            here().lookBackComments.add(plComment);
            if (!checkCommentAfterClosingBrace(ctx.getStart())) {
                here().leftAlignedLookBackComments.add(plComment);
            }
        }
        else {
            here().comments.add(new PlComment(ctx.getText(), here().nextCommentPosition));
        }
    }

    /**
     * Comment on braces or on AND/OR
     * 
     * @param ctx
     */
    private void processIntraCombinedComment(CommentContext ctx) {
        if (here().nextCommentPosition == BEFORE_EXPRESSION) {
            here().lookAheadComments.add(new PlComment(ctx.getText(), BEFORE_EXPRESSION));
        }
        else if (here().nextCommentPosition == AFTER_EXPRESSION) {
            PlComment plComment = new PlComment(ctx.getText(), AFTER_EXPRESSION);
            here().lookBackComments.add(plComment);
            if (!checkCommentAfterClosingBrace(ctx.getStart())) {
                here().leftAlignedLookBackComments.add(plComment);
            }
        }
    }

    /**
     * This method deals with the problem that comments can be placed after closing extra braces.
     * <p>
     * Because we eliminate the useless braces we must attach the comments to the child (not to the parent!)
     * 
     * @param ctx
     */
    private void processTrailingExtraBraceComment(CommentContext ctx) {
        PlComment plComment = new PlComment(ctx.getText(), AFTER_EXPRESSION);
        here().lookBackComments.add(plComment);
        here().leftAlignedLookBackComments.add(plComment);
    }

    /**
     * Comment on a negation
     * 
     * @param ctx
     */
    private void processIntraNotComment(CommentContext ctx) {
        if (here().nextCommentPosition == BEFORE_EXPRESSION) {
            // this is the position between the word NOT and the negated expression
            here().lookAheadComments.add(new PlComment(ctx.getText(), BEFORE_EXPRESSION));
        }
        else {
            here().comments.add(new PlComment(ctx.getText(), here().nextCommentPosition));
        }
    }

    /**
     * Comment inside a CURB expression
     * 
     * @param ctx
     */
    private void processIntraCurbComment(CommentContext ctx) {
        if (here().childExpressions.isEmpty()) {
            // this is the position between the CURB word and the opening brace
            here().lookAheadComments.add(new PlComment(ctx.getText(), BEFORE_EXPRESSION));
        }
        else if (here().curbOperator == null) {
            // this is the comment after the curbed OR but before the operator
            // so the comment belongs to the enclosed OR
            PlExpression<?> curbedOr = here().childExpressions.get(0);
            List<PlComment> comments = new ArrayList<>(curbedOr.allDirectComments());
            comments.add(new PlComment(ctx.getText(), AFTER_EXPRESSION));
            curbedOr = curbedOr.withComments(comments);
            here().childExpressions.set(0, curbedOr);
        }
        else {
            // This should be a comment between the operator and the bound
            here().comments.add(new PlComment(ctx.getText(), here().nextCommentPosition));
        }
    }

    /**
     * This method is a workaround for the problem that comments may be collected after the owning expression is already closed, so the position becomes
     * unclear. We use the raw token position to determine if the comment is right after a closing brace or not, so we can tell if the comment belongs to the
     * combined expression or its last member. In other words: comments must not jump over braces.
     * 
     * @param commentToken
     * @return true if this comment is placed after a closing brace
     */
    private boolean checkCommentAfterClosingBrace(Token commentToken) {
        boolean res = false;
        int idxStart = commentToken.getTokenIndex();
        if (idxStart > 0 && idxStart < tokenNames.length) {
            for (int i = idxStart - 1; !res && i > -1; i--) {
                String tokenName = tokenNames[i];
                if (tokenName.equals("')'")) {
                    res = true;
                }
                else if (!tokenName.equals("WHITESPACE") && !tokenName.equals("COMMENT")) {
                    break;
                }
            }
        }
        return res;
    }

    /**
     * Returns the collector responsible for the current parsing level in the tree
     * 
     * @return collector
     */
    private DataCollector here() {
        return stack.peek();
    }

    /**
     * @param argNameOrValue
     * @return string with optional double quotes and escaping removed
     */
    private String decodePlainText(String argNameOrValue) {
        return AdlTextUtils.unescapeSpecialCharacters(AdlTextUtils.removeDoubleQuotesIfRequired(argNameOrValue));
    }

    /**
     * Logs a processing error and sets the result to error
     * 
     * @param msg
     */
    private void logError(String msg) {
        if (!parseResult.isError()) {
            parseResult.setError(true);
            parseResult.setErrorMessage(msg);
            parseResult.getUserMessages().add(0, AudlangMessage.msg(CommonErrors.ERR_1000_PARSE_FAILED, msg));
        }
    }

    /**
     * This error listener records the problem information for later analysis (parse result)
     */
    private class ProtocolErrorListener extends BaseErrorListener {

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e)
                throws ParseCancellationException {

            String msgFull = String.format("Parse error at line: %s, charPositionInLine: %s, offendingSymbol: %s, msg: %s", line, charPositionInLine,
                    offendingSymbol, msg);

            if (offendingSymbol instanceof Token token) {

                int idx = token.getTokenIndex();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i <= idx; i++) {
                    sb.append(tokenNames[i]);
                    if (i < idx) {
                        sb.append(", ");
                    }
                }
                LOGGER.trace("----------------------> Token Trace: {}", sb);
            }

            if (!parseResult.isError()) {
                logError(msgFull);
            }
            LOGGER.debug(msgFull, e);
            throw new ParseCancellationException(msgFull, e);
        }

    }

    /**
     * A {@link DataCollector} instance isolates the state when parsing a recursive structure. Each level (see {@link ItemType}) opens a fresh collector
     * instance and puts it on a stack. Whenever we finish a certain element the current collector will be discarded and we continue with its parent.
     */
    private static class DataCollector {

        /**
         * Identifies what we are currently constructing
         */
        ItemType itemType = null;

        InnerType innerType = InnerType.PLAIN;

        String argName = null;

        /**
         * tells that the subsequently detected argument name is in fact a reference value
         */
        boolean nameIsReference = false;

        /**
         * kind of match when later parsing the parameters
         */
        PlMatchOperator matchOperator;

        /**
         * list of operands found parsed from the input
         */
        List<PlOperand> matchOperandList = new ArrayList<>();

        /**
         * curb bound value, a negative one means not initialized/error
         */
        int curbBound = -1;

        /**
         * The operator for comparing the curb bound
         */
        PlCurbOperator curbOperator;

        /**
         * collected comments on this level
         */
        List<PlComment> comments = new ArrayList<>();

        /**
         * Comments picked by the parser BEFORE the expression came, so we must move them forward
         */
        List<PlComment> lookAheadComments = new ArrayList<>();

        /**
         * Comments picked by the parser AFTER the expression finished, so we must move them backward
         */
        List<PlComment> lookBackComments = new ArrayList<>();

        /**
         * Tricky comments which actually belong to the last member of a composite because there was no closing brace in-between.
         */
        List<PlComment> leftAlignedLookBackComments = new ArrayList<>();

        /**
         * This list collects expressions collected on the next finer level, see exit-methods
         */
        List<PlExpression<?>> childExpressions = new ArrayList<>();

        /**
         * Any subsequently parsed comment is logically at this position
         */
        Position nextCommentPosition = Position.BEFORE_EXPRESSION;

        DataCollector(ItemType itemType) {
            this.itemType = itemType;
        }

        /**
         * Convenience method to easily get a typed expression from the generic list if we know anyway the type
         * 
         * @param <T>
         * @return the last expression in the list of children
         */
        <T extends PlExpression<?>> T getLastChildExpression() {
            @SuppressWarnings("unchecked")
            T res = (T) childExpressions.get(childExpressions.size() - 1);
            return res;
        }

        @Override
        public String toString() {
            return itemType.name();
        }

    }

    /**
     * This enum covers the level (element) the builder is currently working on
     */
    private enum ItemType {

        ROOT, MATCH, AND, OR, CURB, NOT, STRICT_NOT, EXTRA_BRACES;

    }

    /**
     * type to handle inner rules such as "STRICT NOT CONTAINS"
     */
    private enum InnerType {

        PLAIN, NOT, STRICT_NOT;
    }
}

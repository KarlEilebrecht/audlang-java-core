//@formatter:off
/*
 * CommentUtils
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

import static de.calamanari.adl.FormatConstants.COMMENT_LINE_THRESHOLD;
import static de.calamanari.adl.FormatUtils.appendIndent;
import static de.calamanari.adl.FormatUtils.newLine;
import static de.calamanari.adl.FormatUtils.space;
import static de.calamanari.adl.FormatUtils.stripTrailingWhitespace;
import static de.calamanari.adl.erl.PlComment.Position.BEFORE_OPERAND;
import static de.calamanari.adl.erl.PlComment.Position.C1;
import static de.calamanari.adl.erl.PlComment.Position.C2;
import static de.calamanari.adl.erl.PlComment.Position.C3;
import static de.calamanari.adl.erl.PlComment.Position.C4;
import static de.calamanari.adl.erl.PlComment.Position.C5;
import static de.calamanari.adl.erl.PlComment.Position.C6;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.calamanari.adl.FormatConstants;
import de.calamanari.adl.FormatStyle;
import de.calamanari.adl.erl.PlComment.Position;
import de.calamanari.adl.erl.PlMatchExpression.PlMatchOperator;

/**
 * Some helper methods for writing comments on elements of an expression.
 * <p>
 * It was required to define a standard way to format comments so that single-line and multi-line format correspond to each other in a reasonable way (no flaky
 * behavior).<br>
 * The methods here split/tokenize/mangle any comment to bring it into a generic form. Additionally, this utility covers the knowledge how to present comments
 * in difference positions (in-between operators, operands, etc.).
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class CommentUtils {

    /**
     * We only <i>soft-split</i> comments, usually at the whitespace characters, but worst case we use the given characters for splitting.
     */
    private static final char[] SEPARATORS = ",=()".toCharArray();

    /**
     * @param comments
     * @param validPositions
     * @return true if the positions requested by the comments match are all valid positions
     */
    public static boolean verifyCommentsApplicable(List<PlComment> comments, Set<PlComment.Position> validPositions) {
        if (comments != null && !comments.isEmpty()) {
            return comments.stream().allMatch(comment -> validPositions.contains(comment.position()));
        }
        return true;
    }

    /**
     * Creates a standard single-line format of the comment, which is reproducible.
     * <p>
     * <b>Important:</b> This method's output is <i>compatible</i> to the output of {@link #appendCommentMultiLine(String, StringBuilder, FormatStyle, int)} in
     * a way that both formats can be tokenized again and to be written in the one or the other way without changing anything.<br>
     * This also means: touching the one or the other implementation causes high effort for re-testing!
     * 
     * @param comment not null, text <b>must</b> start with <code>'/*'</code> and end with <code>'*&#47;'</code>, so the shortest possible comment is
     *            <code>'/**&#47;'</code>
     * @return standard comment without any line breaks and minimal whitespace
     */
    public static String normalizeComment(String comment) {
        StringBuilder sb = new StringBuilder();

        List<String> tokens = tokenizeComment(comment);

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (i > 0) {
                space(sb);
            }
            sb.append(token);
        }
        return sb.toString();

    }

    /**
     * This method is used to print a comment on multipliple lines
     * <p>
     * <b>Important:</b> This method's output is <i>compatible</i> to the output of {@link #normalizeComment(String)} in a way that both formats can be
     * tokenized again and to be written in the one or the other way without changing anything.<br>
     * This also means: touching the one or the other implementation causes high effort for re-testing!
     * <p>
     * <b>Note:</b> We try to split lengthy comments but we do not enforce it if there is no way to <i>soft-split</i> a comment at whitespace or certain
     * characters. Especially, we never break a section enclosed or started by double-quote.
     * 
     * @param comment
     * @param sb
     * @param style
     * @param level for nesting
     */
    public static void appendCommentMultiLine(String comment, StringBuilder sb, FormatStyle style, int level) {
        appendIndent(sb, style, level);

        MultiLineCommentOutputState state = new MultiLineCommentOutputState();

        state.tokens = tokenizeComment(comment);
        state.currentLineLength = 0;
        state.sb = sb;
        state.style = style;
        state.level = level;

        for (int tokenIdx = 0; tokenIdx < state.tokens.size(); tokenIdx++) {

            state.currentToken = state.tokens.get(tokenIdx);
            state.currentTokenLength = state.currentToken.length();
            state.needSpaceBeforeToken = false;
            if (tokenIdx > 0) {
                // consider the separator space after the last token
                state.currentTokenLength++;
                state.needSpaceBeforeToken = true;
            }

            if (state.currentLineLength + state.currentTokenLength > COMMENT_LINE_THRESHOLD) {
                handleNextCommentLine(state, tokenIdx);
            }
            else {
                if (state.needSpaceBeforeToken) {
                    space(state.sb);
                }
                state.sb.append(state.currentToken);
                state.currentLineLength = state.currentLineLength + state.currentTokenLength;
            }
        }
    }

    /**
     * @param state
     * @param tokenIdx
     */
    private static void handleNextCommentLine(MultiLineCommentOutputState state, int tokenIdx) {
        newLine(state.sb);
        appendIndent(state.sb, state.style, state.level);
        if (tokenIdx < state.tokens.size() - 1) {
            // this corresponds to the initial '/* ' (just for beauty reasons)
            state.sb.append("   ");
        }
        if (state.needSpaceBeforeToken) {
            space(state.sb);
        }
        state.sb.append(state.currentToken);
        state.currentLineLength = state.currentTokenLength;
    }

    /**
     * This method splits long comment lines into tokens. By default a token is a non-whitespace sequence between any whitespace.<br>
     * Only if we can't find whitespace gaps we try splitting by other special separators. The goal was to split as natural as possible.
     * <p>
     * This method strictly preserves long character sequences and double-quoted text.
     * <p>
     * Control characters &lt; 32 and 127 are treated as whitespace.
     * 
     * @param comment to be tokenized
     */
    public static List<String> tokenizeComment(String comment) {

        TokenizerState state = new TokenizerState();

        while (state.srcIdx < comment.length()) {
            char ch = comment.charAt(state.srcIdx);
            if ((ch >= 0 && ch < 32) || ch == 127) {
                ch = ' ';
            }
            handleCommentCharacter(comment, state, ch);
        }
        if (state.tokenStartIdx < comment.length()) {
            state.tokens.add(comment.substring(state.tokenStartIdx));
        }

        return splitTokensIfRequired(state.tokens);

    }

    /**
     * Takes the tokens from the first tokenization round and tries further splitting if these tokens are still too long.
     * 
     * @param tokens
     * @return new list with potentially more fine-grained tokens
     */
    private static List<String> splitTokensIfRequired(List<String> tokens) {

        List<String> res = new ArrayList<>(tokens.size());
        for (String token : tokens) {
            if (token.length() > FormatConstants.COMPLEX_COMMENT_THRESHOLD && !token.startsWith("\"")) {
                intelliSplitToken(token, res);
            }
            else {
                res.add(token);
            }
        }
        return res;
    }

    /**
     * This method splits in a way that we prioritize certain characters for natural splitting
     * 
     * @param token
     * @param res will be filled with the sub-tokens
     */
    private static void intelliSplitToken(String token, List<String> res) {

        List<String> subTokens = new ArrayList<>();

        splitToken(token, subTokens, 0);

        StringBuilder sb = new StringBuilder();

        for (String subToken : subTokens) {
            if (sb.length() > 0 && (subToken.length() >= FormatConstants.COMPLEX_COMMENT_THRESHOLD
                    || sb.length() + subToken.length() >= FormatConstants.COMPLEX_COMMENT_THRESHOLD)) {
                res.add(sb.toString());
                sb.setLength(0);
            }
            if (subToken.length() >= FormatConstants.COMPLEX_COMMENT_THRESHOLD) {
                res.add(subToken);
            }
            else {
                sb.append(subToken);
            }
        }
        if (sb.length() > 0) {
            res.add(sb.toString());
        }

    }

    /**
     * Performs a (recursive) split attempt on a single token
     * 
     * @param token
     * @param res
     * @param iteration controls the separater character to be used
     */
    private static void splitToken(String token, List<String> res, int iteration) {

        if (iteration < SEPARATORS.length) {

            List<String> splitResult = splitToken(token, SEPARATORS[iteration]);

            for (String sToken : splitResult) {
                if (sToken.length() > FormatConstants.COMPLEX_COMMENT_THRESHOLD) {
                    splitToken(sToken, res, iteration + 1);
                }
                else {
                    res.add(sToken);
                }
            }

        }
        else {
            // can't split
            res.add(token);
        }

    }

    /**
     * Split on a single token
     * 
     * @param token
     * @param separator
     * @return list of sub tokens
     */
    private static List<String> splitToken(String token, char separator) {

        List<String> res = new ArrayList<>();
        int tokenStart = 0;

        for (int i = 0; i < token.length(); i++) {
            char ch = token.charAt(i);
            if (ch == separator) {
                res.add(token.substring(tokenStart, i + 1));
                tokenStart = i + 1;
            }
        }
        if (tokenStart < token.length()) {
            res.add(token.substring(tokenStart));
        }
        return res;
    }

    /**
     * @param comment
     * @param state
     * @param ch
     */
    private static void handleCommentCharacter(String comment, TokenizerState state, char ch) {
        if (!state.insideDoubleQuotes && ch == '"') {
            handleDoubleQuotesStart(comment, state);
        }
        else if (state.insideDoubleQuotes && ch == '"') {
            if (state.srcIdx < comment.length() - 1 && comment.charAt(state.srcIdx + 1) == '"') {
                state.srcIdx = state.srcIdx + 2;
            }
            else {
                handleDoubleQuotesEnd(comment, state);
            }
        }
        else if (!state.insideDoubleQuotes && ch == ' ') {
            handleSkipableWhitespace(comment, state);
        }
        else if ((ch == '/' && state.srcIdx < comment.length() - 1 && comment.charAt(state.srcIdx + 1) == '*')
                || (ch == '*' && state.srcIdx < comment.length() - 1 && comment.charAt(state.srcIdx + 1) == '/')) {
            handleCommentIndicatorToken(comment, state);
        }
        else {
            state.srcIdx++;
        }
    }

    /**
     * @param comment
     * @param state
     */
    private static void handleCommentIndicatorToken(String comment, TokenizerState state) {
        if (state.srcIdx > state.tokenStartIdx) {
            state.tokens.add(comment.substring(state.tokenStartIdx, state.srcIdx).stripTrailing());
        }
        state.tokens.add(comment.substring(state.srcIdx, state.srcIdx + 2));
        state.srcIdx = state.srcIdx + 2;
        state.tokenStartIdx = state.srcIdx;
    }

    /**
     * @param comment
     * @param state
     */
    private static void handleSkipableWhitespace(String comment, TokenizerState state) {
        if (state.srcIdx > state.tokenStartIdx) {
            state.tokens.add(comment.substring(state.tokenStartIdx, state.srcIdx));
        }
        state.srcIdx++;
        state.tokenStartIdx = state.srcIdx;
    }

    /**
     * @param comment
     * @param state
     */
    private static void handleDoubleQuotesEnd(String comment, TokenizerState state) {
        state.insideDoubleQuotes = false;
        state.tokens.add(comment.substring(state.tokenStartIdx, state.srcIdx + 1));
        state.srcIdx = state.srcIdx + 1;
        state.tokenStartIdx = state.srcIdx;
    }

    /**
     * @param comment
     * @param state
     */
    private static void handleDoubleQuotesStart(String comment, TokenizerState state) {
        if (state.srcIdx > state.tokenStartIdx) {
            state.tokens.add(comment.substring(state.tokenStartIdx, state.srcIdx));
        }
        state.tokenStartIdx = state.srcIdx;
        state.srcIdx++;
        state.insideDoubleQuotes = true;
    }

    /**
     * Appends the given comments (filtered) to the string builder according to style and nesting depth
     * 
     * @param sb target
     * @param comments to be appended
     * @param position only append the comments with the given position, ignore the others
     * @param style
     * @param level nesting depth for indentation
     * @param forceSingleLine enforces writing the comment in a row
     * @return true if there was anything appended, otherwise false
     */
    public static boolean appendComments(StringBuilder sb, List<PlComment> comments, PlComment.Position position, FormatStyle style, int level,
            boolean forceSingleLine) {

        int lengthBefore = sb.length();

        comments.stream().filter(comment -> comment.position() == position).forEach(comment -> {

            if (!forceSingleLine && style.isMultiLine() && comment.isComplex()) {
                if (!sb.isEmpty()) {
                    stripTrailingWhitespace(sb);
                    newLine(sb);
                }
                comment.appendMultiLine(sb, style, level);
                newLine(sb);
            }
            else {
                if (!sb.isEmpty()) {
                    space(sb);
                }
                comment.appendSingleLine(sb, style, level);
            }

        });

        return (sb.length() != lengthBefore);
    }

    /**
     * This method deals with the gaps in an expression (e.g., between argument and operator). Depending on the style with either insert a space or the
     * available comments.
     * 
     * @param sb target
     * @param comments to be appended
     * @param position only append the comments with the given position, ignore the others
     * @param style
     * @param level nesting depth for indentation
     * @param forceSingleLine enforces writing the comment in a row
     */
    public static void appendCommentsOrWhitespace(StringBuilder sb, List<PlComment> comments, PlComment.Position position, FormatStyle style, int level,
            boolean forceSingleLine) {
        space(sb);
        if (CommentUtils.appendComments(sb, comments, position, style, level, forceSingleLine)) {
            space(sb);
        }
    }

    private static String createIncompatiblePositionsMessage(PhysicalCommentPosition physicalPosition, PlMatchOperator ctxOperator) {
        return String.format("The pysical comment position %s is incompatible to any relative position of match operator %s.", physicalPosition, ctxOperator);
    }

    /**
     * This method translates physical comment position from a parsed expression into positions relative to the given match operator
     * 
     * @param physicalPosition
     * @param ctxOperator the operator where a comment occurred
     * @return relative position
     */
    // This method is rather a decision table and in this form easier to read than broken down into smaller methods
    @SuppressWarnings("java:S3776")
    public static Position translatePhysicalToRelativeCommentPosition(PhysicalCommentPosition physicalPosition, PlMatchOperator ctxOperator) {
        switch (ctxOperator) {
        case ANY_OF: {
            switch (physicalPosition) {
            case AFTER_ARG_NAME:
                return C1;
            case AFTER_ANY:
                return C2;
            case AFTER_OF:
                return C3;
            // $CASES-OMITTED$
            default:
                throw new IllegalArgumentException(createIncompatiblePositionsMessage(physicalPosition, ctxOperator));
            }
        }
        case NOT_ANY_OF: {
            switch (physicalPosition) {
            case AFTER_ARG_NAME:
                return C1;
            case AFTER_NOT:
                return C2;
            case AFTER_ANY:
                return C3;
            case AFTER_OF:
                return C4;
            // $CASES-OMITTED$
            default:
                throw new IllegalArgumentException(createIncompatiblePositionsMessage(physicalPosition, ctxOperator));
            }
        }
        case STRICT_NOT_ANY_OF: {
            switch (physicalPosition) {
            case AFTER_ARG_NAME:
                return C1;
            case AFTER_STRICT:
                return C2;
            case AFTER_NOT:
                return C3;
            case AFTER_ANY:
                return C4;
            case AFTER_OF:
                return C5;
            // $CASES-OMITTED$
            default:
                throw new IllegalArgumentException(createIncompatiblePositionsMessage(physicalPosition, ctxOperator));
            }
        }
        case CONTAINS_ANY_OF: {
            switch (physicalPosition) {
            case AFTER_ARG_NAME:
                return C1;
            case AFTER_CONTAINS:
                return C2;
            case AFTER_ANY:
                return C3;
            case AFTER_OF:
                return C4;
            // $CASES-OMITTED$
            default:
                throw new IllegalArgumentException(createIncompatiblePositionsMessage(physicalPosition, ctxOperator));
            }
        }
        case NOT_CONTAINS_ANY_OF: {
            switch (physicalPosition) {
            case AFTER_ARG_NAME:
                return C1;
            case AFTER_NOT:
                return C2;
            case AFTER_CONTAINS:
                return C3;
            case AFTER_ANY:
                return C4;
            case AFTER_OF:
                return C5;
            // $CASES-OMITTED$
            default:
                throw new IllegalArgumentException(createIncompatiblePositionsMessage(physicalPosition, ctxOperator));
            }
        }
        case STRICT_NOT_CONTAINS_ANY_OF: {
            switch (physicalPosition) {
            case AFTER_ARG_NAME:
                return C1;
            case AFTER_STRICT:
                return C2;
            case AFTER_NOT:
                return C3;
            case AFTER_CONTAINS:
                return C4;
            case AFTER_ANY:
                return C5;
            case AFTER_OF:
                return C6;
            // $CASES-OMITTED$
            default:
                throw new IllegalArgumentException(createIncompatiblePositionsMessage(physicalPosition, ctxOperator));
            }
        }
        case BETWEEN: {
            switch (physicalPosition) {
            case AFTER_ARG_NAME:
                return C1;
            case AFTER_BETWEEN:
                return C2;
            // $CASES-OMITTED$
            default:
                throw new IllegalArgumentException(createIncompatiblePositionsMessage(physicalPosition, ctxOperator));
            }
        }
        case NOT_CONTAINS: {
            switch (physicalPosition) {
            case AFTER_ARG_NAME:
                return C1;
            case AFTER_NOT:
                return C2;
            case AFTER_CONTAINS:
                return BEFORE_OPERAND;
            // $CASES-OMITTED$
            default:
                throw new IllegalArgumentException(createIncompatiblePositionsMessage(physicalPosition, ctxOperator));
            }
        }
        case STRICT_NOT_CONTAINS: {
            switch (physicalPosition) {
            case AFTER_ARG_NAME:
                return C1;
            case AFTER_STRICT:
                return C2;
            case AFTER_NOT:
                return C3;
            case AFTER_CONTAINS:
                return BEFORE_OPERAND;
            // $CASES-OMITTED$
            default:
                throw new IllegalArgumentException(createIncompatiblePositionsMessage(physicalPosition, ctxOperator));
            }
        }
        case NOT_BETWEEN: {
            switch (physicalPosition) {
            case AFTER_ARG_NAME:
                return C1;
            case AFTER_NOT:
                return C2;
            case AFTER_BETWEEN:
                return C3;
            // $CASES-OMITTED$
            default:
                throw new IllegalArgumentException(createIncompatiblePositionsMessage(physicalPosition, ctxOperator));
            }
        }
        case STRICT_NOT_BETWEEN: {
            switch (physicalPosition) {
            case AFTER_ARG_NAME:
                return C1;
            case AFTER_STRICT:
                return C2;
            case AFTER_NOT:
                return C3;
            case AFTER_BETWEEN:
                return C4;
            // $CASES-OMITTED$
            default:
                throw new IllegalArgumentException(createIncompatiblePositionsMessage(physicalPosition, ctxOperator));
            }
        }
        case STRICT_NOT_EQUALS: {
            switch (physicalPosition) {
            case AFTER_ARG_NAME:
                return C1;
            case AFTER_STRICT:
                return C2;
            case AFTER_OPERATOR:
                return BEFORE_OPERAND;
            // $CASES-OMITTED$
            default:
                throw new IllegalArgumentException(createIncompatiblePositionsMessage(physicalPosition, ctxOperator));
            }
        }
        case IS_UNKNOWN: {
            switch (physicalPosition) {
            case AFTER_ARG_NAME:
                return C1;
            case AFTER_IS:
                return C2;
            // $CASES-OMITTED$
            default:
                throw new IllegalArgumentException(createIncompatiblePositionsMessage(physicalPosition, ctxOperator));
            }
        }
        case IS_NOT_UNKNOWN: {
            switch (physicalPosition) {
            case AFTER_ARG_NAME:
                return C1;
            case AFTER_IS:
                return C2;
            case AFTER_NOT:
                return C3;
            // $CASES-OMITTED$
            default:
                throw new IllegalArgumentException(createIncompatiblePositionsMessage(physicalPosition, ctxOperator));
            }
        }
        case EQUALS, NOT_EQUALS, CONTAINS, LESS_THAN, LESS_THAN_OR_EQUALS, GREATER_THAN, GREATER_THAN_OR_EQUALS:
            return (physicalPosition == PhysicalCommentPosition.AFTER_ARG_NAME ? C1 : BEFORE_OPERAND);
        }
        throw new IllegalArgumentException(createIncompatiblePositionsMessage(physicalPosition, ctxOperator));
    }

    private CommentUtils() {
        // utility
    }

    /**
     * State during tokenization to keep the complexity of the tokenizer manageable
     */
    private static class TokenizerState {

        int srcIdx;

        List<String> tokens = new ArrayList<>();

        boolean insideDoubleQuotes;

        int tokenStartIdx;

    }

    /**
     * State while printing a comment on multiple lines, this class reduces the complexity of the printer
     */
    private static class MultiLineCommentOutputState {

        List<String> tokens;

        int currentLineLength = 0;
        String currentToken = null;
        StringBuilder sb;
        int currentTokenLength = 0;
        boolean needSpaceBeforeToken;

        FormatStyle style;
        int level;

    }

    /**
     * Indicates the exact position of an internal comment in a formatted expression related to its tokens.
     * <p>
     * It is always <i>after</i> a token reflecting the left-to-right formatting approach.
     */
    public enum PhysicalCommentPosition {

        AFTER_ARG_NAME, AFTER_NOT, AFTER_STRICT, AFTER_CONTAINS, AFTER_BETWEEN, AFTER_IS, AFTER_ANY, AFTER_OF, AFTER_OPERATOR;
    }

}

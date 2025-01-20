//@formatter:off
/*
 * ParentAwareExpressionNode
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

package de.calamanari.adl.irl;

import java.util.ArrayList;
import java.util.List;

import de.calamanari.adl.CombinedExpressionType;
import de.calamanari.adl.FormatConstants;

/**
 * The same expression (even same object instance) can occur multiple times inside another expression.<br>
 * This makes it hard to identify and compare parents in a reliable manner.
 * <p>
 * The {@link ParentAwareExpressionNode} inverts the expression DAG, so we can navigate from any {@link SimpleExpression} to the root.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public record ParentAwareExpressionNode(ParentAwareExpressionNode parentNode, CoreExpression expression) {

    /**
     * Recursively (top-down) builds the <i>inverted</i> expression graph with paths from each leaf to the root
     * 
     * @param parentNode current parent, initially null (root does not have any parent)
     * @param expression to be inverted
     * @param result to collect the leafs
     */
    private static void collectSimpleExpressionNodes(ParentAwareExpressionNode parentNode, CoreExpression expression, List<ParentAwareExpressionNode> result) {
        if (expression instanceof CombinedExpression cmb) {
            ParentAwareExpressionNode currentNode = new ParentAwareExpressionNode(parentNode, expression);
            cmb.members().forEach(member -> collectSimpleExpressionNodes(currentNode, member, result));
        }
        else {
            result.add(new ParentAwareExpressionNode(parentNode, expression));
        }

    }

    /**
     * Returns a list with an entry for each leaf in the given expression back to the expression root.
     * 
     * @param rootExpression
     */
    public static List<ParentAwareExpressionNode> collectLeafNodes(CoreExpression rootExpression) {
        List<ParentAwareExpressionNode> res = new ArrayList<>();
        collectSimpleExpressionNodes(null, rootExpression, res);
        return res;
    }

    /**
     * Returns the nearest common parent of two sub-expression when walking the tree towards the root.
     * 
     * @param other
     * @return closest common parent or null if there is no common parent (e.g., other == this or other is a parent of this or this is a parent of other)
     */
    public ParentAwareExpressionNode findNearestCommonParent(ParentAwareExpressionNode other) {
        // By intention we use object identity below
        if (this.equals(other) || this.parentNode == null || other.parentNode == null || this == other.parentNode || other == this.parentNode) {
            return null;
        }
        ParentAwareExpressionNode left = this.parentNode;
        while (left != null) {
            ParentAwareExpressionNode right = other.parentNode;
            while (right != null) {
                if (left == right) {
                    return left;
                }
                right = right.parentNode;
            }
            left = left.parentNode;
        }
        return null;
    }

    /**
     * Determines if two sub-expression have a common parent of type {@link CombinedExpressionType#AND}
     * 
     * @param other
     * @return true if this expression and the other have a common parent of type AND
     */
    public boolean hasCommonAndParentWith(ParentAwareExpressionNode other) {
        ParentAwareExpressionNode nearestCommonParent = findNearestCommonParent(other);
        return nearestCommonParent != null && nearestCommonParent.expression() instanceof CombinedExpression cmb
                && cmb.combiType() == CombinedExpressionType.AND;
    }

    /**
     * For printing the path in the reverse order (top-down)
     * 
     * @param sb
     * @param depth
     */
    private void appendStringIndent(StringBuilder sb, int depth) {

        sb.append(FormatConstants.LINE_BREAK);
        for (int i = 0; i < depth; i++) {
            sb.append(FormatConstants.DEFAULT_INDENT);
        }
        sb.append("+ ");
        sb.append(expression.toString());
    }

    /**
     * For printing the path in the reverse order (top-down)
     * 
     * @param sb
     * @param depth
     * @return maximum depth to calculate the indentation
     */
    private int appendString(StringBuilder sb, int depth) {

        if (parentNode == null) {
            sb.append(expression.toString());
            return depth;
        }
        else {
            int maxDepth = parentNode.appendString(sb, depth + 1);
            appendStringIndent(sb, maxDepth - depth);
            return maxDepth;
        }

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        appendString(sb, 0);
        return sb.toString();
    }

}
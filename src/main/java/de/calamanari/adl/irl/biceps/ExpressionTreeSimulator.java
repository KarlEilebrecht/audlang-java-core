//@formatter:off
/*
 * ExpressionTreeSimulator
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

package de.calamanari.adl.irl.biceps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.calamanari.adl.FormatUtils;
import de.calamanari.adl.irl.CoreExpression;

import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.ALL;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.INVALID;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.NONE;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.getNodeType;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.isSpecialSet;

/**
 * The {@link ExpressionTreeSimulator} allows a <b>brute-force simulation</b> (truth-table) of one or multiple expressions (side-by-side).
 * <p>
 * For very complex expressions with many conditions this kind of simulation may be slow or even infeasible. <br>
 * However, in many cases the simulator allows to check whether an expression after some transformations still behaves in the same way.
 * <p>
 * <b>Important:</b> Meant solely for testing and debugging, this implementation has <b>no built-in protection from combinatoric runaway</b> and can thus
 * produce {@link OutOfMemoryError}s.
 * 
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class ExpressionTreeSimulator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpressionTreeSimulator.class);

    /**
     * Returns a simulation report for the given expression
     * 
     * @param expression
     * @return report
     */
    public String simulate(CoreExpression expression) {
        return simulate(EncodedExpressionTree.fromCoreExpression(expression));
    }

    /**
     * Returns a simulation report for the given tree
     * 
     * @param tree
     * @return report
     */
    public String simulate(EncodedExpressionTree tree) {

        int[] leaves = collectAllLeaves(tree);

        int[] complementLeaves = createLeafComplements(tree, leaves);

        List<TestRun> results = new ArrayList<>();

        simulate(tree, leaves, complementLeaves, 0, new BitSet(leaves.length), results, new int[leaves.length]);

        return createReport(tree, leaves, results);
    }

    /**
     * Creates a simulation report comparing the simulations of the two given expressions
     * 
     * @param left
     * @param right
     * @return report
     */
    public String simulateComparison(CoreExpression left, CoreExpression right) {
        return simulate(EncodedExpressionTree.fromCoreExpression(left).merge(EncodedExpressionTree.fromCoreExpression(right)));
    }

    /**
     * Creates a simulation report comparing the simulations of the two given trees
     * 
     * @param leftTree
     * @param rightTree
     * @return
     */
    public String simulateComparison(EncodedExpressionTree leftTree, EncodedExpressionTree rightTree) {
        return simulate(leftTree.merge(rightTree));
    }

    /**
     * Produces the summary for console or file output
     * 
     * @param tree
     * @param leaves
     * @param results
     * @return textual summary
     */
    private String createReport(EncodedExpressionTree tree, int[] leaves, List<TestRun> results) {
        int[] rootNodes = tree.getRootLevel().members().toArray();

        StringBuilder sb = new StringBuilder();
        for (int idx = 0; idx < rootNodes.length; idx++) {
            sb.append("Simulated expression ");
            sb.append("E");
            sb.append(idx + 1);
            sb.append(": ");
            sb.append(tree.createCoreExpression(rootNodes[idx]));
            sb.append("\n");

        }
        sb.append("\n\n");

        boolean differencesFound = checkForDifferentResults(tree, results);

        if (differencesFound) {
            sb.append("(*) Differences detected.\n\n");
        }
        else if (rootNodes.length > 1) {
            sb.append("No differences detected.\n\n");
        }

        if (!differencesFound) {
            Result allSame = checkForAllSameResult(results);
            if (allSame == Result.TRUE) {
                sb.append("Expression is always true (<ALL>).\n\n");
            }
            else if (allSame == Result.FALSE) {
                sb.append("Expression is always false (<NONE>).\n\n");
            }
        }

        sb.append("Conditions:\n\n");
        for (int i = 0; i < leaves.length; i++) {
            FormatUtils.appendAlignRight(sb, String.valueOf(i), 2);
            sb.append(" : ");
            sb.append(tree.createDebugString(leaves[i]));
            sb.append("\n");
        }
        sb.append("\n");
        sb.append("Results:");
        sb.append("\n\n");
        for (int i = 0; i < leaves.length; i++) {
            FormatUtils.appendAlignCenter(sb, String.valueOf(i), 3);
        }
        sb.append(" ");
        for (int i = 0; i < rootNodes.length; i++) {
            FormatUtils.appendAlignCenter(sb, "E" + (i + 1), 7);
        }

        sb.append("\n");
        for (int i = 0; i < leaves.length; i++) {
            sb.append("---");
        }
        sb.append("-");
        for (int i = 0; i < rootNodes.length; i++) {
            sb.append("-------");
        }

        sb.append("\n");

        for (TestRun run : results) {
            sb.append(run.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * This method left-to-right creates the setups and triggers the simulation. The setupVector contains one bit per available condition, so one test case is a
     * unique sequence of 1s and 0s.
     * <p>
     * When all conditions have been added (either yes or no), then we do the simulation and add the result to the collection.
     * 
     * @param tree
     * @param leaves
     * @param complementLeaves
     * @param idx
     * @param setupVector
     * @param results
     * @param assumptions temp array, basically avoids creating this array over and over again
     */
    private void simulate(EncodedExpressionTree tree, int[] leaves, int[] complementLeaves, int idx, BitSet setupVector, List<TestRun> results,
            int[] assumptions) {
        if (idx < leaves.length) {
            simulate(tree, leaves, complementLeaves, idx + 1, setupVector, results, assumptions);
            setupVector.set(idx, true);
            simulate(tree, leaves, complementLeaves, idx + 1, setupVector, results, assumptions);
            setupVector.set(idx, false);
        }
        else {
            TestRun run = simulate(tree, leaves, complementLeaves, setupVector, assumptions);
            if (run.results[0] != Result.NA) {
                results.add(run);
            }
        }
    }

    /**
     * This is the heart of the simulation, for each bin in the setup-vector we either assume one of the given conditions or its exact complement.
     * <p>
     * Of course, it can happen that the assumptions contain contradictions which invalidates the particular setup. In this case we mark the corresponding run
     * as <i>not applicable</i> ({@link Result#NA}).
     * 
     * @param tree
     * @param leaves
     * @param complementLeaves
     * @param setupVector
     * @param assumptions array for the assumptions for a run
     * @return test result
     */
    private TestRun simulate(EncodedExpressionTree tree, int[] leaves, int[] complementLeaves, BitSet setupVector, int[] assumptions) {

        for (int idx = 0; idx < leaves.length; idx++) {
            if (setupVector.get(idx)) {
                assumptions[idx] = leaves[idx];
            }
            else {
                assumptions[idx] = complementLeaves[idx];
            }
        }

        GrowingIntArray rootNodes = tree.getRootLevel().members();
        Result[] results = new Result[rootNodes.size()];
        TestRun res = null;
        if (haveContradictingAssumptions(tree, assumptions)) {
            Arrays.fill(results, Result.NA);
            res = new TestRun((BitSet) setupVector.clone(), leaves.length, results);
        }
        else {
            Arrays.sort(assumptions);
            for (int idx = 0; idx < rootNodes.size(); idx++) {
                if (evaluate(tree, rootNodes.get(idx), assumptions)) {
                    results[idx] = Result.TRUE;
                }
                else {
                    results[idx] = Result.FALSE;
                }
            }
            res = new TestRun((BitSet) setupVector.clone(), leaves.length, results);
        }

        LOGGER.trace("\n{}", res);
        return res;

    }

    /**
     * Performs the evaluation against the assumptions top-down-bottom-up. The trick is that we don't need any complex logic check anymore, it is sufficient to
     * check if a particular condition is part of the assumptions or not.
     * 
     * @param tree
     * @param node
     * @param assumptions
     * @return true if the conditions of the node are fulfilled by the assumptions
     */
    private boolean evaluate(EncodedExpressionTree tree, int node, int[] assumptions) {
        if (node == ALL) {
            return true;
        }
        else if (node == NONE) {
            return false;
        }
        switch (getNodeType(node)) {
        case AND:
            return evaluateAnd(tree, node, assumptions);
        case OR:
            return evaluateOr(tree, node, assumptions);
        // $CASES-OMITTED$
        default:
            return (Arrays.binarySearch(assumptions, node) > -1);
        }
    }

    /**
     * @param tree
     * @param node
     * @param assumptions
     * @return true if <i>all</i> the members of the given AND are fulfilled by the assumptions
     */
    private boolean evaluateAnd(EncodedExpressionTree tree, int node, int[] assumptions) {
        for (int member : tree.membersOf(node)) {
            if (!evaluate(tree, member, assumptions)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param tree
     * @param node
     * @param assumptions
     * @return true if <i>any</i> of the members of the given OR is fulfilled by the assumptions
     */
    private boolean evaluateOr(EncodedExpressionTree tree, int node, int[] assumptions) {
        for (int member : tree.membersOf(node)) {
            if (evaluate(tree, member, assumptions)) {
                return true;
            }
        }
        return false;
    }

    /**
     * As explained during the setup we cannot avoid creating <i>impossible</i> setups. This method ensures that we detect and skip these setup.
     * 
     * @param tree
     * @param assumptions
     * @return true if the setup is impossible
     */
    private boolean haveContradictingAssumptions(EncodedExpressionTree tree, int[] assumptions) {
        ImplicationResolver implicationResolver = new ImplicationResolver(null);
        int andNode = tree.createNode(NodeType.AND, assumptions);

        andNode = implicationResolver.cleanupImplications(tree, andNode);
        return (andNode == NONE);
    }

    /**
     * For the setup we need <i>complements</i> of all the conditions, so we can later detect contradictions among the assumptions.
     * 
     * @param tree
     * @param cache
     * @param leaves
     * @return array with all the complements corresponding to the given leaves
     */
    private int[] createLeafComplements(EncodedExpressionTree tree, int[] leaves) {
        ImplicationResolver implicationResolver = new ImplicationResolver(null);
        int[] res = new int[leaves.length];
        for (int idx = 0; idx < leaves.length; idx++) {
            int complement = implicationResolver.cleanupImplications(tree, tree.getLogicHelper().createComplementOf(tree, leaves[idx]), false);
            res[idx] = complement;
        }

        return res;
    }

    /**
     * Tells whether any of the test runs produces different results (in case of multiple roots)
     * 
     * @param tree
     * @param results
     * @return true if the evaluation of two given expressions lead to different outcomes
     */
    private boolean checkForDifferentResults(EncodedExpressionTree tree, List<TestRun> results) {
        if (tree.getRootLevel().members().size() > 1) {
            for (TestRun run : results) {
                if (run.containsDifferentResults()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Tells whether all of the results are either true or all false
     * 
     * @param results
     * @return {@link Result#TRUE} if all results were true, {@link Result#FALSE} if all were false, {@link Result#NA} otherwise (mixed results)
     */
    private Result checkForAllSameResult(List<TestRun> results) {
        Result res = Result.NA;
        for (int i = 0; i < results.size(); i++) {
            Result runResult = results.get(i).results[0];
            if (runResult != Result.NA) {
                if (res == Result.NA) {
                    res = runResult;
                }
                else if (res != runResult) {
                    return Result.NA;
                }
            }
        }
        return res;
    }

    /**
     * This method returns all the leaves in the tree, considering multiple roots if required
     * 
     * @param tree
     * @return array with all the leaves (conditions)
     */
    private int[] collectAllLeaves(EncodedExpressionTree tree) {
        ExpressionTreeLevel rootLevel = tree.getRootLevel();
        if (rootLevel.members().isEmpty()) {
            throw new IllegalStateException("Tree has no root.");
        }

        GrowingIntArray leaves = new GrowingIntArray();
        for (int idx = 0; idx < rootLevel.members().size(); idx++) {
            int root = rootLevel.members().get(idx);
            leaves.addAll(tree.collectLeaves(root));
        }
        for (int idx = 0; idx < leaves.size(); idx++) {
            if (isSpecialSet(leaves.get(idx))) {
                leaves.set(idx, INVALID);
            }
        }

        return MemberUtils.sortDistinctMembers(leaves.toArray(), false);
    }

    /**
     * A {@link TestRun} covers the setup of a test plus the result(s). The size of the results array depends on the number expressions we compare.
     */
    public static record TestRun(BitSet setupVector, int size, Result[] results) {

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder((size * 3) + 10);

            for (int i = 0; i < size; i++) {
                sb.append(setupVector.get(i) ? " 1 " : " 0 ");
            }

            sb.append("| ");
            for (Result result : results) {
                FormatUtils.appendAlignLeft(sb, result.name(), 7);
            }
            if (containsDifferentResults()) {
                sb.append(" *");
            }
            return sb.toString();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(results);
            result = prime * result + Objects.hash(setupVector, size);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            TestRun other = (TestRun) obj;
            return Arrays.equals(results, other.results) && Objects.equals(setupVector, other.setupVector) && size == other.size;
        }

        /**
         * @return true if multiple expressions have been tested and if there were different outcomes
         */
        public boolean containsDifferentResults() {
            Result prevResult = null;
            for (Result result : results) {
                if (prevResult != null && result != prevResult) {
                    return true;
                }
                prevResult = result;
            }
            return false;
        }

    }

    /**
     * To avoid null, this enum introduces a third value <i>not applicable</i> for impossible test setups.
     */
    public enum Result {

        TRUE, FALSE, NA;

    }
}

//@formatter:off
/*
 * IsUnknownRemovalConverter
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

import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.ALL;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.NONE;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.getNodeType;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.isCombinedExpressionId;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.isNegatedUnknown;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.isSpecialSet;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.isUnknown;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.calamanari.adl.TimeOut;
import de.calamanari.adl.cnv.tps.ArgMetaInfoLookup;
import de.calamanari.adl.irl.CoreExpression;
import de.calamanari.adl.irl.biceps.CoreExpressionOptimizer;
import de.calamanari.adl.irl.biceps.EncodedExpressionTree;
import de.calamanari.adl.irl.biceps.ImplicationResolver;
import de.calamanari.adl.irl.biceps.MemberUtils;

/**
 * This converter allows to streamline an expression once we have the information if arguments can be UNKNOWN or not. <br>
 * We eliminate useless IS UNKNOWN checks accordingly and try to optimize the expression again.
 * <p>
 * <b>Background:</b> The information whether an argument can be UNKNOWN (aka is <i>nullable</i>) is part of the physical data storage layer, and not
 * necessarily part of the logical data model. This means, this information may become available relatively late while processing a request. This converter
 * allows to react and potentially simplify an expression before executing it.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class IsUnknownRemovalConverter implements ExpressionConverter<CoreExpression, CoreExpression> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IsUnknownRemovalConverter.class);

    private final Function<String, Boolean> argumentIsUnknownSupportCheck;

    private final TimeOut timeout;

    /**
     * @param argumentIsUnknownSupportCheck function that returns true if a given argument name can be UNKNOWN
     * @param timeout if null we will use the default: {@link TimeOut#createDefaultTimeOut(String)}
     */
    public IsUnknownRemovalConverter(Function<String, Boolean> argumentIsUnknownSupportCheck, TimeOut timeout) {
        this.argumentIsUnknownSupportCheck = argumentIsUnknownSupportCheck;
        this.timeout = timeout == null ? TimeOut.createDefaultTimeOut(ImplicationResolver.class.getSimpleName()) : timeout;
    }

    /**
     * Creates instance with default timeout
     * 
     * @param argumentIsUnknownSupportCheck function that returns true if a given argument name can be UNKNOWN
     */
    public IsUnknownRemovalConverter(Function<String, Boolean> argumentIsUnknownSupportCheck) {
        this(argumentIsUnknownSupportCheck, null);
    }

    /**
     * Creates instance with default timeout
     * 
     * @param argMetaLookup to obtain the meta data about the argument to be tested if it can be UNKNOWN or not
     */
    public IsUnknownRemovalConverter(ArgMetaInfoLookup argMetaLookup) {
        this(argName -> !argMetaLookup.isAlwaysKnown(argName));
    }

    /**
     * Creates an instance with based on the given list of arguments that support UNKNOWN.<br>
     * All <i>other</i> argument names are supposed to <i>always</i> have a value.
     * <p>
     * In terms of SQL: <b>all other fields</b> in the underlying data store are <b>not nullable</b>.
     * 
     * @param argNamesThatSupportIsUnknown (null means no argument supports UNKNOWN)
     */
    public IsUnknownRemovalConverter(List<String> argNamesThatSupportIsUnknown) {
        this(createChecker(argNamesThatSupportIsUnknown));
    }

    /**
     * Creates a boolean check function based on the given list
     * 
     * @param argNamesThatSupportIsUnknown
     * @return checker function
     */
    private static Function<String, Boolean> createChecker(List<String> argNamesThatSupportIsUnknown) {
        if (argNamesThatSupportIsUnknown == null || argNamesThatSupportIsUnknown.isEmpty()) {
            return _ -> Boolean.FALSE;
        }
        Set<String> set = new HashSet<>(argNamesThatSupportIsUnknown);
        return set::contains;
    }

    /**
     * @param argName
     * @return true if the argument does not support UNKNOWN, so there will always be a value
     */
    private boolean isAlwaysKnown(String argName) {
        return !this.argumentIsUnknownSupportCheck.apply(argName);
    }

    @Override
    public CoreExpression convert(CoreExpression source) {

        CoreExpression res = source;

        EncodedExpressionTree tree = EncodedExpressionTree.fromCoreExpression(source);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("convert BEFORE: {}", source);
        }

        int rootNode = tree.getRootNode();
        int rootNodeUpd = convert(tree, rootNode);

        if (rootNodeUpd != rootNode) {
            tree.setRootNode(rootNodeUpd);
            CoreExpressionOptimizer optimizer = new CoreExpressionOptimizer(timeout);
            optimizer.process(tree);
            res = tree.createCoreExpression(tree.getRootNode());
        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("convert AFTER: {}{}", ((res.equals(source)) ? "*" : " "), res);
        }

        return res;
    }

    /**
     * Recursively tries to simplify the expression by eliminating IS UNKNOWN checks which are either always true or always false
     * 
     * @param tree
     * @param node
     * @return node or replacement
     */
    private int convert(EncodedExpressionTree tree, int node) {
        if (isCombinedExpressionId(node)) {
            return convertCombinedNode(tree, node);
        }
        else if (!isSpecialSet(node)) {
            if (isUnknown(node) && isAlwaysKnown(tree.getCodec().getArgName(node))) {
                return NONE;
            }
            else if (isNegatedUnknown(node) && isAlwaysKnown(tree.getCodec().getArgName(node))) {
                return ALL;
            }
        }
        return node;
    }

    /**
     * Analyzes and re-composes the given combined expression (AND/OR)
     * 
     * @param tree
     * @param node
     * @return node or replacement
     */
    private int convertCombinedNode(EncodedExpressionTree tree, int node) {
        int[] membersUpd = MemberUtils.EMPTY_MEMBERS;
        int[] members = tree.membersOf(node);
        for (int idx = 0; idx < members.length; idx++) {
            int member = members[idx];
            int memberUpd = convert(tree, member);
            if (memberUpd != member) {
                membersUpd = (membersUpd == MemberUtils.EMPTY_MEMBERS) ? Arrays.copyOf(members, members.length) : membersUpd;
                membersUpd[idx] = memberUpd;
            }
        }
        if (membersUpd != MemberUtils.EMPTY_MEMBERS) {
            return tree.createNode(getNodeType(node), membersUpd);
        }
        return node;
    }

}

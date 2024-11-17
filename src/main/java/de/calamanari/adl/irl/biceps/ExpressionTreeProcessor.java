//@formatter:off
/*
 * ExpressionTreeProcessor
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

/**
 * An {@link ExpressionTreeProcessor} takes a given tree to transform it. The result is a new root node.
 * <p/>
 * <b>Important:</b> The transformation may run on the entire tree. Thus, any node that was previously valid may be invalidated, and new nodes may be created.
 * Consequently, callers should not keep references to any tree nodes created <i>before</i> calling {@link #process(EncodedExpressionTree)}.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public interface ExpressionTreeProcessor {

    /**
     * Runs the transformation and potentially replaces the root node with a new one.
     * 
     * @param tree to be transformed
     */
    void process(EncodedExpressionTree tree);
}

//@formatter:off
/*
 * CombinedExpression
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

package de.calamanari.adl.irl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import de.calamanari.adl.AudlangField;
import de.calamanari.adl.AudlangValidationException;
import de.calamanari.adl.CombinedExpressionType;
import de.calamanari.adl.FormatStyle;
import de.calamanari.adl.FormatUtils.FormatInfo;
import de.calamanari.adl.Visit;

import static de.calamanari.adl.FormatUtils.appendCombinedExpressionMultiLine;
import static de.calamanari.adl.FormatUtils.appendCombinedExpressionSingleLine;

/**
 * A {@link CombinedExpression} connects at least two expressions to form a new one either with {@link CombinedExpressionType#OR} or
 * {@link CombinedExpressionType#AND}
 * <p>
 * Other than on the presentation layer this type of combined core expressions is standardized:
 * <ul>
 * <li>Nested expressions of the same combination type <b>auto-collapse</b>: <code>(a = 1 OR b = 2) OR c = 3</code> turns into
 * <code>a = 1 OR b = 2 OR c = 3</code></li>
 * <li>There cannot be two identical members.</li>
 * <li>{@link CombinedExpression}s never contain members of type {@link SpecialSetExpression}. Instead they collapse or they get filtered<br>
 * Example 1: <code>a = 1 OR &lt;NONE&gt;</code> results in <code>a = 1</code><br>
 * Example 2: <code>a = 1 OR &lt;ALL&gt;</code> results in <code>&lt;ALL&gt;</code><br>
 * Example 1: <code>a = 1 AND &lt;NONE&gt;</code> results in <code>&lt;NONE&gt;</code><br>
 * Example 2: <code>a = 1 AND &lt;ALL&gt;</code> results in <code>a = 1</code></li>
 * </ul>
 * <p>
 * See also <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#41-logical-and">§4.1</a>,
 * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#42-logical-or">§4.2</a> Audlang Spec
 * 
 * @param combiType logical connector
 * @param members the elements inside, list must at least contain two elements
 * @param inline this is an internal value computed by the constructor, no matter what you specify here, it will be ignored
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public record CombinedExpression(CombinedExpressionType combiType, List<CoreExpression> members, String inline) implements CoreExpression {

    private static final String WARNING = "Please avoid calling the CombinedExpression constructor directly. Instead use one of the static creation methods (e.g., orOf(...)).";

    /**
     * @param combiType logical connector
     * @param members the elements inside, list must at least contain two elements
     * @param inline this is an internal value computed by the constructor, no matter what you specify here, it will be ignored
     */
    public CombinedExpression(CombinedExpressionType combiType, List<CoreExpression> members, @SuppressWarnings("java:S1172") String inline) {
        if (combiType == null) {
            throw new AudlangValidationException(String.format("combiType must not be null, given: members=%s, combiType=%s", members, combiType));
        }

        if (members == null || members.size() < 2) {
            throw new AudlangValidationException(String
                    .format("AND- resp. OR-expressions must at least have two members, given: members=%s, combiType=%s, %n%s", members, combiType, WARNING));
        }
        if (!validateMembers(members, combiType)) {
            throw new AudlangValidationException(String.format(
                    "AND- resp. OR-expressions must have UNIQUE and ORDERED members, given: members=%s, combiType=%s, %n%s", members, combiType, WARNING));
        }
        this.members = Collections.unmodifiableList(new ArrayList<>(members));
        this.combiType = combiType;
        this.inline = format(FormatStyle.INLINE);
    }

    /**
     * Creates a combined expression (or a simpler one) based on the given list of expression. The exact return type depends on the outcome of internal
     * simplification (e.g. <b><code>A=1 AND A=1</code></b> would result in <b><code>A=1</code></b>.
     * 
     * 
     * @param members
     * @param combiType
     * @return combined expression or anything simpler if detected
     */
    public static CoreExpression of(List<CoreExpression> members, CombinedExpressionType combiType) {
        if (combiType == null) {
            throw new AudlangValidationException(String.format("combiType must not be null, given: members=%s, combiType=%s", members, combiType));
        }

        if (members == null || members.isEmpty()) {
            throw new AudlangValidationException(
                    String.format("AND- resp. OR-expressions must have members, given: members=%s, combiType=%s", members, combiType));
        }

        List<CoreExpression> preparedMembers = prepareMembers(members, combiType);

        if (preparedMembers.size() == 1) {
            return preparedMembers.get(0);
        }
        else {
            return new CombinedExpression(combiType, preparedMembers, null);
        }

    }

    /**
     * Performs trivial checks to prevent abnormal combinations and duplicates
     * 
     * @param members any members to be joined
     * @param combiType
     * @return clean list of members
     */
    private static List<CoreExpression> prepareMembers(List<CoreExpression> members, CombinedExpressionType combiType) {

        for (CoreExpression member : members) {
            if (SpecialSetExpression.none().equals(member) && combiType == CombinedExpressionType.AND) {
                return Arrays.asList(SpecialSetExpression.none());
            }
            else if (SpecialSetExpression.all().equals(member) && combiType == CombinedExpressionType.OR) {
                return Arrays.asList(SpecialSetExpression.all());
            }
        }

        CoreExpression prevMember = null;
        for (CoreExpression member : members) {
            if (member instanceof SpecialSetExpression || (prevMember != null && prevMember.compareTo(member) >= 0)
                    || (member instanceof CombinedExpression cmb && cmb.combiType == combiType)) {
                List<CoreExpression> updatedMembers = new ArrayList<>(members);
                sortFilterExpandMembers(updatedMembers, combiType);
                return prepareMembersConsiderEmpty(updatedMembers, combiType);
            }
            prevMember = member;
        }
        return prepareMembersConsiderEmpty(members, combiType);

    }

    /**
     * Due to rules the list of members could be empty after preparation (e.g. <code>&lt;NONE&gt; OR &lt;NONE&gt;</code>). In this case we apply the following
     * semantics:
     * <ul>
     * <li>An empty OR means <i>no option</i>, means &lt;NONE&gt;</li>
     * <li>An empty AND meands <i>no restriction</i>, means &lt;ALL&gt;</li>
     * </ul>
     * 
     * @param members
     * @param combiType
     * @return list of members
     */
    private static List<CoreExpression> prepareMembersConsiderEmpty(List<CoreExpression> members, CombinedExpressionType combiType) {
        if (members.isEmpty() && combiType == CombinedExpressionType.AND) {
            return Arrays.asList(SpecialSetExpression.all());
        }
        else if (members.isEmpty() && combiType == CombinedExpressionType.OR) {
            return Arrays.asList(SpecialSetExpression.none());
        }
        else {
            return members;
        }
    }

    /**
     * This method ensures that the members in the given list are unique and sorted. It also auto-expands any combined member of the same type to prevent
     * AND-of-AND resp. OR-of-OR to avoid confusion. Here we also get rid of useless {@link SpecialSetExpression} members.
     * 
     * @param members
     * @param combiType
     */
    private static void sortFilterExpandMembers(List<CoreExpression> members, CombinedExpressionType combiType) {

        List<CoreExpression> res = new ArrayList<>(members.size());
        Collections.sort(members);
        CoreExpression prevMember = null;
        boolean expanded = false;
        for (CoreExpression member : members) {
            if (!(member instanceof SpecialSetExpression) && !member.equals(prevMember)) {
                if (member instanceof CombinedExpression cmb && cmb.combiType == combiType) {
                    expanded = true;
                    res.addAll(cmb.members());
                }
                else {
                    res.add(member);
                }
            }
            prevMember = member;
        }
        if (expanded) {
            sortFilterExpandMembers(res, combiType);
        }
        members.clear();
        members.addAll(res);
    }

    /**
     * Shorthand for creating an AND-expression from the members. Depending on the members the outcome can be any {@link CoreExpression} type
     * 
     * @param members
     * @return expression
     */
    public static CoreExpression andOf(List<CoreExpression> members) {
        return of(members, CombinedExpressionType.AND);
    }

    /**
     * Shorthand for creating an AND-expression from the members. Depending on the members the outcome can be any {@link CoreExpression} type
     * 
     * @param members
     * @return expression
     */
    public static CoreExpression andOf(CoreExpression... members) {
        List<CoreExpression> memberList = Arrays.asList(members);
        return of(memberList, CombinedExpressionType.AND);
    }

    /**
     * Shorthand for creating an OR-expression from the members. Depending on the members the outcome can be any {@link CoreExpression} type
     * 
     * @param members
     * @return expression
     */
    public static CoreExpression orOf(List<CoreExpression> members) {
        return of(members, CombinedExpressionType.OR);
    }

    /**
     * Shorthand for creating an OR-expression from the members. Depending on the members the outcome can be any {@link CoreExpression} type
     * 
     * @param members
     * @return expression
     */
    public static CoreExpression orOf(CoreExpression... members) {
        List<CoreExpression> memberList = Arrays.asList(members);
        return of(memberList, CombinedExpressionType.OR);
    }

    @Override
    public void appendSingleLine(StringBuilder sb, FormatStyle style, int level) {
        appendCombinedExpressionSingleLine(sb, combiType, members, Collections.emptyList(), new FormatInfo(style, level));
    }

    @Override
    public void appendMultiLine(StringBuilder sb, FormatStyle style, int level) {
        appendCombinedExpressionMultiLine(sb, combiType, members, Collections.emptyList(), new FormatInfo(style, level));
    }

    @Override
    public boolean enforceCompositeFormat() {
        return true;
    }

    @Override
    public List<CoreExpression> childExpressions() {
        return members;
    }

    @Override
    public void collectFieldsInternal(Map<String, AudlangField.Builder> fieldMap) {
        members.forEach(member -> member.collectFieldsInternal(fieldMap));
    }

    @Override
    public String toString() {
        return inline;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CombinedExpression cmb && cmb.inline.equals(this.inline);
    }

    @Override
    public int hashCode() {
        return inline.hashCode();
    }

    @Override
    public int compareTo(CoreExpression other) {
        if (other instanceof MatchExpression || other instanceof NegationExpression || other instanceof SpecialSetExpression) {
            // move the combined expressions after the simple ones
            return 1;
        }
        return CoreExpression.super.compareTo(other);
    }

    @Override
    public void accept(CoreExpressionVisitor visitor) {
        visitor.visit(this, Visit.ENTER);
        members.stream().forEach(m -> m.accept(visitor));
        visitor.visit(this, Visit.EXIT);
    }

    @Override
    public CoreExpression negate(boolean strict) {
        return CombinedExpression.of(members.stream().map(e -> e.negate(strict)).toList(), combiType.switchType());
    }

    /**
     * This method ensures consistency during de-serialization, it checks that the given list of members looks exactly like the one the method
     * {@link #prepareMembers(List, CombinedExpressionType)} would have produced.
     * 
     * @param members raw members
     * @param combiType type of parent expression
     * @return true if the list is ok
     */
    private static boolean validateMembers(List<CoreExpression> members, CombinedExpressionType combiType) {
        return members.equals(prepareMembers(members, combiType));
    }

}

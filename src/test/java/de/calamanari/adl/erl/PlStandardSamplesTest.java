//@formatter:off
/*
 * PlStandardSamplesTest
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.calamanari.adl.CombinedExpressionType;
import de.calamanari.adl.FormatStyle;
import de.calamanari.adl.SpecialSetType;
import de.calamanari.adl.erl.PlComment.Position;
import de.calamanari.adl.erl.PlCurbExpression.PlCurbOperator;
import de.calamanari.adl.util.AdlTextUtils;
import de.calamanari.adl.util.sgen.SampleExpression;
import de.calamanari.adl.util.sgen.SampleExpressionGroup;
import de.calamanari.adl.util.sgen.SampleExpressionOperator;
import de.calamanari.adl.util.sgen.SampleExpressionUtils;
import de.calamanari.adl.util.sgen.SampleGenInfo;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class PlStandardSamplesTest {

    static final Logger LOGGER = LoggerFactory.getLogger(PlStandardSamplesTest.class);

    @Test
    void testGeneratedStandardSamples() {

        List<SampleExpressionGroup> allGroups = generateSamples();

        assertNotNull(allGroups);
        assertFalse(allGroups.isEmpty());

        LOGGER.info("Testing {} sample groups ...", allGroups.size());

        int sampleCount = 0;
        int sampleGroupCount = 0;

        for (SampleExpressionGroup sampleGroup : allGroups) {

            LOGGER.debug("Testing sample group {} ...", sampleGroup.group());

            if (sampleGroup.skip()) {
                LOGGER.debug("Skipping group {}", sampleGroup.group());
            }
            else {
                sampleGroupCount++;
                List<SampleExpression> activeSamples = sampleGroup.samples().stream().filter(Predicate.not(sample -> sample.skip())).toList();

                LOGGER.debug("Found {} active samples ...", activeSamples.size());

                sampleCount = sampleCount + evaluateSamples(sampleGroup.group(), activeSamples, true);
                sampleCount = sampleCount + evaluateSamples(sampleGroup.group(), activeSamples, false);

                LOGGER.debug("Finished group {}.", sampleGroup.group());

            }

        }

        LOGGER.info("Successfully tested {}/{} samples in {}/{} sample groups.", sampleCount,
                allGroups.stream().map(g -> g.samples().size()).collect(Collectors.summingInt(Integer::intValue)), sampleGroupCount, allGroups.size());

    }

    private int evaluateSamples(String group, List<SampleExpression> allSamples, boolean filterValid) {
        List<SampleExpression> samples = allSamples.stream().filter(sample -> sample.invalid() != filterValid).toList();

        if (!samples.isEmpty()) {
            LOGGER.debug("Testing {} samples from group {} expected to be {}valid ...", samples.size(), group, (filterValid ? "" : "in"));
            samples.forEach(this::evaluateSample);
        }
        else {
            LOGGER.debug("No {}valid samples to test.", (filterValid ? "" : "in"));
        }
        return samples.size();
    }

    private void evaluateSample(SampleExpression sample) {

        LOGGER.debug("{}", sample.id());
        AudlangParseResult res = PlExpressionBuilder.stringToExpression(sample.expression());

        if (sample.invalid() != res.isError()) {

            LOGGER.error("ERROR: Sample {} was expected to be {}valid but was found {}valid", sample.id(), (sample.invalid() ? "in" : ""),
                    (res.isError() ? "in" : ""));
            LOGGER.error("Expression was: '''{}''', \nParse Result:\n{}", sample.expression(), res);

        }
        assertEquals(sample.invalid(), res.isError());

        if (!sample.invalid() && sample.generationInfo() != null) {
            boolean success = false;
            try {

                assertResultMatchesSampleGenInfo(sample.generationInfo(), res.getResultExpression());

                assertEquals(res.getResultExpression(),
                        PlExpressionBuilder.stringToExpression(res.getResultExpression().format(FormatStyle.INLINE)).getResultExpression());
                assertEquals(res.getResultExpression(),
                        PlExpressionBuilder.stringToExpression(res.getResultExpression().format(FormatStyle.PRETTY_PRINT)).getResultExpression());

                success = true;
            }
            finally {
                if (!success) {
                    LOGGER.error("ERROR: Sample {} was valid but parse result did not match expectations", sample.id());
                    LOGGER.error("Expression was: '''{}''', \nParse Result:\n{}", sample.expression(), res);
                }
            }
        }
    }

    private List<SampleExpressionGroup> generateSamples() {

        String templateFileName = "/samples/sample-expressions-template.json";

        try {

            List<SampleExpressionGroup> templateGroups = SampleExpressionUtils.readSampleGroupsFromJsonResource(templateFileName);

            LOGGER.info("Creating samples from template: {}", templateFileName);

            List<SampleExpressionGroup> sampleGroups = SampleExpressionUtils.generateSamples(templateGroups);

            int numberOfSamples = sampleGroups.stream().map(group -> group.samples().size()).collect(Collectors.summingInt(Integer::intValue));

            LOGGER.info("Generated {} samples within {} groups.", numberOfSamples, sampleGroups.size());

            return Collections.unmodifiableList(sampleGroups);
        }
        catch (IOException ex) {
            throw new RuntimeException("Could not load template file: " + templateFileName);
        }
    }

    private static void assertResultMatchesSampleGenInfo(SampleGenInfo expected, PlExpression<?> resultExpression) {

        expected = SampleGenInfo.createEmptyInstanceNoNulls().combine(expected);
        SampleGenInfo actual = convert(resultExpression);

        assertEquals(expected.getCntAll(), actual.getCntAll());
        assertEquals(expected.getCntAnd(), actual.getCntAnd());
        assertEquals(expected.getCntAny(), actual.getCntAny());
        assertEquals(expected.getCntBetween(), actual.getCntBetween());
        assertEquals(expected.getCntContains(), actual.getCntContains());
        assertEquals(expected.getCntCurb(), actual.getCntCurb());
        assertEquals(expected.getCntIs(), actual.getCntIs());
        assertEquals(expected.getCntUnknown(), actual.getCntUnknown());
        assertEquals(expected.getCntNone(), actual.getCntNone());
        assertEquals(expected.getCntNot(), actual.getCntNot());
        assertEquals(expected.getCntStrict(), actual.getCntStrict());
        assertEquals(expected.getCntOf(), actual.getCntOf());
        assertEquals(expected.getCntOr(), actual.getCntOr());

        assertSameTextElements(expected.getArgNames(), actual.getArgNames());
        assertSameTextElements(expected.getArgRefs(), actual.getArgRefs());
        assertSameTextElements(expected.getArgValues(), actual.getArgValues());
        assertSameElements(expected.getBoundValues(), actual.getBoundValues());
        assertSameComments(expected.getComments(), actual.getComments());
        assertSameElements(expected.getOperators(), actual.getOperators());
        assertSameTextElements(expected.getSnippets(), actual.getSnippets());
    }

    private static void assertSameComments(List<String> expected, List<String> actual) {
        assertSameElements(expected.stream().map(txt -> new PlComment(txt, Position.BEFORE_EXPRESSION)).map(PlComment::comment).toList(),
                actual.stream().map(txt -> new PlComment(txt, Position.BEFORE_EXPRESSION)).map(PlComment::comment).toList());
    }

    private static void assertSameTextElements(List<String> expected, List<String> actual) {
        assertSameElements(expected.stream().map(PlStandardSamplesTest::decodeTextForCompare).toList(), actual);
    }

    private static String decodeTextForCompare(String source) {
        return AdlTextUtils.unescapeSpecialCharacters(AdlTextUtils.removeDoubleQuotesIfRequired(source));
    }

    private static <T extends Comparable<T>> void assertSameElements(List<T> expected, List<T> actual) {

        List<T> expectedOrdered = new ArrayList<>(expected);
        Collections.sort(expectedOrdered);

        List<T> actualOrdered = new ArrayList<>(actual);
        Collections.sort(actualOrdered);

        assertEquals(expectedOrdered, actualOrdered);
    }

    private static SampleGenInfo convert(PlExpression<?> resultExpression) {

        SampleGenInfo res = new SampleGenInfo();
        res.setCntAll(countSpecialSetOccurrences(resultExpression, SpecialSetType.ALL));
        res.setCntAnd(countCombinerOccurrences(resultExpression, CombinedExpressionType.AND));
        res.setCntAny(countMatchOccurrences(resultExpression, PlMatchOperator.ANY_OF, PlMatchOperator.NOT_ANY_OF, PlMatchOperator.STRICT_NOT_ANY_OF,
                PlMatchOperator.CONTAINS_ANY_OF, PlMatchOperator.NOT_CONTAINS_ANY_OF, PlMatchOperator.STRICT_NOT_CONTAINS_ANY_OF));
        res.setCntBetween(countMatchOccurrences(resultExpression, PlMatchOperator.BETWEEN, PlMatchOperator.NOT_BETWEEN, PlMatchOperator.STRICT_NOT_BETWEEN));
        res.setCntContains(countMatchOccurrences(resultExpression, PlMatchOperator.CONTAINS, PlMatchOperator.NOT_CONTAINS, PlMatchOperator.STRICT_NOT_CONTAINS,
                PlMatchOperator.CONTAINS_ANY_OF, PlMatchOperator.NOT_CONTAINS_ANY_OF, PlMatchOperator.STRICT_NOT_CONTAINS_ANY_OF));
        res.setCntCurb(countCurbOccurrences(resultExpression));
        res.setCntIs(countMatchOccurrences(resultExpression, PlMatchOperator.IS_UNKNOWN, PlMatchOperator.IS_NOT_UNKNOWN));
        res.setCntNone(countSpecialSetOccurrences(resultExpression, SpecialSetType.NONE));
        res.setCntNot(countNotOccurrences(resultExpression));
        res.setCntOf(countMatchOccurrences(resultExpression, PlMatchOperator.ANY_OF, PlMatchOperator.NOT_ANY_OF, PlMatchOperator.STRICT_NOT_ANY_OF,
                PlMatchOperator.CONTAINS_ANY_OF, PlMatchOperator.NOT_CONTAINS_ANY_OF, PlMatchOperator.STRICT_NOT_CONTAINS_ANY_OF));
        res.setCntOr(countCombinerOccurrences(resultExpression, CombinedExpressionType.OR));

        res.setCntStrict(countMatchOccurrences(resultExpression, PlMatchOperator.STRICT_NOT_EQUALS, PlMatchOperator.STRICT_NOT_CONTAINS,
                PlMatchOperator.STRICT_NOT_BETWEEN, PlMatchOperator.STRICT_NOT_ANY_OF, PlMatchOperator.STRICT_NOT_CONTAINS_ANY_OF));

        res.setCntUnknown(countMatchOccurrences(resultExpression, PlMatchOperator.IS_UNKNOWN, PlMatchOperator.IS_NOT_UNKNOWN));

        res.setArgNames(collectArgNames(resultExpression));
        res.setArgValues(collectArgValues(resultExpression, false));
        res.setArgRefs(collectArgValues(resultExpression, true));
        res.setBoundValues(collectCurbBoundValues(resultExpression));
        res.setComments(collectComments(resultExpression));
        res.setOperators(collectOperators(resultExpression));
        res.setSnippets(collectSnippets(resultExpression));

        return res;
    }

    private static int countSpecialSetOccurrences(PlExpression<?> resultExpression, SpecialSetType setType) {
        List<PlExpression<?>> specialSets = new ArrayList<>();
        resultExpression.collectExpressions(e -> (e instanceof PlSpecialSetExpression spe && spe.setType() == setType), specialSets);
        return specialSets.size();
    }

    private static int countCombinerOccurrences(PlExpression<?> resultExpression, CombinedExpressionType combiType) {
        List<PlExpression<?>> combinedExpressions = new ArrayList<>();
        resultExpression.collectExpressions(e -> (e instanceof PlCombinedExpression cmb && cmb.combiType() == combiType), combinedExpressions);

        int numberOfCombiners = 0;

        for (PlExpression<?> combinedExpression : combinedExpressions) {
            numberOfCombiners = numberOfCombiners + combinedExpression.childExpressions().size() - 1;
        }

        return numberOfCombiners;
    }

    private static int countMatchOccurrences(PlExpression<?> resultExpression, PlMatchOperator... matchOperators) {
        Set<PlMatchOperator> opFilter = new HashSet<>(Arrays.asList(matchOperators));
        List<PlExpression<?>> matchExpressions = new ArrayList<>();
        resultExpression.collectExpressions(e -> (e instanceof PlMatchExpression match && opFilter.contains(match.operator())), matchExpressions);
        return matchExpressions.size();
    }

    private static int countCurbOccurrences(PlExpression<?> resultExpression) {
        List<PlExpression<?>> curbExpressions = new ArrayList<>();
        resultExpression.collectExpressions(e -> (e instanceof PlCurbExpression), curbExpressions);
        return curbExpressions.size();
    }

    private static int countNotOccurrences(PlExpression<?> resultExpression) {
        List<PlExpression<?>> negExpressions = new ArrayList<>();
        resultExpression.collectExpressions(e -> (e instanceof PlNegationExpression), negExpressions);
        return negExpressions.size() + countMatchOccurrences(resultExpression, PlMatchOperator.NOT_ANY_OF, PlMatchOperator.NOT_BETWEEN,
                PlMatchOperator.NOT_CONTAINS, PlMatchOperator.NOT_CONTAINS_ANY_OF, PlMatchOperator.STRICT_NOT_ANY_OF, PlMatchOperator.STRICT_NOT_BETWEEN,
                PlMatchOperator.STRICT_NOT_CONTAINS, PlMatchOperator.STRICT_NOT_CONTAINS_ANY_OF, PlMatchOperator.IS_NOT_UNKNOWN);

    }

    private static List<String> collectArgNames(PlExpression<?> resultExpression) {

        List<PlExpression<?>> matchExpressions = new ArrayList<>();
        resultExpression.collectExpressions(e -> (e instanceof PlMatchExpression), matchExpressions);

        return matchExpressions.stream().map(PlMatchExpression.class::cast).map(PlMatchExpression::argName).toList();

    }

    private static List<String> collectArgValues(PlExpression<?> resultExpression, boolean ref) {

        Set<PlMatchOperator> containsOps = new HashSet<>(Arrays.asList(PlMatchOperator.CONTAINS, PlMatchOperator.CONTAINS_ANY_OF, PlMatchOperator.NOT_CONTAINS,
                PlMatchOperator.NOT_CONTAINS_ANY_OF, PlMatchOperator.STRICT_NOT_CONTAINS, PlMatchOperator.STRICT_NOT_CONTAINS_ANY_OF));

        List<PlExpression<?>> matchExpressions = new ArrayList<>();
        resultExpression.collectExpressions(e -> (e instanceof PlMatchExpression match && !containsOps.contains(match.operator())), matchExpressions);

        // @formatter:off
        return matchExpressions.stream().map(PlMatchExpression.class::cast)
                .map(PlMatchExpression::operands)
                .flatMap(List::stream)
                .filter(o -> (o.isReference() == ref))
                .map(PlOperand::value)
                .toList();
        // @formatter:on

    }

    private static List<String> collectSnippets(PlExpression<?> resultExpression) {

        Set<PlMatchOperator> containsOps = new HashSet<>(Arrays.asList(PlMatchOperator.CONTAINS, PlMatchOperator.CONTAINS_ANY_OF, PlMatchOperator.NOT_CONTAINS,
                PlMatchOperator.NOT_CONTAINS_ANY_OF, PlMatchOperator.STRICT_NOT_CONTAINS, PlMatchOperator.STRICT_NOT_CONTAINS_ANY_OF));

        List<PlExpression<?>> matchExpressions = new ArrayList<>();
        resultExpression.collectExpressions(e -> (e instanceof PlMatchExpression match && containsOps.contains(match.operator())), matchExpressions);

        // @formatter:off
        return matchExpressions.stream().map(PlMatchExpression.class::cast)
                .map(PlMatchExpression::operands)
                .flatMap(List::stream)
                .map(PlOperand::value)
                .toList();
        // @formatter:on

    }

    private static List<Integer> collectCurbBoundValues(PlExpression<?> resultExpression) {
        List<PlExpression<?>> curbExpressions = new ArrayList<>();
        resultExpression.collectExpressions(e -> (e instanceof PlCurbExpression), curbExpressions);

        return curbExpressions.stream().map(PlCurbExpression.class::cast).map(PlCurbExpression::bound).toList();
    }

    private static List<String> collectComments(PlExpression<?> resultExpression) {
        return resultExpression.allComments().stream().map(PlComment::comment).toList();
    }

    private static List<SampleExpressionOperator> collectOperators(PlExpression<?> resultExpression) {

        List<String> res = new ArrayList<>();

        List<PlExpression<?>> matchExpressions = new ArrayList<>();
        resultExpression.collectExpressions(e -> (e instanceof PlMatchExpression), matchExpressions);

        List<PlMatchOperator> matchOperators = matchExpressions.stream().map(PlMatchExpression.class::cast).map(PlMatchExpression::operator).toList();

        for (PlMatchOperator op : matchOperators) {
            switch (op) {
            case EQUALS, NOT_EQUALS, LESS_THAN, LESS_THAN_OR_EQUALS, GREATER_THAN, GREATER_THAN_OR_EQUALS:
                res.add(op.name());
                break;
            case STRICT_NOT_EQUALS:
                res.add(PlMatchOperator.NOT_EQUALS.name());
                break;
            // $CASES-OMITTED$
            default: // ignore
            }
        }

        List<PlExpression<?>> curbExpressions = new ArrayList<>();
        resultExpression.collectExpressions(e -> (e instanceof PlCurbExpression), curbExpressions);
        curbExpressions.stream().map(PlCurbExpression.class::cast).map(PlCurbExpression::operator).map(PlCurbOperator::name).forEach(res::add);

        return res.stream().map(SampleExpressionOperator::valueOf).toList();
    }

}

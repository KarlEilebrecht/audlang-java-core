//@formatter:off
/*
 * StandardSamplesTest
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.calamanari.adl.TimeOutException;
import de.calamanari.adl.cnv.PlToCoreExpressionConverter;
import de.calamanari.adl.erl.AudlangParseResult;
import de.calamanari.adl.erl.PlExpressionBuilder;
import de.calamanari.adl.irl.biceps.CoreExpressionOptimizer;
import de.calamanari.adl.util.sgen.SampleExpression;
import de.calamanari.adl.util.sgen.SampleExpressionGroup;
import de.calamanari.adl.util.sgen.SampleExpressionUtils;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class StandardSamplesTest {

    static final Logger LOGGER = LoggerFactory.getLogger(StandardSamplesTest.class);

    @Test
    @Disabled("""
            This test is long-running and has minor value for regular execution, it is meant for interactive sessions to check behavior and for timeouts.
            - Expected are 10-15 timeouts for the super complex examples (depends on machine and memory).
            """)
    void testGeneratedStandardSamples() {

        long startTimeMillis = System.currentTimeMillis();
        AtomicInteger timeoutCounter = new AtomicInteger();

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
                List<SampleExpression> activeSamples = sampleGroup.samples().stream().filter(Predicate.not(SampleExpression::skip)).toList();

                LOGGER.debug("Found {} active samples ...", activeSamples.size());

                sampleCount = sampleCount + evaluateSamples(sampleGroup.group(), activeSamples, true, timeoutCounter);
                sampleCount = sampleCount + evaluateSamples(sampleGroup.group(), activeSamples, false, timeoutCounter);

                LOGGER.debug("Finished group {}.", sampleGroup.group());

            }

        }

        LOGGER.info("Successfully tested {}/{} samples in {}/{} sample groups ({} ms, {} timeouts).", sampleCount,
                allGroups.stream().map(g -> g.samples().size()).collect(Collectors.summingInt(Integer::intValue)), sampleGroupCount, allGroups.size(),
                (System.currentTimeMillis() - startTimeMillis), timeoutCounter.get());

    }

    private int evaluateSamples(String group, List<SampleExpression> allSamples, boolean filterValid, AtomicInteger timeoutCounter) {
        List<SampleExpression> samples = allSamples.stream().filter(sample -> sample.invalid() != filterValid).toList();

        if (!samples.isEmpty()) {
            LOGGER.debug("Testing {} samples from group {} expected to be {}valid ...", samples.size(), group, (filterValid ? "" : "in"));
            for (int i = 0; i < samples.size(); i++) {
                evaluateSample(samples.get(i), i, group, samples.size(), timeoutCounter);
            }
        }
        else {
            LOGGER.debug("No {}valid samples to test.", (filterValid ? "" : "in"));
        }
        return samples.size();
    }

    private void evaluateSample(SampleExpression sample, int sampleNumber, String group, int groupSize, AtomicInteger timeoutCounter) {

        LOGGER.info("{} ({} {}/{})", sample.id(), group, sampleNumber, groupSize);
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

                LOGGER.debug("{} argNames in sample", res.getResultExpression().allArgNames().size());

                CoreExpression converted = new PlToCoreExpressionConverter(new CoreExpressionOptimizer()).convert(res.getResultExpression());

                assertNotNull(converted);

                success = true;
            }
            catch (TimeOutException ex) {
                LOGGER.error("ERROR: Processing of sample {} skipped due to timeout", sample.id(), ex);
                timeoutCounter.incrementAndGet();
                success = true;
            }
            finally {
                if (!success) {
                    LOGGER.error("ERROR: Sample {} was valid but parse result did not match expectations", sample.id());
                    LOGGER.error("Expression was: '''{}''', \nParse Result:\n{}", sample.expression(), res);
                    LOGGER.error("After resolving higher language features: {}", res.getResultExpression().resolveHigherLanguageFeatures());
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

}

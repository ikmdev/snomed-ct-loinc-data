package dev.ikm.tinkar.snomedctloinc.integration;

import dev.ikm.elk.snomed.SnomedConcepts;
import dev.ikm.elk.snomed.test.SnomedVersion;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.reasoner.elksnomed.ElkSnomedData;
import dev.ikm.tinkar.reasoner.elksnomed.test.ElkSnomedDataBuilderTestBase;
import dev.ikm.tinkar.reasoner.elksnomed.test.PrimitiveDataTestUtil;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled
public class LoincElkSnomedDataBuilderTestIT extends ElkSnomedDataBuilderTestBase implements SnomedVersion {

    private static final Logger LOG = LoggerFactory.getLogger(LoincElkSnomedDataBuilderTestIT.class);

    private static Path origin;
    private static Path originSnomed;
    private static Path snomedConceptsFile;

    @BeforeAll
    public static void startPrimitiveData() {
        origin = IntegrationTestUtils.findOriginPath(Path.of("..", "snomed-ct-loinc-pipeline", "target", "src")).resolve("Snapshot", "Terminology");
        originSnomed = IntegrationTestUtils.findOriginPath(Path.of("..", "..", "snomed-ct-data", "snomed-ct-pipeline", "target", "src", "snomedFull")).resolve("Snapshot", "Terminology");
        snomedConceptsFile = IntegrationTestUtils.findMatchingFile(originSnomed, "sct2_Concept_Snapshot");

        File datastorePath = new File(System.getProperty("datastorePath"));
        LOG.info("datastorePath: {}", datastorePath);
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT, datastorePath);
        PrimitiveData.selectControllerByName("Open SpinedArrayStore");
        PrimitiveData.start();
    }

    @AfterAll
    public static void stopPrimitiveData() {
        LOG.info("stopPrimitiveData");
        PrimitiveData.stop();
        LOG.info("Stopped");
    }

    @Override
    public String getDir() {
        return origin.toFile().getPath();
    }

    @Override
    public String getEdition() {
        return IntegrationTestUtils.findEditionFromOrigin(origin);
    }

    @Override
    public String getEditionDir() {
        return ""; // Not used
    }

    @Override
    public String getVersion() {
        return IntegrationTestUtils.findVersionFromOrigin(origin);
    }

    @Override
    public String getInternationalVersion() {
        return getVersion();
    }

    @Override
    public void versionDataFile() {
        assertTrue(true);
    }

    @Override
    public void versionClass() {
        assertTrue(true);
    }

    @Test
    @Override
    public void build() throws Exception {
        ElkSnomedData data = buildSnomedData();
        int primordialCount = PrimitiveDataTestUtil.getPrimordialNids().size();
        int primordialSctidCount = PrimitiveDataTestUtil.getPrimordialNidsWithSctids().size();

        SnomedConcepts concepts = new SnomedConcepts();
        concepts.load(concepts_file);
        concepts.load(snomedConceptsFile);

        int totalCount = computeTotalCount();
        int activeCount = concepts.getActiveCount();
        int inactiveCount = totalCount - activeCount - primordialCount + primordialSctidCount;

        assertEquals(activeCount, data.getActiveConceptCount() - primordialCount + primordialSctidCount);
        assertEquals(inactiveCount, data.getInactiveConceptCount());
        assertEquals(data.getActiveConceptCount(), data.getConcepts().size());
        assertEquals(data.getReasonerConceptSet().size(), data.getConcepts().size());
    }

    @Test
    @Override
    public void count() {
        // Conditions already covered in 'build' test
    }

    private int computeTotalCount() {
        AtomicInteger totalCounter = new AtomicInteger();
        PrimitiveData.get().forEachSemanticNidOfPattern(TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN.nid(), _ -> totalCounter.incrementAndGet());
        return totalCounter.get();
    }

}


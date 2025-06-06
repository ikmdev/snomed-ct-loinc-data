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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoincElkSnomedDataBuilderTestIT extends ElkSnomedDataBuilderTestBase implements SnomedVersion {

    private static final Logger LOG = LoggerFactory.getLogger(LoincElkSnomedDataBuilderTestIT.class);

    private static Path origin;
    private static Path originSnomed;
    private static Path snomedConceptsFile;

    @BeforeEach
    public void setUp() throws Exception {
        SnomedConcepts loincConcepts = SnomedConcepts.init(concepts_file);
        SnomedConcepts snomedConcepts = SnomedConcepts.init(snomedConceptsFile);
        int totalCount = computeTotalCount();
        int activeCount = snomedConcepts.getActiveCount() + loincConcepts.getActiveCount();
        int primordialCount = PrimitiveDataTestUtil.getPrimordialNids().size();
        int primordialSctidCount = PrimitiveDataTestUtil.getPrimordialNidsWithSctids().size();
        inactive_count = totalCount - activeCount - primordialCount + primordialSctidCount;
    }

    private int computeTotalCount() {
        AtomicInteger totalCounter = new AtomicInteger();
        PrimitiveData.get().forEachSemanticNidOfPattern(TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN.nid(), _ -> totalCounter.incrementAndGet());
        return totalCounter.get();
    }

    @BeforeAll
    public static void startPrimitiveData() {
        origin = IntegrationTestUtils.findOriginPath(Path.of("..", "snomed-ct-loinc-origin", "target", "origin-sources")).resolve("Snapshot", "Terminology");
        originSnomed = IntegrationTestUtils.findOriginPath(Path.of("..", "..", "snomed-ct-data", "snomed-ct-origin", "target", "origin-sources")).resolve("Snapshot", "Terminology");
        snomedConceptsFile = IntegrationTestUtils.findMatchingFile(originSnomed, "sct2_Concept_Snapshot_").map(File::toPath)
                .orElseThrow(() -> new RuntimeException("unable to locate sct2_Concept_Snapshot file in SNOMED origin: " + originSnomed));

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
        int primordial_cnt = PrimitiveDataTestUtil.getPrimordialNids().size();
        int primordial_sctid_cnt = PrimitiveDataTestUtil.getPrimordialNidsWithSctids().size();
        SnomedConcepts loinc_concepts = SnomedConcepts.init(concepts_file);
        SnomedConcepts snomed_concepts = SnomedConcepts.init(snomedConceptsFile);
        assertEquals(snomed_concepts.getActiveCount() + loinc_concepts.getActiveCount(), data.getActiveConceptCount() - primordial_cnt + primordial_sctid_cnt + 2);
        assertEquals(inactive_count, data.getInactiveConceptCount());
        assertEquals(data.getActiveConceptCount(), data.getConcepts().size());
        assertEquals(data.getReasonerConceptSet().size(), data.getConcepts().size());
    }

}


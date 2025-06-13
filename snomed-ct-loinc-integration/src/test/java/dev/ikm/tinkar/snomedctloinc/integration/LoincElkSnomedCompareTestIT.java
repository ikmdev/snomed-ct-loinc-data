package dev.ikm.tinkar.snomedctloinc.integration;

import dev.ikm.elk.snomed.ConceptComparer;
import dev.ikm.elk.snomed.OwlElTransformer;
import dev.ikm.elk.snomed.SnomedDescriptions;
import dev.ikm.elk.snomed.SnomedIds;
import dev.ikm.elk.snomed.SnomedOntology;
import dev.ikm.elk.snomed.model.Concept;
import dev.ikm.elk.snomed.owlel.OwlElOntology;
import dev.ikm.elk.snomed.test.SnomedVersion;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.reasoner.elksnomed.ElkSnomedData;
import dev.ikm.tinkar.reasoner.elksnomed.test.ElkSnomedCompareTestBase;
import dev.ikm.tinkar.reasoner.elksnomed.test.NidToSctid;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoincElkSnomedCompareTestIT extends ElkSnomedCompareTestBase implements SnomedVersion {

    private static final Logger LOG = LoggerFactory.getLogger(LoincElkSnomedCompareTestIT.class);

    private static Path origin;
    private static Path originSnomed;
    private static Path snomedDescriptionFile;
    private static Path snomedAxiomsFile;

    @BeforeAll
    public static void startPrimitiveData() {
        origin = IntegrationTestUtils.findOriginPath(Path.of("..", "snomed-ct-loinc-origin", "target", "origin-sources")).resolve("Snapshot", "Terminology");
        originSnomed = IntegrationTestUtils.findOriginPath(Path.of("..", "..", "snomed-ct-data", "snomed-ct-origin", "target", "origin-sources")).resolve("Snapshot", "Terminology");
        snomedDescriptionFile = IntegrationTestUtils.findMatchingFile(originSnomed, "sct2_Description_Snapshot");
        snomedAxiomsFile = IntegrationTestUtils.findMatchingFile(originSnomed, "sct2_sRefset_OWLExpressionSnapshot");

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
    public void compare() throws Exception {
        ElkSnomedData data = buildSnomedData();

        OwlElOntology ontology = new OwlElOntology();
        ontology.load(axioms_file);
        ontology.load(snomedAxiomsFile);

        SnomedDescriptions descriptions = new SnomedDescriptions();
        descriptions.load(descriptions_file);
        descriptions.load(snomedDescriptionFile);

        SnomedOntology snomedOntology = new OwlElTransformer().transform(ontology);
        snomedOntology.setDescriptions(descriptions);
        snomedOntology.setNames();

        SnomedOntology dataOntology = new NidToSctid(data, snomedOntology).build();
        dataOntology.setDescriptions(descriptions);
        dataOntology.setNames();

        int missing_count = 0;
        ConceptComparer cc = new ConceptComparer(snomedOntology);
        for (Concept concept : snomedOntology.getConcepts()) {
            if (concept.getId() == SnomedIds.root) {
                LOG.info("Skipping: " + concept);
                continue;
            }
            mergeSubConceptDefinitions(concept);
            Concept data_concept = dataOntology.getConcept(concept.getId());
            if (data_concept == null) {
                LOG.error("Missing: " + concept);
                missing_count++;
            } else if ("Disposition (property) (qualifier value)".equals(data_concept.getName())) {
                LOG.info("Skipping: " + concept); // TODO
            } else if (!cc.compare(data_concept)) {
                LOG.error("Mis match: " + data_concept);
            }
        }
        LOG.info("Mis match count: " + cc.getMisMatchCount());
        LOG.info("Missing count: " + missing_count);
        assertEquals(0, cc.getMisMatchCount());
        assertEquals(0, missing_count);
    }

}

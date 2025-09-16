package dev.ikm.tinkar.snomedctloinc.integration;

import dev.ikm.elk.snomed.SnomedDescriptions;
import dev.ikm.elk.snomed.SnomedIds;
import dev.ikm.elk.snomed.SnomedIsa;
import dev.ikm.elk.snomed.SnomedOntology;
import dev.ikm.elk.snomed.SnomedOntologyReasoner;
import dev.ikm.elk.snomed.model.Concept;
import dev.ikm.elk.snomed.test.SnomedVersion;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.reasoner.elksnomed.ElkSnomedData;
import dev.ikm.tinkar.reasoner.elksnomed.test.ElkSnomedClassifierTestBase;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled
public class LoincElkSnomedClassifierTestIT extends ElkSnomedClassifierTestBase implements SnomedVersion {

    private static final Logger LOG = LoggerFactory.getLogger(LoincElkSnomedClassifierTestIT.class);

    private static Path origin;
    private static Path originSnomed;
    private static Path snomedDescriptionFile;
    private static Path snomedRelsFile;

    private Map<Integer, Long> nid_sctid_map;

    @BeforeAll
    public static void startPrimitiveData() {
        origin = IntegrationTestUtils.findOriginPath(Path.of("..", "snomed-ct-loinc-pipeline", "target", "src")).resolve("Snapshot", "Terminology");
        originSnomed = IntegrationTestUtils.findOriginPath(Path.of("..", "..", "snomed-ct-data", "snomed-ct-pipeline", "target", "src", "snomedFull")).resolve("Snapshot", "Terminology");
        snomedDescriptionFile = IntegrationTestUtils.findMatchingFile(originSnomed, "sct2_Description_Snapshot");
        snomedRelsFile = IntegrationTestUtils.findMatchingFile(originSnomed, "sct2_Relationship_Snapshot");

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
    public void isas() throws Exception {
        ElkSnomedData data = buildSnomedData();
        SnomedOntology ontology = new SnomedOntology(data.getConcepts(), data.getRoleTypes(), data.getConcreteRoleTypes());
        SnomedOntologyReasoner reasoner = SnomedOntologyReasoner.create(ontology);
        int non_snomed_cnt = 0;
        int miss_cnt = 0;
        int match_cnt = 0;

        SnomedIsa isas = new SnomedIsa();
        isas.load(rels_file);
        isas.load(snomedRelsFile);
        isas.init(SnomedIds.root);

        SnomedDescriptions descriptions = new SnomedDescriptions();
        descriptions.load(descriptions_file);
        descriptions.load(snomedDescriptionFile);

        nid_sctid_map = new HashMap<>();
        for (long sctid : isas.getOrderedConcepts()) {
            int nid = ElkSnomedData.getNid(sctid);
            nid_sctid_map.put(nid, sctid);
            if (ontology.getConcept(nid) == null) {
                LOG.info("No concept for: " + sctid + " " + descriptions.getFsn(sctid));
            }
        }

        for (Concept con : ontology.getConcepts()) {
            long nid = con.getId();
            Set<Long> sups = toSctids(reasoner.getSuperConcepts(nid));
            Long sctid = nid_sctid_map.get((int) nid);
            if (sctid == null) {
                non_snomed_cnt++;
                continue;
            }
            Set<Long> parents = isas.getParents(sctid);
            if (sctid == SnomedIds.root) {
                assertTrue(parents.isEmpty());
                // has a parent in the db
                //assertEquals(1, sups.size()); // TODO sups is empty
                assertEquals(TinkarTerm.PHENOMENON.nid(), reasoner.getSuperConcepts(nid).iterator().next());
                continue;
            } else {
                assertNotNull(parents);
            }
            if (parents.equals(sups)) {
                match_cnt++;
            } else {
                LOG.warn("Miss: scid={} nid={} parents={} sups={}", sctid, nid, parents, sups);
                miss_cnt++;
            }
        }
        LOG.info("Miss cnt: " + miss_cnt);
        LOG.info("Match cnt: " + match_cnt);

        int primordialCount = PrimitiveDataTestUtil.getPrimordialNids().size();
        int primordialSctidCount = PrimitiveDataTestUtil.getPrimordialNidsWithSctids().size();
        int expected_non_snomed_cnt = primordialCount - primordialSctidCount + 2;
        assertEquals(expected_non_snomed_cnt, non_snomed_cnt);
        assertEquals(0, miss_cnt);
    }

    private Set<Long> toSctids(Set<Long> nids) {
        return nids.stream().map(x -> nid_sctid_map.get(x.intValue())).filter(Objects::nonNull).collect(Collectors.toSet());
    }

}

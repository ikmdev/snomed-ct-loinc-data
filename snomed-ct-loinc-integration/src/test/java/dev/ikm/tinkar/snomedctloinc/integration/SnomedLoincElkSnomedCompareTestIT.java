package dev.ikm.tinkar.snomedctloinc.integration;

import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.reasoner.elksnomed.ElkSnomedCompareTestBase;
import dev.ikm.tinkar.reasoner.elksnomed.SnomedVersionUs;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SnomedLoincElkSnomedCompareTestIT extends ElkSnomedCompareTestBase implements SnomedVersionUs {

	private static final Logger LOG = LoggerFactory.getLogger(SnomedLoincElkSnomedCompareTestIT.class);

	static {
		test_case = "snomedct-us";
	}

	@BeforeEach
	@Override
	protected void filesExist() {
		axioms_file = Paths.get(getDir(), "sct2_sRefset_OWLExpressionSnapshot_LO1010000_%s.txt".formatted(getVersion()));
		concepts_file = Paths.get(getDir(), "sct2_Concept_Snapshot_LO1010000_%s.txt".formatted(getVersion()));
		rels_file = Paths.get(getDir(), "sct2_Relationship_Snapshot_LO1010000_%s.txt".formatted(getVersion()));
		values_file = Paths.get(getDir(), "sct2_RelationshipConcreteValues_Snapshot_LO1010000_%s.txt".formatted(getVersion()));
		descriptions_file = Paths.get(getDir(), "sct2_Description_Snapshot-en_LO1010000_%s.txt".formatted(getVersion()));
		super.filesExist();
	}

	@Override
	public String getDir() {
        return "../snomed-ct-loinc-origin/target/origin-sources/SnomedCT_LOINCExtension_PRODUCTION_LO1010000_%sT120000Z/Snapshot/Terminology/"
                .formatted(getVersion());
	}

	@Override
	public String getEdition() {
		return "US";
	}

	@Override
	public String getVersion() {
		return "20250321";
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

	@BeforeAll
	public static void startPrimitiveData() {
		ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT, new File("../../snomed-ct-data/target/snomedct-us"));
		PrimitiveData.selectControllerByName("Open SpinedArrayStore");
		PrimitiveData.start();
	}

	@AfterAll
	public static void stopPrimitiveData() {
		LOG.info("stopPrimitiveData");
		PrimitiveData.stop();
		LOG.info("Stopped");
	}

}

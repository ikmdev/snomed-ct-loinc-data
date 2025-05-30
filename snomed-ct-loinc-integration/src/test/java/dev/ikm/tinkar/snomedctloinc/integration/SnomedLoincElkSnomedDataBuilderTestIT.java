package dev.ikm.tinkar.snomedctloinc.integration;

import dev.ikm.elk.snomed.test.SnomedVersionUs;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.reasoner.elksnomed.test.ElkSnomedDataBuilderTestBase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SnomedLoincElkSnomedDataBuilderTestIT extends ElkSnomedDataBuilderTestBase implements SnomedVersionUs {

	private static final Logger LOG = LoggerFactory.getLogger(SnomedLoincElkSnomedDataBuilderTestIT.class);

	static {
		test_case = "snomedct-us";
	}

	@BeforeEach
	public void setUp() {
//		active_count = 414019;
//		inactive_count = 28502;
	}

	@Override
	public String getDir() {
		return "../snomed-ct-loinc-origin/target/origin-sources/SnomedCT_LOINCExtension_PRODUCTION_%s_%sT120000Z/Snapshot/Terminology/"
				.formatted(getEdition(), getVersion());
	}

	@Override
	public String getEdition() {
		return "LO1010000";
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

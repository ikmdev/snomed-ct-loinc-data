package dev.ikm.tinkar.snomedctloinc.integration;

import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.reasoner.elksnomed.ElkSnomedClassifierTestBase;
import dev.ikm.tinkar.reasoner.elksnomed.SnomedVersionUs;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class SnomedLoincElkSnomedClassifierTestIT extends ElkSnomedClassifierTestBase implements SnomedVersionUs {

    private static final Logger LOG = LoggerFactory.getLogger(SnomedLoincElkSnomedClassifierTestIT.class);

    static {
        test_case = "snomedct-us";
    }

    {
        expected_supercs_cnt = 620209; // TODO hardcoded value
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

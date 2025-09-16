package dev.ikm.tinkar.snomedctloinc.integration;

import dev.ikm.maven.SnomedLoincUtility;
import dev.ikm.tinkar.common.util.uuid.UuidUtil;
import dev.ikm.tinkar.coordinate.Calculators;
import dev.ikm.tinkar.coordinate.stamp.StampCoordinateRecord;
import dev.ikm.tinkar.coordinate.stamp.StampPositionRecord;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.stamp.calculator.StampCalculator;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.PatternEntityVersion;
import dev.ikm.tinkar.entity.SemanticRecord;
import dev.ikm.tinkar.entity.SemanticVersionRecord;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SnomedLoincAxiomSemanticIT extends SnomedLoincAbstractIntegrationTest {

    /**
     * Test Snomed Loinc Axiom Semantics.
     *
     * @result Reads content from file and validates Axiom of Semantics by calling protected method assertLine().
     */
    @Test
    public void testAxiomSemantics() throws IOException {
        String baseDir = "../snomed-ct-loinc-pipeline/target/src";
        String errorFile = "target/failsafe-reports/axioms_not_found.txt";

        String absolutePath = findFilePath(baseDir, "sct2_srefset_owl");
        int notFound = processFile(absolutePath, errorFile);

        assertEquals(0, notFound, "Unable to find " + notFound + " snomed axiom semantics. Details written to " + errorFile);
    }

    @Override
    protected boolean assertLine(String[] columns) {
        long effectiveTime = SnomedLoincUtility.snomedTimestampToEpochSeconds(columns[1]);
        StateSet snomedAxiomStatus = Integer.parseInt(columns[2]) == 1 ? StateSet.ACTIVE : StateSet.INACTIVE;
        String owlAxiomStr = SnomedLoincUtility.owlAxiomIdsToPublicIds(UuidUtil.SNOMED_NAMESPACE, columns[6]);
        UUID id = uuid(columns[0]);

        StampPositionRecord stampPosition = StampPositionRecord.make(effectiveTime, TinkarTerm.DEVELOPMENT_PATH.nid());
        StampCalculator stampCalc = StampCoordinateRecord.make(snomedAxiomStatus, stampPosition).stampCalculator();
        SemanticRecord entity = EntityService.get().getEntityFast(id);

        if (entity != null) {
            PatternEntityVersion pattern = (PatternEntityVersion) Calculators.Stamp.DevelopmentLatest().latest(TinkarTerm.OWL_AXIOM_SYNTAX_PATTERN).get();
            Latest<SemanticVersionRecord> latest = stampCalc.latest(entity);
            String fieldValue = pattern.getFieldWithMeaning(TinkarTerm.AXIOM_SYNTAX, latest.get());
            return latest.isPresent() && fieldValue.equals(owlAxiomStr);
        }
        return false;
    }

}

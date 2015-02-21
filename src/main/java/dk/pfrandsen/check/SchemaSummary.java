package dk.pfrandsen.check;

import java.nio.file.Path;

public class SchemaSummary extends FileSummary {

    public SchemaSummary(Path fullPath, AnalysisInformationCollector added, AnalysisInformationCollector resolved,
                         int infoCount) {
        super(fullPath, added, resolved, infoCount);
    }

}

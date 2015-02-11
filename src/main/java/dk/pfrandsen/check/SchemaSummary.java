package dk.pfrandsen.check;

import java.nio.file.Path;

public class SchemaSummary extends FileSummary {

    public SchemaSummary(Path fullPath, AnalysisInformationCollector added, AnalysisInformationCollector resolved,
                         Path fullReport, Path fullReportHtml, Path addedReport, Path diffReportHtml) {
        super(fullPath, added, resolved);
    }

    public SchemaSummary(Path fullPath, AnalysisInformationCollector added, AnalysisInformationCollector resolved) {
        super(fullPath, added, resolved);
    }

}

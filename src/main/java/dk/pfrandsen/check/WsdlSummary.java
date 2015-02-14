package dk.pfrandsen.check;

import java.nio.file.Path;

// TODO: add WS-I
public class WsdlSummary extends FileSummary {

    public WsdlSummary(Path fullPath, AnalysisInformationCollector added, AnalysisInformationCollector resolved,
                         Path fullReport, Path fullReportHtml, Path addedReport, Path diffReportHtml) {
        super(fullPath, added, resolved);
    }

    public WsdlSummary(Path fullPath, AnalysisInformationCollector added, AnalysisInformationCollector resolved) {
        super(fullPath, added, resolved);
    }

}

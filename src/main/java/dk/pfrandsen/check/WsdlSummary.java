package dk.pfrandsen.check;

import java.nio.file.Path;

// TODO: add WS-I
public class WsdlSummary extends FileSummary {

    public WsdlSummary(Path fullPath, AnalysisInformationCollector added, AnalysisInformationCollector resolved,
                       int infoCount) {
        super(fullPath, added, resolved, infoCount);
    }

}

package dk.pfrandsen.check;

import java.nio.file.Path;

public class FileSummary {

    int errorsAdded;
    int errorsResolved;
    int warningsAdded;
    int warningsResolved;
    int infoCount;
    String name;
    Path filePath; // path to schema or wsdl
    Path fullReport = null;
    Path fullReportHtml = null;
    Path addedReport = null;
    Path diffReportHtml = null;
    Path sourceHtml = null;
    AssertionStatistics errors = new AssertionStatistics();
    AssertionStatistics warnings = new AssertionStatistics();

    protected FileSummary(Path fullPath, AnalysisInformationCollector added, AnalysisInformationCollector resolved,
                          int infoCount) {
        this.filePath = fullPath;
        this.name = fullPath.toFile().getName();
        errorsAdded = added.errorCount();
        errorsResolved = resolved.errorCount();
        warningsAdded = added.warningCount();
        warningsResolved = resolved.warningCount();
        this.infoCount = infoCount;
    }

    public boolean hasFullReport() {
        return fullReport != null;
    }

    public boolean hasFullReportHtml() {
        return fullReportHtml != null;
    }

    public boolean hasAddedReport() {
        return addedReport != null;
    }

    public boolean hasDiffReportHtml() {
        return diffReportHtml != null;
    }

    public boolean hasSourceHtml() {
        return sourceHtml != null;
    }

    public int getErrorsAdded() {
        return errorsAdded;
    }

    public int getErrorsResolved() {
        return errorsResolved;
    }

    public int getWarningsAdded() {
        return warningsAdded;
    }

    public int getWarningsResolved() {
        return warningsResolved;
    }

    public int getInfoCount() {
        return infoCount;
    }

    public String getName() {
        return name;
    }

    public Path getFilePath() {
        return filePath;
    }

    public Path getFullReport() {
        return fullReport;
    }

    public Path getFullReportHtml() {
        return fullReportHtml;
    }

    public Path getAddedReport() {
        return addedReport;
    }

    public Path getDiffReportHtml() {
        return diffReportHtml;
    }

    public Path getSourceHtml() {
        return sourceHtml;
    }

    public void setFullReport(Path fullReport) {
        this.fullReport = fullReport;
    }

    public void setFullReportHtml(Path fullReportHtml) {
        this.fullReportHtml = fullReportHtml;
    }

    public void setAddedReport(Path addedReport) {
        this.addedReport = addedReport;
    }

    public void setDiffReportHtml(Path diffReportHtml) {
        this.diffReportHtml = diffReportHtml;
    }

    public void setSourceHtml(Path sourceHtml) {
        this.sourceHtml = sourceHtml;
    }

    public AssertionStatistics getErrors() {
        return errors;
    }

    public void setErrors(AssertionStatistics errors) {
        this.errors = errors != null ? errors : new AssertionStatistics();
    }

    public AssertionStatistics getWarnings() {
        return warnings;
    }

    public void setWarnings(AssertionStatistics warnings) {
        this.warnings = warnings != null ? warnings : new AssertionStatistics();
    }

}

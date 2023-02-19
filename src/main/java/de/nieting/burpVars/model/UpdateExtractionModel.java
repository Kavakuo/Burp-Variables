package de.nieting.burpVars.model;

import de.nieting.burpVars.model.constants.ExtractionScopeMode;

public class UpdateExtractionModel {
    private ExtractionScopeMode extractionScope = ExtractionScopeMode.IN_SCOPE;
    private String extractionUrl;
    private MatchingModel extractionSearchModel = MatchingModel.forVariableUpdate();

    private boolean updateOnlyWhenRequestMatches = false;
    private MatchingModel requestSearchCondition = MatchingModel.forRequests();

    public ExtractionScopeMode getExtractionScope() {
        return extractionScope;
    }

    public void setExtractionScope(ExtractionScopeMode extractionScope) {
        this.extractionScope = extractionScope;
    }

    public String getExtractionUrl() {
        return extractionUrl;
    }

    public void setExtractionUrl(String extractionUrl) {
        this.extractionUrl = extractionUrl;
    }

    public MatchingModel getExtractionSearchModel() {
        return extractionSearchModel;
    }

    public void setExtractionSearchModel(MatchingModel extractionSearchModel) {
        this.extractionSearchModel = extractionSearchModel;
    }

    public MatchingModel getRequestSearchCondition() {
        return requestSearchCondition;
    }

    public void setRequestSearchCondition(MatchingModel requestSearchCondition) {
        this.requestSearchCondition = requestSearchCondition;
    }

    public boolean isUpdateOnlyWhenRequestMatches() {
        return updateOnlyWhenRequestMatches;
    }

    public void setUpdateOnlyWhenRequestMatches(boolean updateOnlyWhenRequestMatches) {
        this.updateOnlyWhenRequestMatches = updateOnlyWhenRequestMatches;
    }
}



package de.nieting.burpVars.model;

import de.nieting.burpVars.model.constants.ExtractionScopeMode;

public class UpdateExtractionModel {
    private ExtractionScopeMode extractionScope = ExtractionScopeMode.IN_SCOPE;
    private String extractionUrl;
    private SearchModel extractionSearchModel = SearchModel.forVariableUpdate();

    private boolean updateOnlyWhenRequestMatches = false;
    private SearchModel requestSearchCondition = SearchModel.forRequests();

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

    public SearchModel getExtractionSearchModel() {
        return extractionSearchModel;
    }

    public void setExtractionSearchModel(SearchModel extractionSearchModel) {
        this.extractionSearchModel = extractionSearchModel;
    }

    public SearchModel getRequestSearchCondition() {
        return requestSearchCondition;
    }

    public void setRequestSearchCondition(SearchModel requestSearchCondition) {
        this.requestSearchCondition = requestSearchCondition;
    }

    public boolean isUpdateOnlyWhenRequestMatches() {
        return updateOnlyWhenRequestMatches;
    }

    public void setUpdateOnlyWhenRequestMatches(boolean updateOnlyWhenRequestMatches) {
        this.updateOnlyWhenRequestMatches = updateOnlyWhenRequestMatches;
    }
}



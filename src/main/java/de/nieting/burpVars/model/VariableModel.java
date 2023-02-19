package de.nieting.burpVars.model;

import java.util.Date;

public class VariableModel {

    private String variableName;
    private String variableValue;
    private boolean updateAutomatically;

    private UpdateModel updateModel = new UpdateModel();
    private ReplaceModel replaceModel = new ReplaceModel();

    private Date lastUpdated;
    private Date lastReplaced;

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public String getVariableValue() {
        return variableValue;
    }

    public void setVariableValue(String variableValue) {
        if (variableValue != null && !variableValue.equals(this.variableValue))
            setLastUpdated(new Date());
        this.variableValue = variableValue;
    }

    public boolean isUpdateAutomatically() {
        return updateAutomatically;
    }

    public void setUpdateAutomatically(boolean updateAutomatically) {
        this.updateAutomatically = updateAutomatically;
    }

    public UpdateModel getUpdateModel() {
        return updateModel;
    }

    public void setUpdateModel(UpdateModel updateModel) {
        this.updateModel = updateModel;
    }

    public ReplaceModel getReplaceModel() {
        return replaceModel;
    }

    public void setReplaceModel(ReplaceModel replaceModel) {
        this.replaceModel = replaceModel;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Date getLastReplaced() {
        return lastReplaced;
    }

    public void setLastReplaced(Date lastReplaced) {
        this.lastReplaced = lastReplaced;
    }

    public void setLastReplacedNow() {
        setLastReplaced(new Date());
    }
}

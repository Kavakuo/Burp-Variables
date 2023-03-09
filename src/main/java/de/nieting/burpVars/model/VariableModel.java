package de.nieting.burpVars.model;

import burp.api.montoya.http.message.HttpRequestResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import de.nieting.burpVars.model.constants.HistoryUpdateReason;
import de.nieting.burpVars.model.constants.RelevantUpdateMessage;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class VariableModel {

    private String variableName;
    private String variableValue;
    private boolean updateAutomatically;

    private UpdateModel updateModel = new UpdateModel();
    private ReplaceModel replaceModel = new ReplaceModel();

    private HistoryListModel historyListModel = new HistoryListModel();

    private Date lastUpdated;
    private Date lastReplaced;

    @JsonIgnore
    private Timer timer = new Timer();

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
        if (variableValue == null || variableValue.equals(this.variableValue)) return;
        this.variableValue = variableValue;
        if (!DataModel.isInitialized) return;

        setLastUpdated(new Date());
        timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                var history = HistoryModel.manualUpdate(null, null, variableValue);
                history.setSource("Variable Editor");
                historyListModel.addHistoryModel(history);
                DataModel.saveToProject();
            }
        }, 750);
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

    public void updateVariableValue(HistoryModel historyModel) {
        if (historyModel.getNewVarValue() != null && !historyModel.getNewVarValue().equals(this.variableValue)) {
            setLastUpdated(new Date());
            variableValue = historyModel.getNewVarValue();
            historyListModel.addHistoryModel(historyModel);
        }
    }

    public HistoryListModel getHistoryListModel() {
        return historyListModel;
    }

    public void setHistoryListModel(HistoryListModel historyListModel) {
        timer.cancel();
        timer = new Timer();
        this.historyListModel = historyListModel;
    }
}

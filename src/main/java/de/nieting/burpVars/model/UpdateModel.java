package de.nieting.burpVars.model;

public class UpdateModel {

    private ToolSelectionModel toolSelectionModel = ToolSelectionModel.createUpdateToolSelection();
    private UpdateExtractionListModel updateExtractionListModel = new UpdateExtractionListModel();

    public ToolSelectionModel getToolSelectionModel() {
        return toolSelectionModel;
    }

    public void setToolSelectionModel(ToolSelectionModel toolSelection) {
        this.toolSelectionModel = toolSelection;
    }

    public UpdateExtractionListModel getUpdateExtractionListModel() {
        return updateExtractionListModel;
    }

    public void setUpdateExtractionListModel(UpdateExtractionListModel updateExtractionListModel) {
        this.updateExtractionListModel = updateExtractionListModel;
    }
}

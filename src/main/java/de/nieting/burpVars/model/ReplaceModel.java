package de.nieting.burpVars.model;

public class ReplaceModel {

    private ToolSelectionModel toolSelectionModel = ToolSelectionModel.createReplaceToolSelection();
    private boolean replaceOnlyInScope;
    private ReplaceListModel replaceListModel = new ReplaceListModel();

    public ToolSelectionModel getToolSelectionModel() {
        return toolSelectionModel;
    }

    public void setToolSelectionModel(ToolSelectionModel toolSelectionModel) {
        this.toolSelectionModel = toolSelectionModel;
    }

    public boolean isReplaceOnlyInScope() {
        return replaceOnlyInScope;
    }

    public void setReplaceOnlyInScope(boolean replaceOnlyInScope) {
        this.replaceOnlyInScope = replaceOnlyInScope;
    }

    public ReplaceListModel getReplaceListModel() {
        return replaceListModel;
    }

    public void setReplaceListModel(ReplaceListModel replaceListModel) {
        this.replaceListModel = replaceListModel;
    }
}

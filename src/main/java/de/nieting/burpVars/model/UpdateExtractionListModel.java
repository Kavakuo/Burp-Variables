package de.nieting.burpVars.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE
)
public class UpdateExtractionListModel extends AbstractListModel<String> {

    @JsonProperty("extractionList")
    private List<UpdateExtractionModel> list = new ArrayList<>();

    @Override
    public int getSize() {
        return list.size();
    }

    @Override
    public String getElementAt(int index) {
        var a = list.get(index).getExtractionSearchModel();
        if (a.getMatchString() != null && !a.getMatchString().equals("")) {
            return a.getMatchString();
        }
        return "Undefined";
    }


    public void addUpdateExtractionModel() {
        list.add(new UpdateExtractionModel());
        fireIntervalAdded(this, list.size() - 1, list.size() - 1);
    }

    public void removeUpdateExtractionModel(int index) {
        list.remove(index);
        fireIntervalRemoved(this, 0, Math.max(0, list.size() - 1));
    }

    public boolean moveUp(int index) {
        if (index == 0) return false;

        var selected = list.get(index);
        var before = list.get(index - 1);
        list.set(index - 1, selected);
        list.set(index, before);
        fireContentsChanged(this, index - 1, index);
        return true;
    }

    public boolean moveDown(int index) {
        if (index == list.size() - 1) return false;

        var selected = list.get(index);
        var next = list.get(index + 1);
        list.set(index + 1, selected);
        list.set(index, next);
        fireContentsChanged(this, index + 1, index);
        return true;
    }

    public List<UpdateExtractionModel> getList() {
        return list;
    }

    public void setList(List<UpdateExtractionModel> list) {
        this.list = list;
    }

    public void changed() {
        fireContentsChanged(this, 0, list.size() - 1);
    }

}

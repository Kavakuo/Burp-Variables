package de.nieting.burpVars.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.swing.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE
)
public class HistoryListModel extends AbstractListModel<String> {

    @JsonProperty("historyList")
    private LinkedList<HistoryModel> list = new LinkedList<>();

    @Override
    public int getSize() {
        return list.size();
    }

    @Override
    public String getElementAt(int index) {
        var a = list.get(index);
        return a.getListEntry();
    }


    public void addHistoryModel(HistoryModel a) {
        list.addFirst(a);
        while (list.size() > 20) {
            list.removeLast();
            fireIntervalRemoved(this, list.size() - 1, list.size() - 1);
        }
        fireIntervalAdded(this, 0, 0);
        fireContentsChanged(this, 1, list.size() - 1);
    }

    public List<HistoryModel> getList() {
        return list;
    }

    public void setList(LinkedList<HistoryModel> list) {
        this.list = list;
    }

    public void changed() {
        fireContentsChanged(this, 0, list.size() - 1);
    }

}

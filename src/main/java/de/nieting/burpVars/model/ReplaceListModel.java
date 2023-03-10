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
public class ReplaceListModel extends AbstractListModel<String> {

    @JsonProperty("replaceList")
    private List<SearchModel> list = new ArrayList<>();

    @Override
    public int getSize() {
        return list.size();
    }

    @Override
    public String getElementAt(int index) {
        var a = list.get(index);
        if (a.getSearchString() != null && !a.getSearchString().equals("")) {
            return a.getSearchString();
        }
        return "Undefined";
    }


    public void addReplaceMatchModel() {
        list.add(SearchModel.forRequests());
        fireIntervalAdded(this, list.size() - 1, list.size() - 1);
    }

    public void removeReplaceMatchModel(int index) {
        list.remove(index);
        fireIntervalRemoved(this, 0, Math.max(0, list.size() - 1));
    }

    public List<SearchModel> getList() {
        return list;
    }

    public void setList(List<SearchModel> list) {
        this.list = list;
    }

    public void changed() {
        fireContentsChanged(this, 0, list.size() - 1);
    }

}

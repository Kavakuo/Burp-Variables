package de.nieting.burpVars;

import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import burp.api.montoya.ui.contextmenu.MessageEditorHttpRequestResponse;
import de.nieting.burpVars.model.DataModel;
import de.nieting.burpVars.model.VariableModel;
import de.nieting.burpVars.model.constants.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import static burp.api.montoya.ui.contextmenu.InvocationType.INTRUDER_PAYLOAD_POSITIONS;
import static burp.api.montoya.ui.contextmenu.InvocationType.MESSAGE_EDITOR_REQUEST;

public class VariableContextMenu implements ContextMenuItemsProvider {

    private final DataModel dataModel;

    public VariableContextMenu(DataModel model) {
        this.dataModel = model;
    }

    private List<Component> getVariableUpdateMenu(String value) {
        var ret = new ArrayList<Component>();
        for (var variable : dataModel.getVariables()) {
            var item = new JMenuItem(variable.getVariableName());
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dataModel.updateVariableValue(variable, value);
                }
            });
            ret.add(item);
        }
        return ret;
    }



    private void insertVariableIntoEditor(VariableModel variable, MessageEditorHttpRequestResponse editor) {
        var req = editor.requestResponse().request();
        var reqString = req.toString();
        var selectionOffset = editor.selectionOffsets();
        if (selectionOffset.isPresent()) {
            var selStartIdx = selectionOffset.get().startIndexInclusive();
            var selEndIdx = selectionOffset.get().endIndexExclusive();
            var tmpReq = reqString.substring(0, selStartIdx);
            tmpReq += "${" + Constants.VAR_PREFIX + variable.getVariableName() + "}";
            tmpReq += reqString.substring(selEndIdx);
            reqString = tmpReq;
        } else {
            var tmpReq = reqString.substring(0, editor.caretPosition());
            tmpReq += "${" + Constants.VAR_PREFIX + variable.getVariableName() + "}";
            tmpReq += reqString.substring(editor.caretPosition());
            reqString = tmpReq;
        }
        editor.setRequest(HttpRequest.httpRequest(reqString));
    }

    private List<Component> getVariableInsertMenu(MessageEditorHttpRequestResponse editor) {
        var ret = new ArrayList<Component>();
        for (var variable : dataModel.getVariables()) {
            var item = new JMenuItem(variable.getVariableName());
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    insertVariableIntoEditor(variable, editor);
                }
            });
            ret.add(item);
        }

        return ret;
    }


    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        var menu = new ArrayList<Component>();

        var optEditor = event.messageEditorRequestResponse();
        if (optEditor.isEmpty()) return null;

        var editor = optEditor.get();
        var selectionOffset = editor.selectionOffsets();
        if (selectionOffset.isPresent()) {
            var updateVariableMenu = new JMenu("Update Variable");
            var httpMsg = editor.requestResponse().request().toString();
            if (editor.selectionContext() == MessageEditorHttpRequestResponse.SelectionContext.RESPONSE) {
                httpMsg = editor.requestResponse().response().toString();
            }
            var selectedText = httpMsg.substring(selectionOffset.get().startIndexInclusive(), selectionOffset.get().endIndexExclusive());
            getVariableUpdateMenu(selectedText).forEach(updateVariableMenu::add);
            menu.add(updateVariableMenu);
        }

        if (event.isFrom(MESSAGE_EDITOR_REQUEST, INTRUDER_PAYLOAD_POSITIONS)) {
            var insertVariableMenu = new JMenu("Insert Variable");
            getVariableInsertMenu(editor).forEach(insertVariableMenu::add);

            menu.add(insertVariableMenu);
        }

        return menu;
    }
}

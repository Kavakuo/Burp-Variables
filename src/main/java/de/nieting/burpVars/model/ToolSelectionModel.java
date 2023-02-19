package de.nieting.burpVars.model;

import burp.api.montoya.core.ToolType;

import java.util.ArrayList;
import java.util.List;

public class ToolSelectionModel {

    private boolean proxy = false;
    private boolean scanner = false;
    private boolean repeater = false;
    private boolean extensions = false;
    private boolean intruder = false;


    public static ToolSelectionModel createUpdateToolSelection() {
        var a = new ToolSelectionModel();
        a.setRepeater(true);
        return a;
    }

    public static ToolSelectionModel createReplaceToolSelection() {
        var a = new ToolSelectionModel();
        a.setRepeater(true);
        a.setIntruder(true);
        a.setExtensions(true);
        return a;
    }

    public boolean isProxy() {
        return proxy;
    }

    public void setProxy(boolean proxy) {
        this.proxy = proxy;
    }

    public boolean isScanner() {
        return scanner;
    }

    public void setScanner(boolean scanner) {
        this.scanner = scanner;
    }

    public boolean isRepeater() {
        return repeater;
    }

    public void setRepeater(boolean repeater) {
        this.repeater = repeater;
    }

    public boolean isExtensions() {
        return extensions;
    }

    public void setExtensions(boolean extensions) {
        this.extensions = extensions;
    }

    public boolean isIntruder() {
        return intruder;
    }

    public void setIntruder(boolean intruder) {
        this.intruder = intruder;
    }


    public boolean validToolSource(ToolType toolType) {
        List<ToolType> toolTypeList = new ArrayList<>();
        if (proxy) toolTypeList.add(ToolType.PROXY);
        if (scanner) toolTypeList.add(ToolType.SCANNER);
        if (repeater) toolTypeList.add(ToolType.REPEATER);
        if (extensions) toolTypeList.add(ToolType.EXTENSIONS);
        if (intruder) toolTypeList.add(ToolType.INTRUDER);

        for (var i: toolTypeList) {
            if (i == toolType) return true;
        }
        return false;
    }
}

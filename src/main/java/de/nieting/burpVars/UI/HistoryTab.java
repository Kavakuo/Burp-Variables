package de.nieting.burpVars.UI;

import burp.api.montoya.core.Marker;
import burp.api.montoya.ui.editor.EditorOptions;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import de.nieting.burpVars.API;
import de.nieting.burpVars.model.DataModel;
import de.nieting.burpVars.model.HistoryListModel;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.util.Locale;

public class HistoryTab {
    public JList historyList;
    public JTabbedPane tabbedPane1;
    public JPanel requestPanel;
    public JPanel responsePanel;
    public JPanel historyPanel;
    public JPanel originalRequestPanel;
    public JTextArea variableValueTextField;
    public JLabel eventLabel;
    public JLabel timeLabel;
    public JLabel sourceLabel;
    public JPanel historyInfoPanel;

    private HistoryListModel historyListModel;

    private DefaultListSelectionModel historyListSelectionModel = new DefaultListSelectionModel();

    private HttpRequestEditor requestEditor;
    private HttpRequestEditor beforeReplaceRequestEditor;
    private HttpResponseEditor responseEditor;

    private ListDataListener dataListener;

    public HistoryTab() {
        historyList.setSelectionModel(historyListSelectionModel);
        historyListSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        if (API.getInstance() != null) {
            requestEditor = API.getInstance().getApi().userInterface().createHttpRequestEditor(EditorOptions.READ_ONLY);
            beforeReplaceRequestEditor = API.getInstance().getApi().userInterface().createHttpRequestEditor(EditorOptions.READ_ONLY);
            responseEditor = API.getInstance().getApi().userInterface().createHttpResponseEditor(EditorOptions.READ_ONLY);

            requestPanel.add(requestEditor.uiComponent(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
            originalRequestPanel.add(beforeReplaceRequestEditor.uiComponent(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
            responsePanel.add(responseEditor.uiComponent(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        }

        historyListSelectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateUIFromDataModel();
            }
        });

        dataListener = new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                // this will also increase the selection index
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {

            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                if (historyList.getSelectedIndex() == -1 && historyListModel.getSize() > 0) {
                    historyListSelectionModel.setSelectionInterval(0, 0);
                }
            }
        };
    }

    public void updateUIFromDataModel() {
        tabbedPane1.setEnabledAt(0, false);
        tabbedPane1.setEnabledAt(1, false);
        tabbedPane1.setEnabledAt(2, false);
        tabbedPane1.setVisible(false);

        if (historyList.getSelectedIndex() == -1) {
            historyInfoPanel.setVisible(false);
            return;
        }
        historyInfoPanel.setVisible(true);

        var historyModel = historyListModel.getList().get(historyList.getSelectedIndex());
        if (historyModel.getRequest() != null) {
            tabbedPane1.setVisible(true);
            tabbedPane1.setEnabledAt(1, true);
            requestEditor.setRequest(historyModel.getRequest());
        }

        if (historyModel.getResponse() != null) {
            tabbedPane1.setVisible(true);
            tabbedPane1.setEnabledAt(2, true);
            responseEditor.setResponse(historyModel.getResponse());
        }

        if (historyModel.getBeforeReplaceRequest() != null) {
            tabbedPane1.setVisible(true);
            tabbedPane1.setEnabledAt(0, true);
            beforeReplaceRequestEditor.setRequest(historyModel.getBeforeReplaceRequest());
        }

        eventLabel.setText(historyModel.getUpdateReason().getReason());
        timeLabel.setText(DataModel.formatDate(historyModel.getTimestamp()));
        sourceLabel.setText(historyModel.getUISource());
        variableValueTextField.setText(historyModel.getNewVarValue());

        if (historyModel.getRelevantUpdateMessage() == null || !tabbedPane1.isVisible()) {
            return;
        }

        switch (historyModel.getRelevantUpdateMessage()) {
            case RESPONSE -> {
                tabbedPane1.setSelectedIndex(2);
            }
            case REQUEST -> {
                tabbedPane1.setSelectedIndex(1);
            }
        }
    }

    public void updateDataModel() {
    }

    public HistoryListModel getHistoryListModel() {
        return historyListModel;
    }

    public void setHistoryListModel(HistoryListModel historyListModel) {
        if (this.historyListModel != null) {
            this.historyListModel.removeListDataListener(dataListener);
        }

        this.historyListModel = historyListModel;
        historyList.setModel(historyListModel);
        if (historyListModel.getList().size() > 0) {
            historyListSelectionModel.setSelectionInterval(0, 0);
        }

        historyListModel.addListDataListener(dataListener);

        updateUIFromDataModel();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        historyPanel = new JPanel();
        historyPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setDividerLocation(256);
        historyPanel.add(splitPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        splitPane1.setLeftComponent(scrollPane1);
        historyList = new JList();
        historyList.setSelectionMode(0);
        scrollPane1.setViewportView(historyList);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 1, new Insets(5, 0, 0, 0), -1, -1));
        splitPane1.setRightComponent(panel1);
        tabbedPane1 = new JTabbedPane();
        tabbedPane1.setTabPlacement(3);
        panel1.add(tabbedPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        originalRequestPanel = new JPanel();
        originalRequestPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("Original Request", originalRequestPanel);
        requestPanel = new JPanel();
        requestPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("Request", requestPanel);
        responsePanel = new JPanel();
        responsePanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("Response", responsePanel);
        historyInfoPanel = new JPanel();
        historyInfoPanel.setLayout(new GridLayoutManager(5, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(historyInfoPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$(null, Font.BOLD, -1, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setText("Time");
        historyInfoPanel.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        historyInfoPanel.add(spacer1, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        timeLabel = new JLabel();
        timeLabel.setText("Label");
        historyInfoPanel.add(timeLabel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        Font label2Font = this.$$$getFont$$$(null, Font.BOLD, -1, label2.getFont());
        if (label2Font != null) label2.setFont(label2Font);
        label2.setText("Source");
        historyInfoPanel.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sourceLabel = new JLabel();
        sourceLabel.setText("Label");
        historyInfoPanel.add(sourceLabel, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        Font label3Font = this.$$$getFont$$$(null, Font.BOLD, -1, label3.getFont());
        if (label3Font != null) label3.setFont(label3Font);
        label3.setText("(New) variable value");
        historyInfoPanel.add(label3, new GridConstraints(3, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        historyInfoPanel.add(scrollPane2, new GridConstraints(4, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(-1, 100), null, 0, false));
        variableValueTextField = new JTextArea();
        variableValueTextField.setAutoscrolls(false);
        variableValueTextField.setFocusCycleRoot(false);
        variableValueTextField.setLineWrap(true);
        variableValueTextField.setWrapStyleWord(false);
        scrollPane2.setViewportView(variableValueTextField);
        final JLabel label4 = new JLabel();
        Font label4Font = this.$$$getFont$$$(null, Font.BOLD, -1, label4.getFont());
        if (label4Font != null) label4.setFont(label4Font);
        label4.setText("Event");
        historyInfoPanel.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        eventLabel = new JLabel();
        eventLabel.setText("Label");
        historyInfoPanel.add(eventLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return historyPanel;
    }

}

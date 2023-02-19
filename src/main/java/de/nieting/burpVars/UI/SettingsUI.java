package de.nieting.burpVars.UI;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import de.nieting.burpVars.API;
import de.nieting.burpVars.model.DataModel;
import de.nieting.burpVars.model.ReplaceListModel;
import de.nieting.burpVars.model.UpdateExtractionListModel;
import de.nieting.burpVars.model.VariableModel;
import de.nieting.burpVars.model.constants.Constants;
import de.nieting.burpVars.model.constants.ExtractionScopeMode;
import de.nieting.burpVars.model.constants.RegexCaseSensitivity;
import de.nieting.burpVars.model.constants.RegexMatchOption;
import de.nieting.burpVars.model.constants.SearchInLocation;
import de.nieting.burpVars.model.constants.SearchMode;
import de.nieting.burpVars.model.constants.VarTableColumn;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Vector;

public class SettingsUI {
    private static final Logger LOGGER = LogManager.getLogger(SettingsUI.class);

    public JPanel panel1;

    private JTable OverviewTable;
    private JTabbedPane ExtractionPane;
    private JButton addVariableButton;
    private JButton removeVariableButton;
    private JCheckBox replaceProxyCheckBox;
    private JCheckBox replaceRepeaterCheckBox;
    private JCheckBox replaceIntruderCheckBox;
    private JCheckBox replaceScannerCheckBox;
    private JCheckBox replaceExtensionsCheckBox;
    private JList replaceList;
    private JButton replaceAddButton;
    private JButton replaceRemoveButton;
    private JTextField variableValueTextField;
    private JCheckBox updateVariableValueAutomaticallyCheckBox;
    private JCheckBox proxyCheckBox1;
    private JCheckBox scannerCheckBox1;
    private JCheckBox repeaterCheckBox1;
    private JCheckBox extensionsCheckBox1;
    private JCheckBox intruderCheckBox1;
    private JButton addButton2;
    private JButton removeButton2;
    private JList updateList;
    private JTextField extractionRegexTextField;
    private JRadioButton extractionCaseSensitiveRadio;
    private JRadioButton extractionCaseInsensitiveRadio;
    private JCheckBox extractionResponseHeaderCheckBox;
    private JCheckBox extractionResponseBodyCheckBox;
    private JRadioButton inScopeResponsesRadioButton;
    private JRadioButton specificURLRadioButton;
    private JTextField updateUrlTextField;
    private JTextField variableNameTextField;
    public JCheckBox onlyReplaceInScope;
    public JRadioButton allResponsesRadioButton;
    public JPanel extractControlPanel;
    public JCheckBox requestConditionCheckBox;
    public JRadioButton requestConditionContainsRadio;
    public JRadioButton requestConditionRegexRadio;
    public JPanel requestMatchingConditionPanel;
    public JTextField requestConditionMatchingTextField;
    public JRadioButton requestConditionMatchingRadio;
    public JRadioButton requestConditionNotMatchingRadio;
    public JRadioButton requestConditionCaseSensitiveRadio;
    public JRadioButton requestConditionCaseInsensitiveRadio;
    public JCheckBox requestConditionReqHeaderCheckbox;
    public JCheckBox requestConditionReqBodyCheckbox;
    public JRadioButton replaceRequestContainsRadio;
    public JRadioButton replaceRequestRegexRadio;
    public JTextField replaceRequestMatchingTextField;
    public JRadioButton replaceRequestMatchingRadio;
    public JRadioButton replaceRequestNotMatchingRadio;
    public JRadioButton replaceRequestCaseSensitiveRadio;
    public JRadioButton replaceRequestCaseInsensitiveRadio;
    public JCheckBox replaceRequestSearchURLCheckbox;
    public JCheckBox replaceRequestSearchHeaderCheckbox;
    public JCheckBox replaceRequestSearchBodyCheckbox;
    public JPanel replaceControlPanel;
    public JPanel updateAutoControlPanel;
    public JPanel variableTab;
    public JPanel replaceTab;
    public JButton upButton;
    public JButton downButton;
    public HelpLabel extractRegexHelpLabel;
    public HelpLabel extractionHelpLabel;
    public HelpLabel updateRestrictionHelpLabel;
    public HelpLabel variableNameHelpLabel;
    public JButton exportDataButton;
    public JButton importDataButton;
    public JComboBox logLevelComboBox;


    private DataModel dataModel;
    private DefaultListSelectionModel variableTableSelectionModel = new DefaultListSelectionModel();
    private DefaultListSelectionModel updateListSelectionModel = new DefaultListSelectionModel();
    private DefaultListSelectionModel replaceListSelectionModel = new DefaultListSelectionModel();

    private int doNotWriteDataToModel = 0;

    private JFileChooser fc = new JFileChooser();

    private OnChangeListener getChangeListener() {
        return new OnChangeListener(() -> {
            this.updateDataModel();
            //this.setData();
        });
    }

    private OnChangeListener getChangeListener(Runnable r) {
        return new OnChangeListener(() -> {
            this.updateDataModel();
            if (doNotWriteDataToModel == 0) r.run();
        });
    }

    private VariableModel selectedVariable;

    public SettingsUI(DataModel dataModel) {
        this.dataModel = dataModel;
        $$$setupUI$$$();
        setupVariableTable();
        setupVariableTab();
        setupReplaceTab();

        if (dataModel.getVariables().size() > 0) {
            variableTableSelectionModel.setSelectionInterval(0, 0);
        }

        updateUIFromDataModel();
        this.dataModel.updateUICallback = (Integer row) -> {
            if (OverviewTable.getSelectedRow() == row) {
                updateUIFromDataModel();
            }
        };
        exportDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int retVal = fc.showSaveDialog(panel1);
                if (retVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    dataModel.saveModelToFile(file);
                }
            }
        });

        importDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int retVal = fc.showOpenDialog(panel1);
                if (retVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();

                    try {
                        dataModel.loadModelFromFile(file);
                        if (dataModel.getRowCount() == 0) {
                            updateUIFromDataModel();
                        } else {
                            variableTableSelectionModel.setSelectionInterval(0, 0);
                        }
                    } catch (Exception ex) {
                        LOGGER.error("Failed to load data from file", ex);
                    }
                }
            }
        });


        var level = API.getInstance().getApi().persistence().preferences().getString("LOG_LEVEL");
        if (level != null) {
            logLevelComboBox.setSelectedItem(level);
        } else {
            logLevelComboBox.setSelectedItem("INFO");
        }

        logLevelComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                var selectedLogLevel = logLevelComboBox.getSelectedItem().toString();
                LOGGER.info("log level changed to {}", selectedLogLevel);
                API.getInstance().getApi().persistence().preferences().setString("LOG_LEVEL", selectedLogLevel);

                var level = Level.getLevel(selectedLogLevel);
                Configurator.setAllLevels(LogManager.getRootLogger().getName(), level);
            }
        });
    }


    private void setupVariableTab() {
        variableNameTextField.getDocument().addDocumentListener(getChangeListener(() -> {
            dataModel.fireTableCellUpdated(OverviewTable.getSelectedRow(), VarTableColumn.VARIABLE_NAME.getColumnIdx());
        }));
        variableValueTextField.getDocument().addDocumentListener(getChangeListener(() -> {
            dataModel.fireTableCellUpdated(OverviewTable.getSelectedRow(), VarTableColumn.VARIABLE_VALUE.getColumnIdx());
            dataModel.fireTableCellUpdated(OverviewTable.getSelectedRow(), VarTableColumn.LAST_UPDATED.getColumnIdx());
        }));
        updateVariableValueAutomaticallyCheckBox.addActionListener(getChangeListener(() -> {
            dataModel.fireTableCellUpdated(OverviewTable.getSelectedRow(), VarTableColumn.AUTO_UPDATE.getColumnIdx());
        }));
        proxyCheckBox1.addActionListener(getChangeListener());
        scannerCheckBox1.addActionListener(getChangeListener());
        repeaterCheckBox1.addActionListener(getChangeListener());
        extensionsCheckBox1.addActionListener(getChangeListener());
        intruderCheckBox1.addActionListener(getChangeListener());

        updateListSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        updateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        updateList.setSelectionModel(updateListSelectionModel);

        allResponsesRadioButton.addActionListener(getChangeListener());
        inScopeResponsesRadioButton.addActionListener(getChangeListener());
        specificURLRadioButton.addActionListener(getChangeListener());
        updateUrlTextField.getDocument().addDocumentListener(getChangeListener());
        requestConditionCheckBox.addActionListener(getChangeListener());
        requestConditionMatchingTextField.getDocument().addDocumentListener(getChangeListener());
        requestConditionRegexRadio.addActionListener(getChangeListener());
        requestConditionContainsRadio.addActionListener(getChangeListener());
        requestConditionMatchingRadio.addActionListener(getChangeListener());
        requestConditionNotMatchingRadio.addActionListener(getChangeListener());
        requestConditionCaseInsensitiveRadio.addActionListener(getChangeListener());
        requestConditionCaseSensitiveRadio.addActionListener(getChangeListener());
        requestConditionReqHeaderCheckbox.addActionListener(getChangeListener());
        requestConditionReqBodyCheckbox.addActionListener(getChangeListener());

        extractionRegexTextField.getDocument().addDocumentListener(getChangeListener(() -> {
            var listM = (UpdateExtractionListModel) updateList.getModel();
            listM.changed();
        }));
        extractionCaseSensitiveRadio.addActionListener(getChangeListener());
        extractionCaseInsensitiveRadio.addActionListener(getChangeListener());
        extractionResponseHeaderCheckBox.addActionListener(getChangeListener());
        extractionResponseBodyCheckBox.addActionListener(getChangeListener());

        updateListSelectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateUIFromDataModel();
            }
        });

        addButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                var model = selectedVariable.getUpdateModel().getUpdateExtractionListModel();
                model.addUpdateExtractionModel();
                updateList.setSelectedIndex(model.getSize() - 1);
                dataModel.saveToProject();
            }
        });

        removeButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int sel = updateList.getSelectedIndex();
                if (sel == -1) return;

                var model = selectedVariable.getUpdateModel().getUpdateExtractionListModel();
                model.removeUpdateExtractionModel(sel);
                if (sel >= model.getSize()) {
                    sel = model.getSize() - 1;
                }
                updateList.setSelectedIndex(sel);
                dataModel.saveToProject();
            }
        });

        upButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int sel = updateList.getSelectedIndex();
                if (sel == -1 || sel == 0) return;

                var model = selectedVariable.getUpdateModel().getUpdateExtractionListModel();
                model.moveUp(sel);
                updateListSelectionModel.setSelectionInterval(sel - 1, sel - 1);
                dataModel.saveToProject();
            }
        });

        downButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int sel = updateList.getSelectedIndex();
                if (sel == -1) return;

                var model = selectedVariable.getUpdateModel().getUpdateExtractionListModel();
                var success = model.moveDown(sel);
                if (success) {
                    updateListSelectionModel.setSelectionInterval(sel + 1, sel + 1);
                    dataModel.saveToProject();
                }
            }
        });
    }

    private void setupReplaceTab() {

        replaceProxyCheckBox.addActionListener(getChangeListener());
        replaceScannerCheckBox.addActionListener(getChangeListener());
        replaceRepeaterCheckBox.addActionListener(getChangeListener());
        replaceExtensionsCheckBox.addActionListener(getChangeListener());
        replaceIntruderCheckBox.addActionListener(getChangeListener());

        onlyReplaceInScope.addActionListener(getChangeListener(() -> {
            dataModel.fireTableCellUpdated(OverviewTable.getSelectedRow(), VarTableColumn.REPLACE_ONLY_IN_SCOPE.getColumnIdx());
        }));

        replaceRequestContainsRadio.addActionListener(getChangeListener());
        replaceRequestRegexRadio.addActionListener(getChangeListener());
        replaceRequestMatchingTextField.getDocument().addDocumentListener(getChangeListener(() -> {
            var listM = (ReplaceListModel) replaceList.getModel();
            listM.changed();
        }));
        replaceRequestMatchingRadio.addActionListener(getChangeListener());
        replaceRequestNotMatchingRadio.addActionListener(getChangeListener());
        replaceRequestCaseInsensitiveRadio.addActionListener(getChangeListener());
        replaceRequestCaseSensitiveRadio.addActionListener(getChangeListener());
        replaceRequestSearchURLCheckbox.addActionListener(getChangeListener());
        replaceRequestSearchHeaderCheckbox.addActionListener(getChangeListener());
        replaceRequestSearchBodyCheckbox.addActionListener(getChangeListener());

        replaceListSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        replaceList.setSelectionModel(replaceListSelectionModel);

        replaceListSelectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateUIFromDataModel();
            }
        });

        replaceAddButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                var model = selectedVariable.getReplaceModel().getReplaceListModel();
                model.addReplaceMatchModel();
                replaceList.setSelectedIndex(model.getSize() - 1);
                dataModel.saveToProject();
            }
        });

        replaceRemoveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int sel = replaceList.getSelectedIndex();
                if (sel == -1) return;

                var model = selectedVariable.getReplaceModel().getReplaceListModel();
                model.removeReplaceMatchModel(sel);
                if (sel >= model.getSize()) {
                    sel = model.getSize() - 1;
                }
                replaceList.setSelectedIndex(sel);
                dataModel.saveToProject();
            }
        });
    }


    private void setupVariableTable() {
        OverviewTable.setModel(dataModel);
        OverviewTable.setSelectionModel(variableTableSelectionModel);
        variableTableSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        addVariableButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dataModel.addNewVariable();
                variableTableSelectionModel.setSelectionInterval(dataModel.getRowCount() - 1, dataModel.getRowCount() - 1);
            }
        });
        removeVariableButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = OverviewTable.getSelectedRow();
                dataModel.removeVariableAtIndex(selectedRow);
                if (selectedRow >= dataModel.getRowCount()) {
                    selectedRow = dataModel.getRowCount() - 1;
                }
                variableTableSelectionModel.setSelectionInterval(selectedRow, selectedRow);
            }
        });

        variableTableSelectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int selRow = OverviewTable.getSelectedRow();
                if (selRow == -1) {
                    if (dataModel.getRowCount() == 0) {
                        selectedVariable = null;
                        updateList.setListData(new Vector());
                        replaceList.setListData(new Vector());
                        updateUIFromDataModel();
                    }
                    return;
                }

                selectedVariable = dataModel.getVariables().get(selRow);
                updateList.setModel(selectedVariable.getUpdateModel().getUpdateExtractionListModel());
                replaceList.setModel(selectedVariable.getReplaceModel().getReplaceListModel());
                updateUIFromDataModel();
            }
        });
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setMinimumSize(new Dimension(500, 700));
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setDividerLocation(200);
        splitPane1.setDividerSize(20);
        splitPane1.setOrientation(0);
        panel1.add(splitPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(3, 4, new Insets(0, 0, 0, 0), -1, -1));
        splitPane1.setLeftComponent(panel2);
        addVariableButton = new JButton();
        addVariableButton.setText("Add Variable");
        panel2.add(addVariableButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        removeVariableButton = new JButton();
        removeVariableButton.setText("Remove Variable");
        panel2.add(removeVariableButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel2.add(spacer2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, null, new Dimension(10, -1), null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel2.add(spacer3, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 5), null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel2.add(scrollPane1, new GridConstraints(0, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        OverviewTable = new JTable();
        scrollPane1.setViewportView(OverviewTable);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane1.setRightComponent(panel3);
        ExtractionPane = new JTabbedPane();
        ExtractionPane.setVisible(true);
        panel3.add(ExtractionPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        scrollPane2.setEnabled(true);
        scrollPane2.setVisible(false);
        ExtractionPane.addTab("Variable", scrollPane2);
        variableTab = new JPanel();
        variableTab.setLayout(new GridLayoutManager(5, 3, new Insets(10, 10, 0, 0), -1, -1));
        variableTab.setVisible(true);
        scrollPane2.setViewportView(variableTab);
        final Spacer spacer4 = new Spacer();
        variableTab.add(spacer4, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        updateVariableValueAutomaticallyCheckBox = new JCheckBox();
        updateVariableValueAutomaticallyCheckBox.setText("Update variable value automatically");
        variableTab.add(updateVariableValueAutomaticallyCheckBox, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        updateAutoControlPanel = new JPanel();
        updateAutoControlPanel.setLayout(new GridLayoutManager(7, 7, new Insets(0, 0, 0, 0), -1, -1));
        variableTab.add(updateAutoControlPanel, new GridConstraints(3, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Update values from responses from");
        updateAutoControlPanel.add(label1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        updateAutoControlPanel.add(spacer5, new GridConstraints(6, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        proxyCheckBox1 = new JCheckBox();
        proxyCheckBox1.setText("Proxy");
        updateAutoControlPanel.add(proxyCheckBox1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        scannerCheckBox1 = new JCheckBox();
        scannerCheckBox1.setText("Scanner");
        updateAutoControlPanel.add(scannerCheckBox1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(119, 22), null, 0, false));
        extensionsCheckBox1 = new JCheckBox();
        extensionsCheckBox1.setText("Extensions");
        updateAutoControlPanel.add(extensionsCheckBox1, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(119, 22), null, 0, false));
        repeaterCheckBox1 = new JCheckBox();
        repeaterCheckBox1.setText("Repeater");
        updateAutoControlPanel.add(repeaterCheckBox1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        intruderCheckBox1 = new JCheckBox();
        intruderCheckBox1.setText("Intruder");
        updateAutoControlPanel.add(intruderCheckBox1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(5, 4, new Insets(0, 0, 0, 0), -1, -1));
        updateAutoControlPanel.add(panel4, new GridConstraints(1, 3, 6, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        extractControlPanel = new JPanel();
        extractControlPanel.setLayout(new GridLayoutManager(8, 3, new Insets(0, 0, 0, 0), -1, -1));
        extractControlPanel.setEnabled(true);
        extractControlPanel.setVisible(true);
        panel4.add(extractControlPanel, new GridConstraints(0, 2, 5, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        extractControlPanel.add(spacer6, new GridConstraints(7, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        inScopeResponsesRadioButton = new JRadioButton();
        inScopeResponsesRadioButton.setText("In-Scope Responses");
        extractControlPanel.add(inScopeResponsesRadioButton, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        specificURLRadioButton = new JRadioButton();
        specificURLRadioButton.setText("Specific URL");
        extractControlPanel.add(specificURLRadioButton, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        updateUrlTextField = new JTextField();
        Font updateUrlTextFieldFont = this.$$$getFont$$$("Monaco", -1, -1, updateUrlTextField.getFont());
        if (updateUrlTextFieldFont != null) updateUrlTextField.setFont(updateUrlTextFieldFont);
        extractControlPanel.add(updateUrlTextField, new GridConstraints(3, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(250, -1), null, 0, false));
        allResponsesRadioButton = new JRadioButton();
        allResponsesRadioButton.setText("All Responses");
        extractControlPanel.add(allResponsesRadioButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Update only from");
        extractControlPanel.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        requestConditionCheckBox = new JCheckBox();
        requestConditionCheckBox.setText("Update only if request matches");
        extractControlPanel.add(requestConditionCheckBox, new GridConstraints(4, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
        extractControlPanel.add(panel5, new GridConstraints(6, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel5.setBorder(BorderFactory.createTitledBorder(null, "Variable Value Extraction", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        extractionRegexTextField = new JTextField();
        Font extractionRegexTextFieldFont = this.$$$getFont$$$("Monaco", -1, -1, extractionRegexTextField.getFont());
        if (extractionRegexTextFieldFont != null) extractionRegexTextField.setFont(extractionRegexTextFieldFont);
        panel5.add(extractionRegexTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(250, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Case sensitivity");
        panel5.add(label3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        extractionCaseSensitiveRadio = new JRadioButton();
        extractionCaseSensitiveRadio.setText("Case sensitive");
        panel5.add(extractionCaseSensitiveRadio, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        extractionCaseInsensitiveRadio = new JRadioButton();
        extractionCaseInsensitiveRadio.setText("Case insensitive");
        panel5.add(extractionCaseInsensitiveRadio, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Search in");
        panel5.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        extractionResponseHeaderCheckBox = new JCheckBox();
        extractionResponseHeaderCheckBox.setText("Response Header");
        panel5.add(extractionResponseHeaderCheckBox, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        extractionResponseBodyCheckBox = new JCheckBox();
        extractionResponseBodyCheckBox.setText("Response Body");
        panel5.add(extractionResponseBodyCheckBox, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel5.add(panel6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Extraction regex");
        label5.setToolTipText("");
        panel6.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel6.add(extractRegexHelpLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        requestMatchingConditionPanel = new JPanel();
        requestMatchingConditionPanel.setLayout(new GridLayoutManager(9, 2, new Insets(0, 0, 0, 0), -1, -1));
        requestMatchingConditionPanel.setVisible(true);
        extractControlPanel.add(requestMatchingConditionPanel, new GridConstraints(5, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        requestMatchingConditionPanel.setBorder(BorderFactory.createTitledBorder(null, "Request Matching Condition", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label6 = new JLabel();
        label6.setText("Matching String");
        label6.setToolTipText("The first capture group is extracted, if available.");
        requestMatchingConditionPanel.add(label6, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        requestConditionMatchingTextField = new JTextField();
        Font requestConditionMatchingTextFieldFont = this.$$$getFont$$$("Monaco", -1, -1, requestConditionMatchingTextField.getFont());
        if (requestConditionMatchingTextFieldFont != null)
            requestConditionMatchingTextField.setFont(requestConditionMatchingTextFieldFont);
        requestMatchingConditionPanel.add(requestConditionMatchingTextField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(250, -1), null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Case sensitivity");
        requestMatchingConditionPanel.add(label7, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        requestConditionCaseSensitiveRadio = new JRadioButton();
        requestConditionCaseSensitiveRadio.setText("Case sensitive");
        requestMatchingConditionPanel.add(requestConditionCaseSensitiveRadio, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        requestConditionCaseInsensitiveRadio = new JRadioButton();
        requestConditionCaseInsensitiveRadio.setText("Case insensitive");
        requestMatchingConditionPanel.add(requestConditionCaseInsensitiveRadio, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Search in");
        requestMatchingConditionPanel.add(label8, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        requestConditionReqHeaderCheckbox = new JCheckBox();
        requestConditionReqHeaderCheckbox.setText("Request Header");
        requestMatchingConditionPanel.add(requestConditionReqHeaderCheckbox, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        requestConditionReqBodyCheckbox = new JCheckBox();
        requestConditionReqBodyCheckbox.setText("Request Body");
        requestMatchingConditionPanel.add(requestConditionReqBodyCheckbox, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Search Mode");
        requestMatchingConditionPanel.add(label9, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        requestConditionContainsRadio = new JRadioButton();
        requestConditionContainsRadio.setText("Contains");
        requestMatchingConditionPanel.add(requestConditionContainsRadio, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        requestConditionRegexRadio = new JRadioButton();
        requestConditionRegexRadio.setText("Regex");
        requestMatchingConditionPanel.add(requestConditionRegexRadio, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("Match Options");
        requestMatchingConditionPanel.add(label10, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        requestConditionMatchingRadio = new JRadioButton();
        requestConditionMatchingRadio.setText("Matching");
        requestMatchingConditionPanel.add(requestConditionMatchingRadio, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        requestConditionNotMatchingRadio = new JRadioButton();
        requestConditionNotMatchingRadio.setText("Not Matching");
        requestMatchingConditionPanel.add(requestConditionNotMatchingRadio, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        extractControlPanel.add(updateRestrictionHelpLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(5, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel7, new GridConstraints(0, 0, 5, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        addButton2 = new JButton();
        addButton2.setText("Add");
        panel7.add(addButton2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        removeButton2 = new JButton();
        removeButton2.setText("Remove");
        panel7.add(removeButton2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer7 = new Spacer();
        panel7.add(spacer7, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        upButton = new JButton();
        upButton.setText("Up");
        panel7.add(upButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        downButton = new JButton();
        downButton.setText("Down");
        panel7.add(downButton, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel8, new GridConstraints(0, 1, 5, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane3 = new JScrollPane();
        panel8.add(scrollPane3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(200, -1), null, 0, false));
        updateList = new JList();
        updateList.setEnabled(true);
        updateList.setSelectionMode(0);
        updateList.setVisible(true);
        scrollPane3.setViewportView(updateList);
        final Spacer spacer8 = new Spacer();
        panel8.add(spacer8, new GridConstraints(1, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer9 = new Spacer();
        updateAutoControlPanel.add(spacer9, new GridConstraints(1, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer10 = new Spacer();
        updateAutoControlPanel.add(spacer10, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(20, -1), null, new Dimension(20, -1), 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("Specify regexes that are used to update the variable value from received responses");
        updateAutoControlPanel.add(label11, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        updateAutoControlPanel.add(extractionHelpLabel, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer11 = new Spacer();
        updateAutoControlPanel.add(spacer11, new GridConstraints(0, 5, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        variableTab.add(variableNameHelpLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        variableTab.add(panel9, new GridConstraints(0, 0, 2, 2, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(400, -1), null, 0, false));
        final JLabel label12 = new JLabel();
        label12.setText("Variable Value");
        panel9.add(label12, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        variableValueTextField = new JTextField();
        panel9.add(variableValueTextField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label13 = new JLabel();
        label13.setText("Variable Name");
        panel9.add(label13, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        variableNameTextField = new JTextField();
        panel9.add(variableNameTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer12 = new Spacer();
        variableTab.add(spacer12, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane4 = new JScrollPane();
        scrollPane4.setVisible(false);
        ExtractionPane.addTab("Replace", scrollPane4);
        replaceTab = new JPanel();
        replaceTab.setLayout(new GridLayoutManager(10, 5, new Insets(10, 10, 0, 0), -1, -1));
        scrollPane4.setViewportView(replaceTab);
        final Spacer spacer13 = new Spacer();
        replaceTab.add(spacer13, new GridConstraints(9, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer14 = new Spacer();
        replaceTab.add(spacer14, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(20, -1), null, new Dimension(20, -1), 0, false));
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        replaceTab.add(panel10, new GridConstraints(2, 3, 8, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        replaceControlPanel = new JPanel();
        replaceControlPanel.setLayout(new GridLayoutManager(6, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel10.add(replaceControlPanel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new GridLayoutManager(10, 2, new Insets(0, 0, 0, 0), -1, -1));
        replaceControlPanel.add(panel11, new GridConstraints(0, 0, 5, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel11.setBorder(BorderFactory.createTitledBorder(null, "Request Matching Condition", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label14 = new JLabel();
        label14.setText("Matching String");
        label14.setToolTipText("The first capture group is extracted, if available.");
        panel11.add(label14, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        replaceRequestMatchingTextField = new JTextField();
        Font replaceRequestMatchingTextFieldFont = this.$$$getFont$$$("Monaco", -1, -1, replaceRequestMatchingTextField.getFont());
        if (replaceRequestMatchingTextFieldFont != null)
            replaceRequestMatchingTextField.setFont(replaceRequestMatchingTextFieldFont);
        panel11.add(replaceRequestMatchingTextField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(250, -1), null, 0, false));
        final JLabel label15 = new JLabel();
        label15.setText("Case sensitivity");
        panel11.add(label15, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        replaceRequestCaseSensitiveRadio = new JRadioButton();
        replaceRequestCaseSensitiveRadio.setText("Case sensitive");
        panel11.add(replaceRequestCaseSensitiveRadio, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        replaceRequestCaseInsensitiveRadio = new JRadioButton();
        replaceRequestCaseInsensitiveRadio.setText("Case insensitive");
        panel11.add(replaceRequestCaseInsensitiveRadio, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label16 = new JLabel();
        label16.setText("Search in");
        panel11.add(label16, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        replaceRequestSearchURLCheckbox = new JCheckBox();
        replaceRequestSearchURLCheckbox.setText("Request URL");
        panel11.add(replaceRequestSearchURLCheckbox, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        replaceRequestSearchHeaderCheckbox = new JCheckBox();
        replaceRequestSearchHeaderCheckbox.setText("Request Header");
        panel11.add(replaceRequestSearchHeaderCheckbox, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label17 = new JLabel();
        label17.setText("Search Mode");
        panel11.add(label17, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        replaceRequestContainsRadio = new JRadioButton();
        replaceRequestContainsRadio.setText("Contains");
        panel11.add(replaceRequestContainsRadio, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        replaceRequestRegexRadio = new JRadioButton();
        replaceRequestRegexRadio.setText("Regex");
        panel11.add(replaceRequestRegexRadio, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        replaceRequestSearchBodyCheckbox = new JCheckBox();
        replaceRequestSearchBodyCheckbox.setText("Request Body");
        panel11.add(replaceRequestSearchBodyCheckbox, new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label18 = new JLabel();
        label18.setText("Match Options");
        panel11.add(label18, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        replaceRequestMatchingRadio = new JRadioButton();
        replaceRequestMatchingRadio.setText("Matching");
        panel11.add(replaceRequestMatchingRadio, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        replaceRequestNotMatchingRadio = new JRadioButton();
        replaceRequestNotMatchingRadio.setText("Not Matching");
        panel11.add(replaceRequestNotMatchingRadio, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer15 = new Spacer();
        replaceControlPanel.add(spacer15, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel12 = new JPanel();
        panel12.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel10.add(panel12, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        replaceAddButton = new JButton();
        replaceAddButton.setText("Add");
        panel12.add(replaceAddButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        replaceRemoveButton = new JButton();
        replaceRemoveButton.setText("Remove");
        panel12.add(replaceRemoveButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer16 = new Spacer();
        panel12.add(spacer16, new GridConstraints(2, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane5 = new JScrollPane();
        scrollPane5.setAutoscrolls(false);
        panel10.add(scrollPane5, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(200, 300), null, 0, false));
        replaceList = new JList();
        replaceList.setMaximumSize(new Dimension(0, 0));
        final DefaultListModel defaultListModel1 = new DefaultListModel();
        replaceList.setModel(defaultListModel1);
        scrollPane5.setViewportView(replaceList);
        final Spacer spacer17 = new Spacer();
        replaceTab.add(spacer17, new GridConstraints(2, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        onlyReplaceInScope = new JCheckBox();
        onlyReplaceInScope.setText("Only replace in in-scope requests");
        replaceTab.add(onlyReplaceInScope, new GridConstraints(0, 3, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label19 = new JLabel();
        label19.setText("Replace variable in requests from");
        replaceTab.add(label19, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        replaceProxyCheckBox = new JCheckBox();
        replaceProxyCheckBox.setText("Proxy");
        replaceTab.add(replaceProxyCheckBox, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        replaceScannerCheckBox = new JCheckBox();
        replaceScannerCheckBox.setText("Scanner");
        replaceTab.add(replaceScannerCheckBox, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        replaceRepeaterCheckBox = new JCheckBox();
        replaceRepeaterCheckBox.setText("Repeater");
        replaceTab.add(replaceRepeaterCheckBox, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        replaceExtensionsCheckBox = new JCheckBox();
        replaceExtensionsCheckBox.setText("Extensions");
        replaceTab.add(replaceExtensionsCheckBox, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        replaceIntruderCheckBox = new JCheckBox();
        replaceIntruderCheckBox.setText("Intruder");
        replaceTab.add(replaceIntruderCheckBox, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label20 = new JLabel();
        label20.setText("Only replace variables in requests matching");
        replaceTab.add(label20, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel13 = new JPanel();
        panel13.setLayout(new GridLayoutManager(3, 4, new Insets(10, 10, 0, 0), -1, -1));
        ExtractionPane.addTab("Global Settings", panel13);
        final Spacer spacer18 = new Spacer();
        panel13.add(spacer18, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer19 = new Spacer();
        panel13.add(spacer19, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        exportDataButton = new JButton();
        exportDataButton.setText("Export Data");
        panel13.add(exportDataButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        importDataButton = new JButton();
        importDataButton.setText("Import Data");
        panel13.add(importDataButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        logLevelComboBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("TRACE");
        defaultComboBoxModel1.addElement("DEBUG");
        defaultComboBoxModel1.addElement("INFO");
        defaultComboBoxModel1.addElement("WARN");
        defaultComboBoxModel1.addElement("ERROR");
        logLevelComboBox.setModel(defaultComboBoxModel1);
        panel13.add(logLevelComboBox, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label21 = new JLabel();
        label21.setText("Log Level");
        panel13.add(label21, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label22 = new JLabel();
        label22.setText("Variable Settings");
        panel13.add(label22, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        label5.setLabelFor(extractionRegexTextField);
        label6.setLabelFor(extractionRegexTextField);
        label12.setLabelFor(variableValueTextField);
        label13.setLabelFor(variableNameTextField);
        label14.setLabelFor(extractionRegexTextField);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(extractionCaseSensitiveRadio);
        buttonGroup.add(extractionCaseInsensitiveRadio);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(inScopeResponsesRadioButton);
        buttonGroup.add(specificURLRadioButton);
        buttonGroup.add(allResponsesRadioButton);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(requestConditionRegexRadio);
        buttonGroup.add(requestConditionContainsRadio);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(requestConditionMatchingRadio);
        buttonGroup.add(requestConditionNotMatchingRadio);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(requestConditionCaseSensitiveRadio);
        buttonGroup.add(requestConditionCaseInsensitiveRadio);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(replaceRequestRegexRadio);
        buttonGroup.add(replaceRequestContainsRadio);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(replaceRequestMatchingRadio);
        buttonGroup.add(replaceRequestNotMatchingRadio);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(replaceRequestCaseSensitiveRadio);
        buttonGroup.add(replaceRequestCaseInsensitiveRadio);
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
        return panel1;
    }

    public void createUIComponents() {
        extractRegexHelpLabel = new HelpLabel("The variable value is updated to the first capture group, if available.");
        extractionHelpLabel = new HelpLabel("If multiple regexes are specified, the evaluation is stopped after the first one matches.");
        updateRestrictionHelpLabel = new HelpLabel("Try to limit the cases when the variable should be updated,\r\nbecause otherwise the regex(es) are matched against all responses.");
        variableNameHelpLabel = new HelpLabel("Use a variable from a request by inserting ${" + Constants.VAR_PREFIX + "NAME}.\r\n" +
                "Inserting and updating a variable is also available from the context menu.");
    }

    // Called when list selection changes
    synchronized public void updateUIFromDataModel() {
        doNotWriteDataToModel++;

        var data = selectedVariable;
        if (data == null) {
            removeVariableButton.setEnabled(false);
            ExtractionPane.setVisible(false);
            doNotWriteDataToModel--;
            return;
        }
        if (!removeVariableButton.isEnabled()) {
            removeVariableButton.setEnabled(true);
            ExtractionPane.setVisible(true);
        }

        variableNameTextField.setText(data.getVariableName());
        variableValueTextField.setText(data.getVariableValue());
        updateVariableValueAutomaticallyCheckBox.setSelected(data.isUpdateAutomatically());
        if (data.isUpdateAutomatically()) {
            enableComponents(updateAutoControlPanel);
        } else {
            disableComponents(updateAutoControlPanel);
        }

        proxyCheckBox1.setSelected(data.getUpdateModel().getToolSelectionModel().isProxy());
        scannerCheckBox1.setSelected(data.getUpdateModel().getToolSelectionModel().isScanner());
        repeaterCheckBox1.setSelected(data.getUpdateModel().getToolSelectionModel().isRepeater());
        extensionsCheckBox1.setSelected(data.getUpdateModel().getToolSelectionModel().isExtensions());
        intruderCheckBox1.setSelected(data.getUpdateModel().getToolSelectionModel().isIntruder());

        int selectedExtraction = updateList.getSelectedIndex();
        if (selectedExtraction > -1 && data.isUpdateAutomatically()) {
            enableComponents(extractControlPanel);
            var extractModel = data.getUpdateModel().getUpdateExtractionListModel().getList().get(selectedExtraction);
            updateUrlTextField.setEnabled(false);
            switch (extractModel.getExtractionScope()) {
                case ALL -> {
                    allResponsesRadioButton.setSelected(true);
                }
                case URL -> {
                    specificURLRadioButton.setSelected(true);
                    updateUrlTextField.setEnabled(true);
                }
                case IN_SCOPE -> {
                    inScopeResponsesRadioButton.setSelected(true);
                }
            }
            updateUrlTextField.setText(extractModel.getExtractionUrl());

            // Request Matching Condition
            requestConditionCheckBox.setSelected(extractModel.isUpdateOnlyWhenRequestMatches());
            requestMatchingConditionPanel.setVisible(extractModel.isUpdateOnlyWhenRequestMatches());

            var reqSearchModel = extractModel.getRequestSearchCondition();
            switch (reqSearchModel.getSearchMode()) {
                case REGEX -> {
                    requestConditionRegexRadio.setSelected(true);
                }
                case CONTAINS -> {
                    requestConditionContainsRadio.setSelected(true);
                }
            }
            requestConditionMatchingTextField.setText(reqSearchModel.getMatchString());

            switch (reqSearchModel.getRegexMatchOption()) {
                case MATCHING -> {
                    requestConditionMatchingRadio.setSelected(true);
                }
                case NOT_MATCHING -> {
                    requestConditionNotMatchingRadio.setSelected(true);
                }
            }

            switch (reqSearchModel.getRegexCaseSensitivity()) {
                case CASE_SENSITIVE -> {
                    requestConditionCaseSensitiveRadio.setSelected(true);
                }
                case CASE_INSENSITIVE -> {
                    requestConditionCaseInsensitiveRadio.setSelected(true);
                }
            }
            requestConditionReqHeaderCheckbox.setSelected(false);
            requestConditionReqBodyCheckbox.setSelected(false);
            for (var v : reqSearchModel.getSearchInLocationList()) {
                switch (v) {
                    case REQUEST_HEADER -> {
                        requestConditionReqHeaderCheckbox.setSelected(true);
                    }
                    case REQUEST_BODY -> {
                        requestConditionReqBodyCheckbox.setSelected(true);
                    }
                }
            }


            // Variable Value Extraction
            extractionRegexTextField.setText(extractModel.getExtractionSearchModel().getMatchString());
            switch (extractModel.getExtractionSearchModel().getRegexCaseSensitivity()) {
                case CASE_SENSITIVE -> {
                    extractionCaseSensitiveRadio.setSelected(true);
                }
                case CASE_INSENSITIVE -> {
                    extractionCaseInsensitiveRadio.setSelected(true);
                }
            }

            extractionResponseHeaderCheckBox.setSelected(false);
            extractionResponseBodyCheckBox.setSelected(false);
            for (var i : extractModel.getExtractionSearchModel().getSearchInLocationList()) {
                switch (i) {
                    case RESPONSE_HEADER -> {
                        extractionResponseHeaderCheckBox.setSelected(true);
                    }
                    case RESPONSE_BODY -> {
                        extractionResponseBodyCheckBox.setSelected(true);
                    }
                }
            }

            upButton.setEnabled(selectedExtraction > 0);
            downButton.setEnabled(selectedExtraction < data.getUpdateModel().getUpdateExtractionListModel().getList().size() - 1);
        } else {
            disableComponents(extractControlPanel);
            upButton.setEnabled(false);
            downButton.setEnabled(false);
        }


        // Replace Tab

        replaceProxyCheckBox.setSelected(data.getReplaceModel().getToolSelectionModel().isProxy());
        replaceScannerCheckBox.setSelected(data.getReplaceModel().getToolSelectionModel().isScanner());
        replaceRepeaterCheckBox.setSelected(data.getReplaceModel().getToolSelectionModel().isRepeater());
        replaceExtensionsCheckBox.setSelected(data.getReplaceModel().getToolSelectionModel().isExtensions());
        replaceIntruderCheckBox.setSelected(data.getReplaceModel().getToolSelectionModel().isIntruder());
        onlyReplaceInScope.setSelected(data.getReplaceModel().isReplaceOnlyInScope());

        int selectedReplace = replaceList.getSelectedIndex();
        if (selectedReplace > -1) {
            enableComponents(replaceControlPanel);
            var model = selectedVariable.getReplaceModel().getReplaceListModel().getList().get(selectedReplace);

            switch (model.getSearchMode()) {
                case CONTAINS -> {
                    replaceRequestContainsRadio.setSelected(true);
                }
                case REGEX -> {
                    replaceRequestRegexRadio.setSelected(true);
                }
            }

            replaceRequestMatchingTextField.setText(model.getMatchString());
            switch (model.getRegexMatchOption()) {
                case MATCHING -> {
                    replaceRequestMatchingRadio.setSelected(true);
                }
                case NOT_MATCHING -> {
                    replaceRequestNotMatchingRadio.setSelected(true);
                }
            }

            switch (model.getRegexCaseSensitivity()) {
                case CASE_INSENSITIVE -> {
                    replaceRequestCaseInsensitiveRadio.setSelected(true);
                }
                case CASE_SENSITIVE -> {
                    replaceRequestCaseSensitiveRadio.setSelected(true);
                }
            }

            replaceRequestSearchBodyCheckbox.setSelected(false);
            replaceRequestSearchHeaderCheckbox.setSelected(false);
            replaceRequestSearchURLCheckbox.setSelected(false);
            for (var i : model.getSearchInLocationList()) {
                switch (i) {
                    case REQUEST_URL -> {
                        replaceRequestSearchURLCheckbox.setSelected(true);
                    }
                    case REQUEST_HEADER -> {
                        replaceRequestSearchHeaderCheckbox.setSelected(true);
                    }
                    case REQUEST_BODY -> {
                        replaceRequestSearchBodyCheckbox.setSelected(true);
                    }
                }
            }
        } else {
            disableComponents(replaceControlPanel);
        }


        doNotWriteDataToModel--;
    }

    // Called by OnChangeListener
    synchronized public void updateDataModel() {
        if (doNotWriteDataToModel > 0) {
            return;
        }

        doNotWriteDataToModel++;

        var selRow = OverviewTable.getSelectedRow();
        if (selRow == -1) {
            return;
        }

        var data = dataModel.getVariables().get(selRow);

        //if (!isModified(data)) return;

        data.setVariableValue(variableValueTextField.getText());
        data.setVariableName(variableNameTextField.getText());
        data.setUpdateAutomatically(updateVariableValueAutomaticallyCheckBox.isSelected());

        if (selectedVariable.isUpdateAutomatically()) {
            enableComponents(updateAutoControlPanel);
        } else {
            disableComponents(updateAutoControlPanel);
        }


        var updateModel = data.getUpdateModel();
        var toolSel = data.getUpdateModel().getToolSelectionModel();
        toolSel.setProxy(proxyCheckBox1.isSelected());
        toolSel.setScanner(scannerCheckBox1.isSelected());
        toolSel.setRepeater(repeaterCheckBox1.isSelected());
        toolSel.setExtensions(extensionsCheckBox1.isSelected());
        toolSel.setIntruder(intruderCheckBox1.isSelected());

        var updateSel = updateList.getSelectedIndex();
        if (updateSel > -1 && selectedVariable.isUpdateAutomatically()) {
            enableComponents(extractControlPanel);
            var m = updateModel.getUpdateExtractionListModel().getList().get(updateSel);

            if (allResponsesRadioButton.isSelected()) {
                m.setExtractionScope(ExtractionScopeMode.ALL);
                updateUrlTextField.setEnabled(false);
            } else if (inScopeResponsesRadioButton.isSelected()) {
                m.setExtractionScope(ExtractionScopeMode.IN_SCOPE);
                updateUrlTextField.setEnabled(false);
            } else if (specificURLRadioButton.isSelected()) {
                m.setExtractionScope(ExtractionScopeMode.URL);
                updateUrlTextField.setEnabled(true);
            }

            // Request Matching Condition
            var reqMatchCond = m.getRequestSearchCondition();
            m.setUpdateOnlyWhenRequestMatches(requestConditionCheckBox.isSelected());
            requestMatchingConditionPanel.setVisible(requestConditionCheckBox.isSelected());
            if (requestConditionCheckBox.isSelected()) {
                if (requestConditionContainsRadio.isSelected()) {
                    reqMatchCond.setSearchMode(SearchMode.CONTAINS);
                } else if (requestConditionRegexRadio.isSelected()) {
                    reqMatchCond.setSearchMode(SearchMode.REGEX);
                }

                reqMatchCond.setMatchString(requestConditionMatchingTextField.getText());

                if (requestConditionMatchingRadio.isSelected()) {
                    reqMatchCond.setRegexMatchOption(RegexMatchOption.MATCHING);
                } else if (requestConditionNotMatchingRadio.isSelected()) {
                    reqMatchCond.setRegexMatchOption(RegexMatchOption.NOT_MATCHING);
                }

                if (requestConditionCaseSensitiveRadio.isSelected()) {
                    reqMatchCond.setRegexCaseSensitivity(RegexCaseSensitivity.CASE_SENSITIVE);
                } else if (requestConditionCaseInsensitiveRadio.isSelected()) {
                    reqMatchCond.setRegexCaseSensitivity(RegexCaseSensitivity.CASE_INSENSITIVE);
                }

                var searchInList = new ArrayList<SearchInLocation>();
                if (requestConditionReqHeaderCheckbox.isSelected()) {
                    searchInList.add(SearchInLocation.REQUEST_HEADER);
                }
                if (requestConditionReqBodyCheckbox.isSelected()) {
                    searchInList.add(SearchInLocation.REQUEST_BODY);
                }
                reqMatchCond.setSearchInLocationList(searchInList);
            }


            // Variable Value Extraction
            m.setExtractionUrl(updateUrlTextField.getText());
            m.getExtractionSearchModel().setMatchString(extractionRegexTextField.getText());
            if (extractionCaseInsensitiveRadio.isSelected()) {
                m.getExtractionSearchModel().setRegexCaseSensitivity(RegexCaseSensitivity.CASE_INSENSITIVE);
            } else if (extractionCaseSensitiveRadio.isSelected()) {
                m.getExtractionSearchModel().setRegexCaseSensitivity(RegexCaseSensitivity.CASE_SENSITIVE);
            }

            var searchList = new ArrayList<SearchInLocation>();
            if (extractionResponseHeaderCheckBox.isSelected()) {
                searchList.add(SearchInLocation.RESPONSE_HEADER);
            }
            if (extractionResponseBodyCheckBox.isSelected()) {
                searchList.add(SearchInLocation.RESPONSE_BODY);
            }
            m.getExtractionSearchModel().setSearchInLocationList(searchList);

            upButton.setEnabled(updateSel > 0);
            downButton.setEnabled(updateSel < data.getUpdateModel().getUpdateExtractionListModel().getList().size() - 1);
        } else {
            disableComponents(extractControlPanel);
            upButton.setEnabled(false);
            downButton.setEnabled(false);
        }


        // Replace Tab
        toolSel = data.getReplaceModel().getToolSelectionModel();
        toolSel.setProxy(replaceProxyCheckBox.isSelected());
        toolSel.setScanner(replaceScannerCheckBox.isSelected());
        toolSel.setRepeater(replaceRepeaterCheckBox.isSelected());
        toolSel.setExtensions(replaceExtensionsCheckBox.isSelected());
        toolSel.setIntruder(replaceIntruderCheckBox.isSelected());
        data.getReplaceModel().setReplaceOnlyInScope(onlyReplaceInScope.isSelected());

        var replaceSel = replaceList.getSelectedIndex();
        if (replaceSel > -1) {
            enableComponents(replaceControlPanel);
            var model = selectedVariable.getReplaceModel().getReplaceListModel().getList().get(replaceSel);
            if (replaceRequestContainsRadio.isSelected()) {
                model.setSearchMode(SearchMode.CONTAINS);
            } else if (replaceRequestRegexRadio.isSelected()) {
                model.setSearchMode(SearchMode.REGEX);
            }

            model.setMatchString(replaceRequestMatchingTextField.getText());
            if (replaceRequestMatchingRadio.isSelected()) {
                model.setRegexMatchOption(RegexMatchOption.MATCHING);
            } else if (replaceRequestNotMatchingRadio.isSelected()) {
                model.setRegexMatchOption(RegexMatchOption.NOT_MATCHING);
            }

            if (replaceRequestCaseSensitiveRadio.isSelected()) {
                model.setRegexCaseSensitivity(RegexCaseSensitivity.CASE_SENSITIVE);
            } else if (replaceRequestCaseInsensitiveRadio.isSelected()) {
                model.setRegexCaseSensitivity(RegexCaseSensitivity.CASE_INSENSITIVE);
            }

            var searchInList = new ArrayList<SearchInLocation>();
            if (replaceRequestSearchURLCheckbox.isSelected()) {
                searchInList.add(SearchInLocation.REQUEST_URL);
            }
            if (replaceRequestSearchHeaderCheckbox.isSelected()) {
                searchInList.add(SearchInLocation.REQUEST_HEADER);
            }
            if (replaceRequestSearchBodyCheckbox.isSelected()) {
                searchInList.add(SearchInLocation.REQUEST_BODY);
            }
            model.setSearchInLocationList(searchInList);
        } else {
            disableComponents(replaceControlPanel);
        }

        doNotWriteDataToModel--;

        dataModel.saveToProject();
    }

    public boolean isModified(VariableModel data) {
        if (variableValueTextField.getText() != null ? !variableValueTextField.getText().equals(data.getVariableValue()) : data.getVariableValue() != null)
            return true;
        if (updateVariableValueAutomaticallyCheckBox.isSelected() != data.isUpdateAutomatically()) return true;
        if (variableNameTextField.getText() != null ? !variableNameTextField.getText().equals(data.getVariableName()) : data.getVariableName() != null)
            return true;
        if (proxyCheckBox1.isSelected() != data.getUpdateModel().getToolSelectionModel().isProxy()) return true;
        if (scannerCheckBox1.isSelected() != data.getUpdateModel().getToolSelectionModel().isScanner()) return true;
        if (repeaterCheckBox1.isSelected() != data.getUpdateModel().getToolSelectionModel().isRepeater()) return true;
        if (extensionsCheckBox1.isSelected() != data.getUpdateModel().getToolSelectionModel().isExtensions())
            return true;
        if (intruderCheckBox1.isSelected() != data.getUpdateModel().getToolSelectionModel().isIntruder()) return true;
        return false;
    }


    private void disableComponents(Container panel) {
        doNotWriteDataToModel++;
        for (var i : panel.getComponents()) {
            i.setEnabled(false);
            if (i instanceof Container) {
                disableComponents((Container) i);
            }
        }
        doNotWriteDataToModel--;
    }

    private void enableComponents(Container panel) {
        doNotWriteDataToModel++;
        for (var i : panel.getComponents()) {
            i.setEnabled(true);
            if (i instanceof Container) {
                enableComponents((Container) i);
            }
        }
        doNotWriteDataToModel--;
    }
}

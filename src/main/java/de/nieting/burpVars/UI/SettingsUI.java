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
    private JRadioButton inScopeResponsesRadioButton;
    private JRadioButton specificURLRadioButton;
    private JTextField updateUrlTextField;
    private JTextField variableNameTextField;
    public JCheckBox onlyReplaceInScope;
    public JRadioButton allResponsesRadioButton;
    public JPanel extractControlPanel;
    public JCheckBox requestConditionCheckBox;
    public JPanel requestMatchingConditionPanel;
    public JPanel replaceControlPanel;
    public JPanel updateAutoControlPanel;
    public JPanel variableTab;
    public JPanel replaceTab;
    public JButton upButton;
    public JButton downButton;
    public HelpLabel extractionHelpLabel;
    public HelpLabel updateRestrictionHelpLabel;
    public HelpLabel variableNameHelpLabel;
    public JButton exportDataButton;
    public JButton importDataButton;
    public JComboBox logLevelComboBox;
    public JPanel historyTab;
    public HistoryTab historyTabForm;
    public SearchOptionsPanel requestMatchingConditionSearchOptions;
    public SearchOptionsPanel variableExtractionSearchOptions;
    public SearchOptionsPanel replaceSearchOptions;
    public JButton duplicateButton;


    private DataModel dataModel;
    private DefaultListSelectionModel variableTableSelectionModel = new DefaultListSelectionModel();
    private DefaultListSelectionModel updateListSelectionModel = new DefaultListSelectionModel();
    private DefaultListSelectionModel replaceListSelectionModel = new DefaultListSelectionModel();

    private int doNotWriteDataToModel = 0;
    private boolean dirtyUIState = false;

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

                Configurator.setLevel(LogManager.getLogger("de.nieting"), level);

//                final LoggerContext context = LoggerContext.getContext(true);
//                //final Configuration config = context.getConfiguration();
//                context.getRootLogger().setLevel(level);
//                context.updateLoggers();
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

        variableExtractionSearchOptions.extractionRegexTextField.getDocument().addDocumentListener(new OnChangeListener(() -> {
            var listM = (UpdateExtractionListModel) updateList.getModel();
            listM.changed();
        }));

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

        replaceSearchOptions.searchStringTextField.getDocument().addDocumentListener(new OnChangeListener(() -> {
            var listM = (ReplaceListModel) replaceList.getModel();
            listM.changed();
        }));

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

        duplicateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dataModel.duplicateVariable(selectedVariable);
                int lastIndex = dataModel.getRowCount() - 1;
                variableTableSelectionModel.setSelectionInterval(lastIndex, lastIndex);
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
                dirtyUIState = true;
                updateList.setModel(selectedVariable.getUpdateModel().getUpdateExtractionListModel());
                replaceList.setModel(selectedVariable.getReplaceModel().getReplaceListModel());
                dirtyUIState = false;

                historyTabForm.setHistoryListModel(selectedVariable.getHistoryListModel());
                updateUIFromDataModel();

                if (updateList.getModel().getSize() > 0) {
                    updateListSelectionModel.setSelectionInterval(0, 0);
                }
                if (replaceList.getModel().getSize() > 0) {
                    replaceListSelectionModel.setSelectionInterval(0, 0);
                }
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
        panel2.setLayout(new GridLayoutManager(3, 5, new Insets(0, 0, 0, 0), -1, -1));
        splitPane1.setLeftComponent(panel2);
        addVariableButton = new JButton();
        addVariableButton.setText("Add Variable");
        panel2.add(addVariableButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        removeVariableButton = new JButton();
        removeVariableButton.setText("Remove Variable");
        panel2.add(removeVariableButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1, new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel2.add(spacer2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, null, new Dimension(10, -1), null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel2.add(spacer3, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 5), null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel2.add(scrollPane1, new GridConstraints(0, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        OverviewTable = new JTable();
        scrollPane1.setViewportView(OverviewTable);
        duplicateButton = new JButton();
        duplicateButton.setText("Duplicate Variable");
        panel2.add(duplicateButton, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane1.setRightComponent(panel3);
        ExtractionPane = new JTabbedPane();
        ExtractionPane.setVisible(true);
        panel3.add(ExtractionPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        scrollPane2.setEnabled(true);
        scrollPane2.setVisible(true);
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
        Font updateUrlTextFieldFont = this.$$$getFont$$$("Monospaced", -1, -1, updateUrlTextField.getFont());
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
        panel5.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        extractControlPanel.add(panel5, new GridConstraints(6, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel5.setBorder(BorderFactory.createTitledBorder(null, "Variable Value Extraction", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        panel5.add(variableExtractionSearchOptions.$$$getRootComponent$$$(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        requestMatchingConditionPanel = new JPanel();
        requestMatchingConditionPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        requestMatchingConditionPanel.setVisible(true);
        extractControlPanel.add(requestMatchingConditionPanel, new GridConstraints(5, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        requestMatchingConditionPanel.setBorder(BorderFactory.createTitledBorder(null, "Request Matching Condition", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        requestMatchingConditionPanel.add(requestMatchingConditionSearchOptions.$$$getRootComponent$$$(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        extractControlPanel.add(updateRestrictionHelpLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(5, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel6, new GridConstraints(0, 0, 5, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        addButton2 = new JButton();
        addButton2.setText("Add");
        panel6.add(addButton2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        removeButton2 = new JButton();
        removeButton2.setText("Remove");
        panel6.add(removeButton2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer7 = new Spacer();
        panel6.add(spacer7, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        upButton = new JButton();
        upButton.setText("Up");
        panel6.add(upButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        downButton = new JButton();
        downButton.setText("Down");
        panel6.add(downButton, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel7, new GridConstraints(0, 1, 5, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane3 = new JScrollPane();
        panel7.add(scrollPane3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 400), null, 0, false));
        updateList = new JList();
        updateList.setEnabled(true);
        updateList.setSelectionMode(0);
        updateList.setVisible(true);
        scrollPane3.setViewportView(updateList);
        final Spacer spacer8 = new Spacer();
        panel7.add(spacer8, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer9 = new Spacer();
        updateAutoControlPanel.add(spacer9, new GridConstraints(1, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer10 = new Spacer();
        updateAutoControlPanel.add(spacer10, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(20, -1), null, new Dimension(20, -1), 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Specify regexes that are used to update the variable value from received responses");
        updateAutoControlPanel.add(label3, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        updateAutoControlPanel.add(extractionHelpLabel, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer11 = new Spacer();
        updateAutoControlPanel.add(spacer11, new GridConstraints(0, 5, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        variableTab.add(variableNameHelpLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        variableTab.add(panel8, new GridConstraints(0, 0, 2, 2, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(400, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Variable Value");
        panel8.add(label4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        variableValueTextField = new JTextField();
        panel8.add(variableValueTextField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Variable Name");
        panel8.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        variableNameTextField = new JTextField();
        panel8.add(variableNameTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
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
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        replaceTab.add(panel9, new GridConstraints(2, 3, 8, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        replaceControlPanel = new JPanel();
        replaceControlPanel.setLayout(new GridLayoutManager(6, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel9.add(replaceControlPanel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        replaceControlPanel.add(panel10, new GridConstraints(0, 0, 5, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel10.setBorder(BorderFactory.createTitledBorder(null, "Request Matching Condition", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        panel10.add(replaceSearchOptions.$$$getRootComponent$$$(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer15 = new Spacer();
        replaceControlPanel.add(spacer15, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel9.add(panel11, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        replaceAddButton = new JButton();
        replaceAddButton.setText("Add");
        panel11.add(replaceAddButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        replaceRemoveButton = new JButton();
        replaceRemoveButton.setText("Remove");
        panel11.add(replaceRemoveButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer16 = new Spacer();
        panel11.add(spacer16, new GridConstraints(2, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane5 = new JScrollPane();
        scrollPane5.setAutoscrolls(false);
        panel9.add(scrollPane5, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(200, 400), null, 0, false));
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
        final JLabel label6 = new JLabel();
        label6.setText("Replace variable in requests from");
        replaceTab.add(label6, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
        final JLabel label7 = new JLabel();
        label7.setText("Only replace variables in requests matching");
        replaceTab.add(label7, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        historyTab = new JPanel();
        historyTab.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        ExtractionPane.addTab("History", historyTab);
        historyTabForm = new HistoryTab();
        historyTab.add(historyTabForm.$$$getRootComponent$$$(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel12 = new JPanel();
        panel12.setLayout(new GridLayoutManager(3, 4, new Insets(10, 10, 0, 0), -1, -1));
        ExtractionPane.addTab("Global Settings", panel12);
        final Spacer spacer18 = new Spacer();
        panel12.add(spacer18, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer19 = new Spacer();
        panel12.add(spacer19, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        exportDataButton = new JButton();
        exportDataButton.setText("Export Data");
        panel12.add(exportDataButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        importDataButton = new JButton();
        importDataButton.setText("Import Data");
        panel12.add(importDataButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        logLevelComboBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("TRACE");
        defaultComboBoxModel1.addElement("DEBUG");
        defaultComboBoxModel1.addElement("INFO");
        defaultComboBoxModel1.addElement("WARN");
        defaultComboBoxModel1.addElement("ERROR");
        logLevelComboBox.setModel(defaultComboBoxModel1);
        panel12.add(logLevelComboBox, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Log Level");
        panel12.add(label8, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Variable Settings");
        panel12.add(label9, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        label4.setLabelFor(variableValueTextField);
        label5.setLabelFor(variableNameTextField);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(inScopeResponsesRadioButton);
        buttonGroup.add(specificURLRadioButton);
        buttonGroup.add(allResponsesRadioButton);
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
        extractionHelpLabel = new HelpLabel("If multiple regexes are specified, the evaluation is stopped after the first one matches.");
        updateRestrictionHelpLabel = new HelpLabel("Try to limit the cases when the variable should be updated,\r\nbecause otherwise the regex(es) are matched against all responses.");
        variableNameHelpLabel = new HelpLabel("Use a variable from a request by inserting ${" + Constants.VAR_PREFIX + "NAME}.\r\n" +
                "Inserting and updating a variable is also available from the context menu.");

        requestMatchingConditionSearchOptions = SearchOptionsPanel.forRequest();
        variableExtractionSearchOptions = SearchOptionsPanel.forVariableExtraction();
        replaceSearchOptions = SearchOptionsPanel.forRequest();
    }

    // Called when list selection changes
    synchronized public void updateUIFromDataModel() {
        if (dirtyUIState) return;
        doNotWriteDataToModel++;

        var data = selectedVariable;
        if (data == null) {
            removeVariableButton.setEnabled(false);
            duplicateButton.setEnabled(false);
            ExtractionPane.setVisible(false);
            doNotWriteDataToModel--;
            return;
        }
        if (!removeVariableButton.isEnabled()) {
            removeVariableButton.setEnabled(true);
            duplicateButton.setEnabled(true);
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
            requestMatchingConditionSearchOptions.setSearchModel(extractModel.getRequestSearchCondition());

            // Variable Extraction
            variableExtractionSearchOptions.setSearchModel(extractModel.getExtractionSearchModel());

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
            replaceSearchOptions.setSearchModel(model);
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
            m.setUpdateOnlyWhenRequestMatches(requestConditionCheckBox.isSelected());
            requestMatchingConditionPanel.setVisible(requestConditionCheckBox.isSelected());

            // Variable Value Extraction
            m.setExtractionUrl(updateUrlTextField.getText());

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

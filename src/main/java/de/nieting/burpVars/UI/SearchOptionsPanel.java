package de.nieting.burpVars.UI;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import de.nieting.burpVars.model.DataModel;
import de.nieting.burpVars.model.SearchModel;
import de.nieting.burpVars.model.constants.SearchCaseSensitivity;
import de.nieting.burpVars.model.constants.SearchLocation;
import de.nieting.burpVars.model.constants.SearchMode;
import de.nieting.burpVars.model.constants.SearchOption;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.util.ArrayList;
import java.util.Locale;

public class SearchOptionsPanel {
    private static final Logger LOGGER = LogManager.getLogger(SearchOptionsMode.class);

    public JComboBox searchModeComboBox;
    public JTextField searchStringTextField;
    public JComboBox searchOptionsComboBox;
    public JComboBox caseSensitivityComboBox;
    public JCheckBox requestHeaderCheckBox;
    public JCheckBox requestBodyCheckBox;
    public JCheckBox responseHeaderCheckBox;
    public JCheckBox responseBodyCheckBox;
    public HelpLabel helpLabel1;
    public JTextField extractionRegexTextField;
    public JCheckBox requestURLCheckBox;
    public JPanel extractionRegexLabel;
    public JLabel searchStringLabel;
    public JPanel searchOptionsPanel;
    public JLabel searchModeLabel;
    public JLabel searchLocationRequestLabel;
    public JLabel searchLocationResponseLabel;

    private final OnChangeListener onChangeListener = new OnChangeListener(() -> {
        this.updateDataModelFromUI();
    });

    private SearchModel searchModel;

    private boolean uiIsUpdating = false;

    private SearchOptionsMode mode;

    public static SearchOptionsPanel forRequest() {
        var ret = new SearchOptionsPanel();
        ret.responseBodyCheckBox.setVisible(false);
        ret.responseHeaderCheckBox.setVisible(false);
        ret.extractionRegexLabel.setVisible(false);
        ret.extractionRegexTextField.setVisible(false);
        ret.searchLocationResponseLabel.setVisible(false);
        ret.mode = SearchOptionsMode.REQUEST;
        return ret;
    }

    public static SearchOptionsPanel forResponses() {
        var ret = new SearchOptionsPanel();
        ret.requestBodyCheckBox.setVisible(false);
        ret.requestHeaderCheckBox.setVisible(false);
        ret.requestURLCheckBox.setVisible(false);
        ret.extractionRegexLabel.setVisible(false);
        ret.extractionRegexTextField.setVisible(false);
        ret.searchLocationRequestLabel.setVisible(false);
        ret.mode = SearchOptionsMode.RESPONSE;
        return ret;
    }

    public static SearchOptionsPanel forVariableExtraction() {
        var ret = SearchOptionsPanel.forResponses();
        ret.extractionRegexLabel.setVisible(true);
        ret.extractionRegexTextField.setVisible(true);
        ret.searchStringLabel.setVisible(false);
        ret.searchStringTextField.setVisible(false);
        ret.searchModeComboBox.setVisible(false);
        ret.searchModeLabel.setVisible(false);
        ret.mode = SearchOptionsMode.EXTRACTION;
        return ret;
    }


    private SearchOptionsPanel() {
        $$$setupUI$$$();
        searchModeComboBox.addActionListener(onChangeListener);
        searchStringTextField.getDocument().addDocumentListener(onChangeListener);
        extractionRegexTextField.getDocument().addDocumentListener(onChangeListener);
        searchOptionsComboBox.addActionListener(onChangeListener);
        caseSensitivityComboBox.addActionListener(onChangeListener);
        requestURLCheckBox.addActionListener(onChangeListener);
        requestHeaderCheckBox.addActionListener(onChangeListener);
        responseHeaderCheckBox.addActionListener(onChangeListener);
        responseBodyCheckBox.addActionListener(onChangeListener);
        searchOptionsPanel.setPreferredSize(null);
        searchOptionsPanel.setMinimumSize(null);
    }

    public void createUIComponents() {
        helpLabel1 = new HelpLabel("The variable value is updated to the first capture group, if available.");
    }

    synchronized private void updateUIFromDataModel() {
        uiIsUpdating = true;

        if (searchModel == null) {
            LOGGER.error("SearchModel is null");
            uiIsUpdating = false;
            return;
        }

        searchModeComboBox.setSelectedIndex(searchModel.getSearchMode().getComboBoxIndex());
        if (mode == SearchOptionsMode.EXTRACTION) {
            extractionRegexTextField.setText(searchModel.getSearchString());
        } else {
            searchStringTextField.setText(searchModel.getSearchString());
        }

        searchOptionsComboBox.setSelectedIndex(searchModel.getRegexMatchOption().getComboBoxIndex());
        caseSensitivityComboBox.setSelectedIndex(searchModel.getRegexCaseSensitivity().getComboBoxIndex());

        var boxes = new JCheckBox[]{responseHeaderCheckBox, responseBodyCheckBox, requestURLCheckBox, requestHeaderCheckBox, requestBodyCheckBox};
        for (JCheckBox box : boxes) {
            box.setSelected(false);
        }

        var locations = searchModel.getSearchLocationList();
        for (var loc : locations) {
            switch (loc) {
                case RESPONSE_HEADER -> {
                    responseHeaderCheckBox.setSelected(true);
                }
                case RESPONSE_BODY -> {
                    responseBodyCheckBox.setSelected(true);
                }
                case REQUEST_URL -> {
                    requestURLCheckBox.setSelected(true);
                }
                case REQUEST_HEADER -> {
                    requestHeaderCheckBox.setSelected(true);
                }
                case REQUEST_BODY -> {
                    requestBodyCheckBox.setSelected(true);
                }
            }
        }

        uiIsUpdating = false;
    }

    synchronized private void updateDataModelFromUI() {
        if (uiIsUpdating) return;

        if (searchModel == null) {
            LOGGER.error("SearchModel is null");
            return;
        }

        searchModel.setSearchMode(SearchMode.forComboBoxIndex(searchModeComboBox.getSelectedIndex()));
        if (mode == SearchOptionsMode.EXTRACTION) {
            searchModel.setSearchString(extractionRegexTextField.getText());
        } else {
            searchModel.setSearchString(searchStringTextField.getText());
        }

        searchModel.setRegexMatchOption(SearchOption.forComboBoxIndex(searchOptionsComboBox.getSelectedIndex()));
        searchModel.setRegexCaseSensitivity(SearchCaseSensitivity.forComboBoxIndex(caseSensitivityComboBox.getSelectedIndex()));

        var locations = new ArrayList<SearchLocation>();
        if (mode == SearchOptionsMode.REQUEST) {
            if (requestURLCheckBox.isSelected()) locations.add(SearchLocation.REQUEST_URL);
            if (requestHeaderCheckBox.isSelected()) locations.add(SearchLocation.REQUEST_HEADER);
            if (requestBodyCheckBox.isSelected()) locations.add(SearchLocation.REQUEST_BODY);
        } else {
            if (responseHeaderCheckBox.isSelected()) locations.add(SearchLocation.RESPONSE_HEADER);
            if (responseBodyCheckBox.isSelected()) locations.add(SearchLocation.RESPONSE_BODY);
        }
        searchModel.setSearchLocationList(locations);

        DataModel.saveToProject();
    }

    public SearchModel getSearchModel() {
        return searchModel;
    }

    public void setSearchModel(SearchModel searchModel) {
        if (searchModel == null) {
            LOGGER.error("Setting null searchModel");
        }
        this.searchModel = searchModel;
        updateUIFromDataModel();
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
        searchOptionsPanel = new JPanel();
        searchOptionsPanel.setLayout(new GridLayoutManager(11, 3, new Insets(5, 5, 5, 5), -1, -1));
        searchOptionsPanel.setMinimumSize(new Dimension(-1, 280));
        searchOptionsPanel.setPreferredSize(new Dimension(-1, -1));
        searchModeLabel = new JLabel();
        searchModeLabel.setText("Search Mode");
        searchOptionsPanel.add(searchModeLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        searchOptionsPanel.add(spacer1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        searchOptionsPanel.add(spacer2, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        searchModeComboBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("Contains");
        defaultComboBoxModel1.addElement("Regex");
        searchModeComboBox.setModel(defaultComboBoxModel1);
        searchOptionsPanel.add(searchModeComboBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        searchStringLabel = new JLabel();
        searchStringLabel.setText("Search String");
        searchOptionsPanel.add(searchStringLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        searchStringTextField = new JTextField();
        Font searchStringTextFieldFont = this.$$$getFont$$$("Monospaced", -1, -1, searchStringTextField.getFont());
        if (searchStringTextFieldFont != null) searchStringTextField.setFont(searchStringTextFieldFont);
        searchOptionsPanel.add(searchStringTextField, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(230, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Search Options");
        searchOptionsPanel.add(label1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        searchOptionsComboBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel2 = new DefaultComboBoxModel();
        defaultComboBoxModel2.addElement("Matching");
        defaultComboBoxModel2.addElement("Not Matching");
        searchOptionsComboBox.setModel(defaultComboBoxModel2);
        searchOptionsPanel.add(searchOptionsComboBox, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Case Sensitivity");
        searchOptionsPanel.add(label2, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        caseSensitivityComboBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel3 = new DefaultComboBoxModel();
        defaultComboBoxModel3.addElement("Case Insensitive");
        defaultComboBoxModel3.addElement("Case Sensitive");
        caseSensitivityComboBox.setModel(defaultComboBoxModel3);
        searchOptionsPanel.add(caseSensitivityComboBox, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        requestHeaderCheckBox = new JCheckBox();
        requestHeaderCheckBox.setText("Request Header");
        searchOptionsPanel.add(requestHeaderCheckBox, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        requestBodyCheckBox = new JCheckBox();
        requestBodyCheckBox.setText("Request Body");
        requestBodyCheckBox.setVisible(true);
        searchOptionsPanel.add(requestBodyCheckBox, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        responseHeaderCheckBox = new JCheckBox();
        responseHeaderCheckBox.setText("Response Header");
        responseHeaderCheckBox.setVisible(true);
        searchOptionsPanel.add(responseHeaderCheckBox, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        responseBodyCheckBox = new JCheckBox();
        responseBodyCheckBox.setText("Response Body");
        searchOptionsPanel.add(responseBodyCheckBox, new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        extractionRegexLabel = new JPanel();
        extractionRegexLabel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        searchOptionsPanel.add(extractionRegexLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Extraction Regex");
        extractionRegexLabel.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        extractionRegexLabel.add(helpLabel1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        extractionRegexTextField = new JTextField();
        Font extractionRegexTextFieldFont = this.$$$getFont$$$("Monospaced", -1, -1, extractionRegexTextField.getFont());
        if (extractionRegexTextFieldFont != null) extractionRegexTextField.setFont(extractionRegexTextFieldFont);
        searchOptionsPanel.add(extractionRegexTextField, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(230, -1), null, 0, false));
        requestURLCheckBox = new JCheckBox();
        requestURLCheckBox.setText("Request URL");
        searchOptionsPanel.add(requestURLCheckBox, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        searchLocationRequestLabel = new JLabel();
        searchLocationRequestLabel.setText("Search Location");
        searchOptionsPanel.add(searchLocationRequestLabel, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        searchLocationResponseLabel = new JLabel();
        searchLocationResponseLabel.setText("Search Location");
        searchOptionsPanel.add(searchLocationResponseLabel, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        searchModeLabel.setLabelFor(searchModeComboBox);
        searchStringLabel.setLabelFor(searchStringTextField);
        label1.setLabelFor(searchOptionsComboBox);
        label2.setLabelFor(caseSensitivityComboBox);
        label3.setLabelFor(extractionRegexTextField);
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
        return searchOptionsPanel;
    }


    private enum SearchOptionsMode {
        REQUEST,
        RESPONSE,
        EXTRACTION
    }
}

package de.nieting.burpVars.model.constants;

public enum SearchCaseSensitivity {
    CASE_SENSITIVE(1),
    CASE_INSENSITIVE(0);

    private int comboBoxIndex;

    private SearchCaseSensitivity(int comboBoxIndex) {
        this.comboBoxIndex = comboBoxIndex;
    }

    public static SearchCaseSensitivity forComboBoxIndex(int idx) {
        switch (idx) {
            case 0 -> {
                return CASE_INSENSITIVE;
            }
            case 1 -> {
                return CASE_SENSITIVE;
            }
            default -> {
                throw new RuntimeException("Invalid index");
            }
        }
    }

    public int getComboBoxIndex() {
        return comboBoxIndex;
    }
}

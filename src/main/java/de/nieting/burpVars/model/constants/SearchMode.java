package de.nieting.burpVars.model.constants;

public enum SearchMode {
    CONTAINS(0),
    REGEX(1);

    private int comboBoxIndex;

    private SearchMode(int comboBoxIndex) {
        this.comboBoxIndex = comboBoxIndex;
    }

    public static SearchMode forComboBoxIndex(int idx) {
        switch (idx) {
            case 0 -> {
                return CONTAINS;
            }
            case 1 -> {
                return REGEX;
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

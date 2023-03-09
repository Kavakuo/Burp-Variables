package de.nieting.burpVars.model.constants;

public enum SearchOption {
    MATCHING(0),
    NOT_MATCHING(1);

    private int comboBoxIndex;

    private SearchOption(int comboBoxIndex) {
        this.comboBoxIndex = comboBoxIndex;
    }

    public static SearchOption forComboBoxIndex(int idx) {
        switch (idx) {
            case 0 -> {
                return MATCHING;
            }
            case 1 -> {
                return NOT_MATCHING;
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

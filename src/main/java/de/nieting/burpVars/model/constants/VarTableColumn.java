package de.nieting.burpVars.model.constants;

public enum VarTableColumn {
    VARIABLE_NAME(0, "Variable Name"),
    VARIABLE_VALUE(1, "Variable Value"),
    AUTO_UPDATE(2, "Update automatically"),
    REPLACE_ONLY_IN_SCOPE(3, "Replace only in-scope"),
    LAST_UPDATED(4, "Value Last Updated"),
    LAST_REPLACED(5, "Variable Last Replaced");

    private final int columnIdx;
    private final String columnName;

    private VarTableColumn(int columnIdx, String columnName) {
        this.columnIdx = columnIdx;
        this.columnName = columnName;
    }

    public static VarTableColumn forIndex(int idx) {
        for (var i : values()) {
            if (i.columnIdx == idx) return i;
        }
        throw new RuntimeException("Unknown column");
    }

    public int getColumnIdx() {
        return columnIdx;
    }

    public String getColumnName() {
        return columnName;
    }
}


package net.jonathangiles.hacking.tableview.cellSpan;

/**
 *
 */
public final class CellSpan {
    private final int rowSpan;
    private final int columnSpan;

    public CellSpan(int rowSpan, int columnSpan) {
        this.rowSpan = rowSpan;
        this.columnSpan = columnSpan;
    }

    public int getRowSpan() {
        return rowSpan;
    }

    public int getColumnSpan() {
        return columnSpan;
    }

    @Override public String toString() {
        return "CellSpan: [ rowSpan: " + rowSpan + ", columnSpan: " + columnSpan + " ] ";
    }
}

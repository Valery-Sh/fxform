package net.jonathangiles.hacking.tableview.cellSpan;

/**
 *
 * @author Jonathan Giles
 */
public interface SpanModel {
    public CellSpan getCellSpanAt(int rowIndex, int columnIndex);
    
    public boolean isCellSpanEnabled();
}

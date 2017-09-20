package net.jonathangiles.hacking.tableview.cellSpan.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import javafx.collections.ListChangeListener;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import com.sun.javafx.scene.control.behavior.CellBehaviorBase;
import com.sun.javafx.scene.control.skin.CellSkinBase;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import net.jonathangiles.hacking.tableview.cellSpan.CellSpan;
import net.jonathangiles.hacking.tableview.cellSpan.CellSpanTableView;
import net.jonathangiles.hacking.tableview.cellSpan.SpanModel;

/**
 */
public class CellSpanTableRowSkin<T> extends CellSkinBase<TableRow<T>, CellBehaviorBase<TableRow<T>>> {
    
    private static enum SpanType {
        NONE,
        COLUMN,
        ROW,
        BOTH,
        UNSET;
    }

    // Specifies the number of times we will call 'recreateCells()' before we blow
    // out the cellsMap structure and rebuild all cells. This helps to prevent
    // against memory leaks in certain extreme circumstances.
    private static final int DEFAULT_FULL_REFRESH_COUNTER = 100;

    /*
     * A map that maps from TableColumn to TableCell (i.e. model to view).
     * This is recreated whenever the leaf columns change, however to increase
     * efficiency we create cells for all columns, even if they aren't visible,
     * and we only create new cells if we don't already have it cached in this
     * map.
     *
     * Note that this means that it is possible for this map to therefore be
     * a memory leak if an application uses TableView and is creating and removing
     * a large number of tableColumns. This is mitigated in the recreateCells()
     * function below - refer to that to learn more.
     */
    private WeakHashMap<TableColumn, TableCell> cellsMap;

    // This observableArrayList contains the currently visible table cells for this row.
    private final List<TableCell> cells = new ArrayList<TableCell>();
    
    private int fullRefreshCounter = DEFAULT_FULL_REFRESH_COUNTER;

    private boolean showColumns = true;
    
    private boolean isDirty = false;
    private boolean updateCells = false;
    
    private TableView<T> tableView;
    
    private ListChangeListener visibleLeafColumnsListener = new ListChangeListener() {
        @Override public void onChanged(ListChangeListener.Change c) {
            isDirty = true;
            requestLayout();
        }
    };
    
    // spanning support
    private CellSpanTableView<T> cellSpanTableView;
    private SpanModel spanModel;
//    private static final Map<TableView, SpanType[][]> spanMap = new WeakHashMap<TableView, SpanType[][]>();
    
    // supports variable row heights
    public static double getTableRowHeight(int index, TableRow tableRow) {
        if (index < 0) {
            return DEFAULT_CELL_SIZE;
        }
        
        Group virtualFlowSheet = (Group) tableRow.getParent();
        Node node = tableRow.getParent().getParent().getParent();
        if (node instanceof VirtualFlow) {
            ObservableList<Node> children = virtualFlowSheet.getChildren();
            
            if (index < children.size()) {
                return children.get(index).prefHeight(tableRow.getWidth());
            }
        }
        
        return DEFAULT_CELL_SIZE;
    }
    
    /**
     * Used in layoutChildren to specify that the node is not visible due to spanning.
     */
    private void hide(Node node) {
        node.setManaged(false);
        node.setVisible(false);
    }
    
    /**
     * Used in layoutChildren to specify that the node is now visible.
     */
    private void show(Node node) {
        node.setManaged(true);
        node.setVisible(true);
    }
    
    // TODO we can optimise this code if we cache the spanTypeArray, which at
    //      present is created for every query
    // TODO we can optimise this code if we set a maximum span distance
    private SpanType getSpanType(final int row, final int column) {
        SpanType[][] spanTypeArray;
//        if (spanMap.containsKey(tableView)) {
//            spanTypeArray = spanMap.get(tableView);
//            
//            // if we already have an array, lets check it for the result
//            if (spanTypeArray != null && row < spanTypeArray.length && column < spanTypeArray[0].length) {
//                SpanType cachedResult = spanTypeArray[row][column];
//                if (cachedResult != SpanType.UNSET) {
//                    return cachedResult;
//                }
//            }
//        } else {
            int rowCount = tableView.getItems().size();
            int columnCount = tableView.getVisibleLeafColumns().size();
            spanTypeArray = new SpanType[rowCount][columnCount];
//            spanMap.put(tableView, spanTypeArray);
            
            // initialise the array to be SpanType.UNSET
            for (int _row = 0; _row < rowCount; _row++) {
                for (int _column = 0; _column < columnCount; _column++) {
                    spanTypeArray[_row][_column] = SpanType.UNSET;
                }
            }
//        }
        
        if (spanModel == null || ! spanModel.isCellSpanEnabled()) {
            spanTypeArray[row][column] = SpanType.NONE;
            return SpanType.NONE;
        }
        
        // for the given row / column position, we need to see if anything in
        // the spanModel will prevent this column from being shown
        
        // Firstly we will check along the x-axis (i.e. whether there is an
        // earlier TableColumn that covers this column index)
        int distance = 0;
        for (int _col = column - 1; _col >= 0; _col--) {
            distance++;
            CellSpan cellSpan = spanModel.getCellSpanAt(row, _col);
            if (cellSpan == null) continue;
            if (cellSpan.getColumnSpan() > distance) {
                spanTypeArray[row][column] = SpanType.COLUMN;
                return SpanType.COLUMN;
            }
        }
        
        // secondly we'll try along the y-axis
        distance = 0;
        for (int _row = row - 1; _row >= 0; _row--) {
            distance++;
            CellSpan cellSpan = spanModel.getCellSpanAt(_row, column);
            if (cellSpan == null) continue;
            if (cellSpan.getRowSpan() > distance) {
                spanTypeArray[row][column] = SpanType.ROW;
                return SpanType.ROW;
            }
        }
        
        // finally, we have to try diagonally
        int rowDistance = 0;
        int columnDistance = 0;
        for (int _col = column - 1, _row = row - 1; _col >= 0 && _row >= 0; _col--, _row--) {
            rowDistance++;
            columnDistance++;
            CellSpan cellSpan = spanModel.getCellSpanAt(_row, _col);
            if (cellSpan == null) continue;
            if (cellSpan.getRowSpan() > rowDistance && 
                cellSpan.getColumnSpan() > columnDistance) {
                    spanTypeArray[row][column] = SpanType.BOTH;
                    return SpanType.BOTH;
            }
        }
        
        spanTypeArray[row][column] = SpanType.NONE;
        return SpanType.NONE;
    }

    public CellSpanTableRowSkin(TableRow<T> tableRow) {
        super(tableRow, new CellBehaviorBase<TableRow<T>>(tableRow));
        
        getSkinnable().setPickOnBounds(false);
        this.tableView = tableRow.getTableView();

        recreateCells();
        updateCells(true);

        initBindings();

        registerChangeListener(tableRow.itemProperty(), "ITEM");
        registerChangeListener(tableRow.editingProperty(), "EDITING");
        registerChangeListener(tableRow.tableViewProperty(), "TABLE_VIEW");
        
        // add listener to cell span model
        if (tableView instanceof CellSpanTableView) {
            cellSpanTableView = (CellSpanTableView) tableView;
            spanModel = cellSpanTableView.getSpanModel();
            registerChangeListener(cellSpanTableView.spanModelProperty(), "SPAN_MODEL");
        } else {
            cellSpanTableView = null;
        }
    }

    @Override protected void handleControlPropertyChanged(String p) {
        // we run this before the super call because we want to update whether
        // we are showing columns or the node (if it isn't null) before the
        // parent class updates the content
        if (p == "TEXT" || p == "GRAPHIC" || p == "EDITING") {
            updateShowColumns();
        }

        super.handleControlPropertyChanged(p);

        if (p == "ITEM") {
            updateCells = true;
            requestLayout();
            getSkinnable().layout();
        } else if (p == "TABLE_VIEW") {
            for (int i = 0; i < getChildren().size(); i++) {
                Node n = getChildren().get(i);
                if (n instanceof TableCell) {
                    ((TableCell)n).updateTableView(getSkinnable().getTableView());
                }
            }
            
            this.tableView = getSkinnable().getTableView();
            if (tableView instanceof CellSpanTableView) {
                cellSpanTableView = (CellSpanTableView) tableView;
                spanModel = cellSpanTableView.getSpanModel();
                registerChangeListener(cellSpanTableView.spanModelProperty(), "SPAN_MODEL");
            } else {
                cellSpanTableView = null;
            }
        } else if (p == "SPAN_MODEL") {
            // TODO update layout based on changes to span model
            spanModel = ((CellSpanTableView)getSkinnable().getTableView()).getSpanModel();
            requestLayout();
        }
    }

    private void updateShowColumns() {
        boolean newValue = (isIgnoreText() && isIgnoreGraphic());
        if (showColumns == newValue) return;
        
        showColumns = newValue;

        requestLayout();
    }
    
    private void initBindings() {
        // watches for any change in the leaf columns observableArrayList - this will indicate
        // that the column order has changed and that we should update the row
        // such that the cells are in the new order
        if (getSkinnable() == null) {
            throw new IllegalStateException("TableRowSkin does not have a Skinnable set to a TableRow instance");
        }
        if (getSkinnable().getTableView() == null) {
            throw new IllegalStateException("TableRow not have the TableView property set");
        }
        
        ObservableList<TableColumn<T,?>> visibleLeafColumns = getSkinnable().getTableView().getVisibleLeafColumns();
        visibleLeafColumns.addListener(
                new WeakListChangeListener<TableColumn<T, ?>>(visibleLeafColumnsListener));
    }
    
    private void doUpdateCheck() {
        if (isDirty) {
            recreateCells();
            updateCells(true);
            isDirty = false;
        } else if (updateCells) {
            updateCells(false);
            updateCells = false;
        }
    }

    @Override protected void layoutChildren(double x, final double y,
            final double w, final double h) {
        doUpdateCheck();
        
        if (tableView == null) return;
        if (cellsMap.isEmpty()) return;
        
        if (showColumns && ! tableView.getVisibleLeafColumns().isEmpty()) {
            // layout the individual column cells
            double width;
            double height;
            
            Insets insets = getInsets();
            
            double verticalPadding = insets.getTop() + insets.getBottom();
            double horizontalPadding = insets.getLeft() + insets.getRight();
            
            int row = getSkinnable().getIndex();
            if (row < 0 || row >= tableView.getItems().size()) return;
            
            for (int column = 0; column < getChildren().size(); column++) {
                // in most cases all children are TableCell instances, but this
                // is not always the case. For example, see RT-17694
                Node node = getChildren().get(column);
                show(node);
                
                width = snapSize(node.prefWidth(-1)) - snapSize(horizontalPadding);
                height = Math.max(getHeight(), node.prefHeight(-1));
                height = snapSize(height) - snapSize(verticalPadding);
                
                ///////////////////////////////////////////
                // cell spanning code starts here
                ///////////////////////////////////////////
                if (spanModel != null && spanModel.isCellSpanEnabled()) {
                    // cell span check - basically, see if there is a cell span
                    // impacting upon the cell at the given row / column index
                    SpanType spanType = getSpanType(row, column);
                    switch (spanType) {
                        case ROW:
                        case BOTH: x += width; // fall through is on purpose here
                        case COLUMN:
                            hide(node);
                            node.resize(0, 0);
                            node.relocate(x, insets.getTop());
                            continue;          // we don't want to fall through
                                               // infact, we return to the loop here
                        case NONE:
                        case UNSET:            // fall through and carry on
                    }
                
                    CellSpan cellSpan = spanModel.getCellSpanAt(row, column);
                    if (cellSpan != null) {
                        if (cellSpan.getColumnSpan() > 1) {
                            // we need to span multiple columns, so we sum up
                            // the width of the additional columns, adding it
                            // to the width variable
                            for (int i = 1, 
                                    colSpan = cellSpan.getColumnSpan(), 
                                    max = getChildren().size() - column; 
                                    i < colSpan && i < max; i++) {
                                // calculate the width
                                Node adjacentNode = getChildren().get(column + i);
                                width += snapSize(adjacentNode.prefWidth(-1));
                            }
                        }
                        
                        if (cellSpan.getRowSpan() > 1) {
                            // we need to span multiple rows, so we sum up
                            // the height of the additional rows, adding it
                            // to the height variable
                            for (int i = 1; i < cellSpan.getRowSpan(); i++) {
                                // calculate the height
                                double rowHeight = getTableRowHeight(row + i, getSkinnable());
                                height += snapSize(rowHeight);
                            }
                        }
                    }
                } 
                ///////////////////////////////////////////
                // cell spanning code ends here
                ///////////////////////////////////////////

                node.resize(width, height);
                node.relocate(x, insets.getTop());
                x += width;
            }
        } else {
            super.layoutChildren(x,y,w,h);
        }
    }

    private int columnCount = 0;
    
    private void recreateCells() {
        // This function is smart in the sense that we don't recreate all
        // TableCell instances every time this function is called. Instead we
        // only create TableCells for TableColumns we haven't already encountered.
        // To avoid a potential memory leak (when the TableColumns in the
        // TableView are created/inserted/removed/deleted, we have a 'refresh
        // counter' that when we reach 0 will delete all cells in this row
        // and recreate all of them.
        
        TableView<T> table = getSkinnable().getTableView();
        if (table == null) {
            clearCellsMap();
            return;
        }
        
        ObservableList<TableColumn<T,?>> columns = table.getVisibleLeafColumns();
        
        if (columns.size() != columnCount || fullRefreshCounter == 0 || cellsMap == null) {
            clearCellsMap();
            cellsMap = new WeakHashMap<TableColumn, TableCell>(columns.size());
            fullRefreshCounter = DEFAULT_FULL_REFRESH_COUNTER;
            getChildren().clear();
        }
        columnCount = columns.size();
        fullRefreshCounter--;
        
        TableRow skinnable = getSkinnable();
        
        for (TableColumn col : columns) {
            if (cellsMap.containsKey(col)) {
                continue;
            }
            
            // we must create a TableCell for each table column
            final TableCell cell = (TableCell) col.getCellFactory().call(col);

            // we set it's TableColumn, TableView and TableRow
            cell.updateTableColumn(col);
            cell.updateTableView(table);
            cell.updateTableRow(skinnable);

            // and store this in our HashMap until needed
            cellsMap.put(col, cell);
        }
    }
    
    private void clearCellsMap() {
        if (cellsMap != null) cellsMap.clear();
    }

    private void updateCells(boolean resetChildren) {
        // if delete isn't called first, we can run into situations where the
        // cells aren't updated properly.
        cells.clear();

        TableRow skinnable = getSkinnable();
        int skinnableIndex = skinnable.getIndex();
        TableView<T> table = skinnable.getTableView();
        if (table != null) {
            List<TableColumn<T,?>> visibleLeafColumns = table.getVisibleLeafColumns();
            for (int i = 0, max = visibleLeafColumns.size(); i < max; i++) {
                TableColumn<T,?> col = visibleLeafColumns.get(i);
                TableCell cell = cellsMap.get(col);
                if (cell == null) continue;

                cell.updateIndex(skinnableIndex);
                cell.updateTableRow(skinnable);
                cells.add(cell);
            }
        }

        // update children of each row
        ObservableList<Node> children = getChildren();
        if (resetChildren) {
            if (showColumns) {
                if (cells.isEmpty()) {
                    children.clear();
                } else {
                    // TODO we can optimise this by only showing cells that are 
                    // visible based on the table width and the amount of horizontal
                    // scrolling.
                    children.setAll(cells);
                }
            } else {
                children.clear();

                if (!isIgnoreText() || !isIgnoreGraphic()) {
                    children.add(skinnable);
                }
            }
        }
    }
    
    @Override protected double computePrefWidth(double height) {
        doUpdateCheck();
        
        if (showColumns) {
            double prefWidth = 0.0F;

            if (getSkinnable().getTableView() != null) {
                List<TableColumn<T,?>> visibleLeafColumns = 
                        getSkinnable().getTableView().getVisibleLeafColumns();
                
                for (int i = 0, max = visibleLeafColumns.size(); i < max; i++) {
                    TableColumn<T,?> tableColumn = visibleLeafColumns.get(i);
                    prefWidth += tableColumn.getWidth();
                }
            }

            return prefWidth;
        } else {
            return super.computePrefWidth(height);
        }
    }
    
    private static final double DEFAULT_CELL_SIZE = 24.0;
    
    @Override protected double computePrefHeight(double width) {
        doUpdateCheck();
        
        if (showColumns) {
            // Support for RT-18467: making it easier to specify a height for
            // cells via CSS, where the desired height is less than the height
            // of the TableCells. Essentially, -fx-cell-size is given higher
            // precedence now
            if (getCellSize() < DEFAULT_CELL_SIZE) {
                return getCellSize();
            }
            
            // FIXME according to profiling, this method is slow and should
            // be optimised
            double prefHeight = 0.0f;
            final int count = cells.size();
            for (int i=0; i<count; i++) {
                final TableCell tableCell = cells.get(i);
                prefHeight = Math.max(prefHeight, tableCell.prefHeight(-1));
            }
            return Math.max(prefHeight, Math.max(getCellSize(), getSkinnable().minHeight(-1)));
        } else {
            return super.computePrefHeight(width);
        }
    }
}

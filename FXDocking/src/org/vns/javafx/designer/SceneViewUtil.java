package org.vns.javafx.designer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.ObservableList;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.transform.Transform;
import org.vns.javafx.dock.api.selection.SelectionFrame;

/**
 *
 * @author Valery
 */
public class SceneViewUtil {

    public static final String GESTURE_SOURCE_KEY = "drag-gesture-source-key";
    public static final String DRAGBOARD_KEY = "dragboard-url-key";
    public static final String REMOVER_KEY = "remove-children-node-key";
    public static final String MOUSE_EVENT_NOTIFIER_KEY = "mouse-event-notifier-key";
    public static final String CHANGE_LISTENER = "object-change-handler-listener-key";

    protected static TreeItem parentOfLevel(TreeView treeView, TreeItem item, int level) {
        TreeItem it = item;
        while (it != null) {
            if (treeView.getTreeItemLevel(it) == level) {
                break;
            }
            it = it.getParent();
        }
        return it;
    }

    public static TreeCell getCell(TreeItemEx item) {
        //return (TreeCell) ((AnchorPane) item.getCellGraphic()).getParent();
        return (TreeCell) item.getCellGraphic().getParent();
    }

    public static Bounds screenTreeItemBounds(TreeItemEx treeItem) {
        Node node = treeItem.getCellGraphic().getParent();
        return node.localToScreen(node.getBoundsInLocal());
    }

    /**
     * Returns visible bounds of a cell which the given {@code treeItem}
     * represents. When calculate the bounds the method takes into account
     * whether there is a horizontal scroll bar. The resulting bounds differ
     * from the full bounds of the cell only with a {@code width } value.
     *
     * @param treeView the tree view of the specified {@code treeItem}
     * @param treeItem ???
     * @return ???
     */
    public static Bounds screenHorVisibleBounds(TreeViewEx treeView, TreeItemEx treeItem) {
        Node node = treeItem.getCellGraphic().getParent();
        Bounds retval = node.localToScreen(node.getBoundsInLocal());
        if (treeView.getHScrollBar().isVisible()) {
            double vertSrollBarWidth = treeView.getVScrollBar().getWidth();
            Bounds b = node.getBoundsInParent();
            Bounds tvb = treeView.getBoundsInLocal();
            double w = tvb.getWidth() - treeView.getInsets().getRight() - treeView.getInsets().getLeft() - vertSrollBarWidth;
            retval = new BoundingBox(retval.getMinX(), retval.getMinY(), w, retval.getHeight());
        }
        return retval;
    }

    public static Bounds getIntersection(Bounds b1, Bounds b2) {

        if (!b1.intersects(b2)) {
            return null;
        }

        double x, y, w, h;
        Bounds ib; // internal
        Bounds eb; // external
        if (b1.getMinX() >= b2.getMinX()) {
            x = b1.getMinX();
            ib = b1;
            eb = b2;
        } else {
            ib = b2;
            eb = b1;
            x = b2.getMinX();
        }

        double iCoord = ib.getMinX();
        double iDimention = ib.getWidth();
        double eCoord = eb.getMinX();
        double eDimention = eb.getWidth();

        double iEndDimention = iCoord + iDimention - 1;
        double eEndDimention = eCoord + eDimention - 1;

        if (iEndDimention <= eEndDimention) {
            w = iDimention;
        } else {
            w = eEndDimention - iCoord + 1;
        }
        //
        // y and h
        //
        if (b1.getMinY() >= b2.getMinY()) {
            y = b1.getMinY();
            ib = b1;
            eb = b2;
        } else {
            ib = b2;
            eb = b1;
            y = b2.getMinY();
        }

        iCoord = ib.getMinY();
        iDimention = ib.getHeight();
        eCoord = eb.getMinY();
        eDimention = eb.getHeight();

        iEndDimention = iCoord + iDimention - 1;
        eEndDimention = eCoord + eDimention - 1;

        if (iEndDimention <= eEndDimention) {
            h = iDimention;
        } else {
            h = eEndDimention - iCoord + 1;
        }

        return new BoundingBox(x, y, w, h);

    }

    public static Bounds translate(Bounds b1, double x, double y) {
        return Transform.translate(x, y).transform(b1);
    }

    public static Bounds screenInsetsFreeBounds(Region node) {
        Bounds b = node.localToScreen(node.getBoundsInLocal());
        Insets ins = node.getInsets();
        return new BoundingBox(b.getMinX() + ins.getLeft(),
                b.getMinY() + ins.getTop(),
                b.getWidth() - ins.getLeft() - ins.getRight(),
                b.getHeight() - ins.getTop() - ins.getBottom()
        );
    }

    public static TreeItemEx findTreeItemByObject(Object value) {
        TreeItemEx item = null;
        SceneView sgv = DesignerLookup.lookup(SceneView.class);
        if (sgv != null) {
            if (sgv.getTreeView().getRoot().getValue() == value) {
                item = (TreeItemEx) sgv.getTreeView().getRoot();
            } else {
                item = SceneViewUtil.findTreeItemByObject(sgv.getTreeView(), value);
            }
        }
        return item;
    }
    public static Parent findParentByObject(Object value) {
        Parent retval = null;
        TreeItem item = null;
        SceneView sgv = DesignerLookup.lookup(SceneView.class);
        if (sgv != null) {
            if (sgv.getTreeView().getRoot().getValue() == value) {
                item = (TreeItemEx) sgv.getTreeView().getRoot();
            } else {
                item = SceneViewUtil.findTreeItemByObject(sgv.getTreeView(), value);
            }
            item = item.getParent();

            while( item != null ) {
                if ( (item.getValue() instanceof Parent)) {
                    retval = (Parent) item.getValue();
                    break;
                }
                item = item.getParent();
            }
        }
        
        return retval;
    }

    public static TreeItemEx findTreeItemByObject(TreeView treeView, Object sourceGesture) {
        if (treeView.getRoot() != null && treeView.getRoot().getValue() == sourceGesture) {
            return (TreeItemEx) treeView.getRoot();
        }
        return findChildTreeItem((TreeItemEx) treeView.getRoot(), sourceGesture);
    }

    protected static TreeItemEx findChildTreeItem(TreeItemEx item, Object sourceGesture) {
        TreeItemEx retval = null;
        if (item == null || item.getChildren().isEmpty()) {
            return null;
        }
        for (TreeItem it : item.getChildren()) {
            if (it.getValue() == sourceGesture) {
                retval = (TreeItemEx) it;
                break;
            }
            retval = findChildTreeItem((TreeItemEx) it, sourceGesture);
            if (retval != null) {
                break;
            }
        }
        return retval;
    }

    protected static TreeItemEx findByTreeItemObject(TreeItemEx item) {
        if (item.getValue() == null) {
            return null;
        }
        TreeItemEx root = findRootTreeItem(item);
        return findChildTreeItem(root, item.getValue());
    }

    protected static TreeItemEx findRootTreeItem(TreeItemEx item) {
        TreeItemEx root = item;
        TreeItemEx retval = null;
        while (root != null) {
            retval = root;
            root = (TreeItemEx) root.getParent();

        }
        return retval;
    }

    public static TreeItemEx findTreeItem(TreeViewEx treeView, double x, double y) {
        TreeItemEx retval = null;
        int count = treeView.getExpandedItemCount();
        for (int i = 0; i < count; i++) {
            TreeCell cell = (TreeCell) ((TreeItemEx) treeView.getTreeItem(i)).getCellGraphic().getParent();
            if (cell == null) {
                continue;
            }
            if (cell.contains(cell.screenToLocal(x, y))) {
                retval = (TreeItemEx) treeView.getTreeItem(i);
                break;
            }
        }
        return retval;
    }

    /*    public static TreeViewEx getTargetTreeView(double x, double y) {
        TreeViewEx retval = null;
        List<Stage> allStages = StageHelper.getStages();
        if (allStages.isEmpty()) {
            return null;
        }
        for (Stage s : allStages) {
            if (s.getScene() == null || s.getScene().getRoot() == null) {
                break;
            }
            Bounds b = s.getScene().getRoot().localToScreen(s.getScene().getRoot().getBoundsInLocal());
            if (!b.contains(x, y)) {
                continue;
            }
            Node n = s.getScene().lookup("." + TreeViewEx.LOOKUP_SELECTOR);

            if (n != null && (n instanceof TreeViewEx)) {
                retval = (TreeViewEx) n;
                Set<Node> set = s.getScene().getRoot().lookupAll("." + TreeViewEx.LOOKUP_SELECTOR);
                retval = (TreeViewEx) TopNodeHelper.getTopNode(set);
            }
        }

        return retval;
    }
     */
    public static String changeNodeStyle(String oldStyle, String newStyle) {
        String retval = "";
        if (oldStyle == null || oldStyle.trim().isEmpty() || newStyle == null || newStyle.trim().isEmpty()) {
            retval = newStyle;
        } else {
            String[] oldStyles = oldStyle.split(";");
            String[] newStyles = newStyle.split(";");
            Map<String, String> oldMap = new HashMap<>();
            Map<String, String> newMap = new HashMap<>();

            for (String s : oldStyles) {
                String[] kv = s.split(":");
                String key = kv[0].trim();
                String val = kv[1].trim();
                oldMap.put(key, val);
            }
            for (String s : newStyles) {
                String[] kv = s.split(":");
                String key = kv[0].trim();
                String val = kv[1].trim();
                newMap.put(key, val);
            }
            newMap.forEach((k, v) -> {
                oldMap.put(k, v);
            });
            StringBuilder sb = new StringBuilder(retval);
            oldMap.forEach((k, v) -> {
                sb.append(k + ":" + v + ";");
            });
            retval = sb.toString();
        }
        return retval;
    }

    /*    public static List<Node> getChildren(Node child) {
        Parent p = child.getParent();
        if (child.getParent() == null) {
            return null;
        }
        ObservableList list = null;
        Class<?> c = p.getClass();
        Method method;
        try {
            method = c.getDeclaredMethod("getChildren");
            method.setAccessible(true);
            list = (ObservableList) method.invoke(p);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            
        }
        return list;
    }
     */
    public static ObservableList<Node> getChildren(Parent p) {

        ObservableList list = null;

        if (p instanceof Pane) {
            list = ((Pane) p).getChildren();
        }
        if (list != null) {
            return list;
        }
        Class<?> c = p.getClass();
        Method method;
        try {
            method = c.getDeclaredMethod("getChildren");
            method.setAccessible(true);
            list = (ObservableList) method.invoke(p);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {

        }
        return list;
    }

    public static boolean addToParent(Parent parent, Node toAdd) {
        boolean retval = false;
        List children = getChildren(parent);
        if (children != null) {
            retval = true;
            children.add(toAdd);
        }
        return retval;
    }

    public static boolean removeFromParent(Parent parent, Node toRemove) {
        boolean retval = false;

        List children = getChildren(parent);
        if (children != null) {
            retval = true;
            children.remove(toRemove);
        }
        return retval;
    }

/*    public static Parent getTopParentOf(Node node) {
        Parent retval = null;
        Parent p = node.getParent();
        while (p != null) {
            retval = p;
            p = p.getParent();
        }
        if (retval == null && (node instanceof Parent)) {
            retval = (Parent) node;
        }
        return retval;
    }
*/
    public static void toBack(SelectionFrame frame, Node node) {
        if (node == null || node.getParent() == null) {
            return;
        }
        List list = getChildren(node.getParent());
        if (list == null) {
            return;
        }
        int idx = list.indexOf(node);
        int idx1 = list.indexOf(frame);
        if (idx < 0 || idx < idx1) {
            list.add(idx, frame);
        }
    }
}

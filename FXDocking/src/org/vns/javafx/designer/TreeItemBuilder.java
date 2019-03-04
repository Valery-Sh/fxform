/*
 * Copyright 2018 Your Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.vns.javafx.designer;

import org.vns.javafx.designer.descr.NodeProperty;
import org.vns.javafx.designer.descr.NodeDescriptorRegistry;
import org.vns.javafx.designer.descr.NodeDescriptor;
import org.vns.javafx.designer.descr.NodeList;
import org.vns.javafx.designer.descr.NodeContent;
import org.vns.javafx.designer.descr.NodeElement;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ConstraintsBase;
import javafx.scene.layout.HBox;
import static org.vns.javafx.designer.SceneView.ANCHOR_OFFSET;
import static org.vns.javafx.designer.SceneView.FIRST;
import static org.vns.javafx.designer.SceneView.LAST;
import org.vns.javafx.designer.TreeItemEx.ItemType;
import static org.vns.javafx.designer.TreeItemEx.ItemType.CONTENT;
import static org.vns.javafx.designer.TreeItemEx.ItemType.DEFAULTLIST;
import static org.vns.javafx.designer.TreeItemEx.ItemType.LIST;
import static org.vns.javafx.designer.TreeItemEx.ItemType.MIXED;
import org.vns.javafx.dock.api.Util;
import org.vns.javafx.dock.api.DockRegistry;
import org.vns.javafx.dock.api.Selection;
import org.vns.javafx.dock.api.bean.BeanAdapter;
import org.vns.javafx.dock.api.bean.ReflectHelper;

/**
 *
 * The objects of this class are used to create a representation of a specified
 * object as a tree of objects of type
 * {@link org.vns.javafx.designer.TreeItemEx}. To create the tree the following
 * code may be used
 * <pre>
 *  Object obj = ...
 *  TreItemExe result = build(obj);
 * </pre>
 *
 * The variable {@code result} may contain a list of children. And each child
 * may contain a list of children respectively and so on.
 *
 * <p>
 * To create the tree the class uses objects of type (@link
 * org.vns.javafx.designer.descr.NodeDescriptor}.
 * </p>
 *
 * @author Valery Shyshkin
 */
public class TreeItemBuilder {

    public static final String ACCEPT_TYPES_KEY = "tree-item-builder-accept-types";
    public static final String CELL_UUID = "uuid-29a4b479-0282-41f1-8ac8-21b4923235be";
    public static final String NODE_UUID = "uuid-f53db037-2e33-4c68-8ffa-06044fc10f81";

    private final boolean designer;
    private Object objToBuild;

    public TreeItemBuilder(boolean designer) {
        this.designer = designer;
    }

    public TreeItemBuilder(Object object) {
        this(true);
        objToBuild = object;
    }

    public TreeItemBuilder() {
        this(true);
    }

    private void setContexts(Object obj) {
        if (!designer) {
            return;
        }
//        if (SceneView.isFrame(obj)) {
        if (Util.isForeign(obj)) {
            return;
        }
        PalettePane palette = DesignerLookup.lookup(PalettePane.class);
        if (palette == null) {
            return;
        }

        palette.setLayoutContext(obj);
        palette.setDockableContext(obj);
    }

    public TreeItemEx build(Object obj) {
        return build(obj, null);
    }

    protected TreeItemEx build(Object obj, NodeElement p) {
        //if (SceneView.isFrame(obj)) {
        if ( Util.isForeign(obj) ) {
            return null;
        }
        setContexts(obj);
        PalettePane.addDesignerStyles(obj);
        TreeItemEx retval;
        if (p != null && (p instanceof NodeContent)) {
            retval = createContentItem(obj, (NodeContent) p);
        } else if (p != null && (p instanceof NodeList)) {
            retval = createListContentItem(obj, (NodeList) p);
        } else {
            retval = createListElementItem(obj);
        }
        if (p != null && (p instanceof NodeProperty)) {
            retval.setPropertyName(((NodeProperty) p).getName());
        }
        if (p != null && (p instanceof NodeList)) {
            ObservableList ol = (ObservableList) obj;
            for (int i = 0; i < ol.size(); i++) {
                if (Util.isForeign(ol.get(i))) {
                    continue;
                }
                TreeItemEx it = build(ol.get(i));
                retval.getChildren().add(it);
                PalettePane.addDesignerStyles(ol.get(i));
            }
            return retval;
        }
        if (obj == null && !(p instanceof NodeList)) {
            return retval;
        }

        NodeDescriptor nd = NodeDescriptorRegistry.getInstance().getDescriptor(obj.getClass());

        BeanAdapter adapter = new BeanAdapter(obj);
        int defaultCount = 0;
        boolean alwaysVisible = true;
        for (NodeProperty cp : nd.getProperties()) {
            Object cpObj = adapter.get(cp.getName());
            if ((cp instanceof NodeList)) {
                if (nd.getProperties().size() == 1 && !((NodeList) cp).isAlwaysVisible()) {
                    alwaysVisible = false;
                    //
                    // Omit TreeItem for ListItem
                    //
                    ObservableList ol = (ObservableList) cpObj;
                    for (int i = 0; i < ol.size(); i++) {
                        if (Util.isForeign(ol.get(i))) {
                            continue;
                        }
                        TreeItemEx it = build(ol.get(i));
                        retval.getChildren().add(it);
                        PalettePane.addDesignerStyles(ol.get(i));

                        //retval.getChildren().add(build(ol.get(i)));
                    }
                    retval.setItemType(ItemType.DEFAULTLIST);
                } else {
                    if (!((NodeList) cp).isAlwaysVisible()) {
                        alwaysVisible = false;
                    }
                    TreeItemEx listItem = build(cpObj, (NodeList) cp);
                    retval.getChildren().add(listItem);
                    listItem.setPropertyName(cp.getName());
                    PalettePane.addDesignerStyles(cpObj);
                }
                if (cp.isDefault()) {
                    defaultCount++;
                }
            } else if ((cp instanceof NodeContent) && (cpObj != null || !((NodeContent) cp).isHideWhenNull())) {
                TreeItemEx item = build(cpObj, (NodeContent) cp);
                item.setPropertyName(cp.getName());
                retval.getChildren().add(item);
                PalettePane.addDesignerStyles(cpObj);

                if (cp.isDefault()) {
                    defaultCount++;
                }
            } else {
                alwaysVisible = false;
            }
        }//for
        if (alwaysVisible && defaultCount >= 1) {
            retval.setItemType(MIXED);
        }
        return retval;
    }

    public final HBox createListElementItemContent(Object obj) {

        HBox box = new HBox(new HBox()); // placeholder 
        NodeDescriptor nd = NodeDescriptorRegistry.getInstance().getDescriptor(obj.getClass());
        String text = "";
        String title = nd.getTitle();
        if (title != null) {
            text = title;
        } else {
            String tp = nd.getTitleProperty();

            if (tp != null) {
                BeanAdapter adapter = new BeanAdapter(obj);
                text = (String) adapter.get(tp);
                if (text == null) {
                    text = "";
                }
            }
        }
        Label label = new Label();
        if (nd.getTitle() != null) {
            label.setText(title);
        } else {
            label.setText((obj.getClass().getSimpleName() + " " + text).trim());
        }
        //Label label = new Label((obj.getClass().getSimpleName() + " " + text).trim());

        String styleClass = nd.getStyleClass();
        if (styleClass == null) {
            styleClass = "tree-item-node-" + obj.getClass().getSimpleName().toLowerCase();
        }
        label.getStyleClass().add(styleClass);
        box.getChildren().add(label);
        return box;
    }

    public final TreeItemEx createListElementItem(Object obj) {
        HBox box = new HBox();
        AnchorPane anchorPane = new AnchorPane(box);
        AnchorPane.setBottomAnchor(box, ANCHOR_OFFSET);
        AnchorPane.setTopAnchor(box, ANCHOR_OFFSET);

        TreeItemEx retval = new TreeItemEx();

        retval.setValue(obj);

        box.getChildren().add(createListElementItemContent(obj));

        retval.setCellGraphic(anchorPane);
        retval.setItemType(TreeItemEx.ItemType.ELEMENT);
        try {
            retval.registerChangeHandlers();
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
            Logger.getLogger(TreeItemBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retval;
    }

    protected HBox createListContentContent(Object obj, NodeList nodeList) {
        HBox box = new HBox(new HBox()); // placeholder 
        String title = nodeList.getTitle();

        if (title == null) {
            title = NodeList.DEFAULT_TITLE;
        }
        Label label = new Label(title.trim());
        String styleClass = nodeList.getStyleClass();
        if (styleClass == null) {
            styleClass = NodeList.DEFAULT_STYLE_CLASS;
        }
        label.getStyleClass().add(styleClass);
        box.getChildren().add(label);
        return box;
    }

    public final TreeItemEx createListContentItem(Object obj, NodeList nodeList) {
        HBox box = new HBox();
        AnchorPane anchorPane = new AnchorPane(box);
        AnchorPane.setBottomAnchor(box, ANCHOR_OFFSET);
        AnchorPane.setTopAnchor(box, ANCHOR_OFFSET);

        TreeItemEx retval = new TreeItemEx();

        retval.setValue(obj);
        box.getChildren().add(createListContentContent(obj, nodeList));

        retval.setCellGraphic(anchorPane);

        retval.setItemType(TreeItemEx.ItemType.LIST);
        try {
            retval.registerChangeHandlers();
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
            Logger.getLogger(TreeItemBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retval;
    }

    public final TreeItemEx createContentItem(Object obj, NodeContent cp) {
        HBox box = new HBox();
        AnchorPane anchorPane = new AnchorPane(box);
        AnchorPane.setBottomAnchor(box, ANCHOR_OFFSET);
        AnchorPane.setTopAnchor(box, ANCHOR_OFFSET);
        TreeItemEx retval = new TreeItemEx();
        retval.setValue(obj);

        box.getChildren().add(createContentItemContent(obj, cp));
        retval.setCellGraphic(anchorPane);
        retval.setItemType(TreeItemEx.ItemType.CONTENT);
        try {

            retval.registerChangeHandlers();
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
            Logger.getLogger(TreeItemBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }

        return retval;
    }

    protected HBox createContentItemContent(Object obj, NodeContent cp) {
        Label iconLabel = new Label();
        HBox retval = new HBox(iconLabel);

        String style = cp.getStyleClass();
        if (style == null) {
            style = NodeContent.DEFAULT_STYLE_CLASS;
        }

        iconLabel.getStyleClass().add(style);

        String title = cp.getTitle();

        if (title == null) {
            title = NodeContent.DEFAULT_TITLE;
        }

        if (obj == null) {
            iconLabel.setText(title.trim());
        } else {
            NodeDescriptor nd = NodeDescriptorRegistry.getInstance().getDescriptor(obj.getClass());
            title = nd.getTitle();
            if (title == null) {
                title = "";
                String tp = nd.getTitleProperty();
                if (tp != null) {
                    BeanAdapter adapter = new BeanAdapter(obj);
                    title = (String) adapter.get(tp);
                    if (title == null) {
                        title = "";
                    }
                }
                title = "";
            }
            Label label = new Label();
            if (nd.getTitle() != null) {
                label.setText(nd.getTitle());
            } else {
                label.setText((obj.getClass().getSimpleName() + " " + title).trim());
            }

            //Label glb = new Label(obj.getClass().getSimpleName());
            label.getStyleClass().add("tree-item-node-" + obj.getClass().getSimpleName().toLowerCase());
            retval.getChildren().add(label);

            //glb.setText(glb.getText() + " " + title.trim());
        }
        return retval;
    }

    /**
     *
     * @param treeView the treeView/ Cannot be null
     * @param target the item which is an actual layoutNode item to accept a
     * dragged object
     * @param place the item which is a gesture layoutNode during the
     * drag-and-drop operation
     * @param dragObject an object which is an actual object to be accepted by
     * the layoutNode item.
     * @return true if the builder evaluates that a specified dragObject can be
     * accepted by the given layoutNode tree item
     */
    public boolean isAdmissiblePosition(TreeViewEx treeView, TreeItemEx target,
            TreeItemEx place,
            Object dragObject) {
        //
        // Check if the dragObject equals to targetItemObject 
        //
        if (target.getValue() == dragObject) {
            return false;
        }
        TreeItemEx dragItem = SceneViewUtil.findTreeItemByObject(treeView, dragObject);
        //
        // First check if the layoutNode item corresponds to LIST ItemType
        //

        NodeDescriptor nd = null;
        if (target.getItemType() != LIST) {
            if (target.getValue() != null) {
                nd = NodeDescriptor.get(target.getValue().getClass());
            }
        }
        if (target == place && place.getDragDropQualifier() == LAST && nd != null && target.getItemType() == MIXED) {
            return isAcceptable(target, dragObject);
        }
        if (target.getItemType() == LIST || target.getItemType() == DEFAULTLIST) {
            if (dragItem != null && dragItem.previousSibling() == place) {
                return false;
            }
            if (dragItem == place) {
                if (treeView.getTreeItemLevel(place) - treeView.getTreeItemLevel(target) > 1) {

                    int level = treeView.getTreeItemLevel(target) + 1;

                    TreeItemEx actualPlace = (TreeItemEx) SceneViewUtil.parentOfLevel(treeView, place, level);
                    if (dragItem == actualPlace || dragItem.previousSibling() == actualPlace) {
                        return false;
                    }
                }
            }

            int insPos = getInsertIndex(treeView, target, place);
            int dragPos = target.getChildren().indexOf(dragItem);
            int targetSize = target.getChildren().size();

            if (target == place && target.getChildren().contains(dragItem)) {
                if (insPos == 0 && dragPos == 0) {
                    return false;
                }
                if (insPos == targetSize && dragPos == targetSize - 1) {
                    return false;
                }
            }
            if (dragItem == place && target != place && target.getChildren().contains(dragItem)) {
                if (insPos - dragPos == 1) {
                    return false;
                }
            }
        } else if (target.getValue() != null) {
            if (target != place) {
                return false;
            }
            //
            // Now target==place && target.getValue != null 
            //
            NodeProperty prop = nd.getDefaultContentProperty();

            if (prop == null) {
                return false;
            }
            //
            // layoutNode.getValue() may be null for NodeContent
            //
            BeanAdapter ba = new BeanAdapter(target.getValue());
            //
            // get object for default property of the target
            //
            Object o = ba.get(prop.getName());
            if (o != null && (prop instanceof NodeContent) && !((NodeContent) prop).isReplaceable()) {
                return false;
            }
            //
            // ckeck if assignable
            //
            if (prop instanceof NodeContent) {
                return ba.getType(prop.getName()).isAssignableFrom(dragObject.getClass());
            }
        }
        return isAcceptable(target, dragObject);
    }

    protected boolean isAcceptable(TreeItemEx target, Object toAccept) {
        if (toAccept == null) {
            return false;
        }
        boolean retval = true;
        NodeDescriptor nd;
        if (null == target.getItemType()) {
            //
            // The ItemType of the layoutNode TreeItem equals to NodeElement
            //
            nd = NodeDescriptor.get(target.getValue().getClass());
            NodeProperty prop = nd.getDefaultContentProperty();
            if (prop != null) {
                BeanAdapter ba = new BeanAdapter((target.getValue()));
                retval = ba.getType(prop.getName()).isAssignableFrom(toAccept.getClass());
            }
        } else {
            switch (target.getItemType()) {
                case CONTENT:
                    TreeItemEx parent = (TreeItemEx) target.getParent();
                    NodeProperty cp = parent.getProperty(target.getPropertyName());//nc.getProperties().get(layoutNode.getInsertIndex());
                    BeanAdapter adapter = new BeanAdapter(parent.getValue());
                    retval = adapter.getType(cp.getName()).isAssignableFrom(toAccept.getClass());
                    break;
                case MIXED:
                    retval = getDefaultPropertyNameFor(target, toAccept) != null;
                    break;
                case LIST:
                case DEFAULTLIST:
                    TreeItemEx listItem = getListTreeItemFor(target);
                    String propName = null;
                    if (listItem != null) {
                        propName = getListPropertyNameFor(target);
                    }
                    if (listItem != null) {
                        Class clazz = ReflectHelper.getListGenericType(listItem.getValue().getClass(), propName);
                        if (clazz != null) {
                            retval = clazz.isAssignableFrom(toAccept.getClass());
                        }
                    }
                    break;
                default:
                    //
                    // The ItemType of the layoutNode TreeItem equals to NodeElement
                    //
                    nd = NodeDescriptorRegistry.getInstance().getDescriptor(target.getValue().getClass());
                    NodeProperty prop = nd.getDefaultContentProperty();
                    if (prop != null) {
                        BeanAdapter ba = new BeanAdapter((target.getValue()));
                        retval = ba.getType(prop.getName()).isAssignableFrom(toAccept.getClass());
                    }
                    break;
            }
        }
        return retval;
    }

    protected TreeItemEx getListTreeItemFor(TreeItemEx item) {
        TreeItemEx retval = null;
        if (item.getItemType() == LIST) {
            retval = (TreeItemEx) item.getParent();
        } else if (item.getItemType() == DEFAULTLIST) {
            retval = item;
        }
        return retval;
    }

    protected String getListPropertyNameFor(TreeItemEx item) {
        String retval = null;
        if (item.getItemType() == LIST) {
            retval = item.getPropertyName();
        } else if (item.getItemType() == DEFAULTLIST) {
            NodeDescriptor nd = NodeDescriptorRegistry.getInstance().getDescriptor(item.getValue().getClass());

            NodeProperty p = nd.getDefaultListProperty();
            if (p != null) {
                retval = p.getName();
            }
        }
        return retval;
    }

    /**
     * Called if the the property {@code itemType} of the given item is equal to
     * {@code ItemType.MIXED}.
     *
     * @param item the item whose property is searched
     * @param dragObject the dragged object
     * @return the property name or null.
     */
    protected String getDefaultPropertyNameFor(TreeItemEx item, Object dragObject) {
        String retval = null;
        NodeDescriptor nd = NodeDescriptorRegistry.getInstance().getDescriptor(item.getValue().getClass());
        for (NodeProperty p : nd.getProperties()) {
            if (!p.isDefault()) {
                continue;
            }
            String name = p.getName();

            if (p instanceof NodeList) {
                Class clazz = ReflectHelper.getListGenericType(item.getValue().getClass(), name);
                if (clazz != null && clazz.isAssignableFrom(dragObject.getClass())) {
                    retval = name;
                    break;
                }
            } else if (p instanceof NodeContent) {
                BeanAdapter adapter = new BeanAdapter(item.getValue());
                if (adapter.getType(p.getName()).isAssignableFrom(dragObject.getClass())) {
                    retval = name;
                    break;
                }
            }
        }//for
        return retval;
    }

    protected void removeByItemValue(TreeViewEx treeView, Object value) {
        TreeItemEx item = SceneViewUtil.findTreeItemByObject(treeView, value);
        if (item != null) {
            updateOnMove(item);
        }
    }

    public void updateOnMove(TreeItemEx child) {
        TreeItemEx parent = (TreeItemEx) child.getParent();
        if (parent == null) {
            //
            // child is a root TreeItem
            //
            return;
        }
        if (null == parent.getItemType()) {
            BeanAdapter ba = new BeanAdapter(parent.getValue());
            ba.put(child.getPropertyName(), null);
        } else {
            switch (parent.getItemType()) {
                case LIST:
                    ((ObservableList) parent.getValue()).remove(child.getValue());
                    break;
                case DEFAULTLIST: {
                    NodeDescriptor nd = NodeDescriptorRegistry.getInstance().getDescriptor(parent.getValue().getClass());
                    BeanAdapter ba = new BeanAdapter(parent.getValue());
                    ((ObservableList) ba.get(nd.getDefaultListProperty().getName())).remove(child.getValue());
                    break;
                }
                default: {
                    BeanAdapter ba = new BeanAdapter(parent.getValue());
                    ba.put(child.getPropertyName(), null);
                    break;
                }
            }
        }
    }

    public static void updateOnMove(TreeCell cell) {
        TreeItemEx child = (TreeItemEx) cell.getTreeItem();
        TreeItemEx parent = (TreeItemEx) child.getParent();
        if (parent == null) {
            //
            // child is a root TreeItem
            //
            return;
        }
        if (null == parent.getItemType()) {
            BeanAdapter ba = new BeanAdapter(parent.getValue());
            ba.put(child.getPropertyName(), null);
        } else {
            switch (parent.getItemType()) {
                case LIST:
                    ((ObservableList) parent.getValue()).remove(child.getValue());
                    break;
                case DEFAULTLIST: {
                    NodeDescriptor nd = NodeDescriptorRegistry.getInstance().getDescriptor(parent.getValue().getClass());
                    BeanAdapter ba = new BeanAdapter(parent.getValue());
                    ((ObservableList) ba.get(nd.getDefaultListProperty().getName())).remove(child.getValue());
                    break;
                }
                default: {
                    BeanAdapter ba = new BeanAdapter(parent.getValue());
                    ba.put(child.getPropertyName(), null);
                    break;
                }
            }
        }
    }

    public void accept(TreeViewEx treeView, TreeItemEx target, TreeItemEx place, Object value) {

        //
        // A position where a new TreeItem should be inserted before uptateOnMove 
        // method call. We must consider for the list that the insertion 
        // position can change, since the method updateOnMove deletes the item which
        // corresponds to the dragged value
        //
        if (target == place && place.getDragDropQualifier() == LAST && target.getValue() != null && target.getItemType() == MIXED) {
            accept(treeView, target, value);
            return;
        }
        int insertIndex = getInsertIndex(treeView, target, place);

        if (target != null && (target.getItemType() == LIST || target.getItemType() == DEFAULTLIST)) {
            TreeItemEx it = SceneViewUtil.findTreeItemByObject(treeView, value);
            if (it != null) {
                int idx = target.getChildren().indexOf(it);
                if (idx >= 0 && idx < insertIndex) {
                    insertIndex--;
                }
            }
        }

        update(treeView, target, insertIndex, value);

    }

    /**
     * Executes when the target item equals to place and the itemType property
     * value of the target item equals to ItemType.MIXED.
     *
     * @param treeView an instance of TreeViewEx
     * @param target the target item with itemType ItemType.MIXED
     * @param value the dragged value
     */
    public void accept(TreeViewEx treeView, TreeItemEx target, Object value) {
        //TreeItemEx defaultItem = 
        NodeDescriptor nd = NodeDescriptor.get(target.getValue().getClass());

        if (nd == null) {
            return;
        }
        NodeProperty defaultProperty = null;
        String propName = getDefaultPropertyNameFor(target, value);
        if (propName == null) {
            return;
        }
        for (NodeProperty p : nd.getProperties()) {
            if (propName.equals(p.getName())) {
                defaultProperty = p;
                break;
            }
        }
        TreeItemEx item = (TreeItemEx) target.getChildren().get(nd.getProperties().indexOf(defaultProperty));

        int insertIndex = item.getChildren().size();
        if (item != null && (item.getItemType() == LIST || item.getItemType() == DEFAULTLIST)) {
            TreeItemEx it = SceneViewUtil.findTreeItemByObject(treeView, value);
            if (it != null) {
                int idx = item.getChildren().indexOf(it);
                if (idx >= 0 && idx < insertIndex) {
                    insertIndex--;
                }
            }
        }

        update(treeView, item, insertIndex, value);

    }

    protected void update(TreeViewEx treeView, TreeItemEx target, int insertIndex, Object sourceObject) {

        switch (target.getItemType()) {
            case LIST:
                ((ObservableList) target.getValue()).add(insertIndex, sourceObject);
                break;
            case DEFAULTLIST:
                updateList(treeView, target, insertIndex, sourceObject);
                break;
            default:
                NodeDescriptor nd;
                if (target.getValue() == null) {
                    BeanAdapter ba = new BeanAdapter(target.getParent().getValue());
                    ba.put(target.getPropertyName(), sourceObject);
                } else {
                    nd = NodeDescriptorRegistry.getInstance().getDescriptor(target.getValue().getClass());
                    BeanAdapter ba = new BeanAdapter(target.getValue());
                    ba.put(nd.getDefaultContentProperty().getName(), sourceObject);
                }
                break;
        }
        Selection sel = DockRegistry.lookup(Selection.class);
        Platform.runLater(() -> {
            sel.setSelected(sourceObject);
        });
    }

    protected void updateList(TreeViewEx treeView, TreeItemEx target, int placeIndex, Object sourceObject) {
        NodeDescriptor nd = NodeDescriptorRegistry.getInstance().getDescriptor(target.getValue().getClass());
        BeanAdapter ba = new BeanAdapter(target.getValue());
        ObservableList ol = (ObservableList) ba.get(nd.getProperties().get(0).getName());
        ol.add(placeIndex, sourceObject);
    }

    /**
     * Tries to calculate an index in the children collection of the item
     * specified by the parameter {@code layoutNode } where a new item can be
     * inserted.
     *
     * @param treeView the node to search in
     * @param target the layoutNode TreeItem where the new TreeItem should be
     * place as a children
     * @param place the object of type TreeItem which represents a drag
     * layoutNode TreeCell
     *
     * @return an index in the collection of children in the layoutNode TreeItem
     * used to insert a new TreeItem
     */
    protected int getInsertIndex(TreeViewEx treeView, TreeItemEx target, TreeItemEx place) {

        int idx = -1;

        if (target == place) {
            int q = place.getDragDropQualifier();

            if (q == FIRST) {
                idx = 0;
            } else {
                idx = target.getChildren().size();
            }
        } else {
            int targetLevel = treeView.getTreeItemLevel(target);
            int placeLevel = treeView.getTreeItemLevel(place);
            TreeItemEx parent = place;
            if (targetLevel - placeLevel != 1) {
                //
                // Occurs when place is the last TreeItem of it's parent
                //
                while (treeView.getTreeItemLevel(parent) - targetLevel > 1) {
                    parent = (TreeItemEx) parent.getParent();
                }
            }
            idx = target.getChildren().indexOf(parent) + 1;
        }
        return idx;
    }

    protected int getInsertIndex(TreeViewEx treeView, TreeItemEx target, Object value) {
        int idx = target.getChildren().size();

        return idx;
    }

}

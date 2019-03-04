/*
 * Copyright 2017 Your Organisation.
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
package org.vns.javafx.dock.api.dragging.view;

import org.vns.javafx.dock.api.resizer.PopupControlResizer;
import org.vns.javafx.dock.api.resizer.Resizer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.PopupControl;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.PopupWindow;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.vns.javafx.dock.DockNode;
import org.vns.javafx.dock.DockPane;
//import org.vns.javafx.dock.DockPane;
import org.vns.javafx.dock.api.Util;
import org.vns.javafx.dock.api.DockRegistry;
import org.vns.javafx.dock.api.Dockable;
import org.vns.javafx.dock.api.DockableContext;
import org.vns.javafx.dock.api.DragContainer;
import org.vns.javafx.dock.api.LayoutContext;
import org.vns.javafx.dock.api.Selection;

/**
 *
 * @author Valery
 */
public class FloatPopupControlView implements FloatWindowView {

    private StageStyle stageStyle = StageStyle.TRANSPARENT;

    private final ObjectProperty<Window> floatingWindow = new SimpleObjectProperty<>();

    private final ObjectProperty value = new SimpleObjectProperty();

    protected Pane windowRoot;

    private final DockableContext dockableContext;

    private Resizer resizer;

    private final MouseResizeHandler mouseResizeHanler;

    private final BooleanProperty floating = createFloatingProperty();

    private Cursor[] supportedCursors = new Cursor[]{
        Cursor.S_RESIZE, Cursor.E_RESIZE, Cursor.N_RESIZE, Cursor.W_RESIZE,
        Cursor.SE_RESIZE, Cursor.NE_RESIZE, Cursor.SW_RESIZE, Cursor.NW_RESIZE
    };

    public FloatPopupControlView(Dockable dockable) {
        this.dockableContext = dockable.getContext();
        mouseResizeHanler = new MouseResizeHandler(this);
    }

    @Override
    public void initialize() {
    }
    
    
    public MouseResizeHandler getMouseResizeHanler() {
        return mouseResizeHanler;
    }

    public StageStyle getStageStyle() {
        return stageStyle;
    }

    @Override
    public Pane getWindowRoot() {
        return windowRoot;
    }

    protected void setWindowRoot(Pane windowRoot) {
        this.windowRoot = windowRoot;
    }

    public DockableContext getDockableContext() {
        return dockableContext;
    }

    @Override
    public Resizer getResizer() {
        return resizer;
    }

    public void setStageStyle(StageStyle stageStyle) {
        this.stageStyle = stageStyle;
    }

    @Override
    public Cursor[] getSupportedCursors() {
        return supportedCursors;
    }

    @Override
    public void setSupportedCursors(Cursor[] supportedCursors) {
        this.supportedCursors = supportedCursors;
    }

    @Override
    public ObjectProperty<Window> floatingWindowProperty() {
        return floatingWindow;
    }

    @Override
    public Window getFloatingWindow() {
        return floatingWindow.get();
    }

    protected void setFloatingWindow(Window window) {
        floatingWindow.set(window);
    }

    protected void markFloating(Window toMark) {
        toMark.getScene().getRoot().getStyleClass().add(FLOAT_WINDOW);
        floatingWindow.set(toMark);
    }

    @Override
    public Dockable getDockable() {
        return dockableContext.getDockable();
    }

    public final boolean isDecorated() {
        return stageStyle != StageStyle.TRANSPARENT && stageStyle != StageStyle.UNDECORATED;
    }

    @Override
    public Window make(Dockable dockable, boolean show) {

        DragContainer dc = dockable.getContext().getDragContainer();
        Object v;
        if (dc != null) {
            v = dc.getValue();
            if (v != null && ((!dc.isValueDockable() || dc.isDragAsObject()))) {
                return make(dockable, v, show);
            } else if (dc.isValueDockable()) {
                return make(dockable, Dockable.of(v), show);
            }
        }
        System.err.println("FLOATING WIN MAKE !!");
        setSupportedCursors(DEFAULT_CURSORS);

        Node node = dockable.getNode();
        //
        // Removes selected and then Removes all MMOUSE_CLICKED event handlers 
        // and filters of type SeectionListener
        //
        Selection.removeListeners(node);

        Window owner;
        if ((node.getScene() == null || node.getScene().getWindow() == null)) {
            return null;
        } else {
            owner = node.getScene().getWindow();
        }

        double nodeWidth = node.getBoundsInParent().getWidth();
        double nodeHeight = node.getBoundsInParent().getHeight();

        Point2D windowPos = node.localToScreen(node.parentToLocal(0, 0));

        if (windowPos == null) {
            windowPos = new Point2D(400, 400);
        }
        Node titleBar = dockable.getContext().getTitleBar();
        if (titleBar != null) {
            titleBar.setVisible(true);
            titleBar.setManaged(true);
        }

        if (isDocked(dockable) && getLayoutContext(dockable).getLayoutNode() != null) {
            Window targetNodeWindow = Util.getOwnerWindow(getLayoutContext(dockable).getLayoutNode());
            if (Util.getOwnerWindow(dockable.getNode()) != targetNodeWindow) {
                windowRoot = (Pane) dockable.getNode().getScene().getRoot();
                markFloating(dockable.getNode().getScene().getWindow());
                setSupportedCursors(DEFAULT_CURSORS);
                Window retval = dockable.getNode().getScene().getWindow();
                getLayoutContext(dockable).undock(dockable);
                return retval;
            }
        }
        System.err.println("FLOATING WIN MAKE 1");
        boolean saveSize = false;

        if (isDocked(dockable)) {
            if ((dockable.getNode() instanceof DockNode) && (getLayoutContext(dockable).getLayoutNode() instanceof DockPane)) {
                saveSize = true;
            }
            LayoutContext oldLayoutContext = getLayoutContext(dockable);
            //03.04getLayoutContext(dockable).undock(dockable.node());
            getLayoutContext(dockable).undock(dockable);
            //LayoutContext tc = dockable.getContext().getLayoutContext();
            // if (tc instanceof ScenePaneContext) {
            //     ((ScenePaneContext) tc).setRestoreContext(oldLayoutContext);
            // }
        }

//        if (dockable.getContext().isDocked()) {
//            getLayoutContext(dockable).undock(dockable.node());
//        }
        final PopupControl window = new PopupControl();
        window.getScene().setFill(Color.rgb(240, 240, 240,1));

        window.setAnchorLocation(PopupWindow.AnchorLocation.WINDOW_TOP_LEFT);
        window.setAutoFix(false);


        windowRoot = new RootPane(dockable.getNode());
        windowRoot.getStyleClass().add(FLOAT_WINDOW_ROOT);   
        windowRoot.setId("FLOAT-VIEW");
        
        //windowRoot.getStyleClass().add(FLOAT_WINDOW);
        windowRoot.getStyleClass().add(FLOATVIEW);
        /*windowRoot.setStyle("-fx-border-width: 1; -fx-border-color: red");*/


        //windowRoot.getStyleClass().add("float-window-root");
        window.getScene().setRoot(windowRoot);

        window.getScene().setCursor(Cursor.HAND);
        markFloating(window);

        node.applyCss();
        windowRoot.applyCss();
        if (saveSize) {
            Bounds bounds = new BoundingBox(windowPos.getX(), windowPos.getY(), nodeWidth, nodeHeight);
            FloatView.layout(window, bounds);
        }

        window.setOnShown(e -> {
//            DockRegistry.register(window);
        });

        window.setOnHidden(e -> {
//            DockRegistry.unregister(window);
        });

        if (show) {
            window.show(owner);
        }
        addResizer();
        
        ChangeListener<Parent> pcl = new ChangeListener<Parent>() {
            @Override
            public void changed(ObservableValue<? extends Parent> observable, Parent oldValue, Parent newValue) {
                if (window != null) {
                    window.hide();
                }
                dockable.getNode().parentProperty().removeListener(this);
            }
        };

        dockable.getNode().parentProperty().addListener(pcl);

        return window;
    }

    /**
     * Makes a window when the dragContainer is not null and is not dockable
     *
     * @param dockable the object for which the window is to be created
     * @param dragged the dragged object
     * @param show true if the created window must be shown
     * @return the created window
     */
    protected Window make(Dockable dockable, Object dragged, boolean show) {
        setSupportedCursors(DEFAULT_CURSORS);

        DockableContext context = dockable.getContext();
        Node node = context.getDockable().getNode();
        Window owner = null;
        if ((node.getScene() == null || node.getScene().getWindow() == null)) {
            return null;
        } else {
            owner = node.getScene().getWindow();
        }
        //double nodeWidth = ((Region) node).getWidth();
        //double nodeHeight = ((Region) node).getHeight();

        Point2D windowPos = node.localToScreen(0, 0);

        if (windowPos == null) {
            windowPos = new Point2D(400, 400);
        }
        getLayoutContext(dockable).undock(dockable);
        PopupControl window = new PopupControl();
        window.getScene().setFill(Color.rgb(240, 240, 240,1));

       

        node = context.getDragContainer().getPlaceholder();
        windowRoot = createRoot(node);
        windowRoot.setId("FLOAT-VIEW-1");
        windowRoot.getStyleClass().add(FLOAT_WINDOW_ROOT);
        //windowRoot.getStyleClass().add(FLOAT_WINDOW);
        windowRoot.getStyleClass().add(FLOATVIEW);        
        //windowRoot.getChildren().add(node);

        window.getScene().setRoot(windowRoot);
        window.getScene().setCursor(Cursor.HAND);

        markFloating(window);

        //windowRoot.setStyle("-fx-background-color: transparent");

        addResizer();

        window.getStyleClass().clear();
        window.setOnShown(e -> {
//            DockRegistry.register(window);
        });
        window.setOnHidden(e -> {
//            DockRegistry.unregister(window);
        });
        if (show) {
            window.show(owner);
        }
        return window;

    }

    /**
     * Makes a window when the dragContainer is not null and is dragged
     *
     * @param dockable the object for which the window is to be created
     * @param dragged the dragged object
     * @param show true if the created window must be shown
     * @return the created window
     */
    protected Window make(Dockable dockable, Dockable dragged, boolean show) {

        setSupportedCursors(DEFAULT_CURSORS);
        Node node = dockable.getNode();
        Window owner = null;
        if ((node.getScene() == null || node.getScene().getWindow() == null)) {
            return null;
        } else {
            owner = node.getScene().getWindow();
        }

/*        double nodeWidth;// = node.getBoundsInLocal().getWidth();
        double nodeHeight;// = node.getBoundsInLocal().getHeight();
        if (node instanceof Region) {
            nodeWidth = ((Region) node).getWidth();
            nodeHeight = ((Region) node).getHeight();
        }
*/
        Point2D windowPos = node.localToScreen(0, 0);

        if (windowPos == null) {
            windowPos = new Point2D(400, 400);
        }

        Node titleBar = dragged.getContext().getTitleBar();
        if (titleBar != null) {
            titleBar.setVisible(true);
            titleBar.setManaged(true);
        }

        DockableContext draggedContext = dragged.getContext();

        if (isDocked(dragged) && getLayoutContext(dragged).getLayoutNode() != null) {
            Window targetNodeWindow = Util.getOwnerWindow(getLayoutContext(dragged).getLayoutNode());
            if (Util.getOwnerWindow(dragged.getNode()) != targetNodeWindow) {
                windowRoot = (Pane) dragged.getNode().getScene().getRoot();
                markFloating(dragged.getNode().getScene().getWindow());
                setSupportedCursors(DEFAULT_CURSORS);
                getLayoutContext(dockable).undock(dockable);
                return dragged.getNode().getScene().getWindow();
            }
        }
        if (isDocked(dragged)) {
            getLayoutContext(dockable).undock(dockable);
            LayoutContext tc = dockable.getContext().getLayoutContext();
        }

        PopupControl window = new PopupControl();
        window.setAnchorLocation(PopupWindow.AnchorLocation.WINDOW_TOP_LEFT);
        window.getScene().setFill(Color.rgb(240, 240, 240,1));
        
        node = dragged.getNode();
        windowRoot = createRoot(node);
        windowRoot.setId("FLOAT-VIEW-2");
        windowRoot.getStyleClass().add(FLOAT_WINDOW_ROOT);
        //windowRoot.getStyleClass().add(FLOAT_WINDOW);
        windowRoot.getStyleClass().add(FLOATVIEW);

        windowRoot.getStyleClass().add("float-window-root");

        window.getScene().setRoot(windowRoot);
        window.getScene().setCursor(Cursor.HAND);
        markFloating(window);

        node.applyCss();
        windowRoot.applyCss();

        window.getStyleClass().clear();
        window.setOnShown(e -> {
//            DockRegistry.register(window);
        });
        window.setOnHidden(e -> {
//            DockRegistry.unregister(window);
        });
        if (show) {
            window.show(owner);
        }

        addResizer();
        ChangeListener<Parent> pcl = new ChangeListener<Parent>() {
            @Override
            public void changed(ObservableValue<? extends Parent> observable, Parent oldValue, Parent newValue) {
                if (window != null) {
                    window.hide();
                }
                dragged.getNode().parentProperty().removeListener(this);
            }
        };

        dragged.getNode().parentProperty().addListener(pcl);
        return window;
    }

    protected LayoutContext getLayoutContext(Dockable d) {
        return d.getContext().getLayoutContext();
    }

    @Override
    public Window make(Dockable dockable) {
        return make(dockable, true);
    }

    @Override
    public void addResizer() {
        if (dockableContext.isResizable()) {
            removeListeners(dockableContext.getDockable());
            addListeners(getFloatingWindow());
        } else {
            removeListeners(dockableContext.getDockable());
        }

        setResizer(new PopupControlResizer(this));

    }

    protected void setResizer(Resizer resizer) {
        this.resizer = resizer;
    }

    public ObjectProperty valueProperty() {
        return value;
    }

    @Override
    public Object getValue() {
        return value.get();
    }

    public void setValue(Object obj) {
        this.value.set(obj);
    }

    protected void addListeners(Window window) {
        window.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseResizeHanler);
        window.addEventFilter(MouseEvent.MOUSE_MOVED, mouseResizeHanler);
        window.addEventFilter(MouseEvent.MOUSE_DRAGGED, mouseResizeHanler);
    }

    public void removeListeners(Dockable dockable) {
        dockable.getNode().getScene().getWindow().removeEventFilter(MouseEvent.MOUSE_PRESSED, mouseResizeHanler);
        dockable.getNode().getScene().getWindow().removeEventHandler(MouseEvent.MOUSE_PRESSED, mouseResizeHanler);
        dockable.getNode().getScene().getWindow().removeEventFilter(MouseEvent.MOUSE_MOVED, mouseResizeHanler);
        dockable.getNode().getScene().getWindow().removeEventHandler(MouseEvent.MOUSE_MOVED, mouseResizeHanler);

        dockable.getNode().getScene().getWindow().removeEventHandler(MouseEvent.MOUSE_DRAGGED, mouseResizeHanler);
        dockable.getNode().getScene().getWindow().removeEventFilter(MouseEvent.MOUSE_DRAGGED, mouseResizeHanler);

    }

}//class FloatWindowBuilder

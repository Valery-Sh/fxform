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
package org.vns.javafx.dock.api;

import java.util.Set;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import org.vns.javafx.dock.api.dragging.DefaultMouseDragHandler;

/**
 *
 * @author Valery Shyshkin
 */
public class DockTabPane2MouseDragHandler extends DefaultMouseDragHandler {

    public DockTabPane2MouseDragHandler(DockableContext context) {
        super(context);
    }

    @Override
    public void mousePressed(MouseEvent ev) {
        //System.err.println("DockTabPane2MouseDragHandler mousePressed");

        setStartMousePos(null);
        Point2D pos = new Point2D(ev.getX(), ev.getY());
        Node headerArea = getHeaderArea(ev);
        if (!ev.isPrimaryButtonDown() || headerArea == null) {
            return;
        }

        if (getHeadersRegion(ev) != null) {
//            System.err.println("DockTabPane2MouseDragHandler: headersRegion != null");
            Tab tab = getTab(ev);
            Node tabNode = tab.getTabPane().lookup("." + getUUIDStyle(tab));

            getContext().setDragContainer(new DragContainer(DragContainer.placeholderOf(tab), tab));
            WritableImage wi = null;

            if (tabNode != null) {
                wi = tabNode.snapshot(null, null);
                if (wi != null) {
                    Node node = new ImageView(wi);
                    node.setOpacity(0.75);
                    getContext().getDragContainer().setPlaceholder(node);
                }

            }
            pos = tabNode.screenToLocal(ev.getScreenX(), ev.getScreenY());
            setStartMousePos(pos);
        }
        if ( getStartMousePos() == null && headerArea != null ) {
            setStartMousePos(new Point2D(ev.getX(), ev.getY()));
        }
//        System.err.println("DockTabPane2MouseDragHandler: startMousePos=" + getStartMousePos());
    }

    @Override
    protected void prepare() {
        DragContainer dc = getContext().getDragContainer();
        if (dc != null && dc.getPlaceholder() != null && dc.getValue() != null && dc.getValue() instanceof Tab) {
            Tab tab = (Tab) dc.getValue();
            if (tab.getTabPane() != null) {
                tab.getTabPane().getTabs().remove(tab);
            }
        }
    }

    private String getUUIDStyle(Tab tab) {
        String retval = null;
        for (String s : tab.getStyleClass()) {
            if (s.startsWith("tab-uuid-")) {
                retval = s;
                break;
            }
        }
        return retval;
    }

    protected Node getHeaderArea(MouseEvent ev) {
        Node retval = getContext().getDragNode().lookup(".tab-header-area");
        if (retval == null || !Util.contains(retval, ev.getScreenX(), ev.getScreenY())) {
            retval = null;
        }
        return retval;
    }

    protected Node getHeadersRegion(MouseEvent ev) {

        Node retval = getContext().getDragNode().lookup(".headers-region");
        if (retval == null || !Util.contains(retval, ev.getScreenX(), ev.getScreenY())) {
            retval = null;
        }
        return retval;
    }

    protected Tab getTab(MouseEvent ev) {
        Tab retval = null;
        Set<Node> set = getContext().getDragNode().lookupAll(".tab");
        Node tabNode = null;
        for (Node node : set) {
            if (Util.contains(node, ev.getScreenX(), ev.getScreenY())) {
                tabNode = node;
                break;
            }
        }
        String style = null;
        if (tabNode != null) {
            for (String s : tabNode.getStyleClass()) {
                if (s.startsWith("tab-uuid-")) {
                    style = s;
                    break;
                }
            }
            TabPane pane = (TabPane) getContext().getDragNode();

            if (style != null) {
                for (Tab tab : pane.getTabs()) {
                    for (String s : tab.getStyleClass()) {
                        if (s.equals(style)) {
                            retval = tab;
                            break;
                        }
                    }
                }
            }
        }
        return retval;
    }
  
}

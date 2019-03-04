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
package org.vns.javafx.dock.api.dragging;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import org.vns.javafx.dock.DockNode;
import org.vns.javafx.dock.api.DockableContext;
import org.vns.javafx.dock.api.DragContainer;

/**
 *
 * @author Valery Shyshkin
 */
public abstract class MouseDragHandler implements EventHandler<MouseEvent> {

    private final DockableContext context;
    private Point2D startMousePos;
    private DragManager dragManager;
    
    protected MouseDragHandler(DockableContext context) {
        this.context = context;
    }

    public abstract void mouseDragDetected(MouseEvent ev);
    
    protected void prepare() {
        
    }
    public abstract void mousePressed(MouseEvent ev); 
    
    public void mouseReleased(MouseEvent ev) {
        startMousePos = null;
        ev.consume();
        DragContainer dc = getContext().getDragContainer();
        if (dc != null && dc.getPlaceholder() != null ) {
            getContext().setDragContainer(null);
        }
    }

    @Override
    public void handle(MouseEvent ev ) {
        if (ev.getEventType() == MouseEvent.MOUSE_PRESSED) {
            mousePressed(ev);
        } else if (ev.getEventType() == MouseEvent.DRAG_DETECTED) {
            mouseDragDetected(ev);
        } else if (ev.getEventType() == MouseEvent.MOUSE_RELEASED) {
            mouseReleased(ev);
        }
        ev.consume();
    }
    /**
     * May return null. If so then Mouse.DRAGDETECTED even is not handled
     * @return  mouse cursor position
     */
    public Point2D getStartMousePos() {
        return startMousePos;
    }

    public void setStartMousePos(Point2D startMousePos) {
        this.startMousePos = convertMousePos(startMousePos);
    }
    protected Point2D convertMousePos(Point2D startMousePos) {
        if ( startMousePos == null ) {
            return null;
        }
        if ( getContext().getDockable().getNode() instanceof DockNode) {
            //return startMousePos;
        }
        return new Point2D(0,0);
    }
    public DockableContext getContext() {
        return context;
    }


    public DragManager createDragManager(Event ev) {
        dragManager = getContext().getDragManager();
        return dragManager;
    }

    public DragManager getDragManager() {
        return dragManager;
    }
    
}

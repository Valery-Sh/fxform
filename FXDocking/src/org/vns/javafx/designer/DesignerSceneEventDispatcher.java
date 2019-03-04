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

import java.util.function.BiPredicate;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;
import org.vns.javafx.dock.api.Util;
import org.vns.javafx.dock.api.DockRegistry;
import org.vns.javafx.dock.api.Dockable;
import org.vns.javafx.dock.api.Selection;
import org.vns.javafx.dock.api.dragging.DragManager;

public class DesignerSceneEventDispatcher implements PalettePane.PaletteEventDispatcher {

    private EventDispatcher initial;
    private Node target;
    private Node node;
    private BiPredicate<Event, Node> preventCondition;
    private Scene scene;
    private Window window;

    boolean pressed;
    boolean dragDetected;
    boolean dragged;
    boolean released;

    public DesignerSceneEventDispatcher() {
        this(null);
    }

    public DesignerSceneEventDispatcher(BiPredicate<Event, Node> cond) {
        this.scene = scene;
        preventCondition = cond;
        init();
    }

    private void init() {
        released = true;
    }

    @Override
    public void start(Node node) {
        this.target = node;
        initial = node.getEventDispatcher();
        node.setEventDispatcher(this);
    }

    public void start(Scene scene) {
        this.scene = scene;
        initial = scene.getEventDispatcher();
        scene.setEventDispatcher(this);
    }

    @Override
    public BiPredicate<Event, Node> getPreventCondition() {
        return preventCondition;
    }

    @Override
    public void setPreventCondition(BiPredicate<Event, Node> preventCondition) {
        this.preventCondition = preventCondition;
    }

    @Override
    public Event dispatchEvent(Event event, EventDispatchChain tail) {
        //System.err.println("event type = " + event.getEventType());
        //if ( scene != null && (scene.getRoot() instanceof VBox) ) {
         //   System.err.println("Event = " + event.getEventType() + "; " + scene.getRoot());
        //}
        if ( event instanceof ContextMenuEvent ) {
            return initial.dispatchEvent(event, tail);
        }
        
        if ( !(event instanceof MouseEvent)) {
            return null;
        }

        MouseEvent mouseEvent = (MouseEvent) event;

        if (mouseEvent.getEventType() == MouseEvent.MOUSE_ENTERED || mouseEvent.getEventType() == MouseEvent.MOUSE_ENTERED_TARGET) {
            return null;
        }
        if (mouseEvent.getEventType() == MouseEvent.MOUSE_EXITED || mouseEvent.getEventType() == MouseEvent.MOUSE_EXITED_TARGET) {

        }

        //
        // not primary button
        //
        if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {
              //System.err.println("eventTarget = " + event.getTarget());
  //          System.err.println("MOUSE PRESSED " + mouseEvent.isPrimaryButtonDown());            
        }
        
        //if ( mouseEvent.isPrimaryButtonDown() && (Util.isFrameShape(event.getTarget()) || Util.isFrameLine(event.getTarget()) || Util.isForeign(event.getTarget())) ) {
//        boolean foreign = Util.isForeign(event.getTarget());
        boolean foreign = ( Util.isFrameShape(event.getTarget()) || Util.isFrameLine(event.getTarget()) || Util.isForeign(event.getTarget()));        
        //System.err.println("mouseEvent.isPrimaryButtonDown() = " + mouseEvent.isPrimaryButtonDown());
        if ( foreign ) {        
//            System.err.println("IS FOREIGN eventtype = " + mouseEvent.getEventType());         
//            System.err.println("  -- mouseEvent.getSource = " + mouseEvent.getSource());
//            System.err.println("  -- mouseEvent.getTarget = " + mouseEvent.getTarget());
            return initial.dispatchEvent(event, tail);
        }
        
        if (!(mouseEvent.getTarget() instanceof Node)) {
            return null;
        }
        //System.err.println("event.pickResult = " + mouseEvent.getPickResult());
        if (mouseEvent.getEventType() != MouseEvent.MOUSE_RELEASED && !mouseEvent.isPrimaryButtonDown()) {
            if (mouseEvent.getEventType() != MouseEvent.MOUSE_MOVED) {
                //System.err.println("INITIAL !!!!!!!!!!!!!!!!!!!!!!! " + mouseEvent.getEventType());
            }
            //return initial.dispatchEvent(event, tail);
//            if ( event.getTarget() instanceof Node )
//                System.err.println("eventTarget = " + event.getTarget());
                //return event;
        }
        if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {
              //System.err.println("eventTarget = " + event.getTarget());
//            System.err.println("MOUSE PRESSED " + mouseEvent.isPrimaryButtonDown());            
        }
        
//        if ( SceneView.isFrameShape(event.getTarget()) ) {
//            return initial.dispatchEvent(event, tail);
//        }
        //System.err.println("2 Scene dispatch ev " + mouseEvent.getEventType());
        if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {
            return pressed(event, tail);
        } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED) {
            return released(event, tail);
        } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
            return mouseClicked(event, tail);
        } else if (mouseEvent.getEventType() == MouseEvent.DRAG_DETECTED) {
            return dragDetected(event, tail);
        } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_DRAGGED) {
            return dragged(event, tail);
        } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_EXITED || mouseEvent.getEventType() == MouseEvent.MOUSE_EXITED_TARGET) {
            return mouseEnteredOrExited(event, tail);
        } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_MOVED) {
            return event;
        } else {
            return null;
        }
        //return initial.dispatchEvent(event, tail);
    }

    protected boolean acceptable(Event event) {
        boolean retval = false;
        // if ((event instanceof MouseEvent) && event.getSource() == node) {
        //     retval = true;
        // }
        Node parent = null;
        if (event.getTarget() == target) {
            parent = target;
        } else if (Dockable.of(event.getTarget()) == null) {
            parent = getDockableParent((Node) event.getTarget());
        }
        if (parent == target) {
            retval = true;
        }
        return retval;
    }

    public Node getDockableParent(Node startNode) {
        //System.err.println("getDockableParent tar = " + startNode);
        if (Dockable.of(startNode) != null) {
            return startNode;
        }
        Parent p = startNode.getParent();
        //System.err.println("getDockableParent p = " + p);
        while (p != null && p != this.target && Dockable.of(p) == null) {
            p = p.getParent();
        }
        //System.err.println("getDockableParent retval = " + p);
        return p;
    }

    protected Event mouseClicked(Event event, EventDispatchChain tail) {
        //System.err.println("DISPATCHER MOUSE MOVED");
        //event.consume();
        return null;
        //event.consume();
        //return null;
    }

    protected Event pressed(Event event, EventDispatchChain tail) {
        //System.err.println("pickTop node = " + TopNodeHelper.pickTop(scene.getWindow(), ((MouseEvent)event).getScreenX(), ((MouseEvent)event).getScreenY(), n -> {return true;}));
//        System.err.println("SCENE PRESSED TARGET = " + target);
//        System.err.println("Scene pressed ev.target = " + event.getTarget());
//        System.err.println("SCENE PRESSED isPrimary = " + ((MouseEvent)event).isPrimaryButtonDown());
//        System.err.println("Scene pressed node = " + node);
//        System.err.println("Scene pressed target = " + target);
//        System.err.println("Scene pressed ev.source = " + event.getSource());
//        System.err.println("Scene pressed ev.target = " + event.getTarget());

//        System.err.println("DRAGGED event WINDOW " + ((Node) event.getTarget()).getScene().getWindow());
//        if (target != null) {
//            System.err.println("DRAGGED  WINDOW " + target.getScene().getWindow());
//        }
//        System.err.println("1. pressed target = " + target);
//        System.err.println("pressed event.target = " + event.getTarget());
        if (target != null) {
            return null;
        }

        target = (Node) event.getTarget();
        node = getDockableParent(target);
//        System.err.println("pressed target " + target + "; dockable node = " + node);
//        System.err.println("PRESSED node = " + node);
        Selection.MouseSelectionListener l = DockRegistry.lookup(Selection.MouseSelectionListener.class);
        if (l != null) { //&& (event.getTarget() instanceof Node)) {
//            System.err.println("PRESSED selectionListener = " + l);
            //l.handle((MouseEvent) event, (Node) event.getTarget());
            MouseEvent copy = (MouseEvent) event.copyFor(node, node);
//            System.err.println("pressed Selection.SelectionListener " + Selection.MouseSelectionListener.class);
            l.handle(copy, node);
            //l.mousePressed(copy);
        }
        if (Dockable.of(node) != null) {

//            System.err.println("SceneDispatcher mousePressed node " + node);
//            System.err.println("SceneDispatcher mousePressed source " + event.getSource());
//        System.err.println("DefaultDragHandler mousePressed target " + ev.getTarget());
//            System.err.println("SceneDispatcher mousePressed x = " + ((MouseEvent)event).getX() + "; y = " + ((MouseEvent)event).getY());
            MouseEvent copy = (MouseEvent) event.copyFor(node, node);
            Dockable.of(node).getContext().getDragDetector().handle(copy);
//             System.err.println("SceneDispatcher copy mousePressed x = " + copy.getX() + "; y = " + copy.getY());
            double x = node.getBoundsInParent().getMinX();
            double y = node.getBoundsInParent().getMinY();
            //Point2D point = new Point2D( ((MouseEvent)event).getX(), ((MouseEvent)event).getY());
            Point2D point = new Point2D(x, y);
//            System.err.println("SceneDispatcher mousePressed Point2D " + point);
            //Dockable.of(node).getContext().getDragDetector().getDragHandler().setStartMousePos(point);
            //event.consume();

        }
        released = false;
        pressed = true;
//        System.err.println("2. pressed target = " + target);        
        return null;

        //return initial.dispatchEvent(event, tail);
    }

    protected Event released(Event event, EventDispatchChain tail) {
//        System.err.println("dragget Scene RELEASED ");
//        System.err.println("released event.target " + event.getTarget());
//        System.err.println("1. released target " + target);

        if (Dockable.of(node) != null) {
            DragManager dm = Dockable.of(node).getContext().getDragDetector().getDragHandler().getDragManager();
//            System.err.println("dragget Scene released  1 ");
            MouseEvent copy = (MouseEvent) event.copyFor(node, node);
            if (dm != null || (dm instanceof EventHandler)) {
                ((javafx.event.EventHandler) dm).handle(copy);
            }
            Dockable.of(node).getContext().getDragDetector().getDragHandler().handle(copy);
        }
        node = null;
        target = null;
        pressed = false;
        released = true;
        dragged = false;
        dragDetected = false;
//        System.err.println("2. released target " + target);
        
        //SceneView.getResizeFrame().layoutChildren();
                
        return null;

    }

    protected Event clicked(Event event, EventDispatchChain tail) {
        /*        System.err.println("CLICKED");
        System.err.println("Scene CLICKED node = " + node);
        System.err.println("Scene CLICKED target = " + target);
        System.err.println("Scene CLICKED ev.source = " + event.getSource());
        System.err.println("Scene CLICKED ev.target = " + event.getTarget());
         */
        return pressed(event, tail);
    }

    protected Event dragDetected(Event event, EventDispatchChain tail) {
//        System.err.println("dragDetected dispatch target " + event.getTarget());
        /*            System.err.println("dragDetected dispatch node " + node);
            System.err.println("dragDetected dispatch source " + event.getSource());
            System.err.println("dragDetected dispatch target " + event.getTarget());
            System.err.println("dragDetected dispatch isConsumed " + event.isConsumed());
            System.err.println("-----------");
         */
        if (dragDetected) {
            return null;
        }
        if (target != event.getTarget()) {
            return null;
        }

        /*
        System.err.println("Scene dragDetected target = " + target);
        System.err.println("Scene dragDetected ev.source = " + event.getSource());
        System.err.println("Scene dragDetected ev.target = " + event.getTarget());
         */
        if (Dockable.of(node) != null) {
            MouseEvent copy = (MouseEvent) event.copyFor(node, node);
            Dockable.of(node).getContext().getDragDetector().handle(copy);
        }
        dragDetected = true;
        return null;
    }

    protected Event dragged(Event event, EventDispatchChain tail) {
//        System.err.println("Scene dragged ev.target = " + event.getTarget());
//        System.err.println("DRAGGED WINDOW " + target.getScene().getWindow());
//        System.err.println("DRAGGED event WINDOW " + ((Node) event.getTarget()).getScene().getWindow());
        /*        System.err.println("Scene dragged node = " + node);
        System.err.println("Scene dragged target = " + target);
        System.err.println("Scene dragged ev.source = " + event.getSource());
        System.err.println("Scene dragged ev.target = " + event.getTarget());
         */
        if (true) {
//            return initial.dispatchEvent(event, tail);            
        }
        if (target == null || !dragDetected) {
            return null;
        }
//        System.err.println("dragget before redirect dragManager 1");
        if (Dockable.of(node) != null) {
            if (!Dockable.of(node).getContext().isFloating()) {
                return null;
            }
//            System.err.println("dragget before redirect dragManager 2");
            DragManager dm = Dockable.of(node).getContext().getDragDetector().getDragHandler().getDragManager();
//            System.err.println("dragget before redirect dragManager 3 dm = " + dm);
            if (dm instanceof EventHandler) {
//                System.err.println("dragget before redirect dragManager 4");
                MouseEvent copy = (MouseEvent) event.copyFor(node, node);
                //((javafx.event.EventHandler)dm).handle(copy);    
//                System.err.println("dragget redirect dragManager");
                ((javafx.event.EventHandler) dm).handle(copy);
            }

        }
//        System.err.println("Scene dragged source = " + event.getSource());
//        System.err.println("Scene dragged target = " + event.getTarget());

        return null;
    }

    protected Event mouseEnteredOrExited(Event event, EventDispatchChain tail) {
        //System.err.println("**************** mouseEnteredOrExited = " + event.getTarget());

        //node = getDockableParent(target);
        return null;
        //return initial.dispatchEvent(event, tail);
    }

    @Override
    public void finish(Node node) {
        node.setEventDispatcher(initial);
    }

    public void finish(Scene scene) {
        scene.setEventDispatcher(initial);
    }

}

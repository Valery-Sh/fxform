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
package org.vns.javafx.dock.api.selection;

import org.vns.javafx.dock.api.selection.NodeFraming;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Window;

/**
 *
 * @author Valery
 */
public abstract class AbstractNodeFraming implements NodeFraming {

    private final ReadOnlyObjectWrapper<Node> nodeWrapper = new ReadOnlyObjectWrapper<>();

    private final ObservableList<String> styleClass = FXCollections.observableArrayList();
    private final ObjectProperty<String> style = new SimpleObjectProperty<>();
    //
    // NodeFraming Implementation
    //
    private ChangeListener<Node> nodeParentListener;
    private ChangeListener<Scene> nodeSceneListener;
    private ChangeListener<Window> nodeWindowListener;

    @Override
    public final void show(Node node, Object... parms) {
        if (!isAcceptable(node)) {
            return;
        }
        if (getNode() != null) {
            finalizeNode();
            hide();
        }
        setNode(node);
        //
        // Check when null to avoid problem when the method isAcceptable is overidden
        //
        if (node != null) {
            initializeNode();
        }
        initializeOnShow(node);
    }

    public ObservableList<String> getStyleClass() {
        return styleClass;
    }

    public ObjectProperty<String> styleProperty() {
        return style;
    }

    public String getStyle() {
        return style.get();
    }

    public void setStyle(String style) {
        this.style.set(style);
    }

    private void initializeNode() {
        nodeParentListener = (ov, oldValue, newvalue) -> {
            hide();
        };
        nodeSceneListener = (ov, oldValue, newvalue) -> {
            hide();
        };
        nodeWindowListener = (ov, oldValue, newvalue) -> {
            hide();
        };

        getNode().parentProperty().addListener(nodeParentListener);
        getNode().sceneProperty().addListener(nodeSceneListener);
        getNode().getScene().windowProperty().addListener(nodeWindowListener);

    }
    
    @Override
    public void removeListeners() {
        if ( getNode() == null ) {
            return;
        }
        getNode().parentProperty().removeListener(nodeParentListener);
        getNode().sceneProperty().removeListener(nodeSceneListener);
        getNode().getScene().windowProperty().removeListener(nodeWindowListener);

    }
    protected abstract void initializeOnShow(Node node);

    protected abstract void finalizeOnHide(Node node);

    
    private void finalizeNode() {
        getNode().parentProperty().removeListener(nodeParentListener);
        getNode().sceneProperty().removeListener(nodeSceneListener);
        if (getNode().getScene() != null) {
            getNode().getScene().windowProperty().removeListener(nodeWindowListener);
        }

    }

    @Override
    public final void hide() {
        if (getNode() == null) {
            return;
        }
        finalizeNode();
        finalizeOnHide(getNode());

        //setNode(null);        
    }
    @Override
    public void showParent(Node node, Object... parms) {
        
    }
//    @Override
    public boolean isShowing(Node node) {
        return isShowing() && (getNode() == node);
    }
//    @Override

    public boolean isShowing() {
        return getNode() != null;
    }

    //
    //
    //
    public ReadOnlyObjectProperty<Node> nodeProperty() {
        return nodeWrapper.getReadOnlyProperty();
    }

    public Node getNode() {
        return nodeWrapper.getReadOnlyProperty().getValue();
    }

    protected void setNode(Node node) {
        nodeWrapper.setValue(node);
    }

    protected boolean isAcceptable(Node node) {
        boolean retval = true;

        //&&& 14.11 if (node == null || node.getParent() == null || node.getScene() == null || node.getScene().getWindow() == null) {
        if (node == null || node.getScene() == null || node.getScene().getWindow() == null) {        
            retval = false;
        }
        return retval;
    }
}

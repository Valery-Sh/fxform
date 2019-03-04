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

import org.vns.javafx.dock.api.selection.WindowNodeFraming;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.control.PopupControl;
import javafx.stage.Window;

/**
 *
 * @author Valery
 */
public class PopupNodeFraming extends WindowNodeFraming {

    protected PopupNodeFraming() {
        super();
    }
    
    public static WindowNodeFraming getInstance() {
        return SingletonInstance.instance;
    }

    @Override
    protected void createWindow() {
        setWindow(new PopupControl());
    }
    
    @Override
    protected void initScene() {
        getWindow().getScene().setRoot((Parent)getRoot());
    }

    @Override
    protected void doShow(Window owner) {
        ((PopupControl) getWindow()).show(owner);
    }

    @Override
    protected void setWindowSize(Bounds bounds, double borderWidth, double borderHeight) {
    }
    
    private static class SingletonInstance {
        private static final PopupNodeFraming instance = new PopupNodeFraming();
    }
}

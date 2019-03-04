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
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 *
 * @author Valery
 */
public class StageNodeFraming extends WindowNodeFraming {

    protected StageNodeFraming() {
    }
    public static WindowNodeFraming getInstance() {
        return SingletonInstance.instance;
    }

    @Override
    protected void createWindow() {
        Stage stage = new Stage(StageStyle.TRANSPARENT);
        setWindow(stage);
        stage.setAlwaysOnTop(false);
    }
    @Override
   protected void initScene() {
        Scene scene = new Scene((Parent)getRoot());
        scene.setFill(Color.TRANSPARENT);
        ((Stage)getWindow()).setScene(scene);            
    }
    @Override
    protected void setWindowSize(Bounds bounds, double borderWidth, double borderHeight) {
        //System.err.println("bounds.getWidth() = " + bounds.getWidth());
        //getWindow().setWidth(bounds.getWidth() + borderWidth);
        //getWindow().setHeight(bounds.getHeight() + borderHeight);  
        getWindow().sizeToScene();
    }    
    @Override
    protected void doShow(Window owner) {
        ((Stage)getWindow()).initOwner(owner);
        ((Stage)getWindow()).show();
    }
    private static class SingletonInstance {
        private static final StageNodeFraming instance = new StageNodeFraming();
    }
    
}

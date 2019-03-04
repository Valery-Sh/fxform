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
package org.vns.javafx.scene.control.paint.skin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import org.vns.javafx.scene.control.paint.ColorChooserPane;
import org.vns.javafx.scene.control.paint.GradientPane;
import org.vns.javafx.scene.control.paint.StopPane;

/**
 *
 * @author Nastia
 */
public class GradientPaneSkin extends SkinBase<GradientPane> {

        private final GridPane samplePane;
        private final GridPane propertiesPane;

        private final StopPane stopPane;
        private final GradientPane control;
        private final GridPane grid;
        private final Rectangle sampleShape;
        private final ColorChooserPane colorChooserPane;
        
        protected CheckBox proportional;
        protected ComboBox cycleMethodBox;
        
        protected Slider topSlider;
        protected Slider rightSlider;
        protected Slider bottomSlider;
        protected Slider leftSlider;

        public GradientPaneSkin(GradientPane control) {
            super(control);

            this.control = control;
            colorChooserPane = control.getContent();
            grid = colorChooserPane.getContent();
            int row = 5;
            
            stopPane = new StopPane(colorChooserPane);
            
            samplePane = new GridPane();

            propertiesPane = new GridPane();
            propertiesPane.getStyleClass().add("properties-pane");
            
            samplePane.getStyleClass().add("sample-pane");

            sampleShape = new Rectangle(100, 100);
            sampleShape.getStyleClass().addAll("sample-shape", "sample-rect");

//            samplePane.setVgap(5); set in CSS
//            samplePane.setHgap(5); set in CSS
            
            StackPane topPane = new StackPane();
            StackPane rightPane = new StackPane();
            StackPane bottomPane = new StackPane();
            StackPane leftPane = new StackPane();

            samplePane.add(topPane, 1, 0);
            samplePane.add(rightPane, 2, 0, 1, 3);
            samplePane.add(bottomPane, 1, 2);
            samplePane.add(leftPane, 0, 0, 1, 3);

            samplePane.add(sampleShape, 1, 1);
            GridPane.setHgrow(topPane, Priority.NEVER);
            GridPane.setHgrow(rightPane, Priority.ALWAYS);
            GridPane.setVgrow(rightPane, Priority.NEVER);
            GridPane.setHgrow(leftPane, Priority.ALWAYS);
            GridPane.setVgrow(leftPane, Priority.NEVER);
            GridPane.setHgrow(sampleShape, Priority.NEVER);

            topSlider = new Slider();
            topSlider.getStyleClass().add("top-slider");
            rightSlider = new Slider();
            rightSlider.getStyleClass().add("right-slider");
            bottomSlider = new Slider();
            bottomSlider.getStyleClass().add("bottom-slider");            
            leftSlider = new Slider();
            leftSlider.getStyleClass().add("left-slider");
            leftSlider.setOrientation(Orientation.VERTICAL);
            rightSlider.setOrientation(Orientation.VERTICAL);

            topPane.getChildren().add(topSlider);
            rightPane.getChildren().add(rightSlider);
            bottomPane.getChildren().add(bottomSlider);
            leftPane.getChildren().add(leftSlider);

            StackPane.setAlignment(topSlider, Pos.CENTER);
            StackPane.setAlignment(rightSlider, Pos.CENTER_LEFT);
            StackPane.setAlignment(bottomSlider, Pos.CENTER);
            StackPane.setAlignment(leftSlider, Pos.CENTER_RIGHT);

            topSlider.prefWidthProperty().bind(sampleShape.widthProperty());
            rightSlider.prefHeightProperty().bind(sampleShape.heightProperty());
            bottomSlider.prefWidthProperty().bind(sampleShape.widthProperty());
            leftSlider.prefHeightProperty().bind(sampleShape.heightProperty());

            topSlider.maxWidthProperty().bind(sampleShape.widthProperty());
            rightSlider.maxHeightProperty().bind(sampleShape.heightProperty());
            bottomSlider.maxWidthProperty().bind(sampleShape.widthProperty());
            leftSlider.maxHeightProperty().bind(sampleShape.heightProperty());

            //
            // propertiesPane
            //
            
            proportional = new CheckBox();
            proportional.setSelected(true);
            Label title = new Label("proportional");
            propertiesPane.add(title, 0, 0);
            propertiesPane.add(proportional, 1, 0);

            ObservableList<CycleMethod> items = FXCollections.observableArrayList();
            items.addAll(CycleMethod.NO_CYCLE, CycleMethod.REFLECT, CycleMethod.REPEAT);
            cycleMethodBox = new ComboBox(items);
            cycleMethodBox.getSelectionModel().selectFirst();
            title = new Label("cycleMethod");
            propertiesPane.add(title, 0, 1);
            propertiesPane.add(cycleMethodBox, 1, 1,2,1);

            //grid.add(colorChooserPane, 0, 0, 3, 1);
            grid.add(stopPane, 0, row, 2, 1);
            grid.add(samplePane, 0, row + 1, 2, 1);
            grid.add(propertiesPane, 0, row + 2, 2, 1);

            GridPane.setHgrow(stopPane, Priority.ALWAYS);
            //
            // Listeners to update chosenPaint
            //
            stopPane.stopsProperty().bindBidirectional(control.stopsProperty());
            topSlider.valueProperty().bindBidirectional(control.topValueProperty());
            rightSlider.valueProperty().bindBidirectional(control.rightValueProperty());
            bottomSlider.valueProperty().bindBidirectional(control.bottomValueProperty());
            leftSlider.valueProperty().bindBidirectional(control.leftValueProperty());
            
            stopPane.stopsProperty().addListener((v, ov, nv) -> updateGradient());
            
            topSlider.valueProperty().addListener((v, ov, nv) -> updateGradient());
            rightSlider.valueProperty().addListener((v, ov, nv) -> updateGradient());
            bottomSlider.valueProperty().addListener((v, ov, nv) -> updateGradient());
            leftSlider.valueProperty().addListener((v, ov, nv) -> updateGradient());
            proportional.selectedProperty().addListener((v, ov, nv) -> updateGradient());
            cycleMethodBox.getSelectionModel().selectedItemProperty().addListener((v, ov, nv) -> updateGradient());
            
            control.currentPaintProperty().addListener((v,ov,nv) -> {
                stopPane.setCurrentPaint(control.getCurrentPaint());
            });
            
            System.err.println("***UPDATE VALUES");
            control.updateValues();
            
            control.chosenPaintProperty().addListener((v,ov,nv) -> {
                sampleShape.setFill(nv);
            });

            
            stopPane.setCurrentPaint(control.getCurrentPaint());
            
            getChildren().add(colorChooserPane);
        }

        public StopPane getStopPane() {
            return stopPane;
        }

        public GridPane getPropertiesPane() {
            return propertiesPane;
        }

        public CheckBox getProportional() {
            return proportional;
        }

        public ComboBox<CycleMethod> getCycleMethodBox() {
            return cycleMethodBox;
        }

        public Slider getTopSlider() {
            return topSlider;
        }

        public Slider getRightSlider() {
            return rightSlider;
        }

        public Slider getBottomSlider() {
            return bottomSlider;
        }

        public Slider getLeftSlider() {
            return leftSlider;
        }

        protected Paint createGradient() {
            return null;
        }

        private void updateSample(Paint gradient) {
            sampleShape.setFill(gradient);
        }

        protected void updateGradient() {
            Paint gr = createGradient();
            updateSample(gr);
            control.setChosenPaint(gr);
        }
/*        protected void updateGradient(Paint gradient) {
            updateSample(gradient);
            updateProperties(gradient); 
            updateStops(gradient);
        }
*/
        protected void updateProperties(Paint gradient) {
            
        }
        protected void updateStops(Paint gradient) {
        }
    }//skin GradientPane
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javacompunit;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

/**
 *
 * @author Valery
 */
public class TestCodeGen {
    public void initComponents() {
       "".toString();
       button1 = new Button("button1"); 
       //vbox1.getChildren().add(button1);
       addparent_button1_vbox1: "".toCharArray();
       vbox1.getChildren().add(button1);
       end:;
       properties:
       button1.setText("Ok");
       end_properties: ;
       
    }
    private Button button1;
    private VBox vbox1;
}

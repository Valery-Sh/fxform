/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.vns.javafx.form.edit.design.palette;

import javafx.scene.Node;
import javafx.scene.control.Label;

/**
 *
 * @author Valery
 */
public class PaletteItem {
    private final String displayName;
    private final Node icon;
    private Node displayNode;
    
    protected PaletteItem(String displayName, Node icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public Node getDisplayNode() {
        if ( displayNode == null ) {
            displayNode = new Label(displayName);
        }
        return displayNode;
    }

    public void setDisplayNode(Node displayNode) {
        this.displayNode = displayNode;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public Node getIcon() {
        return icon;
    }
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.vns.javafx.form.edit.design.palette;

import javafx.scene.Node;

/**
 *
 * @author Valery
 */
public class DesignItem extends PaletteItem {
    
    private Class dragObjectClass;
    
    public DesignItem(Class<?> dragObjectClass, String displayName, Node icon) {
        super(displayName, icon);
    }

    public Class getDragObjectClass() {
        return dragObjectClass;
    }
    
}

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
public class Category extends PaletteItem {
    private String id;
    private PaletteModel model;
    
    protected Category(PaletteModel model,String id,String displayName, Node icon) {
        super(displayName, icon);
        this.id = id;
        init();
    }
    protected Category(PaletteModel model,String id,String displayName) {
        this(model,id,displayName, null);
    }
    private void init() {
        
    }
    public String getId() {
        return id;
    }

    public int add(DesignItem item) {

        int cidx = model.getItems().indexOf(this);
        if ( cidx < 0 ) {
            return -1;
        }
        int idx = cidx + 1;
        while ( idx < model.getItems().size() && ( model.getItems().get(idx) instanceof DesignItem )) {
            idx++;
        }
        model.getItems().add(--idx, item);
        return idx;
    }

    public void remove(DesignItem item) {
        model.getItems().remove(item);
    }

    
}

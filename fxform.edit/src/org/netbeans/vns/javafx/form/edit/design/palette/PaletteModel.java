/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.vns.javafx.form.edit.design.palette;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;

/**
 *
 * @author Valery
 */
public class PaletteModel {

    private final ObservableList<PaletteItem> items = FXCollections.observableArrayList();

    public ObservableList<PaletteItem> getItems() {
        return items;
    }

    public Category addCategory(String id, String categoryName, Node icon) {
        for ( PaletteItem pi : items) {
            if ( pi == null ) {
                throw new NullPointerException("The scpecified 'id' parameter cannot be null");                
            }
            if ( (pi instanceof Category) && id.equals(((Category)pi).getId()) ) {
                throw new IllegalArgumentException("Dublicate id. The category with specified 'id'  already exists");
            }
        }
        Category c = new Category(this, id, categoryName,icon);
        items.add(c);
        return c;
    }

    public Category addCategory(String id, String categoryName) {
        return this.addCategory(id, categoryName,null);
    }

    public void remove(Category category) {
        items.remove(category);
    }

    public Category getCategory(String id) {
        Category retval = null;
        for ( PaletteItem pi : items) {
            if ( (pi instanceof Category) && id.equals(((Category)pi).getId()) ) {
                retval = (Category) pi;
                break;
            }
        }
        return retval;
    }

    public DesignItem itemByClass(Class<?> dragObjectClass) {
        DesignItem retval = null;
        for (PaletteItem item : getItems()) {
            if ((item instanceof DesignItem) && (item.getClass().equals(dragObjectClass))) {
                retval = (DesignItem) item;
                break;

            }
        }
        return retval;
    }

}

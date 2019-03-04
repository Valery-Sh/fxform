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
package org.vns.javafx.scene.control.editors.beans;

import javafx.beans.DefaultProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.scene.control.Control;

@DefaultProperty("beanModels")
public class PropertyPaneModel extends Control {

    private final ObservableList<BeanModel> beanModels = FXCollections.observableArrayList();
    private final ObservableList<Class<?>> compositeProperties = FXCollections.observableArrayList();
    
    public PropertyPaneModel() {
        init();
    }

    private void init() {
    }


    public ObservableList<BeanModel> getBeanModels() {
        return beanModels;
    }

    public BeanModel getBeanModel(String className) {
        BeanModel retval = null;
        for (BeanModel bd : getBeanModels()) {
            if (className.equals(bd.getBeanClassName())) {
                retval = bd;
                break;
            }
        }
        return retval;
    }
    public boolean isComposite(Class<?> clazz) {
        boolean retval = false;
        for ( Class c : compositeProperties ) {
            if ( c.isAssignableFrom(clazz)) {
                retval = true;
                break;
            }
        }
        return retval;
    }
    protected void initialize() {
        for (BeanModel bd : getBeanModels()) {
            if (bd instanceof CompositeBeanModel ) {
                compositeProperties.add(bd.getBeanType());
            }
        }
    }

}

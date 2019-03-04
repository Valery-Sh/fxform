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

/**
 *
 * @author Valery
 */
@DefaultProperty("insertList")
public class InsertAfter extends BeanProperty implements NamedItemList<BeanProperty>, InsertAfterItem<BeanProperty>{
    private final ObservableList<BeanProperty> insertList = FXCollections.observableArrayList();

    public InsertAfter(String name) {
        super(name, null);
    }

    public InsertAfter() {
    }

    @Override
    public ObservableList<BeanProperty> getItems() {
        return insertList;
    }
    @Override
    public ObservableList<BeanProperty> getInsertList() {
        return insertList;
    }
}

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
package org.vns.javafx.scene.control;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 *
 * @author Valery
 */
public class BrowserService extends Application {

    private BrowserService() {
    }

    public static BrowserService getInstance() {
        return Singleton.INSTANCE;
    }

    public void showDocument(String uri) {
        getHostServices().showDocument(uri);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static class Singleton {

        private static final BrowserService INSTANCE = new BrowserService();
    }
}

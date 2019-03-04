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
package org.vns.javafx.designer;

import org.vns.javafx.dock.api.Selection;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import org.vns.javafx.BaseContextLookup;
import org.vns.javafx.ContextLookup;
import org.vns.javafx.dock.api.DockRegistry;
import org.vns.javafx.dock.api.SaveRestore;
import org.vns.javafx.dock.api.ScenePaneContext.ScenePaneContextFactory;
import org.vns.javafx.dock.api.Selection.SelectionHandler;
import org.vns.javafx.designer.DesignerScenePaneContext.DesignerScenePaneContextFactory;
import org.vns.javafx.dock.api.selection.NodeFraming;
import org.vns.javafx.dock.api.resizer.ResizerFactory;
import org.vns.javafx.dock.api.resizer.ResizerFactory.NodeResizerFactory;
import org.vns.javafx.dock.api.selection.StageNodeFraming;
import org.vns.javafx.dock.api.selection.WindowNodeFraming;
import org.vns.javafx.dock.api.Selection.MouseSelectionListener;
import org.vns.javafx.scene.control.editors.PropertyEditorPane;

/**
 *
 * @author Valery
 */
public class DesignerLookup { // implements ContextLookup {

    private final ContextLookup lookup;

    protected DesignerLookup() {
        lookup = new BaseContextLookup();
        init();
    }

    private void init() {
        DockRegistry.getInstance().getLookup().putUnique(SaveRestore.class, new AutoSaveRestore2());
  /*      DockRegistry.getInstance().getLookup().putUnique(TrashTray.class, new TrashTray());
        DockRegistry.getInstance().getLookup().putUnique(SaveRestore.class, new AutoSaveRestore());
        DockRegistry.getInstance().getLookup().putUnique(Selection.class, new DesignerSelection());
        DockRegistry.getInstance().getLookup().putUnique(WindowNodeFraming.class, StageNodeFraming.getInstance());
        DockRegistry.getInstance().getLookup().putUnique(NodeFraming.class, new DesignerFraming());

        DockRegistry.getInstance().getLookup().putUnique(SelectionListener.class, new SelectionHandler());
        DockRegistry.getInstance().getLookup().putUnique(ApplicationContext.class, new DesignerApplicationContext());
        DockRegistry.getInstance().getLookup().putUnique(ScenePaneContextFactory.class, new DesignerScenePaneContextFactory());
*/
        initDockRegistry();
        lookup.putUnique(PalettePane.class, new PalettePane(true));
        lookup.putUnique(PropertyEditorPane.class, new PropertyEditorPane());
        lookup.putUnique(SceneView.class, new SceneView(true));
    }
    private Map<Class<?>, Object> dockRegistrySafe = FXCollections.observableHashMap();

    private void saveDockRegistry(Class<?> clazz) {
        dockRegistrySafe.put(clazz, DockRegistry.lookup(clazz));
    }
    
    public void restoreDockRegistry() {
        dockRegistrySafe.forEach((k,v) -> {
            //System.err.println("k = " + k + "; v = " + v);
            if ( v == null ) {
                System.err.println("k = " + k + "; v = " + v);
                DockRegistry.getInstance().getLookup().clear(k);
            } else {
                System.err.println("again k = " + k + "; v = " + v);
                DockRegistry.getInstance().getLookup().putUnique(k, v);
            }
        });
        System.err.println("===========================================================");
         dockRegistrySafe.forEach((k,v) -> {
            //System.err.println("k = " + k + "; v = " + v);
            Object value = DockRegistry.getInstance().getLookup().lookup(k);
            System.err.println("k = " + k + "; v = " + value);
                
        });
    }
    
    public void initDockRegistry() {

        saveDockRegistry(ResizerFactory.class);
        DockRegistry.getInstance().getLookup().put(ResizerFactory.class,new NodeResizerFactory());
//        DockRegistry.getInstance().getLookup().put(NodeResizerFactory.class,new RegionNodeResizerFactory());
        
        saveDockRegistry(TrashTray.class);
        DockRegistry.getInstance().getLookup().putUnique(TrashTray.class, new TrashTray());
        
        saveDockRegistry(SaveRestore.class);
        //DockRegistry.getInstance().getLookup().putUnique(SaveRestore.class, new AutoSaveRestore2());

        saveDockRegistry(Selection.class);
        DockRegistry.getInstance().getLookup().putUnique(Selection.class, new DesignerSelection());
        
        saveDockRegistry(WindowNodeFraming.class);
        DockRegistry.getInstance().getLookup().putUnique(WindowNodeFraming.class, StageNodeFraming.getInstance());
        
        saveDockRegistry(NodeFraming.class);        
        DockRegistry.getInstance().getLookup().putUnique(NodeFraming.class, new DesignerFraming());

        saveDockRegistry(MouseSelectionListener.class);        
        DockRegistry.getInstance().getLookup().putUnique(MouseSelectionListener.class, new SelectionHandler());

//        saveDockRegistry(ApplicationContext.class);        
//        DockRegistry.getInstance().getLookup().putUnique(ApplicationContext.class, new DesignerApplicationContext());
        
        saveDockRegistry(ScenePaneContextFactory.class);        
        DockRegistry.getInstance().getLookup().putUnique(ScenePaneContextFactory.class, new DesignerScenePaneContextFactory());
    }

    public static DesignerLookup getInstance() {
        return SingletonInstance.INSTANCE;
    }

    public static <T> T lookup(Class<T> clazz) {
        return getInstance().lookup.lookup(clazz);
    }

    public static <T> List<? extends T> lookupAll(Class clazz) {
        return getInstance().lookup.lookupAll(clazz);
    }

    public static <T> void add(T obj) {
        getInstance().lookup.add(obj);
    }

    public static <T> void remove(T obj) {
        getInstance().lookup.remove(obj);
    }

    public static <T> void putUnique(Class key, T obj) {
        getInstance().lookup.putUnique(key, obj);
    }
    public static <T> void put(Class key, T obj) {
        getInstance().lookup.put(key, obj);
    }

    public static <T> void remove(Class key, T obj) {
        getInstance().lookup.remove(key, obj);
    }

/*    public static class DesignerApplicationContext implements ApplicationContext {

        @Override
        public boolean isDesignerContext() {
            return true;
        }

    }
*/
    private static class SingletonInstance {

        private static final DesignerLookup INSTANCE = new DesignerLookup();
    }

}

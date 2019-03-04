package org.vns.javafx.dock.api;

import org.vns.javafx.ContextLookup;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;

import javafx.stage.Window;
import org.vns.javafx.BaseContextLookup;
import org.vns.javafx.TopWindowFinder;
import org.vns.javafx.DefaultTopWindowFinder;

/**
 * The class contains methods to manage all windows
 *
 * @author Valery Shyshkin
 */
public class DockRegistry {

    public final ContextLookup lookup;

    private final ObservableList<Window> windows = FXCollections.observableArrayList();

//    private final Map<Window, Window> owners = new HashMap<>();
    private final ObservableList<Window> excluded = FXCollections.observableArrayList();

    //private final ObservableMap<Node, Dockable> dockables = FXCollections.observableHashMap();
    //private final ObservableMap<Node, DockLayout> dockLayouts = FXCollections.observableHashMap();
//    private BeanRemover beanRemover;
    

    private DockRegistry() {
        //beanRemover = new DefaultNodeRemover();
        lookup = new BaseContextLookup();
        init();
    }

    public ContextLookup getLookup() {
        return lookup;
    }

    private void init() {
        windows.addListener(this::windowsChanged);
        lookup.putUnique(ScopeEvaluator.class, new LayoutContext.DefaultScopeEvaluator());
        TopWindowFinder wf = DefaultTopWindowFinder.getInstance();
        wf.start();
        lookup.putUnique(TopWindowFinder.class, wf);

        //getTopWindowFinder();
    }

    public static <T> T lookup(Class<T> clazz) {
        return getInstance().lookup.lookup(clazz);
    }

    public void windowsChanged(ListChangeListener.Change<? extends Window> change) {
        while (change.next()) {
            if (change.wasRemoved()) {
                List<? extends Window> list = change.getRemoved();
                for (Window win : list) {

                }

            }
            if (change.wasAdded()) {
                List<? extends Window> list = change.getAddedSubList();
            }
        }//while
    }

    public Window getFocusedWindow() {
        Window retval = null;
        for (Window win : windows) {
            if (win.isFocused()) {
                retval = win;
                break;
            }
        }
        return retval;
    }

    public static DockRegistry getInstance() {
        return SingletonInstance.instance;
    }

    /*    public BeanRemover getBeanRemover() {
//        if ( beanRemover == null )
        return beanRemover;
    }

    public void setBeanRemover(BeanRemover beanRemover) {
        this.beanRemover = beanRemover;
    }
     */
/*    public static void register(Window window, boolean excluded) {
        register(window);
        if (excluded && !getInstance().getExcluded().contains(window)) {
            getInstance().getExcluded().add(window);
        }
    }

    protected ObservableList<Window> getExcluded() {
        return excluded;
    }

    public static void register(Window window) {
        getInstance().doRegister(window);
        if (!(window instanceof Stage)) {
            getInstance().getWindows().add(window);
        }
    }

    public static void start() {
        if (!getInstance().registerDone) {
            getInstance().registerDone = true;
            StageHelper.getStages().forEach(s -> {
                //
                // Add in reverse order
                //
                getInstance().windows.add(0, s);

            });
            StageHelper.getStages().addListener(getInstance()::onChangeStages);
        }
    }
*/
/*    protected void onChangeStages(ListChangeListener.Change<? extends Stage> change) {
        while (change.next()) {
            if (change.wasPermutated()) {

            } else if (change.wasUpdated()) {

            } else if (change.wasReplaced()) {
            } else {
                if (change.wasRemoved()) {
                    change.getRemoved().forEach(s -> {
                        getInstance().windows.remove(s);
                        //getInstance().owners.clear(s);
                    });
                } else if (change.wasAdded()) {
                    change.getAddedSubList().forEach(s -> {
                        register(s);
                        if (s.getOwner() != null) {
                            //getInstance().owners.put(s, s.getOwner());
                        }
                    });

                    Platform.runLater(() -> {
                        //updateRegistry();
                    });
                }
            }
        }

    }

    public static void unregister(Window window) {
        if (getInstance().getExcluded().contains(window)) {
            getInstance().getExcluded().remove(window);
        }

        if (window instanceof Stage) {
            return;
        }
        getInstance().windows.remove(window);
        //getInstance().owners.clear(window);
        Platform.runLater(() -> {
            // getInstance().updateRegistry();
        });
    }

    public static ObservableList<Window> getWindows() {
        return getInstance().windows;
    }

    private void doRegister(Window window) {
        window.focusedProperty().addListener((ov, oldValue, newValue) -> {
            if (newValue) {
                windows.remove(window);
                windows.add(0, window);
            }
        });
    }
*/
/*    private boolean isChild(Window parent, Window child) {
        boolean retval = false;
        Window win = child;
        int i = 0;
        while (win != null) {
            if (!(win instanceof PopupWindow) && !(win instanceof Stage)) {
                break;
            } else if ((win instanceof Stage) && ((Stage) win).getOwner() == parent) {
                retval = true;
                break;
            }

            if ((win instanceof PopupWindow) && ((PopupWindow) win).getOwnerWindow() == parent) {
                retval = true;
                break;
            } else if ((win instanceof Stage) && ((Stage) win).getOwner() == parent) {
                retval = true;
                break;
            }

            if ((win instanceof PopupWindow) && ((PopupWindow) win).getOwnerWindow() == parent) {
                win = ((PopupWindow) win).getOwnerWindow();
            } else if ((win instanceof Stage)) {
                win = ((Stage) win).getOwner();
            } else {
                win = null;
            }
        }//while
        return retval;
    }

    private int zorder(Window window) {
        return JdkUtil.getWindows().indexOf(window);
    }
*/
    public Window getTopWindow(double x, double y, Window... excl) {
        Window retval = Util.getWindowIfSingle(x, y, excl);

        return retval;
    }

    public static TopWindowFinder getTopWindowFinder() {
        TopWindowFinder wf = lookup(TopWindowFinder.class);
        if (wf == null) {
            wf = DefaultTopWindowFinder.getInstance();
            wf.start();
            getInstance().getLookup().putUnique(TopWindowFinder.class, wf);
        }
        return wf;
    }

/*    public Window getTopWindow__(double x, double y, Window excl) {

        List<Window> allWindows = getWindows__(x, y, excl);

        if (allWindows.isEmpty()) {
            return null;
        }
        //System.err.println("allWindows.get(0).id = " + allWindows.get(0).getScene().getRoot().getId());
        //System.err.println("allWindows.get(0).ROOT = " + allWindows.get(0).getScene().getRoot());
        Window retval = null;
        List<Window> targetStages = new ArrayList<>();
        allWindows.forEach(w -> {
            Node topNode = Util.getTop(w, x, y, n -> {
                return (n instanceof Node);
            });
            if (topNode != null) {
                targetStages.add(w);
            }
        });
        for (Window w : targetStages) {
            retval = w;
            for (Window w2 : allWindows) {
                if (w == w2) {
                    continue;
                }
                if (w != DockRegistry.this.getTarget(w, w2)) {
                    retval = null;
                    break;
                }
            }
            if (retval != null) {
                break;
            }
        }
        return retval;

    }

    public Window getTopWindow_OLD(double x, double y, Window excl) {
        Window retval = null;
        List<Window> allWindows = getWindows(x, y, excl);

        if (allWindows.isEmpty()) {
            return null;
        }
        List<Window> targetStages = new ArrayList<>();
        allWindows.forEach(w -> {
            Node topNode = Util.getTop(w, x, y, n -> {
                return (n instanceof Node);
            });
            if (topNode != null) {
                targetStages.add(w);
            }
        });
        for (Window w : targetStages) {
            retval = w;
            for (Window w2 : allWindows) {
                if (w == w2) {
                    continue;
                }
                if (w != DockRegistry.this.getTarget(w, w2)) {
                    retval = null;
                    break;
                }
            }
            if (retval != null) {
                break;
            }
        }
        return retval;
    }
*/    
    public Window getTarget(double x, double y, Window excl) {
        TopWindowFinder wf = getTopWindowFinder();
        Window win = wf.getTopWindow(x, y, excl);
        Node topNode = Util.getTop(win, x, y, n -> {
            return isDockLayout(n);
        });
        return topNode == null ? null : win;
    }
/*    public Window getTarget__OLD(double x, double y, Window excl) {
        Window retval = null;
        List<Window> allWindows = getWindows__(x, y, excl);
        if (allWindows.isEmpty()) {
            return null;
        }
        List<Window> targetStages = new ArrayList<>();
        allWindows.forEach(w -> {
            Node topNode = Util.getTop(w, x, y, n -> {
                return isDockLayout(n);
            });
            if (topNode != null) {
                targetStages.add(w);
            }
        });

        for (Window w : targetStages) {
            retval = w;
            for (Window w2 : allWindows) {
                if (w == w2) {
                    continue;
                }
                if (w != DockRegistry.this.getTarget(w, w2)) {
                    retval = null;
                    break;
                }
            }
            if (retval != null) {
                break;
            }
        }
        return retval;
        
    }
*/
    /*    public Window getTarget(double x, double y, Window excl, Predicate<Node> predicate) {
        Window retval = null;
        List<Window> allStages = getWindows(x, y, excl);
        if (allStages.isEmpty()) {
            return null;
        }
        List<Window> targetStages = new ArrayList<>();
        allStages.forEach(s -> {
            Node topNode = Util.getTop(s, x, y, n -> {
                return predicate.test(n);
            });
            if (topNode != null) {
                targetStages.add(s);
            }
        });
        for (Window s1 : targetStages) {
            retval = s1;
            for (Window s2 : allStages) {
                if (s1 == s2) {
                    continue;
                }
                if (s1 != DockRegistry.this.getTarget(s1, s2)) {
                    retval = null;
                    break;
                }
            }
            if (retval != null) {
                break;
            }
        }
        return retval;
    }
     */
/*    public Window getTarget(Window w1, Window w2) {
        Window retval = null;

        //Window s = w1;
        boolean b1 = false;
        boolean b2 = false;

        if (w1 instanceof PopupWindow) {
            b1 = true;
        } else if ((w1 instanceof Stage)) {

            b1 = ((Stage) w1).isAlwaysOnTop();
        }
        if (w2 instanceof PopupWindow) {
            b2 = true;
        } else if ((w2 instanceof Stage)) {
            b2 = ((Stage) w2).isAlwaysOnTop();
        }

        if (isChild(w1, w2)) {
            //
            //retval must be null w2 is a child window of w1
            //

        } else if (isChild(w2, w1)) {
            retval = w1;
        } else if (zorder(w1) < zorder(w2) && !b1 && !b2) {
            retval = w1;
        } else if (zorder(w1) < zorder(w2) && b1 && b2) {
            retval = w1;
        } else if (b1 && !b2) {
            retval = w1;
        } else if (!b1 && b2) {
        }
        return retval;
    }
*/
/*    public static List<Window> getWindows__(double x, double y, Window excl) {
        //System.err.println("EXCL = " + excl.getScene().getRoot());
        List<Window> retlist = new ArrayList<>();
        List<Window> list = JdkUtil.getWindows();
        list.forEach(s -> {
            if (!((x < s.getX() || x > s.getX() + s.getWidth()
                    || y < s.getY() || y > s.getY() + s.getHeight()))) {
                if (s != excl && !getInstance().getExcluded().contains(s)) {
                    retlist.add(s);
                }
            }
        });
        return retlist;
    }

    public static List<Window> getWindows(double x, double y, Window excl) {
        List<Window> retlist = new ArrayList<>();
        StageHelper.getStages().forEach(s -> {
            if (!((x < s.getX() || x > s.getX() + s.getWidth()
                    || y < s.getY() || y > s.getY() + s.getHeight()))) {
                if (s != excl && !getInstance().getExcluded().contains(s)) {
                    retlist.add(s);
                }
            }
        });
        getInstance().windows.forEach(s -> {
            if (!(s instanceof Stage)) {
                if (!((x < s.getX() || x > s.getX() + s.getWidth()
                        || y < s.getY() || y > s.getY() + s.getHeight()))) {
                    if (s != excl && !getInstance().getExcluded().contains(s)) {
                        retlist.add(s);
                    }
                }

            }
        });
        return retlist;
    }
*/
    /*    protected boolean isNodeDockable(Node node) {
        boolean retval = node instanceof Dockable;
        if (!retval && dockables.get(node) != null) {
            retval = true;
        } else if (!retval) {
            Object d = node.getProperties().get(Dockable.DOCKABLE);

            if (d != null && (d instanceof Dockable) && ((Dockable) d).node() == node) {
                retval = true;
            }
        }
        return retval;
    }
     */
    public static Dockable makeDockable(Node node) {
        if (isDockable(node)) {
            return dockable(node);
        }
        Dockable d = new DefaultDockable(node);

        d.getContext().setDragNode(node);
        node.getProperties().put(Dockable.DOCKABLE, d);
        return d;
    }

    public static void unregisterDockable(Object obj) {
        if ((obj instanceof Dockable) || Dockable.of(obj) == null) {
            return;
        }
        Dockable.of(obj).getContext().reset();
        Dockable.of(obj).getNode().getProperties().remove(Dockable.DOCKABLE);
    }

    public static void unregisterDockLayout(Object obj) {
        if ((obj instanceof DockLayout) || DockLayout.of(obj) == null) {
            return;
        }
        DockLayout.of(obj).getLayoutContext().reset();
        DockLayout.of(obj).getLayoutNode().getProperties().remove(DockLayout.DOCKLAYOUT);

    }

    public static DockLayout makeDockLayout(Node node, LayoutContext layoutContext) {

        if (node instanceof DockLayout) {
            return (DockLayout) node;
        }
        if (node.getProperties().get(DockLayout.DOCKLAYOUT) != null) {
            return (DockLayout) node.getProperties().get(DockLayout.DOCKLAYOUT);
        }
        DockLayout d = new DefaultDockLayout(node, layoutContext);
        node.getProperties().put(DockLayout.DOCKLAYOUT, d);
        return d;
    }

    /*    public void register(Dockable dockable) {
        if (dockable.node() instanceof Dockable) {
            return;
        }
        if (dockables.get(dockable.node()) != null) {
            return;
        }
        dockables.put(dockable.node(), dockable);
    }
     */
 /*    public void register(DockLayout dockLayout) {
        if (dockLayout.layoutNode() instanceof DockLayout) {
            return;
        }
        if (dockLayouts.get(dockLayout.layoutNode()) != null) {
            return;
        }
        dockLayouts.put(dockLayout.layoutNode(), dockLayout);

    }
     */
    public static LayoutContext getLayoutContext(Object obj) {
        if (isDockLayout(obj)) {
            return dockLayout(obj).getLayoutContext();
        }
        Node node = null;
        if (obj == null || !(obj instanceof Node)) {
            return null;
        }
        node = (Node) obj;

        LayoutContext retval = new DefaultLayoutContextFactory().getContext(node);

        if (retval == null) {
            //
            // try to find in lookup
            //
            List<? extends LayoutContextFactory> list = DockRegistry.getInstance().getLookup().lookupAll(LayoutContextFactory.class);
            for (LayoutContextFactory f : list) {
                retval = f.getContext(node);
                if (retval != null) {
                    break;
                }
            }
        }
        return retval;

    }

    public static DockLayout makeDockLayout(Node node) {
        if (isDockLayout(node)) {
            return dockLayout(node);
        }

        LayoutContext c = new DefaultLayoutContextFactory().getContext(node);

        if (c == null) {
            //
            // try to find in lookup
            //
            List<? extends LayoutContextFactory> list = DockRegistry.getInstance().getLookup().lookupAll(LayoutContextFactory.class);
            for (LayoutContextFactory f : list) {
                c = f.getContext(node);
                if (c != null) {
                    break;
                }
            }
            if (c == null) {
                return null;
            }
        }
        DockLayout dt = new DefaultDockLayout(node, c);
        node.getProperties().put(DockLayout.DOCKLAYOUT, dt);
        return dt;
    }
    //
    // DELETE
/*    public Dockable getDefaultDockable(Node node) {
        if (node instanceof Dockable) {
            return (Dockable) node;
        }
        if (node.getProperties().get(Dockable.DOCKABLE) != null) {
            return (Dockable) node.getProperties().get(Dockable.DOCKABLE);
        }
        Dockable d = new DefaultDockable(node);
        node.getProperties().put(Dockable.DOCKABLE, d);
        return d;
    }
     */
    public static boolean isDockable(Object obj) {
        if ((obj instanceof Dockable)) {
            Node node = ((Dockable) obj).getNode();
            if ((obj instanceof Node) && node != obj) {
                return false;
            }
            return true;
        }

        if (!(obj instanceof Node)) {
            return false;
        }
        Node node = (Node) obj;
        return node.getProperties().get(Dockable.DOCKABLE) != null;
    }

    public static Dockable dockable(Object obj) {
        if (obj == null) {
            return null;
        }

        if ((obj instanceof Dockable)) {
            Node node = ((Dockable) obj).getNode();
            if ((obj instanceof Node) && node != obj) {
                return null;
            }
            return (Dockable) obj;
        }

        if (!(obj instanceof Node)) {
            return null;
        }
        Node node = (Node) obj;

        return (Dockable) node.getProperties().get(Dockable.DOCKABLE);
    }

    /*    public static boolean instanceOfDockLayout(Node node) {
        return getInstance().isNodeDockLayout(node);
    }
     */
    public static boolean isDockLayout(Object obj) {
        if (obj == null) {
            return false;
        }
        if ((obj instanceof DockLayout)) {
            Node node = ((DockLayout) obj).getLayoutNode();
            if ((obj instanceof Node) && node != obj) {
                return false;
            }
            return true;
        }

        if (!(obj instanceof Node)) {
            return false;
        }
        Node node = (Node) obj;
        return node.getProperties().get(DockLayout.DOCKLAYOUT) != null;
    }

    /*    private boolean isNodeDockLayout(Node node) {
        boolean retval = node instanceof DockLayout;
        if (!retval && dockLayouts.get(node) != null) {
            retval = true;
        } else if (!retval) {
            Object d = node.getProperties().get(DockLayout.DOCKLAYOUT);
            if (d != null && (d instanceof DockLayout) && ((DockLayout) d).layoutNode() == node) {
                retval = true;
            }
        }
        return retval;
    }
     */
    public static DockLayout dockLayout(Object obj) {
        if (obj == null) {
            return null;
        }
        if ((obj instanceof DockLayout)) {
            Node node = ((DockLayout) obj).getLayoutNode();
            if ((obj instanceof Node) && node != obj) {
                return null;
            }
            return (DockLayout) obj;
        }
//        if (obj instanceof DockLayout) {
//            return (DockLayout) obj;
//        }
        if (!(obj instanceof Node)) {
            return null;
        }
        return (DockLayout) ((Node) obj).getProperties().get(DockLayout.DOCKLAYOUT);

    }

    private static class SingletonInstance {

        private static final DockRegistry instance = new DockRegistry();
    }

    public static class DefaultDockable implements Dockable {

        private final Node node;
        private DockableContext context;

        public DefaultDockable(Node node) {
            this.node = node;
            init();
        }

        private void init() {
            context = new DockableContext(this);
        }

        @Override
        public Node getNode() {
            return node;
        }

        @Override
        public DockableContext getContext() {
            return context;
        }

    }

    public static class DefaultDockLayout implements DockLayout {

        private final Node node;
        private LayoutContext context;

        public DefaultDockLayout(Node node, LayoutContext context) {
            super();
            this.node = node;
            this.context = context;
        }

        @Override
        public Node getLayoutNode() {
            return node;
        }

        @Override
        public LayoutContext getLayoutContext() {
            return context;
        }
    }
}

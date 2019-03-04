package org.vns.javafx.dock.api;

import javafx.scene.Node;
import javafx.scene.control.SplitPane;

/**
 *
 * @author Valery Shyshkin
 */
public class DockSplitPane extends SplitPane {// implements ListChangeListener {

    //private EventHandler<ActionEvent> root;
    //private ListChangeListener itemsChangeListener;

    public DockSplitPane() {
        //init();
    }

    public DockSplitPane(Node... items) {
        super(items);
        //init();
    }

/*    public EventHandler<ActionEvent> getRoot() {
        return root;
    }

    public void setRoot(EventHandler<ActionEvent> root) {
        this.root = root;
    }
*/
/*    private void init() {
        DockTarget dpt = DockUtil.getParentDockPane(this);
        if (dpt != null && getItems().size() > 0) {
            getItems().forEach(it -> {
                //!!!08
                if (DockRegistry.isDockable(it)) {
                    //Dockable.of(it).getContext().setTargetContext(dpt.getTargetContext());
                }
            });
        }
        if (dpt != null) {
            //update();
        }
        //getItems().addListener(this);
    }
*/
/*    @Override
    public void onChanged(ListChangeListener.Change change) {
        itemsChanged(change);
    }

    protected void itemsChanged(ListChangeListener.Change<? extends Node> change) {
        DockTarget dpt = null;
        while (change.next()) {
            if (change.wasRemoved()) {
                List<? extends Node> list = change.getRemoved();
                if (!list.isEmpty() && dpt == null) {
                    dpt = DockUtil.getParentDockPane(list.get(0));
                }
                for (Node node : list) {
                    //!!!08
                    if (dpt != null && DockRegistry.isDockable(node)) {
                    } else if (dpt != null && node instanceof DockSplitPane) {
                        splitPaneRemoved((SplitPane) node, dpt);
                    }
                }

            }
            if (change.wasAdded()) {
                List<? extends Node> list = change.getAddedSubList();
                if (!list.isEmpty() && dpt == null) {
                    dpt = DockUtil.getParentDockPane(list.get(0));
                }
                for (Node node : list) {
                    //!!!08
                    if (dpt != null && DockRegistry.isDockable(node)) {
                        Dockable.of(node).getContext().setTargetContext(dpt.getTargetContext());
                    } else if (dpt != null && node instanceof DockSplitPane) {
                        splitPaneAdded((SplitPane) node, dpt);
                    } else if (node instanceof DockSplitPane) {
                        ((DockSplitPane) node).setRoot(getRoot());
                    }
                }
            }
        }//while
        update();
    }
*/
/*    protected void update(DockSplitPane split, TargetContext ph) {
        for (int i = 0; i < split.getItems().size(); i++) {
            Node node = split.getItems().get(i);
            //!!!08
            if (DockRegistry.isDockable(node)) {
                Dockable d = Dockable.of(node);
                d.getContext().setTargetContext(ph);
            } else if (node instanceof DockSplitPane) {
                ((DockSplitPane) node).setRoot(getRoot());
                DockSplitPane sp = (DockSplitPane) node;
                update(sp, ph);
            }
        }
    }

    public void update() {
        if (getRoot() != null) {
            getRoot().handle(new ActionEvent());
        }
    }

    protected void splitPaneAdded(SplitPane sp, DockTarget dpt) {
        for (int di = 0; di < sp.getDividerPositions().length; di++) {
            sp.setDividerPosition(di, sp.getDividerPositions()[di] + 0.01);
        }

        sp.getItems().forEach((node) -> {
            //!!!08
            if (DockRegistry.isDockable(node)) {
                Dockable.of(node).getContext().setTargetContext(dpt.getTargetContext());
            } else if (node instanceof SplitPane) {
                splitPaneAdded(((SplitPane) node), dpt);
            }
        });
    }

    protected void splitPaneRemoved(SplitPane sp, DockTarget dpt) {
        sp.getItems().forEach((node) -> {
            //!!!08
            if (DockRegistry.isDockable(node)) {
            } else if (node instanceof SplitPane) {
                splitPaneRemoved(((SplitPane) node), dpt);
            }
        });
    }
*/
}

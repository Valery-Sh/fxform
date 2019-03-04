package org.vns.javafx.dock.api;

import org.vns.javafx.ContextLookup;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.stage.Window;
import static org.vns.javafx.dock.api.LayoutContext.getValue;
import org.vns.javafx.dock.api.bean.ReflectHelper;

/**
 *
 * @author Valery Shyshkin
 */
public class ScenePaneContext extends LayoutContext {

    private final Dockable dockable;

    public ScenePaneContext(Dockable dockable) {
        super();
        this.dockable = dockable;
        init();
    }

    private void init() {
        dockable.getNode().parentProperty().addListener(this::parentChanged);
    }

    @Override
    protected void initLookup(ContextLookup lookup) {
    }

    protected void parentChanged(ObservableValue<? extends Parent> value, Parent oldValue, Parent newValue) {
        if (oldValue != null) {
            oldValue.parentProperty().removeListener(this::parentChanged);
        }
        setLayoutNode(newValue);
    }

    @Override
    public boolean contains(Object obj) {
        if (obj == null || !(obj instanceof Node)) {
            return false;
        }
        return Dockable.of(obj) != null && Dockable.of(obj).getContext().getLayoutContext() == this && ((Node) obj).getParent() != null;
    }

    @Override
    public void remove(Object obj) {
        if (!(obj instanceof Node)) {
            return;
        }
        Node dockNode = (Node) obj;
        if (!contains(dockNode)) {
            //return;
        }
        if (!(obj instanceof Node) || ((Node) obj).getParent() == null) {
            return;
        }
        Node node = ((Node) obj).getParent();
        while (node != null) {
            if (ReflectHelper.isPublic(node.getClass())) {
                break;
            }
            node = node.getParent();
        }
        if (node == null) {
            return;
        }
//        if (DockRegistry.getInstance().getBeanRemover() != null) {
//            DockRegistry.getInstance().getBeanRemover().remove(dockNode);
//        }
    }

    @Override
    public void dock(Point2D mousePos, Dockable dockable) {
        if (!isAcceptable(dockable)) {
            return;
        }
        Object o = getValue(dockable);
        if (o == null || Dockable.of(o) == null) {
            return;
        }

        Dockable d = Dockable.of(o);

        Node node = d.getNode();
        Window stage = null;
        if (node.getScene() != null && node.getScene().getWindow() != null) { //&& (node.getScene().getWindow() instanceof Stage)) {
            stage = node.getScene().getWindow();
        }

        if (stage != null) {
            if ((stage instanceof Stage)) {
                ((Stage) stage).close();
            } else {
                stage.hide();
            }
            d.getContext().setLayoutContext(this);
        }
    }

    public static class ScenePaneContextFactory {

        public ScenePaneContext getContext(Dockable dockable) {
            return new ScenePaneContext(dockable);
        }
    }

}

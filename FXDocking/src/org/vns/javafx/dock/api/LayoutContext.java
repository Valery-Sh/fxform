package org.vns.javafx.dock.api;

import java.util.Set;
import java.util.function.BiPredicate;
import org.vns.javafx.ContextLookup;
import org.vns.javafx.dock.api.indicator.PositionIndicator;
import org.vns.javafx.dock.api.indicator.IndicatorPopup;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import org.vns.javafx.BaseContextLookup;

/**
 *
 * @author Valery
 */
public abstract class LayoutContext {

    private ContextLookup lookup;

    private Node layoutNode;
   
    private PositionIndicator positionIndicator;
    //private boolean indicatorIntersection;
    
    //private final ObjectProperty<Node> focusedDockNode = new SimpleObjectProperty<>();

    private boolean usedAsDockLayout = true;

    private BiPredicate<LayoutContext, Dockable> acceptFilter = (lc, d) -> {
        return true;
    };

    private ObservableSet<Scope> scopes = FXCollections.observableSet();

    protected LayoutContext(Node layoutNode) {
        this.layoutNode = layoutNode;
        init();
    }

    protected LayoutContext() {
        init();
    }

    public ObservableSet<Scope> getScopes() {
        return scopes;
    }

    public ContextLookup getLookup() {
        if (lookup == null) {
            lookup = new BaseContextLookup();
            initLookup(lookup);
        }
        return lookup;
    }

    private void init() {
        inititialize();
        getLookup().add(new IndicatorPopup(this));
    }

    protected void initLookup(ContextLookup lookup) {
    }
    void reset() {
        layoutNode = null;
        positionIndicator = null;
    }
    public BiPredicate<LayoutContext, Dockable> getAcceptFilter() {
        return acceptFilter;
    }

    public void setAcceptFilter(BiPredicate<LayoutContext, Dockable> acceptFilter) {
        this.acceptFilter = acceptFilter;
    }


    protected void commitDock(Object obj) {
        if (obj != null && DockRegistry.isDockable(obj)) {
            DockableContext dockableContext = Dockable.of(obj).getContext();
            if (dockableContext.getLayoutContext() != this) {
                dockableContext.setLayoutContext(this);
                ConstraintsFactory f = getLookup().lookup(ConstraintsFactory.class);
                if ( f != null ) {
                    Constraints c = f.getConstraints(Dockable.of(obj).getNode());
                    Dockable.of(obj).getNode().getProperties().put(Constraints.PROPERTY_NAME, c);
                }
            }
        }
    }

    protected void inititialize() {
        //DockRegistry.start();
        initListeners();
    }

    protected void initListeners() {
        if (getLayoutNode() == null) {
            return;
        }
    }

    /**
     * The method is called by the object {@code DragManager } when the mouse
     * event of type {@code MOUSE_DRAGGED} is handled. May be useful for example
     * when implement animation for scroll bars.
     *
     * @param dockable the dragged object
     * @param ev the object of type {@code MouseEvent }
     *
     */
    public void mouseDragged(Dockable dockable, MouseEvent ev) {

    }

    public static Object getValue(Dockable dockable) {
        Object retval = null;
        DragContainer dc = dockable.getContext().getDragContainer();
        if (dc != null) {
            retval = dc.getValue();
        } else if (dc == null) {
            retval = dockable;
        }
        return retval;
    }

    public boolean isAdmissiblePosition(Dockable dockable, Point2D mousePos) {
        return true;
    }

    public boolean isAcceptable(Dockable dockable) {
        Object v = getValue(dockable);
        if (Dockable.of(v) == null) {
            return isAcceptable(v);
        }
        if (acceptFilter != null && !acceptFilter.test(this, dockable)) {
            return false;
        }
        return (DockRegistry.lookup(ScopeEvaluator.class).evaluate(this, dockable.getContext()));
    }
    
    protected boolean isAcceptable(Object obj) {
        return false;
    }
    
    public final void executeDock(Point2D mousePos, Dockable dockable) {
        dock(mousePos, dockable);
        commitDock(dockable.getContext().getDragValue());
    }

    public boolean isUsedAsDockLayout() {
        return usedAsDockLayout;
    }

    public void setUsedAsDockLayout(boolean usedAsDockLayout) {
        this.usedAsDockLayout = usedAsDockLayout;
    }

    public PositionIndicator getPositionIndicator() {
        if (positionIndicator == null) {
            positionIndicator = getLookup().lookup(PositionIndicator.class);
        }
        return positionIndicator;
    }

    /**
     * Returns the node for which this context was created The node may throw
     * {@code NullPointerException} in case when the both conditions below are
     * met:
     * <ul>
     * <li> ! (this instanceof ScenePaneContext)</li>
     * <li>layoutNode == null</li>
     * </ul>
     *
     * @return the node for which this context was created.
     */
    public final Node getLayoutNode() {
        if (!(this instanceof ScenePaneContext) && layoutNode == null) {
            throw new NullPointerException("The property layoutNode cannot be null");
        }
        return this.layoutNode;
    }

    protected void setLayoutNode(Node layoutNode) {
        this.layoutNode = layoutNode;
    }

    /**
     * isDocked(Node) returns true even if the node is docked to the given
     * {@code LayoutContext}
     *
     * @param tc the object of type {@code LayoutContext}
     * @param dockable the object to chack
     * @return true even if the node is docked to the given
     * {@code LayoutContext}
     */
    public static boolean isDocked(LayoutContext tc, Dockable dockable) {
        return tc.isDocked(dockable);
    }

    public boolean isDocked(Dockable dockable) {
        Object obj = dockable.getNode();
        DragContainer dc = dockable.getContext().getDragContainer();
        if (dc != null && dc.getValue() != null && dc.isValueDockable()) {
            obj = Dockable.of(dc.getValue()).getNode();
        } else if (dc != null && dc.getValue() != null && !dc.isValueDockable()) {
            obj = dc.getValue();
        }
        return contains(obj);
    }


    public void undock(Dockable dockable) {
        if (dockable == null) {
            return;
        }
        DockableContext ctx = dockable.getContext();
        Object obj = dockable.getNode();

        DragContainer dc = ctx.getDragContainer();

        if (dc != null && Dockable.of(dc.getValue()) != null) {
            obj = Dockable.of(dc.getValue()).getNode();
        } else if (dc != null) {
            obj = dc.getValue();
        }
        Dockable dockableObj = Dockable.of(obj);
        if ((obj instanceof Node) && dockableObj != null && dockableObj.getContext().getLayoutContext().isDocked(dockableObj)) {
            ctx = dockableObj.getContext();
            ctx.getLayoutContext().remove(obj);
            Node node = (Node) obj;
            if ( node.getProperties().get(Constraints.PROPERTY_NAME) != null  ){
                ((Constraints)node.getProperties().get(Constraints.PROPERTY_NAME)).delete();
            }
            ctx.setLayoutContext(null);
            //LayoutContext tc = ctx.getLayoutContext();
        } else if (dockableObj == null) {
            if (dc.getDragSource() != null) {
                dc.getDragSource().remove(dc.getValue());
            }
        }

    }
    public abstract boolean contains(Object obj);
    public abstract void remove(Object obj);
    public abstract void dock(Point2D mousePos, Dockable dockable);

    public static class DefaultScopeEvaluator implements ScopeEvaluator {

        @Override
        public boolean evaluate(LayoutContext layoutContext, DockableContext dockableContext) {
            boolean retval = false;
            Set<Scope> lset = FXCollections.observableSet(layoutContext.getScopes());
            Set<Scope> dset = FXCollections.observableSet();
            if (lset.isEmpty()) {
                lset.add(createLayoutScope(layoutContext));
            }
         
            if (dockableContext.getDragContainer() != null) {
                
                Object obj = dockableContext.getDragContainer().getValue();
                if (Dockable.of(obj) != null) {
                    dset.addAll(Dockable.of(obj).getContext().getScopes());
                }
            } else {
                dset.addAll(dockableContext.getScopes());
            }
            if (dset.isEmpty()) {
                dset.add(createDockableScope(dockableContext));
            }
            

            for (Scope ls : lset) {
                for (Scope ds : dset) {
                    if (evaluate(layoutContext, dockableContext, ls, ds)) {
                        retval = true;
                        break;
                    }
                }
                if (retval) {
                    break;
                }
            }
            return retval;
        }

        protected boolean evaluate(LayoutContext lc, DockableContext dc, Scope layoutScope, Scope dockableScope) {
            if (!Scope.test(layoutScope, dockableScope)) {
                return false;
            }
            boolean retval = true;
            if (layoutScope.getFilter() != null && !layoutScope.getFilter().test(lc, dc)) {
                retval = false;
            }
            if (retval && dockableScope.getFilter() != null && !dockableScope.getFilter().test(lc, dc)) {
                retval = false;
            }
            return retval;
        }

        @Override
        public Scope createLayoutScope(LayoutContext LayoutContext) {
            return new Scope("default");
        }

        @Override
        public Scope createDockableScope(DockableContext LayoutContext) {
            return new Scope("default");
        }
    }
}//class

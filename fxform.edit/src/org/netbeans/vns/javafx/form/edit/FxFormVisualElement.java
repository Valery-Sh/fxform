package org.netbeans.vns.javafx.form.edit;

import com.sun.source.tree.Tree;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javafx.embed.swing.JFXPanel;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.DataEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@TopComponent.Description(
        preferredID = "FxFormVisual"
)
@MultiViewElement.Registration(
        displayName = "#LBL_FxForm_VISUAL",
        iconBase = "org/netbeans/vns/javafx/form/edit/resources/multiView_16.png",
        mimeType = "text/x-fxform",
        persistenceType = TopComponent.PERSISTENCE_NEVER,
        preferredID = "FxFormVisual",
        position = 2000
)
@Messages("LBL_FxForm_VISUAL=Fx Design")
public final class FxFormVisualElement extends TopComponent implements MultiViewElement {

    public static final String DESIGN_MARKER = "nbdesign_visualcomponent";

    private final FxFormDataObject formDataObject;
    private final JToolBar toolbar = new JToolBar();
    private transient MultiViewElementCallback callback;

    private final JPanel jTopPanel;
    private final JFXPanel jfxRootPanel;

    public FxFormVisualElement(Lookup lookup) {
        Util.out("FxFormVisualElement =============================== " + this);
        formDataObject = lookup.lookup(FxFormDataObject.class);
        Util.out("   --- formDataObject = " + formDataObject);
        assert formDataObject != null;
        //FileObject dfo = formDataObject.getPrimaryFile().getParent().getFileObject("Sanple01_design.java");
        File f = new File("D:\\JavaFX-Tests\\JavaApplication5\\src\\javaapplication5\\Sanple01_design.java");

        FileObject dfo = FileUtil.toFileObject(f);
        Project p = FileOwnerQuery.getOwner(dfo);
        Util.out("PROJECT LOOKUP ===============================");
        for (Object o : p.getLookup().lookupAll(Object.class)) {
            Util.out("   --- obj = " + o);
        }
        Util.out("END PROJECT LOOKUP ===============================");

        Util.out("FxFormVisualElement:constr dfo =  " + dfo);
        try {
            DataObject dob = DataObject.find(dfo);
            DataEditorSupport des1 = dob.getLookup().lookup(DataEditorSupport.class);
            Util.out("     --- DataEditorSupport = " + des1);
            Util.out("     --- Document = " + des1.getDocument());
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }

        initComponents();

        jTopPanel = new JPanel();

        jTopPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 12));
        jTopPanel.setBackground(java.awt.Color.MAGENTA);

        jfxRootPanel = new JFXEditorPanel(lookup);
        jfxRootPanel.setOpaque(true);
        jfxRootPanel.setBorder(new LineBorder(java.awt.Color.BLUE, 8));
        jfxRootPanel.setPreferredSize(new Dimension(408, 308));
        jfxRootPanel.setBackground(java.awt.Color.red);

        jTopPanel.add(jfxRootPanel);

        ((JFXEditorPanel) jfxRootPanel).startResizer();

        Collection c = lookup.lookupAll(Object.class);

        c = formDataObject.getLookup().lookupAll(Object.class);
    }

    private void editorInit() {
        removeAll();

        JScrollPane scrollPane = new JScrollPane(jTopPanel);
        scrollPane.setBorder(null); // disable border, winsys will handle borders itself
        scrollPane.setViewportBorder(null); // disable also GTK L&F viewport border 
        scrollPane.getVerticalScrollBar().setUnitIncrement(5); // Issue 50054
        scrollPane.getHorizontalScrollBar().setUnitIncrement(5);
        add(scrollPane); // if not yet loaded, the canvas shows "Loading"        
    }

    @Override
    public String getName() {
        return "FxFormVisualElement";
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBackground(new java.awt.Color(0, 0, 0));
        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        setMinimumSize(new java.awt.Dimension(10, 10));
        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        return toolbar;
    }

    @Override
    public Action[] getActions() {
        return new Action[0];
    }

    @Override
    public Lookup getLookup() {
        return formDataObject.getLookup();
    }

    public void print() {
        WindowManager wm = WindowManager.getDefault();
        Frame fr = wm.getMainWindow();
        Util.out("#############################################");
        Util.out("FRAME class: " + fr.getClass().getName());
        Util.out("FRAME isActive: " + fr.isActive());
        Util.out("FRAME isFocusOwner: " + fr.isFocusOwner());
        Util.out("FRAME isFocusable: " + fr.isFocusable());
        Util.out("FRAME isFocusableWindow: " + fr.isFocusableWindow());
        Util.out("FRAME isFocused: " + fr.isFocused());

        Util.out("#############################################");

        Collection c = Utilities.actionsGlobalContext().lookupAll(Object.class);
        c.forEach(el -> {
            Util.out("GLOABAL: " + el.getClass().getName());
        });
        Set<TopComponent> comps = TopComponent.getRegistry().getOpened();
        for (TopComponent tc : comps) {
            Util.out("OPENED TC: " + tc.getDisplayName());
            Util.out("   --- isFocusOwner: " + tc.isFocusOwner());
            Util.out("   --- isFocusable: " + tc.isFocusable());

        }
        Util.out("ACTIVATED TC: " + TopComponent.getRegistry().getActivated().getDisplayName());
        TopComponent.getRegistry().getCurrentNodes();
        for (org.openide.nodes.Node tc : TopComponent.getRegistry().getCurrentNodes()) {
            Util.out("CURRENTNODE: " + tc.getDisplayName());
        }

    }

    @Override
    public void componentOpened() {
        Util.out("=== componentOpened");
        WindowManager wm = WindowManager.getDefault();
        Frame fr = wm.getMainWindow();
        TopComponent tp = WindowManager.getDefault().findTopComponent("FXPaletteTopComponent");
        Util.out("   --- 1. tp = " + tp);
        Util.out("   --- 2. tp = " + tp);
        WindowManager.getDefault().findMode(tp).dockInto(tp);
        //tp = new FXPaletteTopComponent();
        if ( tp.isOpened() ) {
            Util.out("   --- 3. tp not opened");
            tp.open();
        }

    }

    @Override
    public void componentClosed() {
        Util.out("=== componentClosed");
    }

    @Override
    public void componentShowing() {
        Util.out("=== componentShowing ");

        List<Tree> list = new ArrayList<>();
        if (!isAncestorOf(jTopPanel)) {
            DataEditorSupport des = getLookup().lookup(DataEditorSupport.class);

            if (des != null) {
                //FxCodeGenerator.inserStartTempCode(des.getDocument(), des.getDataObject().getPrimaryFile());
                //list = (List<Tree>) FxCodeGenerator.getDesignerTree(des.getDocument(), des.getDataObject().getPrimaryFile());
                Util.out("COMP SHOWING list.size() = " + list.size());
                list.forEach(t -> {
                    Util.out("LIST: TRRE = " + t);
                });
                //FileObject dfo = des.getDataObject().getPrimaryFile().getParent().getFileObject("Sanple01_design.java");
                //FileObject dfo = des.getDataObject().getPrimaryFile().getParent().getFileObject("Sample01.java");
                //File f = new File("D:\\JavaFX-Tests\\JavaApplication5\\src\\javaapplication5\\Sanple01_design.java");
                //FileObject dfo = FileUtil.toFileObject(f);
                FileObject dfo = des.getDataObject().getPrimaryFile();
                try {
                    DataObject dob = DataObject.find(dfo);
                    DataEditorSupport des1 = dob.getLookup().lookup(DataEditorSupport.class);
                    Util.out("Sample01_design DataEditorSupport = " + des1);
                    Util.out("Sample01_design Document = " + des1.getDocument());
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }

                //FxCodeGenerator.modifyDesignerTree(null, des.getDataObject().getPrimaryFile().getParent().getFileObject("Sanple01_design.java"), list);
                FxCodeGenerator.inserStartTempCode(des.getDocument(), dfo);
                try {

                    //des.saveDocument();
                    //FileObject fo = des.getDataObject().getPrimaryFile().getParent();
                    //des.saveAs(fo, des.getDataObject().getPrimaryFile().getName() + ".fxjava");
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
//            SwingUtilities.invokeLater(() -> {
            FileObject fo = des.getDataObject().getPrimaryFile();
            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(100);
                    if (isCompileOnSaveFinished(fo)) {
                        break;
                    }
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            editorInit();
            //          });
        }
    }

    public void componentShowing_OLD() {
        Util.out("=== componentShowing ");
        List<Tree> list = null;
        if (!isAncestorOf(jTopPanel)) {
            DataEditorSupport des = getLookup().lookup(DataEditorSupport.class);

            if (des != null) {
                //FxCodeGenerator.inserStartTempCode(des.getDocument(), des.getDataObject().getPrimaryFile());
                list = (List<Tree>) FxCodeGenerator.getDesignerTree(des.getDocument(), des.getDataObject().getPrimaryFile());
                Util.out("COMP SHOWING list.size() = " + list.size());
                list.forEach(t -> {
                    Util.out("LIST: TRRE = " + t);
                });
                //FileObject dfo = des.getDataObject().getPrimaryFile().getParent().getFileObject("Sanple01_design.java");
                File f = new File("D:\\JavaFX-Tests\\JavaApplication5\\src\\javaapplication5\\Sanple01_design.java");
                FileObject dfo = FileUtil.toFileObject(f);

                try {
                    DataObject dob = DataObject.find(dfo);
                    DataEditorSupport des1 = dob.getLookup().lookup(DataEditorSupport.class);
                    Util.out("Sample01_design DataEditorSupport = " + des1);
                    Util.out("Sample01_design Document = " + des1.getDocument());
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
                //FxCodeGenerator.modifyDesignerTree(null, des.getDataObject().getPrimaryFile().getParent().getFileObject("Sanple01_design.java"), list);
                FxCodeGenerator.modifyDesignerTree(null, dfo, list);
                try {

                    //des.saveDocument();
                    //FileObject fo = des.getDataObject().getPrimaryFile().getParent();
                    //des.saveAs(fo, des.getDataObject().getPrimaryFile().getName() + ".fxjava");
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
//            SwingUtilities.invokeLater(() -> {
            FileObject fo = des.getDataObject().getPrimaryFile();
            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(100);
                    if (isCompileOnSaveFinished(fo)) {
                        break;
                    }
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            editorInit();
            //          });
        }
    }

    protected boolean isCompileOnSaveFinished(FileObject fo) {
        boolean retval = false;
        try {
            ClassLoader pcl = FxClassPathUtils.getProjectClassLoader(fo);
            String className = Util.toClassName(fo);

            FxProjectClassReloader crl = new FxProjectClassReloader(pcl, className);
            Class clazz = crl.loadClass(className);

            Field f = clazz.getDeclaredField(DESIGN_MARKER);
            retval = true;
            Util.out("LOAD field = " + f);
        } catch (ClassNotFoundException | NoSuchFieldException | SecurityException ex) {
        }

        return retval;
    }

    @Override
    public void componentHidden() {
        Util.out("=== componentHidden");
    }

    @Override
    public void removeNotify() {
        System.err.println(" === removeNotify = ");
    }

    @Override
    public void componentActivated() {
        Util.out("=== componentActivated ");// + formStage + "; isShoing=" + isShowing());        

    }

    @Override
    public void componentDeactivated() {
        Util.out("=== componentDeactivated ");
    }

    @Override
    public UndoRedo getUndoRedo() {
        return UndoRedo.NONE;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        //Util.out("FxFormVisualElement.setMultiViewCallback = " + callback);
        this.callback = callback;
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

}

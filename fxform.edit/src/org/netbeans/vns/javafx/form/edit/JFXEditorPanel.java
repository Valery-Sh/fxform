/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.vns.javafx.form.edit;

import java.awt.Container;
import java.awt.Dimension;
import java.util.Collection;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.vns.javafx.dock.DockBorderPane;
import org.vns.javafx.dock.DockNode;
import org.vns.javafx.dock.DockSideBar;
import org.vns.javafx.dock.api.DockRegistry;

/**
 *
 * @author Valery
 */
public class JFXEditorPanel extends JFXPanel {

    private final FxFormDataObject formDataObject;
    private final JToolBar toolbar = new JToolBar();
//    private transient MultiViewElementCallback callback;
    private StackPane formNode;
    private DockBorderPane mainBorderPane;
    private Lookup lookup;
    private Scene scene = null;
    private JComponentResizer resizer;

    public JFXEditorPanel(Lookup lookup) {
        this.lookup = lookup;
        formDataObject = lookup.lookup(FxFormDataObject.class);
        assert formDataObject != null;
        initComponents();
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                initGUI();
            }
        });
        //  resizer = new JComponentResizer(this);
    }

    public void startResizer() {
        resizer = new JComponentResizer(this);
    }

    private void initGUI() {

        Platform.setImplicitExit(false);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                //Util.out("FxFormVisualElement Platform.runLater");
                initFX();

                Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
                /*                Dockable.initDefaultStylesheet(null);
                DataEditorSupport des = lookup.lookup(DataEditorSupport.class);
                FxCodeGenerator.removeStartTempCode(des.getDocument(), des.getDataObject().getPrimaryFile());
                try {
                    des.saveDocument();
                    Util.out("FxFormVisualElement SAVED !!!!!");
                } catch (IOException ex) {
                    Util.out("??????????? EXCEPTION");
                    Exceptions.printStackTrace(ex);
                }
                 */
            }
        });
    }

    private void initFX() {
        Util.out(" --- initFX");
        scene = createScene();
        setScene(scene);
        DockRegistry.register(scene.getWindow());

    }

    private Scene createScene() {
        Util.out(" --- createScene 1");
        mainBorderPane = new DockBorderPane();
        //scene = new Scene(new Button("PLACEHOLDER"), Color.AQUA);
        scene = new Scene(mainBorderPane, Color.AQUA);

        //formNode = new DockNode("To Edit");
        formNode = new StackPane();
        formNode.setStyle("-fx-background-color: green");
        DockNode sceneGraph = new DockNode("Node hierarchy");
        Button sgBtn1 = new Button("Scene Graph Btn");
        sceneGraph.setContent(sgBtn1);

        //Util.out("CREATE SCENE WINDOW = " + getScene().getWindow());
        //formNode.getProperties().put("fxdocking:dockable:scene", scene);
//        SwingUtilities.invokeLater(() -> {
            Node content = getSoureSceneGraph();
            Util.out("CONTENT = " + content);
//            Platform.runLater(() -> {
                formNode.getChildren().add(content);
  //          });
            

//        });
        mainBorderPane.setCenter(formNode);

        DockSideBar mainLeftSideBar = new DockSideBar();
        mainLeftSideBar.getItems().add(sceneGraph);

        mainBorderPane.setLeft(mainLeftSideBar);
        mainLeftSideBar.setOrientation(Orientation.VERTICAL);
        mainLeftSideBar.setRotation(DockSideBar.Rotation.DOWN_UP);
        mainLeftSideBar.setSide(Side.LEFT);

        Button btn1 = new Button("resize");
        Button btn2 = new Button("Button2");
        
        //formNode.setContent(btn1);        
        //((Pane)content).getChildren().addAll(btn1,text);
        //((Pane) content).getChildren().addAll(btn1);
        sgBtn1.setOnAction(a -> {
            Node c = getSoureSceneGraph();
            Util.out("CONTENT = " + c);
            formNode.getChildren().add(c);
            //formNode.setContent(content);            
        });
        btn1.setOnAction(a -> {
            //btn1.getScene().getWindow().setHeight(1000);
            Util.out("BEFORE RESIZE");
            Dimension oldDim = getPreferredSize();
            Dimension newDim = new Dimension((int) oldDim.getWidth() + 30, (int) oldDim.getHeight() + 30);
            setPreferredSize(newDim);
            resize();
            //print();
        });
        return (scene);
    }

    private void resize() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                /*                Dimension oldDim = getPreferredSize();
                Dimension newDim = new Dimension((int) oldDim.getWidth() + 30, (int) oldDim.getHeight() + 30);
                setPreferredSize(newDim);
                 */
                Util.out("NEW DIM = " + getPreferredSize());
                Util.out("parent = " + getParent());
                //TopComponent tc = WindowManager.getDefault().findTopComponent("FxFormVisual");

                //
                // Do not remember that the parent JPanel is assigned to ScrollPane  
                //
                Container viewPort = getParent().getParent();
                Util.out("parent.parent = " + viewPort);

                //viewPort.invalidate();
                //viewPort.repaint();
                //viewPort.revalidate();
                revalidate();
                //invalidate();
                //repaint();
            }
        });
    }

    public Lookup getLookup() {
        return lookup;
    }

    private Node getSoureSceneGraph() {
        //this.repaint();
        //this.revalidate();
        Node retval = null;
        FileObject fo = null;
        Collection<? extends FileObject> foCollection = getLookup().lookupAll(FileObject.class);
        for (FileObject o : foCollection) {
            if ("java".equals(o.getExt())) {
                fo = o;
                break;
            }
        }

        try {
            //String name = 
            //FxClassPathUtils.releaseFormClassLoader(fo);
            //Class clazz = FxClassPathUtils.loadClass("fx.Sample01", fo);
            //Class clazz = FxClassPathUtils.findClass("fx.Sample01");
            Class clazz = FxClassPathUtils.loadClass("fx.Sample01", fo);
//            clazz = FxClassPathUtils.loadClass("fx.Sample01", fo);
            
            retval = (Node) clazz.newInstance();
            //clazz = FxClassPathUtils.reloadClass("fx.Sample01", fo);
            //retval = (Node) clazz.newInstance();
            //((DockBorderPane) scene.getRoot()).setCenter(o);
            //((Group) scene.getRoot()).getChildren().add(o);
            System.err.println("addSceneGraph INSTANCE = " + retval);
            Util.out("   --- addSceneGraph INSTANCE = : " + retval);

//        getClass().getClassLoader().
        } catch (ClassNotFoundException ex) {
            Util.out("   --- addSceneGraph ClassNotFoundException = : " + ex.getMessage());
            Exceptions.printStackTrace(ex);
        } catch (InstantiationException ex) {
            Util.out("   --- addSceneGraph InstantiationException = : " + ex.getMessage());
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Util.out("   --- addSceneGraph IllegalAccessException = : " + ex.getMessage());
            Exceptions.printStackTrace(ex);
        }
        Util.out("   --- END getSourceSceneGraph retval = " + retval);
        return retval;
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        /*        JPanel jPanel1 = new javax.swing.JPanel();
        JButton jButton1 = new JButton();

        setBackground(new java.awt.Color(255, 204, 204));
        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        setMinimumSize(new java.awt.Dimension(10, 10));
        setLayout(new java.awt.BorderLayout());

        jPanel1.setBackground(new java.awt.Color(204, 255, 255));

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(FxFormVisualElement.class, "FxFormVisualElement.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1);

        add(jPanel1, java.awt.BorderLayout.CENTER);
         */
    }// </editor-fold>                        

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }

}

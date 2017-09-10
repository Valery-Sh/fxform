/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.vns.javafx.form.edit;

/**
 *
 * @author Valery
 */
import javax.swing.Action;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.api.java.loaders.JavaDataSupport;
import org.openide.actions.EditAction;
import org.openide.actions.OpenAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

import org.openide.util.actions.SystemAction;

/** The DataNode for Forms.
 *
 * @author Ian Formanek
 */
public class FxFormDataNode extends FilterNode {
    /** generated Serialized Version UID */
    //  static final long serialVersionUID = 1795549004166402392L;

    /** Icon base for form data objects. */
    @StaticResource
    private static final String FORM_ICON_BASE = "org/netbeans/vns/javafx/form/edit/resources/icon.png"; // NOI18N

    /** Constructs a new FormDataObject for specified primary file
     * 
     * @param fdo form data object
     */
    public FxFormDataNode(FxFormDataObject fdo) {
        this(JavaDataSupport.createJavaNode(fdo.getPrimaryFile()));
        Util.out("FxFormDataNode CONSTR fdo=" + fdo);
        //GuardedSectionManager gg;
    }
    
    private FxFormDataNode(Node orig) {
        super(orig);
        //Util.out("FxFormDataNode(Node orig) CONSTR fdo=" + orig);
        ((AbstractNode) orig).setIconBaseWithExtension(FORM_ICON_BASE);
    }
    
    @Override
    public Action getPreferredAction() {
        // issue 56351
        return new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                Util.out("FxFormDataNode actionperformed getCookie == " + getCookie(FxEditorSupport.class));
                //EditorSupport supp = (EditorSupport)getCookie(EditorSupport.class);                
                
                //supp.open();
                FxFormEditorSupport supp = (FxFormEditorSupport)getCookie(FxEditorSupport.class);
                supp.openFormEditor(false);
            }
        };
    }
    
    @Override
    public Action[] getActions(boolean context) {
        Action[] javaActions = super.getActions(context);
        Action[] formActions = new Action[javaActions.length+2];
        formActions[0] = SystemAction.get(OpenAction.class);
        formActions[1] = SystemAction.get(EditAction.class);
        formActions[2] = null;
        // Skipping the first (e.g. Open) action
        Util.out("FxFormDataNode getActions");
        
        System.arraycopy(javaActions, 1, formActions, 3, javaActions.length-1);
        return formActions;
    }

}


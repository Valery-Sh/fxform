/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.vns.javafx.form.edit;

import javax.swing.JEditorPane;
import org.openide.filesystems.FileObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiDataObject.Entry;
import org.openide.nodes.Node;

/**
 *
 * @author Valery
 */
public interface FxFormServices {
    JEditorPane createCodeEditorPane();
    Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile);
    boolean isLayoutExtensionsLibrarySupported();
    Node createFormDataNode(FxFormDataObject formDataObject);
    void setupCodeEditorPane(JEditorPane editor, FileObject srcFile, int ccPosition);
    Class<? extends FxEditorSupport> getEditorSupportClass(FxFormDataObject formDataObject);
    FxEditorSupport createEditorSupport(FxFormDataObject formDataObject);
}

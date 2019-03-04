/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.vns.javafx.form.edit;

import java.util.logging.Level;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.api.editor.DialogBinding;
import org.netbeans.api.java.loaders.JavaDataSupport;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiDataObject.Entry;
import org.openide.nodes.Node;
import org.openide.util.lookup.ServiceProvider;

/**
 * NetBeans implementation of {@code FormServices}.
 *
 * @author Jan Stola
 */
@ServiceProvider(service=FxFormServices.class)
public class FxFormServicesImpl implements FxFormServices {

    @Override
    public JEditorPane createCodeEditorPane() {
        //Util.out("=== FxFormServices.createCodeEditorPane");
        return new JEditorPane();
    }

    @Override
    public void setupCodeEditorPane(JEditorPane editor, FileObject srcFile, int ccPosition) {
        DataObject dob = null;
        try {
            dob = DataObject.find(srcFile);
        } catch (DataObjectNotFoundException dnfex) {
            //FormUtils.LOGGER.log(Level.INFO, dnfex.getMessage(), dnfex);
        }
        if (!(dob instanceof FxFormDataObject)) {
            //FormUtils.LOGGER.log(Level.INFO, "Unable to find FormDataObject for {0}", srcFile); // NOI18N
            return;
        }
        FxFormDataObject formDob = (FxFormDataObject)dob;
        Document document = formDob.getFormEditorSupport().getDocument();
        DialogBinding.bindComponentToDocument(document, ccPosition, 0, editor);

        // do not highlight current row
        editor.putClientProperty(
            "HighlightsLayerExcludes", //NOI18N
            "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$" //NOI18N
        );

        //FormUtils.setupTextUndoRedo(editor);
    }


/*    public ClassSource getCopiedBeanClassSource(Transferable transferable) {
        DataObject dobj = NodeTransfer.cookie(transferable, NodeTransfer.COPY, DataObject.class);
        FileObject fo = (dobj != null && dobj.isValid()) ? dobj.getPrimaryFile() : null;
        if (fo == null) {
            return null;
        }

        String clsName = BeanInstaller.findJavaBeanName(fo);
        if (clsName == null) {
            return null;
        }

        return BeanInstaller.getProjectClassSource(fo, clsName);
    }
*/
  
    @Override
    public Node createFormDataNode(FxFormDataObject formDataObject) {
        return new FxFormDataNode(formDataObject);
        //Util.out("=== FxFormServices.createFormDataNode fo=" + formDataObject);
        //return node;
    }


    @Override
    public Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
        //Util.out("=== FxFormServices.createPrimaryEntry");
        
        return JavaDataSupport.createJavaFileEntry(obj, primaryFile);
    }

    @Override
    public boolean isLayoutExtensionsLibrarySupported() {
        //Util.out("=== FxFormServices.isLayoutExtensionsLibrarySupported");
        
        return true;
    }
    

    @Override
    public Class<? extends FxEditorSupport> getEditorSupportClass(FxFormDataObject formDataObject) {
        return FxFormEditorSupport.class;
    }

    @Override
    public FxEditorSupport createEditorSupport(FxFormDataObject formDataObject) {
        return new FxFormEditorSupport(formDataObject);
    }
    
}

package org.netbeans.vns.javafx.form.edit;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.Lookup;

/** Loader for Forms. Recognizes file with extension .form and .java and with extension class if
 * there is their source and form file.
 *
 * @author Ian Formanek
 */
@MIMEResolver.ExtensionRegistration(
    displayName="FxFormResolver.xml",
    mimeType = "text/x-fxform",
    extension = {"fxform"},        
    position=137
)
public class FxFormDataLoader extends MultiFileLoader {
    /** The standard extensions of the recognized files */
    public static final String FORM_EXTENSION = "fxform"; // NOI18N
    /** The standard extension for Java source files. */
    public static final String JAVA_EXTENSION = "java"; // NOI18N

    static final long serialVersionUID =7359146057404524013L;
    /** Constructs a new FormDataLoader */
    public FxFormDataLoader() {
        super(FxFormDataObject.class.getName());
        //Util.out("FxFormDataLoader.CONSTR");
    }

    
    /** Gets default display name. Overides superclass method. */
    @Override
    protected String defaultDisplayName() {
        return org.openide.util.NbBundle.getBundle(FxFormDataLoader.class)
                 .getString("PROP_FormLoader_Name"); // NOI18N
    }

    @Override
    protected String actionsContext () {
        return "Loaders/text/x-java/Actions/"; // NOI18N
    }

    /** For a given file finds a primary file.
     * @param fo the file to find primary file for
     *
     * @return the primary file for the file or null if the file is not
     *   recognized by this loader
     */
    @Override
    protected FileObject findPrimaryFile(FileObject fo) {
	// never recognize folders.
        if (fo.isFolder()) return null;
        String ext = fo.getExt();
        //Util.out("FxFormDataLoader.findPrimaryFile fo " + fo);        
        
        if (ext.equals(FORM_EXTENSION)) {
           // Util.out("FxFormDataLoader.findPrimaryFile brother " + FileUtil.findBrother(fo, JAVA_EXTENSION));
            return FileUtil.findBrother(fo, JAVA_EXTENSION);
        }
        FileObject javaFile = findJavaPrimaryFile(fo);
       // Util.out("FxFormDataLoader.findPrimaryFile javaFile=" + javaFile);
        FileObject retval = javaFile != null
                    && FileUtil.findBrother(javaFile, FORM_EXTENSION) != null ?
            javaFile : null;
        //Util.out("FxFormDataLoader.findPrimaryFile retval = " + retval);        
        
        return retval ;
    }

    /** 
     * Creates the right data object for given primary file.
     * It is guaranteed that the provided file is actually a primary file
     * returned from the method findPrimaryFile.
     *
     * @param primaryFile the primary file
     * @return the data object for this file
     * @exception DataObjectExistsException if the primary file already has data object
     */
    @Override
    protected MultiDataObject createMultiObject(FileObject primaryFile)
        throws DataObjectExistsException
    {
       // Util.out("FxFormDataLoader.createMultiObject primaryFile=" + primaryFile);
        
        return new FxFormDataObject(FileUtil.findBrother(primaryFile, FORM_EXTENSION),
                                  primaryFile,
                                  this);
    }

    @Override
    protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
       // Util.out("FxFormDataLoader.createPrimaryEntry");        
        FxFormServices services = Lookup.getDefault().lookup(FxFormServices.class);
        MultiDataObject.Entry entry = services.createPrimaryEntry(obj, primaryFile);
        return entry;
    }

    private FileObject findJavaPrimaryFile(FileObject fo) {
       // Util.out("--- FxFormDataLoader.findJavaPrimaryFile");        
        
        if (fo.getExt().equals(JAVA_EXTENSION)) {
           // Util.out("FxFormDataLoader.findJavaPrimaryFile fo=" + fo);        
            return fo;
        }
        //Util.out("FxFormDataLoader.findJavaPrimaryFile fo= null" );        
        return null;
    }

    @Override
    protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj,
                                                         FileObject secondaryFile)
    {
       // Util.out("FxFormDataLoader.createSecondaryEntry");        
        
        assert FORM_EXTENSION.equals(secondaryFile.getExt());
        
        FileEntry formEntry = new FormEntry(obj, secondaryFile);
        ((FxFormDataObject)obj).formEntry = formEntry;
        return formEntry;
    }

    private static class FormEntry extends FileEntry {
        public FormEntry(MultiDataObject mdo, FileObject fo) {
            super(mdo, fo);
            //Util.out("FxFormDataLoader.FormEntry.CONSTR");        

        }

        @Override
        public FileObject createFromTemplate(FileObject folder, String name) throws IOException {
            //Util.out("FxFormDataLoader.FormEntry.createFromTemplate");        
            
            return FileUtil.copyFile(getFile(), folder, name);
        }
    }
}

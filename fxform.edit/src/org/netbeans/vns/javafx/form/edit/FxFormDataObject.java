package org.netbeans.vns.javafx.form.edit;
import java.io.IOException;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.SaveAsCapable;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.util.Lookup;

/**
 *
 * @author Valery
 */
public class FxFormDataObject extends MultiDataObject {
    transient private FxEditorSupport formEditor;
    transient private OpenEdit openEdit;

    /** The entry for the .form file */
    FileEntry formEntry;

    //--------------------------------------------------------------------
    // Constructors

    static final long serialVersionUID =-975322003627854168L;

    public FxFormDataObject(FileObject formFo, FileObject javaFo, FxFormDataLoader loader)
        throws DataObjectExistsException
    {
        super(javaFo, loader);
        //Util.out("FxFormDataObject CONSTR");
        formEntry = (FileEntry)registerEntry(formFo);
        getCookieSet().assign( SaveAsCapable.class, new SaveAsCapable() {
            @Override
            public void saveAs(FileObject folder, String fileName) throws IOException {
                Util.out("FxFormDataObject saveAs fileName = " + fileName);
                getFormEditorSupport().saveAs( folder, fileName );
            }
        });
    }
    private Class getFormEditorSupportClass() {
        //Util.out(" --- FxFormDataObject getFormEditorSupportClass --- ");
        
//        return null;
        Class clazz = Lookup.getDefault().lookup(FxFormServices.class).getEditorSupportClass(this);
        //Util.out("FxFormDataObject getFormEditorSupportClass");        
        return clazz;
    }

    public synchronized FxEditorSupport getFormEditorSupport() {
       // Util.out("!!!! FxFormDataObject getFormEditorSupport");
        if (formEditor == null) {
            FxFormServices services = Lookup.getDefault().lookup(FxFormServices.class);
            formEditor = services.createEditorSupport(this);
        }
        return formEditor;
    }

    //--------------------------------------------------------------------
    // Other methods

    @Override
    public <T extends Cookie> T getCookie(Class<T> type) {
        T retValue = null;
        //Util.out("--- FxFormDataObject getCookie ---- ");            
        if (OpenCookie.class.equals(type) || EditCookie.class.equals(type)) {
            ///Util.out("FxFormDataObject getCookie type = " + type);            
            if (openEdit == null)
                openEdit = new OpenEdit();
            retValue = type.cast(openEdit);
        } else if (!type.equals(Cookie.class) && type.isAssignableFrom(getFormEditorSupportClass())) {
            // Avoid calling synchronized getFormEditorSupport() when invoked from node lookup
            // initialization from cookies (asking for base Node.Cookie).
            retValue = (T) getFormEditorSupport();
        } else {
            retValue = super.getCookie(type);
        }
        return retValue;
    }
    
    @Override
    public Lookup getLookup() {
//Util.out("FxFormDataObject getLookup 1 ");                            
        Lookup retval = isValid() ? getNodeDelegate().getLookup() : Lookup.EMPTY;
//Util.out("FxFormDataObject getLookup = " + retval);                    
        return retval;
    }
    private class OpenEdit implements OpenCookie, EditCookie {
        @Override
        public void open() {
            //Util.out("FxFormDataObject.OpenEdit open()");
            // open form editor with form designer selected
            getFormEditorSupport().openDesign();
        }
        @Override
        public void edit() {
            //Util.out("FxFormDataObject.OpenEdit edit()");
            
            // open form editor with java editor selected (form not loaded)
            getFormEditorSupport().openSource();
        }
    }

    public FileObject getFormFile() {
//Util.out("FxFormDataObject getFormFile()");        
        return formEntry.getFile();
    }

    public boolean isReadOnly() {

        FileObject javaFO = getPrimaryFile();
        FileObject formFO = formEntry.getFile();
//Util.out("FxFormDataObject isReadOnly() " + (!javaFO.canWrite() || !formFO.canWrite()));                        
        return !javaFO.canWrite() || !formFO.canWrite();
    }

    public boolean formFileReadOnly() {
//Util.out("FxFormDataObject formFileReadOnly " + !formEntry.getFile().canWrite());                
        
        return !formEntry.getFile().canWrite();
    }

    public final CookieSet getCookies() {
//Util.out("FxFormDataObject getCookies = " + getCookieSet());                            
        return getCookieSet();
    }


    FileEntry getFormEntry() {
//Util.out("FxFormDataObject getFormEntry() = " + formEntry);                        
        return formEntry;
    }

    /** Provides node that should represent this data object. When a node for
     * representation in a parent is requested by a call to getNode(parent) it
     * is the exact copy of this node with only parent changed. This
     * implementation creates instance <CODE>DataNode</CODE>.  <P> This method
     * is called only once.
     *
     * @return the node representation for this data object
     * @see DataNode
     */
    @Override
    protected Node createNodeDelegate() {
        FxFormServices services = Lookup.getDefault().lookup(FxFormServices.class);
        //Util.out("FxFormDataObject createNodeDelegate() = " + formEntry);                        
        Node retval = services.createFormDataNode(this);;
        //Util.out("FxFormDataObject createNodeDelegate() node = " + retval);                                
        return retval;
    }

    //--------------------------------------------------------------------
    // Serialization

    private void readObject(java.io.ObjectInputStream is)
        throws java.io.IOException, ClassNotFoundException {
        is.defaultReadObject();
    }

    @Override
    protected DataObject handleCopyRename(DataFolder df, String name, String ext) throws IOException {
        FileObject fo = getPrimaryEntry().copyRename (df.getPrimaryFile (), name, ext);
        //Util.out("FxFormDataObject handleCopyRename() fo = " + fo);                                
        
        return DataObject.find( fo );
    }

}


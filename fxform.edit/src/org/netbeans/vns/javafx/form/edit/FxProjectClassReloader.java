package org.netbeans.vns.javafx.form.edit;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import org.openide.ErrorManager;

/**
 * A special (@code classLoader} capable to reload a specified class.
 *
 * @author Valery Shyshkin
 */

public class FxProjectClassReloader extends ClassLoader {

    private final ClassLoader projectClassLoader;
    private final String reloadClassName;
    
    public FxProjectClassReloader(ClassLoader projectClassLoader, String reloadClassName ) {
        super(projectClassLoader);
        this.projectClassLoader = projectClassLoader;
        this.reloadClassName = reloadClassName;
    }

    protected ClassLoader getProjectClassLoader() {
        return projectClassLoader;
    }
    
    @Override
    public Class loadClass(String name) throws ClassNotFoundException {
        if ( ! reloadClassName.equals(name)) {
            return super.loadClass(name);
        }
        Class c = null;
        String fileName = name.replace('.', '/').concat(".class"); // NOI18N
        
        URL url = projectClassLoader.getResource(fileName);
        
        if (url != null) {
            InputStream is = null;
            try {
                is = url.openStream();
                byte[] data = null;
                int first;
                int available = is.available();
                while ((first = is.read()) != -1) {
                    int length = is.available();
                    if (length != available) { // Workaround for issue 4401122
                        length++;
                    }
                    byte[] b = new byte[length];
                    b[0] = (byte) first;
                    int count = 1;
                    while (count < length) {
                        int read = is.read(b, count, length - count);
                        assert (read != -1);
                        count += read;
                    }
                    if (data == null) {
                        data = b;
                    }
                    else {
                        byte[] temp = new byte[data.length + count];
                        System.arraycopy(data, 0, temp, 0, data.length);
                        System.arraycopy(b, 0, temp, data.length, count);
                        data = temp;
                    }
                }
                int dot = name.lastIndexOf('.');
                if (dot != -1) { // Is there anything we should do for the default package?
                    String packageName = name.substring(0, dot);
                    
                    Package pckg = getPackage(packageName);
                    if (pckg == null) {
                        // PENDING are we able to determine the attributes somehow?
                        definePackage(packageName, null, null, null, null, null, null, null);
                    }
                }
                c = defineClass(name, data, 0, data.length);
            }
            catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ioex) {
                        // ignore
                    }
                }
            }
        }
        if (c == null) {
            throw new ClassNotFoundException(name);
        }
        return c;
    }

    @Override
    public URL getResource(String name) {
        return projectClassLoader != null ? projectClassLoader.getResource(name) : null;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return  projectClassLoader != null ? projectClassLoader.getResources(name) : null;
       
    }

}

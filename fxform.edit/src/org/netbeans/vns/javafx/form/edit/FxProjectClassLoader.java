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
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.editor.NbEditorUtilities;

import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.modules.ModuleInfo;
//import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 * A class loader loading user classes from given project (execution classpath
 * is used) with special care given to resources. When finding a resource, the
 * the project's sources are tried first (before execution classpath) to allow
 * components added to a form in this project to access resources without need
 * to build the project first. Even if built, the resources in sources take
 * precedence as they are likely more up-to-date.
 *
 * @author Tomas Pavek
 */

class FxProjectClassLoader extends ClassLoader {

    private final ClassLoader projectClassLoaderDelegate;
    private final ClassPath sources;
    private final ClassLoader systemClassLoader;
    private ClassLoader orgJDesktopLayoutClassLoader;

    private FxProjectClassLoader(ClassLoader projectClassLoaderDelegate, ClassPath sources) {
        
        this.projectClassLoaderDelegate = projectClassLoaderDelegate;
        this.sources = sources;
        this.systemClassLoader = org.openide.util.Lookup.getDefault().lookup(ClassLoader.class);
    }

    static ClassLoader getUpToDateClassLoader(FileObject fileInProject, ClassLoader clSoFar) {
        ClassLoader existingCL = clSoFar instanceof FxProjectClassLoader ?
                ((FxProjectClassLoader)clSoFar).projectClassLoaderDelegate : clSoFar;
        ClassPath classPath = ClassPath.getClassPath(fileInProject, ClassPath.EXECUTE);
        
        ClassLoader actualCL = classPath != null ? classPath.getClassLoader(true) : null;
        if (actualCL == existingCL)
            return clSoFar;
        if (actualCL == null)
            return null;
        return new FxProjectClassLoader(actualCL, ClassPath.getClassPath(fileInProject, ClassPath.SOURCE));
    }

    @Override
    protected Class findClass(String name) throws ClassNotFoundException {
        if (name.startsWith("org.apache.commons.logging.")) { // NOI18N HACK: Issue 50642
            try {
                ClassLoader classLoader = getCommonsLoggingClassLoader();
                if (classLoader != null) {
                    return classLoader.loadClass(name);
                }
            } catch (ClassNotFoundException cnfex) {
                // The logging classes are not in the IDE, we can use ProjectClassLoader
            }
        }
        Class c = null;
        if (FxClassPathUtils.getClassLoadingType(name) == FxClassPathUtils.SYSTEM_CLASS) {
            // This gets called if some class from user project needs a class that
            // is defined as system (example: a custom binding converter).
            // [Previously (5.5) this was used only as fallback if not found in
            // the project. Changed due to the beans binding. So now it is not
            // possible to load such a class from project. If we find a case
            // when the project class needs to be preferred over the system,
            // we'll need an additional category to SYSTEM_CLASS.]

            if (name.startsWith("org.jdesktop.layout")) { // NOI18N
                // See issues 135745 and 221685: the classes that this
                // classloader is able to load should be the same as the ones
                // loaded by systemClassLoader, but there shouldn't be the clash
                // with a copy of GroupLayout hacked by libs.ppawtlayout module.
                if (orgJDesktopLayoutClassLoader == null) {
                    FxFormServices services = Lookup.getDefault().lookup(FxFormServices.class);
                    orgJDesktopLayoutClassLoader = services.getClass().getClassLoader();
                }
                c = orgJDesktopLayoutClassLoader.loadClass(name);
            } else {
                c = systemClassLoader.loadClass(name);
            }
        } else {
            String filename = name.replace('.', '/').concat(".class"); // NOI18N
            URL url = projectClassLoaderDelegate.getResource(filename);
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
                        Package pakcage = getPackage(packageName);
                        if (pakcage == null) {
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
        }
        if (c == null)
            throw new ClassNotFoundException(name);
        return c;
    }

    @Override
    protected URL findResource(String name) {
        // In design time some resources added/changed by the user might not be propagated
        // to execution classpath yet (until the project is rebuilt), so not found by
        // custom components. That's why we prefer to look for them on sources classpath
        // first (bug 69377). An exception is use of @NbBundle.Messages annotations which
        // fills the properties file only in built results. If the same file is also
        // present in sources then it is incomplete and we should not use it (bug 238094).
        if ((!name.equals("Bundle.properties") && !name.endsWith("/Bundle.properties")) // NOI18N
                || !isProjectWithNbBundle()) {
            FileObject fo = sources.findResource(name);
            if (fo != null) {
                return fo.toURL();
            }
        }
        return projectClassLoaderDelegate.getResource(name);
    }

    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        Set<URL> urls = new HashSet<URL>();
        List<FileObject> fos = sources.findAllResources(name);
        for (FileObject fo : fos) {
            try {
                urls.add(fo.getURL());
            } catch (FileStateInvalidException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        Enumeration<URL> e = projectClassLoaderDelegate.getResources(name);
        while (e.hasMoreElements()) {
            urls.add(e.nextElement());
        }
        return Collections.enumeration(urls);
    }

    private boolean isProjectWithNbBundle() {
        return projectClassLoaderDelegate.getResource("org/openide/util/NbBundle.class") != null; // NOI18N
    }

    private ClassLoader commonsLoggingClassLoader;
    private boolean commonsLoggingClassLoaderSearched = false;
    
    private ClassLoader getCommonsLoggingClassLoader() {
        if (!commonsLoggingClassLoaderSearched) {
            for (ModuleInfo info : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
                if (info.getCodeName().startsWith("o.apache.commons.logging")) { // NOI18N
                    commonsLoggingClassLoader = info.getClassLoader();
                    break;
                }
            }
            commonsLoggingClassLoaderSearched = true;
        }
        return commonsLoggingClassLoader;
    }

}

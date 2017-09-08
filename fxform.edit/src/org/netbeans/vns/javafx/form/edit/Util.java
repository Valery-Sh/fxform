/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.vns.javafx.form.edit;

import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Valery
 */
public class Util {
    public static void out(String msg) {
        InputOutput io = IOProvider.getDefault().getIO("ShowMessage", false);
        io.getOut().println(msg);
        io.getOut().close();
    }
    public static String toClassName(FileObject fo) {
            String className  = ClassPath
                    .getClassPath(fo, ClassPath.SOURCE)
                    .getResourceName(fo);
            className = className
                    .substring(0, className.length() - ".java".length())
                    .replace("\\", "/")
                    .replace("/", ".");
            return className;
    }
}

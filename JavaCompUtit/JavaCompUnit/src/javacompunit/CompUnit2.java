/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javacompunit;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Trees;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

/**
 *
 * @author Valery
 */
public class CompUnit2 {
    public static void main(String[] args) throws IOException {
/*        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        File f = new File("d:\\JavaFX-Tests\\JavaCompUtit\\JavaCompUnit\\src\\javacompunit\\TestCodeGen.java");
        boolean ex = f.exists();
        JavaSourceUtil so = new JavaSourceUtil(new File("d:\\JavaFX-Tests\\JavaCompUtit\\JavaCompUnit\\src\\javacompunit\\TestCodeGen.java"));
        List<JavaFileObject> compilationUnits
                = Collections.<JavaFileObject>singletonList(so);
        JavacTask task = (JavacTask) javac.getTask(null, null, null, null, null,
                compilationUnits);
        Trees trees = Trees.instance(task);
        CompilationUnitTree toplevel = task.parse().iterator().next();
        ClassTree classTree = (ClassTree) toplevel.getTypeDecls().get(0);
        System.out.println("ClassTree = " + classTree);
        Tree tree = ((ClassTree) toplevel.getTypeDecls().get(0)).getMembers().get(0);
        long pos = trees.getSourcePositions().getStartPosition(toplevel, tree);
        if (pos != 13) {
            throw new AssertionError(String.format("Start pos for %s is incorrect (%s)!",
                    tree, pos));
        }
        pos = trees.getSourcePositions().getEndPosition(toplevel, tree);
        if (pos != 23) {
            throw new AssertionError(String.format("End pos for %s is incorrect (%s)!",
                    tree, pos));
        }
*/
    }
    
}

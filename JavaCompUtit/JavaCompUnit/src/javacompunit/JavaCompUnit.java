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
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.Name;
import javax.lang.model.util.Elements;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;


/**
 *
 * @author Valery
 */
public class JavaCompUnit {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        List<JavaFileObject> compilationUnits
                = Collections.<JavaFileObject>singletonList(new CompUnit1());
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
    }

    public static void mainElem(String[] args) throws IOException {
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        List<JavaFileObject> compilationUnits
                = Collections.<JavaFileObject>singletonList(new CompUnit1());
        JavacTask task = (JavacTask) javac.getTask(null, null, null, null, null,
                compilationUnits);
        Elements elems = task.getElements();
        Trees trees = Trees.instance(task);
        CompilationUnitTree toplevel = task.parse().iterator().next();

        
        ClassTree classTree = (ClassTree) toplevel.getTypeDecls().get(0);
        System.out.println("ClassTree = " + classTree);
        Tree tree = ((ClassTree) toplevel.getTypeDecls().get(0)).getMembers().get(0);
        TreePath path = TreePath.getPath(toplevel, tree);
        Iterator<Tree> iterator = path.iterator();   
        while( iterator.hasNext() ) {
            Tree t = iterator.next();
        }
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
    }
    
}

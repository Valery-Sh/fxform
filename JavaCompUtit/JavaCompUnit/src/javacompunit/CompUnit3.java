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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject.Kind;

import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
/**
 *
 * @author Valery
 */
public class CompUnit3 {

    public static void main(String[] args) throws IOException {
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = javac.getStandardFileManager(
                diagnosticCollector, null, null);

        StringWriter compilerOut = new StringWriter();

/*        List<String> options = ImmutableList.of("-sourcepath",
                tmpDir.getPath(), "-d", tmpDir.getPath(), "-processor",
                TestProcessor.class.getName(), "-Xlint");
*/
        List<String> options = new ArrayList<>();
        options.add("-sourcepath");
        options.add("d:\\JavaFX-Tests\\JavaCompUtit\\JavaCompUnit\\src\\javacompunit\\");
        options.add("-d");
        options.add("d:\\JavaFX-Tests\\JavaCompUtit\\JavaCompUnit\\src\\javacompunit\\");
        
        javac.getTask(compilerOut, fileManager, diagnosticCollector,
                options, null, null);
        // This doesn't compile anything but communicates the paths to the JavaFileManager.

        JavaFileObject sourceFile = fileManager.getJavaFileForInput(
                StandardLocation.SOURCE_PATH, "TestCodeGen",
                Kind.SOURCE);
        List<JavaFileObject> compilationUnits
                = Collections.<JavaFileObject>singletonList(sourceFile);
        // Compile the empty source file to trigger the annotation processor.
        // (Annotation processors are somewhat misnamed because they run even on classes with no
        // annotations.)
        List<String> list1 = new ArrayList<>();
        list1.add("TestCodeGen");
        List<JavaFileObject> list2 = new ArrayList<>();
        list1.add("sourceFile");
        
/*        JavaCompiler.CompilationTask javacTask = javac.getTask(compilerOut,
                fileManager, diagnosticCollector, options,
                list1,
                list2);
*/        
        JavacTask task = (JavacTask) javac.getTask(compilerOut, fileManager, diagnosticCollector, options, 
                list1, compilationUnits);        

        Trees trees = Trees.instance(task);
        CompilationUnitTree toplevel = task.parse().iterator().next();
        System.out.println("topLevel = " + toplevel);
//        boolean compiledOk = javacTask.call();
    }

}

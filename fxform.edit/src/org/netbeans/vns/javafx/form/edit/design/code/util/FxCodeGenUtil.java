/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.vns.javafx.form.edit.design.code.util;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.DataEditorSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author Valery
 */
public class FxCodeGenUtil {

    private final Document doc;
    private TreeMaker make;
    private WorkingCopy wCopy;
    private ClassTree classTree;

    public FxCodeGenUtil(Document doc) {
        this.doc = doc;
        init();
    }

    public int[] getInitPosition(List<StatementTree> stList, String fieldName) {
        int[] pos = new int[]{-1, -1};

        return pos;
    }

    public void init() {

        JavaSource javaSource = JavaSource.forDocument(doc);

        CancellableTask task = new CancellableTask<WorkingCopy>() {
            @Override
            public void run(WorkingCopy workingCopy) throws IOException {
                wCopy = workingCopy;
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                make = workingCopy.getTreeMaker();

                for (Tree typeDecl : cut.getTypeDecls()) {
                    /*if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        classTree = (ClassTree) typeDecl;
                                                MethodTree mt = findMethodInvocation(make, classTree, "initComponents");

                        if (mt == null) {
                            return;
                        }
                        BlockTree methodBody = mt.getBody();
                        List stList = methodBody.getStatements();
                        int[] pos = getInitPosition(stList, "button1");
                        //AnnotationTree newAnnotation = treeMaker.Annotation(
                        //treeMaker.QualIdent("com.acme.InsertedAnnotation"),
                        //Collections.singletonList(commentTree)); 

                        //BlockTree constrBody = mt.getBody();
                        //BlockTree modifiedBody = make.removeBlockStatement(constrBody, 1);
                        //workingCopy.rewrite(constrBody, modifiedBody);
                    }//if
                     */
                } //for  

            }//run

            public void cancel() {
            }

        };//task

        try {
            ModificationResult result = javaSource.runModificationTask(task);
            result.commit();

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }//removeStartTempCode

    public static MethodTree findNoArgsConstructor(TreeMaker make, ClassTree clazz) {
        MethodTree retval = null;
        List<? extends Tree> list = clazz.getMembers();
        Tree t = null;
        for (int i = 0; i < list.size(); i++) {
            t = list.get(i);
            if (t.getKind() == Tree.Kind.METHOD) {
                MethodTree mt = (MethodTree) t;
                if (mt.getName().contentEquals("<init>") && mt.getParameters().isEmpty()) {
                    retval = mt;
                    break;
                }
            }
        }
        return retval;
    }

    public static MethodTree findMethod(TreeMaker make, ClassTree clazz, String methodName) {
        MethodTree retval = null;
        
        List<? extends Tree> list = clazz.getMembers();
        Tree t = null;
        for (int i = 0; i < list.size(); i++) {
            t = list.get(i);
            if (t.getKind() == Tree.Kind.METHOD) {
                MethodTree mt = (MethodTree) t;
                if (mt.getName().contentEquals(methodName) && mt.getParameters().isEmpty()) {
                    retval = mt;
                    break;
                }
            }
        }
        return retval;
    }

    public TreeMaker getMaker() {
        return make;
    }

    public WorkingCopy getWorkingCopy() {
        return wCopy;
    }

    public ClassTree getClassTree() {
        return classTree;
    }
    public static Document getDocument(File f) {
        boolean b = f.exists();
        FileObject dfo = FileUtil.toFileObject(f);
        Document doc = null;
        try {
            DataObject dob = DataObject.find(dfo);
            
            DataEditorSupport des = dob.getLookup().lookup(DataEditorSupport.class);
            System.err.println("Sample01_design DataEditorSupport = " + des);
            System.err.println("Sample01_design Document = " + des.getDocument());
            doc = des.getDocument();
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return doc;
    }
}

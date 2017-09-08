package org.netbeans.vns.javafx.form.edit;

import com.sun.source.doctree.TextTree;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.DocTrees;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.geometry.Side;
import javax.lang.model.element.Modifier;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.vns.javafx.demo.design.code.DockPaneCodeGenerator;
import org.netbeans.vns.javafx.demo.design.code.DockUndockCodeGenerator;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Valery
 */
public class FxCodeGenerator {

    public static final String DESIGN_MARKER = "nbdesign_visualcomponent";

    public static void removeStartTempCode(Document doc, FileObject fo) {

        JavaSource javaSource = JavaSource.forDocument(doc);

        CancellableTask task = new CancellableTask<WorkingCopy>() {
            @Override
            public void run(WorkingCopy workingCopy) throws IOException {

                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        MethodTree mt = findNoArgsConstructor(make, clazz);

                        if (mt == null) {
                            return;
                        }
                        IdentifierTree commentTree = make.Identifier("/* ... */");
                        //AnnotationTree newAnnotation = treeMaker.Annotation(
                        //treeMaker.QualIdent("com.acme.InsertedAnnotation"),
                        //Collections.singletonList(commentTree)); 

                        BlockTree constrBody = mt.getBody();
                        BlockTree modifiedBody = make.removeBlockStatement(constrBody, 1);
                        workingCopy.rewrite(constrBody, modifiedBody);
                    }//if
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

    public static VariableTree getVariable(String varName, ClassTree ct) {
        List<? extends Tree> list = ct.getMembers();
        VariableTree retval = null;
        for (Tree t : list) {
            if (t instanceof VariableTree) {
                VariableTree vt = (VariableTree) t;
                if (vt.getName().contentEquals(varName)) {
                    retval = vt;
                    break;
                }
            }
        }
        return retval;
    }

    public static IfTree getStartTempIfTree(MethodTree constr) {
        IfTree retval = null;
        BlockTree constrBody = constr.getBody();
        if (constrBody.getStatements().size() <= 1 || !(constrBody.getStatements().get(1) instanceof IfTree)) {
            return null;
        }
        retval = (IfTree) constrBody.getStatements().get(1);

        if (retval.getCondition().getKind() != Tree.Kind.PARENTHESIZED) {
            return null;
        }

        ParenthesizedTree pt = (ParenthesizedTree) retval.getCondition();
        if (pt.getExpression().getKind() != Tree.Kind.BOOLEAN_LITERAL) {
            return null;
        }
        LiteralTree lt = (LiteralTree) pt.getExpression();
        if (!lt.getValue().equals(true)) {
            return null;
        }
        StatementTree st = retval.getThenStatement();
        if (st == null || st.getKind() != Tree.Kind.BLOCK) {
            return null;
        }
        BlockTree bt = (BlockTree) st;
        if (bt.getStatements().size() != 2) {
            return null;
        }

        if (bt.getStatements().get(0).getKind() != Tree.Kind.EXPRESSION_STATEMENT
                || bt.getStatements().get(1).getKind() != Tree.Kind.RETURN) {
            return null;
        }
        ExpressionStatementTree es = (ExpressionStatementTree) bt.getStatements().get(0);
        if (es.getExpression().getKind() != Tree.Kind.METHOD_INVOCATION) {
            //Util.out("GEN CODE: *** 1 " + es.getExpression().getKind());

            return null;
        }
        MethodInvocationTree mit = (MethodInvocationTree) es.getExpression();
        if (mit.getMethodSelect().getKind() != Tree.Kind.IDENTIFIER) {
            //Util.out("GEN CODE: *** 2 " + mit.getMethodSelect().getKind());
            return null;
        }
        if (!((IdentifierTree) mit.getMethodSelect()).getName().contentEquals("initComponents")) {
            //Util.out("GEN CODE: *** 3 " + ((IdentifierTree) mit.getMethodSelect()).getName());

            return null;
        }
        return retval;
    }

    public  static void inserStartTempCode(Document doc, FileObject fo) {
        JavaSource javaSource = JavaSource.forDocument(doc);

        CancellableTask task = new CancellableTask<WorkingCopy>() {
            @Override
            public void run(WorkingCopy workingCopy) throws IOException {

                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                
                //workingCopy.getTreeUtilities().ElementUtilities().;

                for (Tree typeDecl : cut.getTypeDecls()) {

                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree clazz = (ClassTree) typeDecl;

                        if (getVariable(DESIGN_MARKER, clazz) == null) {
                            VariableTree design = make.Variable(make.Modifiers(
                                    Collections.<Modifier>singleton(Modifier.PUBLIC),
                                    Collections.<AnnotationTree>emptyList()),
                                    DESIGN_MARKER,
                                    make.Identifier("String"),
                                    null
                            );

                            clazz = make.addClassMember(clazz, design);

                        }
                        MethodTree mt = findNoArgsConstructor(make, clazz);
                        if (mt == null) {
                            return;
                        }
                        BlockTree bt_ = mt.getBody();
                        //Util.out("+++ BlockTree = " + bt_);

                        //bt_.getStatements().forEach(s -> {
                        //Comment comment = Comment.create(Comment.Style.LINE, -2, -2,
                        //        -2, "TestCreator.variantMethods.defaultComment");
                        //make.addComment(s, comment, true);

                        //Util.out("+++ st = " + s);
                        //Util.out("   ---  kind = " + s.getKind());
                        //Util.out("   ---  class = " + s.getClass().getName());
                        //});
                        Tree ifTree = getStartTempIfTree(mt);
                        if (ifTree != null) {
                                                        Util.out("### ******* NOT NULL ");

                            List<Comment> coms = workingCopy.getTreeUtilities().getComments(ifTree, true);
                            coms.forEach(c -> {
                                Util.out("### c = " + c);
                                Util.out("   ---  = text " + c.getText());
                                Util.out("   ---  = isDocument " + c.isDocComment());
                                Util.out("   ---  = isNew = " + c.isNew());
                                Util.out("----------------------------------------");

                            });
                            
                        }
                        if (getStartTempIfTree(mt) == null) {
                            BlockTree constrBody = mt.getBody();
                            MethodInvocationTree mit = make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.Identifier("initComponents"), Collections.<ExpressionTree>emptyList());
                            ExpressionStatementTree est = make.ExpressionStatement(mit);
                            constrBody.getStatements().forEach(s -> {
                                
                                List<Comment> coms = workingCopy.getTreeUtilities().getComments(s, false);
                                coms.forEach(c -> {
                                    Util.out("**** statement = " + s);
                                    Util.out("   ---  = text " + c.getText());
                                    Util.out("   ---  = isDocument " + c.isDocComment());
                                    Util.out("   ---  = isNew = " + c.isNew());
                                    Util.out("----------------------------------------");

                                });

                            });

                            ReturnTree rt = make.Return(null);

                            Comment comment = Comment.create(Comment.Style.LINE, -2, -2,
                                    -2, "TestCreator.variantMethods.defaultComment");
                            make.addComment(rt, comment, false);
                            List<StatementTree> list = new ArrayList<>();
                            list.add(est);
                            list.add(rt);

                            BlockTree bt = make.Block(list, false);

                            IfTree ift = make.If(make.Literal(new Boolean(true)), bt, null);

                            BlockTree modifiedBody = make.insertBlockStatement(constrBody, 1, ift);
                            workingCopy.rewrite(constrBody, modifiedBody);
                            workingCopy.rewrite((ClassTree) typeDecl, clazz);
                        }
                    }//if
                } //for  

            }//run

            @Override
            public void cancel() {
            }

        };//task

        try {
            ModificationResult result = javaSource.runModificationTask(task);
            result.commit();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//inserTempCode

    public static MethodInvocationTree findMethodInvocation(BlockTree tree, String methodName) {
        MethodInvocationTree retval = null;
        List<? extends StatementTree> mlist = tree.getStatements();
        for (StatementTree st : mlist) {
            if (Tree.Kind.EXPRESSION_STATEMENT == st.getKind()) {
                ExpressionStatementTree es = (ExpressionStatementTree) st;
                if (es.getExpression().getKind() == Tree.Kind.METHOD_INVOCATION) {
                    MethodInvocationTree mit = (MethodInvocationTree) es.getExpression();
                    if (methodName.equals(mit.getMethodSelect().toString()) || !mit.getArguments().isEmpty()) {
                        retval = mit;
                        break;
                    }
                }
            }
        }
        return retval;
    }

    public static List<Tree> getDesignerTree(Document doc, FileObject fo) {
        List<Tree> retval = new ArrayList<>();

        JavaSource javaSource = JavaSource.forDocument(doc);

        CancellableTask task = new CancellableTask<WorkingCopy>() {
            @Override
            public void run(WorkingCopy workingCopy) throws IOException {

                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                //TreeMaker make = workingCopy.getTreeMaker();

                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        MethodTree mt = findMethodTree(clazz, "initComponents");
                        if (mt != null) {
                            retval.add(mt);
                        }
                        break;
                    }//if
                } //for  

            }//run

            @Override
            public void cancel() {
            }

        };//task

        try {
            ModificationResult result = javaSource.runModificationTask(task);
            //result.commit();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return retval;
    }

    public static void modifyDesignerTree(Document doc, FileObject fo, List<Tree> list) {

        JavaSource javaSource = JavaSource.forFileObject(fo);
        //Util.out("modifyDesignerTree javaSource = " + javaSource);

        CancellableTask task = new CancellableTask<WorkingCopy>() {
            @Override
            public void run(WorkingCopy workingCopy) throws IOException {

                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                List stList = new ArrayList<>();

                ExpressionStatementTree es_ = make.ExpressionStatement(make.Identifier("pane1.getItems().add((3, btn1); // CCCCC22222"));
                TextTree tt = make.Text("//field: ; //prp[erty:");
                //LiteralTree ct = make.Code(tt);
                ExpressionStatementTree cm1 = make.ExpressionStatement(make.Identifier("//field:"));
                ExpressionStatementTree cm2 = make.ExpressionStatement(make.Identifier("//property:"));
                //StatementTree st_ = (StatementTree) make.Identifier("pane.1.getItems().add((3, btn1);");
                //make.MethodInvocation(stList, method, stList)
                stList.add(cm1);
                stList.add(cm2);
                stList.add(es_);

                BlockTree bt_ = make.Block(stList, false);
                list.add(bt_);
                list.add(es_);
                DockUndockCodeGenerator dcg = new DockPaneCodeGenerator(null, "myDockPane");
                StatementTree dst = dcg.Dock(make, null, "myDockPane", Side.LEFT, null, "sideNode");
                list.add(dst);
                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (Tree.Kind.CLASS == typeDecl.getKind()) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        ClassTree modifiedClassTree = clazz;
                        for (int i = 0; i < list.size(); i++) {
                            modifiedClassTree = make.addClassMember(modifiedClassTree, list.get(i));
                            workingCopy.rewrite((ClassTree) typeDecl, modifiedClassTree);
                            //Util.out("modifiedClassTree = " + modifiedClassTree);
                        }
                        break;
                    }//if
                } //for  

            }//run

            @Override
            public void cancel() {
            }

        };//task

        try {
            ModificationResult result = javaSource.runModificationTask(task);
            result.commit();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static MethodTree findMethodTree(ClassTree classTree, String methodName) {
        MethodTree retval = null;
        for (Tree member : classTree.getMembers()) {
            if (member.getKind() == Tree.Kind.METHOD) {
                MethodTree mt = (MethodTree) member;
                if (mt.getName().contentEquals(methodName)) {
                    retval = mt;
                    break;
                }
            }
        }
        return retval;
    }

    public static int indexOfMethodInvocation(BlockTree tree, String methodName) {
        int retval = -1;
        MethodInvocationTree methodTree = null;
        List<? extends StatementTree> mlist = tree.getStatements();
        for (int i = 0; i < mlist.size(); i++) {
            StatementTree st = mlist.get(i);
            //for (StatementTree st : mlist) {
            if (Tree.Kind.EXPRESSION_STATEMENT == st.getKind()) {
                ExpressionStatementTree es = (ExpressionStatementTree) st;
                if (es.getExpression().getKind() == Tree.Kind.METHOD_INVOCATION) {
                    MethodInvocationTree mit = (MethodInvocationTree) es.getExpression();
                    if (methodName.equals(mit.getMethodSelect().toString()) || !mit.getArguments().isEmpty()) {
                        retval = i;
                        break;
                    }
                }
            }
        }
        return retval;
    }

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

}

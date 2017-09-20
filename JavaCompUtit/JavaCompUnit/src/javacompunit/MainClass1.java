/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javacompunit;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.lang.model.element.Name;
import jdk.nashorn.internal.ir.ExpressionStatement;

/**
 *
 * @author Valery
 */
public class MainClass1 {

    public static void main_OLD(String[] args) throws IOException {
        File file = new File("d:\\JavaFX-Tests\\JavaCompUtit\\JavaCompUnit\\src\\javacompunit\\TestCodeGen");
        CompilationUnitTree tree = JavaSourceUtil.unitFor(file.getParentFile(), "TestCodeGen");
        
        //System.out.println("TREE = " + tree);
        ClassTree classTree = JavaSourceUtil.findClassTree(tree);
        MethodTree mt = JavaSourceUtil.findMethodTree(classTree, "initComponents");
        BlockTree body = mt.getBody();
        List<? extends StatementTree> stList = body.getStatements();
        for (int i = 0; i < stList.size(); i++) {
            StatementTree stTree = stList.get(i);
            
            System.out.println("StatementYree = " + stList.get(i).getKind());
            //List<Comment> comments = Comment.
            if (stTree.getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
                ExpressionStatementTree es = (ExpressionStatementTree) stTree;
                ExpressionTree et = es.getExpression();
                System.out.println("  +++ " + et.getKind());
                if (et.getKind() == Tree.Kind.ASSIGNMENT) {
                    AssignmentTree at = (AssignmentTree) et;
                    System.out.println("  --- getVariable() = " + at.getVariable().getKind());
                    if (at.getVariable().getKind() == Tree.Kind.IDENTIFIER) {
                        IdentifierTree it = (IdentifierTree) at.getVariable();
                        System.out.println("  --- identifier.name = " + it.getName());
                    }
                } else if (et.getKind() == Tree.Kind.METHOD_INVOCATION) {
                    System.out.println("=== METHOD INVOCATION");
                    MethodInvocationTree mit = (MethodInvocationTree) et;
                    List<? extends ExpressionTree> methodArgs = mit.getArguments();
                    System.out.println("   --- size = " + methodArgs.size());
                    if (methodArgs.size() == 1) {
                        ExpressionTree mst = mit.getMethodSelect();
                        System.out.println("  --- methodSelect.kind = " + mst.getKind());
                        //
                        // mst may be of Kind: MEMBER_SELECT or IDENTIFIER
                        // button1.setText() or a.b.c.setText() - both of kind == MEMBER_SELECT
                        // setText("txt") - IDENTIFIER
                        //
                        if ( mst.getKind() == Tree.Kind.MEMBER_SELECT) {
                            MemberSelectTree ms = (MemberSelectTree) mst; 
                            //
                            // Now get method name/ It's ms.getIdentifier()
                            //
                            Name methodName = ms.getIdentifier();
                            System.out.println("  --- memberSelect.kind = " + ms.getKind());    
                            System.out.println("  --- memberSelect.identifier = " + methodName);    
                            //
                            // Now get the  expression on the left of methodName
                            //
                            ExpressionTree msLeft = ms.getExpression();
                            System.out.println("msLeft.kind = " + msLeft.getKind());
                            //
                            // 1. button1.setText() => IDENTIFIER
                            // 2. a.b.button1.setText() or a.b() or a().b() etc. => MemberSelect
                            //
                            if ( msLeft.getKind() == Tree.Kind.IDENTIFIER ) {
                                IdentifierTree it = (IdentifierTree) msLeft;
                                String fieldName = it.getName().toString();
                                System.out.println("fieldName = " + fieldName);
                            }
                        }
                    }
                }
            } else if (stTree.getKind() == Tree.Kind.LABELED_STATEMENT) {
                LabeledStatementTree lst = (LabeledStatementTree) stTree;
                System.out.println("lst.getLabel = " + lst.getLabel());
                System.out.println("lst.getStatement = " + lst.getStatement().getKind());
            }

        }
    }
    public static void main(String[] args) throws IOException {
        File file = new File("d:\\JavaFX-Tests\\JavaCompUtit\\JavaCompUnit\\src\\javacompunit\\TestCodeGen");
        CompilationUnitTree tree = JavaSourceUtil.unitFor(file.getParentFile(), "TestCodeGen");
        
        //System.out.println("TREE = " + tree);
        ClassTree classTree = JavaSourceUtil.findClassTree(tree);
        MethodTree mt = JavaSourceUtil.findMethodTree(classTree, "initComponents");
        BlockTree body = mt.getBody();
        List stList = body.getStatements();
        int[] pos = new int[]{-1,-1};
        pos = JavaSourceUtil.getPropertyPosition(stList, "button1", "text");
        System.out.println("text PROP pos0 = " + pos[0] + "; pos1 = " + pos[1]);
        //pos = JavaSourceUtil.getFieldInitPosition(stList, "button1");
        //System.out.println("FIELD INIT pos0 = " + pos[0] + "; pos1 = " + pos[1]);
        
        //pos = JavaSourceUtil.getAddParentPosition(stList, "button1", "vbox1");
        //System.out.println("ADD PARENT pos0 = " + pos[0] + "; pos1 = " + pos[1]);
        
        
    }    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javacompunit;

import com.sun.source.tree.AssignmentTree;
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
import com.sun.source.util.JavacTask;
import com.sun.source.util.Trees;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Name;
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
public class JavaSourceUtil {
    public static final String PROPERTIES_LABEL = "properties";
    public static final String END_PROPERTIES_LABEL = "end_properties";
    public static final String END_LABEL = "end";
    public static final String PROP_LABEL_PREFIX = "property_";    
    public static final String INIT_LABEL_PREFIX = "init_";    
    public static final String MODIFYPARENT_LABEL_PREFIX = "modifyparent_";    
    
    public static CompilationUnitTree unitFor(File sourceDir, String className) throws IOException {
        CompilationUnitTree retval = null;
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
        options.add(sourceDir.getPath());
        options.add("-d");
        options.add(sourceDir.getPath());

        javac.getTask(compilerOut, fileManager, diagnosticCollector,
                options, null, null);
        // This doesn't compile anything but communicates the paths to the JavaFileManager.

        JavaFileObject sourceFile = fileManager.getJavaFileForInput(
                StandardLocation.SOURCE_PATH, className,
                Kind.SOURCE);

        List<JavaFileObject> compilationUnits
                = Collections.<JavaFileObject>singletonList(sourceFile);
        // Compile the empty source file to trigger the annotation processor.
        // (Annotation processors are somewhat misnamed because they run even on classes with no
        // annotations.)
        List<String> list1 = new ArrayList<>();
        list1.add(className);
        List<JavaFileObject> list2 = new ArrayList<>();
        list1.add("sourceFile");

        /*        JavaCompiler.CompilationTask javacTask = javac.getTask(compilerOut,
                fileManager, diagnosticCollector, options,
                list1,
                list2);
         */
//        JavacTask task = (JavacTask) javac.getTask(compilerOut, fileManager, diagnosticCollector, options, list1,
//                compilationUnits);
        JavacTask task = (JavacTask) javac.getTask(compilerOut, fileManager, null, options, list1,
                compilationUnits);
        Trees trees = Trees.instance(task);
        retval = task.parse().iterator().next();
        System.out.println("topLevel = " + retval);
//        boolean compiledOk = javacTask.call();
        
        return retval;
    }

    public static MethodTree findMethodTree(ClassTree clazz, String methodName) {
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

    public static ClassTree findClassTree(CompilationUnitTree tree) {
        return (ClassTree) tree.getTypeDecls().get(0);
    }
    public static int[] getPropertiesSectionPosition(List<StatementTree> stList) {
        int[] pos = new int[]{-1, -1};

        for (int i = 0; i < stList.size(); i++) {
            StatementTree stTree = stList.get(i);
            if (stTree.getKind() == Tree.Kind.LABELED_STATEMENT) {
                LabeledStatementTree lst = (LabeledStatementTree) stTree;
                String labelName = lst.getLabel().toString();

                if (!labelName.startsWith(PROPERTIES_LABEL)) {
                    continue;
                }
                pos[0] = i;
                pos[1] = getEndPropertiesSectionPosition(stList, i);
                break;
            }             
        }
        return pos;
    }
    public static int[] getPropertyPosition(List<StatementTree> stList, String fieldName, String propName) {
        int[] pos = new int[]{-1, -1};
        //int startIdx = -1;
        int[] posSec = getPropertiesSectionPosition(stList);
        if ( posSec == null || posSec[0] < 0 ) {
            return pos;
        }
        for (int i = posSec[0]; i < posSec[1]; i++) {
            StatementTree stTree = stList.get(i);

            System.out.println("StatementYree = " + stList.get(i).getKind());
            if (stTree.getKind() == Tree.Kind.LABELED_STATEMENT) {
                LabeledStatementTree lst = (LabeledStatementTree) stTree;
                String labelName = lst.getLabel().toString();
                if (labelName.equals(PROPERTIES_LABEL)) {
                    stTree = lst.getStatement();
                }
            }            

            if (stTree.getKind() == Tree.Kind.LABELED_STATEMENT) {
                LabeledStatementTree lst = (LabeledStatementTree) stTree;
                String labelName = lst.getLabel().toString();
                if (!labelName.equals(PROPERTIES_LABEL)) {
                    continue;
                }

                if (!labelName.startsWith(PROP_LABEL_PREFIX)) {
                    continue;
                }
                String[] a = labelName.split("_");
                if (a.length < 3 || !fieldName.equals(a[1]) || !propName.equals(a[2])) {
                    continue;
                }
                pos[0] = i;
                pos[1] = getEndLabelPosition(stList, i);
            } else if (stTree.getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
                ExpressionStatementTree es = (ExpressionStatementTree) stTree;
                ExpressionTree et = es.getExpression();
                System.out.println("  +++ " + et.getKind());
                /*                if (et.getKind() == Tree.Kind.ASSIGNMENT) {
                    AssignmentTree at = (AssignmentTree) et;
                    System.out.println("  --- getVariable() = " + at.getVariable().getKind());
                    if (at.getVariable().getKind() == Tree.Kind.IDENTIFIER) {
                        IdentifierTree it = (IdentifierTree) at.getVariable();
                        System.out.println("  --- identifier.name = " + it.getName());
                    }

                } else  
                 */
                if (et.getKind() == Tree.Kind.METHOD_INVOCATION) {
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
                        if (mst.getKind() == Tree.Kind.MEMBER_SELECT) {
                            MemberSelectTree ms = (MemberSelectTree) mst;
                            //
                            // Now get method name/ It's ms.getIdentifier()
                            //
                            String methodName = ms.getIdentifier().toString();
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
                            if (msLeft.getKind() == Tree.Kind.IDENTIFIER) {
                                IdentifierTree it = (IdentifierTree) msLeft;
                                String nm = it.getName().toString();
                                System.out.println("fieldName = " + nm);
                                if (methodName.length() > 3 && fieldName.equals(nm) && capatalizeFieldName(propName).equals(methodName.substring(3))) {
                                    pos[0] = i;
                                    pos[1] = i;
                                    break;
                                }

                            }
                        }
                    }
                }
            }
        }//for
        return pos;
    }

    public static int[] getFieldInitPosition(List<StatementTree> stList, String fieldName) {
        int[] pos = new int[]{-1, -1};

        for (int i = 0; i < stList.size(); i++) {
            StatementTree stTree = stList.get(i);

            System.out.println("StatementYree = " + stList.get(i).getKind());

            if (stTree.getKind() == Tree.Kind.LABELED_STATEMENT) {
                LabeledStatementTree lst = (LabeledStatementTree) stTree;
                String labelName = lst.getLabel().toString();

                if (!labelName.startsWith(INIT_LABEL_PREFIX)) {
                    continue;
                }
                String[] a = labelName.split("_");
                if (a.length < 2 || !fieldName.equals(a[1])) {
                    continue;
                }
                pos[0] = i;
                pos[1] = getEndLabelPosition(stList, i);
            } else if (stTree.getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
                ExpressionStatementTree es = (ExpressionStatementTree) stTree;
                ExpressionTree et = es.getExpression();
                System.out.println("  +++ " + et.getKind());
                if (et.getKind() == Tree.Kind.ASSIGNMENT) {
                    AssignmentTree at = (AssignmentTree) et;
                    System.out.println("  --- getVariable() = " + at.getVariable().getKind());
                    if (at.getVariable().getKind() == Tree.Kind.IDENTIFIER) {
                        IdentifierTree it = (IdentifierTree) at.getVariable();
                        System.out.println("  --- identifier.name = " + it.getName());
                        if (it.getName().contentEquals(fieldName)) {
                            pos[0] = i;
                            pos[1] = i;
                            break;
                        }

                    }
                }
            }
        }//for
        return pos;
    }

    public static int getEndLabelPosition(List<StatementTree> stList, int startFrom) {
        int pos = -1;
        for (int i = startFrom; i < stList.size(); i++) {
            if (stList.get(i).getKind() == Tree.Kind.LABELED_STATEMENT) {
                LabeledStatementTree lst = (LabeledStatementTree) stList.get(i);
                if (lst.getLabel().contentEquals(END_LABEL)) {
                    pos = i;
                    break;
                }
            }
        }
        return pos;
    }
    public static int getEndPropertiesSectionPosition(List<StatementTree> stList, int startFrom) {
        int pos = -1;
        for (int i = startFrom; i < stList.size(); i++) {
            if (stList.get(i).getKind() == Tree.Kind.LABELED_STATEMENT) {
                LabeledStatementTree lst = (LabeledStatementTree) stList.get(i);
                if (lst.getLabel().contentEquals(END_PROPERTIES_LABEL)) {
                    pos = i;
                    break;
                }
            }
        }
        return pos;
    }

    public static String capatalizeFieldName(String fieldName) {
        String retval;
        if (fieldName != null && !fieldName.isEmpty()
                && Character.isLowerCase(fieldName.charAt(0))
                && (fieldName.length() == 1 || Character.isLowerCase(fieldName.charAt(1)))) {
            retval = fieldName.substring(0, 1).toUpperCase();
            if (fieldName.length() > 1) {
                retval = retval + fieldName.substring(1);
            }
        } else {
            retval = fieldName;
        }
        return retval;
    }

    public static int[] getParentModifyPosition(List<StatementTree> stList, String fieldName, String parentName) {
        int[] pos = new int[]{-1, -1};
        int startIdx = -1;
        int[] fieldInitPos = getFieldInitPosition(stList, fieldName);
        if (fieldInitPos[0] < 0 || fieldInitPos[1] < 0 || fieldInitPos[1] == stList.size()) {
            return pos;
        }
        int i = fieldInitPos[1] + 1;
        //for (int i = fieldInitPos[1] + 1; i < stList.size(); i++) {
        StatementTree stTree = stList.get(i);

        //System.out.println("StatementYree = " + stList.get(i).getKind());
        //List<Comment> comments = Comment.
        if (stTree.getKind() == Tree.Kind.LABELED_STATEMENT) {
            LabeledStatementTree lst = (LabeledStatementTree) stTree;
            String labelName = lst.getLabel().toString();

            if (!labelName.startsWith(MODIFYPARENT_LABEL_PREFIX)) {
                return pos;
            }
            String[] a = labelName.split("_");
            if (a.length < 3 || !fieldName.equals(a[1]) || !parentName.equals(a[2])) {
                return pos;
            }
            pos[0] = i;
            pos[1] = getEndLabelPosition(stList, i);
        } else if (stTree.getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
            ExpressionStatementTree es = (ExpressionStatementTree) stTree;
            ExpressionTree et = es.getExpression();
            System.out.println("  +++ " + et.getKind());
            if (et.getKind() == Tree.Kind.METHOD_INVOCATION) {
                System.out.println("=== METHOD INVOCATION");
                MethodInvocationTree mit = (MethodInvocationTree) et;
                IdentifierTree it = getStartIdentifier(mit);
                if (it != null) {
                    System.out.println("IDENTIFIER == " + it.getName());
                    if ( it.getName().contentEquals(parentName)) {
                        pos[0] = i;
                        pos[1] = i;
                    }
                }
            }
        }
        //}//for
        return pos;
    }

    protected static IdentifierTree getStartIdentifier(MethodInvocationTree mit) {
        IdentifierTree id = null;
        ExpressionTree mst = mit.getMethodSelect();
        //
        // mst may be of Kind: MEMBER_SELECT or IDENTIFIER
        // button1.setText() or a.b.c.setText() - both of kind == MEMBER_SELECT
        // setText("txt") - IDENTIFIER
        //
        //if (mst.getKind() == Tree.Kind.MEMBER_SELECT) {

        while (true) {
            if (mst.getKind() == Tree.Kind.MEMBER_SELECT) {
                MemberSelectTree ms = (MemberSelectTree) mst;
                System.out.println("  --- memberSelect.kind = " + ms.getKind());
                //
                // Now get the  expression on the left of methodName
                //
                ExpressionTree msLeft = ms.getExpression();
                System.out.println("msLeft.kind = " + msLeft.getKind());
                //
                // 1. button1.setText() => IDENTIFIER
                // 2. a.b.button1.setText() or a.b() or a().b() etc. => MemberSelect
                //
                if (msLeft.getKind() == Tree.Kind.IDENTIFIER) {
                    id = (IdentifierTree) msLeft;
                    break;
                } else if (msLeft.getKind() == Tree.Kind.METHOD_INVOCATION) {
                    id = getStartIdentifier((MethodInvocationTree) msLeft);
                } else if (msLeft.getKind() == Tree.Kind.MEMBER_SELECT) {
                    mst = msLeft;
                } else {
                    break;
                }
            }
            break;
        }//while
        return id;

    }
}

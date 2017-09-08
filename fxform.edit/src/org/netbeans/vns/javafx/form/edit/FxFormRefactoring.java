/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.vns.javafx.form.edit;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.DataEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Valery
 */
public class FxFormRefactoring {
    
    public static void rename(FxFormDataObject fdo,String currentName, String newName) {
//        FxFormDataObject formDO = FxFormEditor.getFormDataObject(formModel);
        JavaSource js = JavaSource.forFileObject(fdo.getPrimaryFile());
        MemberVisitor scanner = new MemberVisitor(currentName, true); //privateField);
        try {
            js.runUserActionTask(scanner, true);
            //rename(fdo, newName, scanner.getTreePathHandle());
        } catch (IOException e) {
            Logger.getLogger(FxFormRefactoring.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }
    }
    public static void rename(FileObject fo,String currentName, String newName) {
//        FxFormDataObject formDO = FxFormEditor.getFormDataObject(formModel);
        JavaSource js = JavaSource.forFileObject(fo);
        MemberVisitor scanner = new MemberVisitor(currentName, true); //privateField);
        try {
            js.runUserActionTask(scanner, true);
            rename(fo, newName, scanner.getTreePathHandle());
        } catch (IOException e) {
            Logger.getLogger(FxFormRefactoring.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }
    }
    
    
    public static void rename(FileObject fo, String newName, TreePathHandle handle) throws IOException {
        if (handle == null) {
            //this would only happen if setName were called without the correct component being
            //selected some how...
            return;
        }
        DataEditorSupport fes = DataObject.find(fo).getLookup().lookup(DataEditorSupport.class);
        if (fes.isModified()) {
            fes.saveDocument();
        }
        //ok, so we are now ready to actually setup our RenameRefactoring...we need the element TreePathHandle
        Lookup rnl = Lookups.singleton(handle);
        RefactoringSession renameSession = RefactoringSession.create("Change variable name");//NOI18N
        RenameRefactoring refactoring = new RenameRefactoring(rnl);
        Problem pre = refactoring.preCheck();
        if (pre != null && pre.isFatal()) {
            Logger.getLogger("global").log(Level.WARNING, "There were problems trying to perform the refactoring.");
        }

        Problem p = null;

        if ((!(pre != null && pre.isFatal())) && !emptyOrNull(newName)) {
            refactoring.setNewName(newName);
            p = refactoring.prepare(renameSession);
        }

        if ((!(p != null && p.isFatal())) && !emptyOrNull(newName)) {
            renameSession.doRefactoring(true);
        }
    }

    private static boolean emptyOrNull(String s) {
        return s == null || s.trim().length() == 0;
    }

    private static class MemberVisitor
            extends TreePathScanner<Void, Void>
            implements CancellableTask<CompilationController> {

        private CompilationInfo info;
        private String member = null;
        private TreePathHandle treePathHandle = null;

        boolean findUsages;
        private Element variableElement;
        private List<Integer> usagesPositions;

        public TreePathHandle getTreePathHandle() {
            return treePathHandle;
        }
/*
        public void setTreePathHandle(TreePathHandle treePathHandle) {
            this.treePathHandle = treePathHandle;
        }
*/
        public MemberVisitor(String member, boolean findUsages) {
            this.member = member;
            this.findUsages = findUsages;
        }

        @Override
        public Void visitClass(ClassTree classTree, Void v) {
            if (variableElement == null) {
                // try to find the component's field variable in the class
                List<? extends Tree> members = (List<? extends Tree>) classTree.getMembers();
                Iterator<? extends Tree> memberIterator = members.iterator();
                while (memberIterator.hasNext()) {
                    Tree tree = memberIterator.next();
                    if (tree.getKind() == Tree.Kind.VARIABLE) {
                        Trees trees = info.getTrees();
                        TreePath varTreePath = new TreePath(getCurrentPath(), tree);
                        Element el = trees.getElement(varTreePath);
                        if (el != null) { // Issue 185420
                            String sname = el.getSimpleName().toString();
                            if (sname.equals(this.member)) {
                                this.treePathHandle = TreePathHandle.create(varTreePath, info);
                                variableElement = el;
                                if (findUsages) {
                                    usagesPositions = new ArrayList<Integer>();
                                }
                            }
                        }
                    }
                }
            }
            if (findUsages) {
                super.visitClass(classTree, v);
            }
            return null;
        }

        @Override
        public Void visitIdentifier(IdentifierTree tree, Void v) {
            if (findUsages) {
                Element el = info.getTrees().getElement(getCurrentPath());
                if (variableElement != null && variableElement.equals(el)) {
                    int pos = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), tree);
                    usagesPositions.add(pos);
                }
            }
            return super.visitIdentifier(tree, v);
        }

        @Override
        public void cancel() {
        }

        @Override
        public void run(CompilationController controller) throws IOException {
            this.info = controller;
            controller.toPhase(Phase.RESOLVED);
            this.scan(controller.getCompilationUnit(), null);
        }
    }

}

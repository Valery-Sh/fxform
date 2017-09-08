/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.vns.javafx.demo.design.code;

import com.sun.source.tree.StatementTree;
import javafx.scene.Node;
import org.netbeans.api.java.source.TreeMaker;

/**
 *
 * @author Valery
 */
public interface DockUndockCodeGenerator {
    StatementTree Dock(TreeMaker make, Node dockedNode, String dockedNodeName, Object... dockPosition);
    StatementTree Undock(TreeMaker make, Node dockedNode, String dockedNodeName, Object... dockPosition);    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.vns.javafx.demo.design.code;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.StatementTree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.geometry.Side;
import javafx.scene.Node;
import org.netbeans.api.java.source.TreeMaker;

/**
 *
 * @author Valery
 */
public class DockPaneCodeGenerator implements DockUndockCodeGenerator {
    protected Node targetNode;
    protected String targetNodeName;    
    protected String dockedNodeName;
    
    public DockPaneCodeGenerator(Node targetNode, String targetNodeName) {
        this.targetNode = targetNode;
        this.targetNodeName = targetNodeName;
    }

    @Override
    public StatementTree Dock(TreeMaker make, Node dockedNode, String dockedNodeName, Object... dockPosition) {
        List<ExpressionTree> args = new ArrayList<>();
        
        Side pos = null;
        //Node sideNode =  null;
        String sideNodeName = null;        
        
        if ( dockPosition.length >= 1 ) {
            pos = (Side) dockPosition[0];
            
        } 
        if ( dockPosition.length > 2 ) {
            //sideNode =  (Node) dockPosition[1];
            sideNodeName = (String) dockPosition[2];
        } 
        args.add( make.Identifier(dockedNodeName));
        
        MemberSelectTree ms = make.MemberSelect(make.QualIdent(Side.class.getName()), pos.toString());
        
        args.add(ms);
        if ( sideNodeName != null ) {
            args.add( make.Identifier(sideNodeName));
        }
        ExpressionTree et = make.MethodInvocation(Collections.<ExpressionTree>emptyList(), 
                make.Identifier("dockNode"),
                args );
                
                
        return make.ExpressionStatement(et);
        
    }

    @Override
    public StatementTree Undock(TreeMaker make, Node dockedNode, String dockedNodeName, Object... dockPosition) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}

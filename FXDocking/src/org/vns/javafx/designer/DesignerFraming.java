/*
 * Copyright 2018 Your Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.vns.javafx.designer;

import org.vns.javafx.dock.api.selection.ObjectFramingProvider;
import org.vns.javafx.dock.api.selection.ObjectFraming;
import org.vns.javafx.dock.api.selection.AbstractNodeFraming;
import org.vns.javafx.dock.api.selection.SelectionFrame;
import javafx.scene.Node;
import javafx.scene.Parent;
import org.vns.javafx.dock.api.DockLayout;
import org.vns.javafx.dock.api.Selection;

/**
 *
 * @author Olga
 */
public class DesignerFraming extends AbstractNodeFraming {

    @Override
    protected void initializeOnShow(Node node) {

        SceneView sv = DesignerLookup.lookup(SceneView.class);
        if (sv == null || sv.getRoot() == null || sv.getRoot().getScene() != node.getScene()) {
            return;
        }
        //Parent p = EditorUtil.getTopParentOf(node);
        Parent p = SceneViewUtil.findParentByObject(node);

        if (p != null && node != sv.getRoot()) {
            //if (node.getParent() != null && node.getParent() != sv.getRoot()) {
            SelectionFrame parentPane = null;
            ObjectFraming objFraming = getParentObjectFraming(p, node);

            if (objFraming != null) {
                objFraming.showParent();
            } else {
                parentPane = SceneView.getParentFrame();
                //parentPane.setBoundNode(node.getParent());
                parentPane.setBoundNode(p);
            }

            //}
            SelectionFrame resizePane = SceneView.getResizeFrame();
            resizePane.setBoundNode(node);
        }

        Selection sel = sv.getC.lookup(Selection.class);
        if (sel != null) {
            sel.notifySelected(node);
        }
    }

    @Override
    public void showParent(Node node, Object... parms) {
        SceneView sv = DesignerLookup.lookup(SceneView.class);
//        Parent p = EditorUtil.getTopParentOf(node);
        Parent p = SceneViewUtil.findParentByObject(node);

        if (p != null && node != sv.getRoot()) {
            //          if (node.getParent() != null && node.getParent() != sv.getRoot()) {

            ObjectFraming objFraming = getParentObjectFraming(p, node);

            if (objFraming != null) {
                objFraming.showParent();
            } else {
                SelectionFrame parentPane = SceneView.getParentFrame();
                //parentPane.setBoundNode(node.getParent());
                parentPane.setBoundNode(p);
            }

//            FramePane parentPane = SceneView.getParentFrame();
//            parentPane.setBoundNode(node.getParent());
//            }
        }
    }

    @Override
    protected void finalizeOnHide(Node node) {
        //System.err.println("finalizeOnHide node = " + node);
        SceneView sv = DesignerLookup.lookup(SceneView.class);
        
        if (sv == null || sv.getRoot() == null || sv.getRoot().getScene() == null  || sv.getRoot().getScene() != node.getScene()) {
            return;
        }
        //Parent p = EditorUtil.getTopParentOf(node);
        Parent p = SceneViewUtil.findParentByObject(node);

        if (p != null) {
            SelectionFrame resizePane = SceneView.getResizeFrame();
            if (resizePane != null) {
                resizePane.setBoundNode(null);
            }
            SelectionFrame parentPane = SceneView.getParentFrame();
            if (parentPane != null) {
                parentPane.setBoundNode(null);
            }
            ObjectFraming of = getParentObjectFraming(p, node);
            if (of != null) {
                of.hide();
            }
        }
    }

    protected ObjectFraming getParentObjectFraming(Parent p, Node node) {
        ObjectFraming retval = null;
        if (DockLayout.test(p)) {
            DockLayout dl = DockLayout.of(p);
            ObjectFramingProvider fp = dl.getLayoutContext().getLookup().lookup(ObjectFramingProvider.class);
            if (fp != null) {
                retval = fp.getInstance(node);
            }
        }
        return retval;
    }
}

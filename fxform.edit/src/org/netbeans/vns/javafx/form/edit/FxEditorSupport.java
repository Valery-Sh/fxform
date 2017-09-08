package org.netbeans.vns.javafx.form.edit;

import java.io.IOException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author Tomas Pavek
 */
public interface FxEditorSupport extends Node.Cookie {
    static String SECTION_INIT = "initLayout"; // NOI18N
    static String SECTION_VARIABLES = "variables"; // NOI18N

    Document getDocument();
    GuardedSectionManager getGuardedSectionManager();
    void markModified();
    Object getJavaContext();
    void openAt(Position pos);
    void discardEditorUndoableEdits();
    void saveAs(FileObject folder, String fileName) throws IOException;
    void openDesign();
    void openSource();
    void reloadForm();
    boolean isJavaEditorDisplayed();
    Boolean getFoldState(int offset);
    void restoreFoldState(boolean collapsed, int startOffset, int endOffset);
    int getCodeIndentSize();
    boolean getCodeBraceOnNewLine();
    boolean canGenerateNBMnemonicsCode();
}

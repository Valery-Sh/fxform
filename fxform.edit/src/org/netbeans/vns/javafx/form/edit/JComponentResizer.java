package org.netbeans.vns.javafx.form.edit;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 *
 * @author Valery Shyshkin
 */
public class JComponentResizer {

    private final JComponent component;
    private final Set<Integer> cursorTypes = new HashSet<>();
    private double borderRight = 0;
    private double borderBottom = 0;

    private Dimension startDimension;
    private double startX = Double.MIN_VALUE;
    private double startY = Double.MIN_VALUE;    
    
    private int startCursor;

    public JComponentResizer(JComponent component) {
        this.component = component;
        if (component.getBorder() != null) {
            Insets ins = component.getBorder().getBorderInsets(component);
            borderBottom = ins.bottom;
            borderRight = ins.right;
        }

        cursorTypes.add(Cursor.E_RESIZE_CURSOR);
        cursorTypes.add(Cursor.S_RESIZE_CURSOR);
        cursorTypes.add(Cursor.SE_RESIZE_CURSOR);
        addResizeListeners();
    }

    private void addResizeListeners() {
        component.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent ev) {
                if (startDimension == null) {
                    return;
                }
                switch (startCursor) {
                    case Cursor.DEFAULT_CURSOR:
                        break;
                    case Cursor.E_RESIZE_CURSOR:
                        resize(ev);
                        break;
                    case Cursor.S_RESIZE_CURSOR:
                        resize(ev);
                        break;

                    case Cursor.SE_RESIZE_CURSOR:
                        resize(ev);
                        break;

                }
                ev.consume();
            }

            @Override
            public void mouseMoved(java.awt.event.MouseEvent ev) {
                component.setCursor(new Cursor(getCursor(ev)));
                ev.consume();
            }
        });
        component.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent ev) {
                startCursor = getCursor(ev);
                if (startCursor == Cursor.DEFAULT_CURSOR) {
                    startDimension = null;
                    return;
                }

                startDimension = new Dimension(component.getWidth(), component.getHeight());
                startX = ev.getX();
                startY = ev.getY();
                ev.consume();
            }
        });
    }

    public void resize(java.awt.event.MouseEvent ev) {

        double w = component.getPreferredSize().getWidth();
        double h = component.getPreferredSize().getHeight();

        switch (startCursor) {
            case Cursor.DEFAULT_CURSOR:
                return;
            case Cursor.E_RESIZE_CURSOR:
                w = ev.getX() - startX + startDimension.getWidth();
                h = startDimension.getHeight();
                break;
            case Cursor.S_RESIZE_CURSOR:
                h = ev.getY() - startY + startDimension.getHeight();
                w = startDimension.getWidth();
                break;

            case Cursor.SE_RESIZE_CURSOR:
                h = ev.getY() - startY + startDimension.getHeight();
                w = ev.getX() - startX + startDimension.getWidth();
                break;
        }
        component.setPreferredSize(new Dimension((int)w,(int)h));
        
        SwingUtilities.invokeLater(() -> {
            //
            // Do not remember that the parent JPanel is assigned to ScrollPane
            //
//            Container viewPort = component.getParent().getParent();
            
/*            viewPort.invalidate();
            viewPort.repaint();
            viewPort.revalidate();
            component.invalidate();
            component.repaint();
*/
            component.revalidate();
        });

    }

    public int getCursor(java.awt.event.MouseEvent ev) {
        if ( borderRight <= 0 ) {
           borderRight = 4; 
        }
        if ( borderBottom <= 0 ) {
           borderBottom = 4; 
        }
        
        if (ev.getX() > component.getWidth() - borderRight && ev.getX() < component.getWidth() && ev.getY() < component.getHeight()
                && ev.getY() > component.getHeight() - borderBottom && ev.getY() < component.getWidth()) {
            component.setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));
            return Cursor.SE_RESIZE_CURSOR;
        } else if (ev.getX() > component.getWidth() - borderRight && ev.getX() < component.getWidth() && ev.getY() < component.getHeight()) {
            component.setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
            return Cursor.E_RESIZE_CURSOR;

        //} else if (ev.getY() > component.getHeight() - borderBottom && ev.getY() < component.getWidth()) {
        } else if (ev.getY() > component.getHeight() - borderBottom && ev.getY() < component.getHeight() && ev.getX() < component.getWidth()) {        
            component.setCursor(new Cursor(Cursor.S_RESIZE_CURSOR));
            return Cursor.S_RESIZE_CURSOR;
        }
        return Cursor.DEFAULT_CURSOR;
    }

}

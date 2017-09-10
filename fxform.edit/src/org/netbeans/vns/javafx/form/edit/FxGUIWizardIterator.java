package org.netbeans.vns.javafx.form.edit;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.api.templates.TemplateRegistrations;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

// TODO define position attribute
@TemplateRegistrations({
    //@TemplateRegistration(folder = "JavaFx GUI Form", content = {"Main.java.template", "MainLayout.java.template","Main.fxform.template"}, displayName = "#FXForm.main", iconBase = "org/netbeans/vns/javafx/demo/icon.png", description = "Main.html", scriptEngine = "freemarker")
    @TemplateRegistration(folder = "JavaFX GUI Form", 
            content = {"resources/JavaFXApplication.java.template", "resources/JavaFXApplicationLayout.java.template", "resources/JavaFXApplication.fxform.template"}, 
            displayName = "#FXForm.main", 
            iconBase = "org/netbeans/vns/javafx/form/edit/resources/icon.png", 
            description = "resources/JavaFXApplication.html", scriptEngine = "freemarker")    
    ,
   // @TemplateRegistration(folder = "JavaFx GUI Form", content = "Scene.java.template", displayName = "#FXForm.scene", iconBase = "org/netbeans/vns/javafx/demo/icon.png", description = "Scene.html", scriptEngine = "freemarker")
})
//@Messages({"FXForm.main=JavaFX Application Class", "FXForm.scene=JavaFX Scene class"})
@Messages({"FXForm.main=JavaFX Application Class"})

public final class FxGUIWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {
    @StaticResource
    public static final String RESOURCES_ = "org/netbeans/vns/javafx/form/edit/resources/form.gif";
    public static final String RESOURCES = RESOURCES_.substring(0, RESOURCES_.length()-8);
    
    //@StaticResource(searchClasspath=true)
    public static final String JAVAFXAPPLICATION_FXFORM_TEMPLATE = RESOURCES + "/JavaFXApplication.fxform.template";
    public static final String JAVAFXAPPLICATION_HTML = RESOURCES + "/JavaFXApplication.html";
    public static final String JAVAFXAPPLICATION_JAVA_TEMPLATE = RESOURCES + "/JavaFXApplication.java.template";    
    
    public static final String JAVAFXAPPLICATION_LAYOUT_JAVA_TEMPLATE = RESOURCES + "/JavaFXApplicationLayout.java.template";    
    
    private int index;

    private WizardDescriptor wiz;
    private List<WizardDescriptor.Panel<WizardDescriptor>> panels;

    private List<WizardDescriptor.Panel<WizardDescriptor>> getPanels() {
        if (panels == null) {
            Project project = Templates.getProject(wiz);
            if (project == null) {
                throw new NullPointerException("No project found for: " + wiz);
            }
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            assert groups != null : "Cannot return null from Sources.getSourceGroups: " + sources;
            groups = checkNotNull(groups, sources);
            if (groups.length == 0) {
                Util.out("groups.length == 0");
                /*                groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
                groups = checkNotNull(groups, sources);
                return new WizardDescriptor.Panel[]{
                    Templates.buildSimpleTargetChooser(project, groups).create(),};
                 */
            }

            WizardDescriptor.Panel<WizardDescriptor> p = JavaTemplates.createPackageChooser(project, groups);

            WizardDescriptor.Panel<WizardDescriptor>[] ps = new WizardDescriptor.Panel[]{p};

            panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
            Collections.addAll(panels, ps);

            String[] steps = createSteps();
            for (int i = 0; i < panels.size(); i++) {
                Component c = panels.get(i).getComponent();
                if (steps[i] == null) {
                    // Default step name to component name of panel. Mainly
                    // useful for getting the name of the target chooser to
                    // appear in the list of steps.
                    steps[i] = c.getName();
                }
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);

                }
            }
        }
        return panels;
    }

    private static SourceGroup[] checkNotNull(SourceGroup[] groups, Sources sources) {
        List<SourceGroup> sourceGroups = new ArrayList<SourceGroup>();
        for (SourceGroup sourceGroup : groups) {
            if (sourceGroup == null) {
                Exceptions.printStackTrace(new NullPointerException(sources + " returns null SourceGroup!"));
            } else {
                sourceGroups.add(sourceGroup);
            }
        }

        return sourceGroups.toArray(new SourceGroup[sourceGroups.size()]);
    }

    @Override
    public Set<?> instantiate() throws IOException {
        // TODO return set of FileObject (or DataObject) you have created
        FileObject dir = Templates.getTargetFolder(wiz);
        DataFolder dirDO = DataFolder.findFolder(dir);
//        Util.out("Dir: " + dir);
        String targetName = Templates.getTargetName(wiz);
//        Util.out("targetName: " + targetName);

        FileObject primaryTemplate = Templates.getTemplate(wiz);
//         Util.out("primaryTemplate: " + primaryTemplate);
        FileObject primaryFile = null;
        FileObject layoutTemplate = getLayoutTemplate(primaryTemplate); 
        FileObject layoutFile = null;
        FileObject fxformTemplate = getFxFormTemplate(primaryTemplate); 
        FileObject fxformFile = null;
        
        DataObject primaryTemplateDO = DataObject.find(primaryTemplate);
//        Util.out("primaryTemplateDO: " + primaryTemplateDO);
//        Util.out("fxformTemplate: " + fxformTemplate);
        DataObject layoutTemplateDO = DataObject.find(layoutTemplate);
        layoutTemplate.setAttribute("javax.script.ScriptEngine", primaryTemplate.getAttribute("javax.script.ScriptEngine"));  

        //DataObject fxformTemplateDO = DataObject.find(fxformTemplate);
        fxformTemplate.setAttribute("javax.script.ScriptEngine", primaryTemplate.getAttribute("javax.script.ScriptEngine"));          
        
        DataObject primaryDO = primaryTemplateDO.createFromTemplate(dirDO, targetName);
        DataObject layoutDO = layoutTemplateDO.createFromTemplate(dirDO, targetName + "Layout");        
        //DataObject fxformDO = fxformTemplateDO.createFromTemplate(dirDO, targetName);        

        
//        Util.out("dobj: " + primaryDO);

        primaryFile = primaryDO.getPrimaryFile();
        layoutFile = layoutDO.getPrimaryFile();
        //fxformFile = fxformDO.getPrimaryFile();
        
        
        //Util.out("fxformDO.primaryFile: " + fxformDO);
        Set set = new HashSet();
        set.add(primaryFile);
        set.add(layoutFile);
        return set;

        
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
//        Util.out("INITIALIZE: wiz=" + wizard);

        this.wiz = wizard;
        getPanels();
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
/*        wiz.getProperties().forEach((k, v) -> {
            Util.out("k=" + k + "; v=" + v);
        });

        String[] a = (String[]) wiz.getProperty("WizardPanel_contentData");
        for (String s : a) {
            Util.out("CONTENT: " + s);
        }
*/        
        return getPanels().get(index);
    }

    @Override
    public String name() {
        return index + 1 + ". from " + getPanels().size();
    }

    @Override
    public boolean hasNext() {
        return index < getPanels().size() - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
/*        wiz.getProperties().forEach((k, v) -> {
            Util.out("1 k=" + k + "; v=" + v);
        });
*/
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    private FileObject getLayoutTemplate(FileObject fileObject) {
        for (int i = 0; i < fileObject.getParent().getChildren().length; i++) {
            FileObject obj = fileObject.getParent().getChildren()[i];

            if (obj.getName().equals(fileObject.getName() + "Layout")) {
                return obj;
            }
        }

        return null;
    }
    private FileObject getFxFormTemplate(FileObject fileObject) {
        //FileObject fileObject = null;
        for (int i = 0; i < fileObject.getParent().getChildren().length; i++) {
            FileObject obj = fileObject.getParent().getChildren()[i];
//            Util.out("getFxFormTemplate: name=" + obj.getName() + ": nameEx=" + obj.getNameExt());
            //if (obj.getName().equals(fileObject.getName() + "Layout")) {
            if (obj.getName().equals(fileObject.getName()) && "fxform".equals(obj.getExt())) {
//                Util.out("getFxFormTemplate " + obj);
                return obj;
            }
        }

        return null;
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
/*    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }
     */
    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then use
    // ChangeSupport to implement add/removeChangeListener and call fireChange
    // when needed
    // You could safely ignore this method. Is is here to keep steps which were
    // there before this wizard was instantiated. It should be better handled
    // by NetBeans Wizard API itself rather than needed to be implemented by a
    // client code.
    private String[] createSteps() {
        String[] beforeSteps = (String[]) wiz.getProperty("WizardPanel_contentData");
        assert beforeSteps != null : "This wizard may only be used embedded in the template wizard";
        String[] res = new String[(beforeSteps.length - 1) + panels.size()];
        for (int i = 0; i < res.length; i++) {
            if (i < (beforeSteps.length - 1)) {
                res[i] = beforeSteps[i];
            } else {
                res[i] = panels.get(i - beforeSteps.length + 1).getComponent().getName();
            }
        }
        return res;
    }

    private final transient Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);

    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    protected final void fireChangeEvent() {
        ChangeListener[] ls;
        synchronized (listeners) {
            ls = listeners.toArray(new ChangeListener[listeners.size()]);
        }
        ChangeEvent ev = new ChangeEvent(this);
        for (ChangeListener l : ls) {
            l.stateChanged(ev);
        }
    }
/*
    protected FileObject getTemplateFolder() {
        FileObject fo = FileUtil.getConfigFile("Templates"); // NOI18N

        if (fo != null && fo.isFolder()) {

            return fo;

        }
        return null;
    }

    protected FileObject getTemplateByNameAndFolder(String name, String folder) {
        FileObject fo = FileUtil.getConfigFile("Templates"); // NOI18N

        FileObject tmplFolder = getTemplateFolder(fo, folder);

        if (null != tmplFolder && tmplFolder.isFolder()) {
            return tmplFolder.getFileObject(name);
        } else {
            return null;
        }

    }

    protected FileObject getTemplateFolder(FileObject base, String folder) {
        if (null == base) {
            return null;
        }

        if (!base.isFolder()) {
            return null;
        }

        if (null == folder || folder.trim().length() == 0) {
            return null;
        }

        FileObject[] templates = base.getChildren();

        if (folder.contains("/")) {
            // Folder name contains subfolders

            StringTokenizer st = new StringTokenizer(folder, "/");

            String nextSubfolderName = st.nextToken();

            String remainingFolderName = folder.substring(nextSubfolderName.length() + 1);

            FileObject subfolder = base.getFileObject(nextSubfolderName);

            return getTemplateFolder(subfolder, remainingFolderName);

        } else {

            FileObject fo = base.getFileObject(folder);

            return fo;
        }

    }
*/
}//class

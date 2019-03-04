package org.vns.javafx.designer.descr;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.DefaultProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import org.vns.javafx.dock.api.bean.ReflectHelper.MethodUtil;

/**
 *
 * @author Valery
 */
public class NodeDescriptorRegistry {

    //private final ObservableMap<String, TreeItemBuilder> descriptors = FXCollections.observableHashMap();
    private final ObservableMap<Class, NodeDescriptor> descriptors = FXCollections.observableHashMap();

    public static NodeDescriptorRegistry getInstance() {
        return SingletonInstance.INSTANCE;
    }

    public NodeDescriptor getDescriptor(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        if (descriptors.isEmpty()) {
            //createDefaultDescriptors();
            loadDefaultDescriptors();
        }
        NodeDescriptor retval;
       
        retval = find(clazz);
        
        if (retval == null) {
            //
            // try to find DefaultProperty
            //
            Annotation annotation = clazz.getDeclaredAnnotation(DefaultProperty.class);
            if (annotation != null) {
                
                String name = ((DefaultProperty) annotation).value();
                retval = new NodeDescriptor();
                retval.setType(clazz.getName());
                try {

                    Method method = MethodUtil.getMethod(clazz, "get" + name.substring(0, 1).toUpperCase() + name.substring(1), new Class[0]);
                    Class returnType = method.getReturnType();
                    NodeProperty p;
                    if (ObservableList.class.equals(returnType)) {
                        p = new NodeList();
                    } else {
                        p = new NodeContent();
                    }
                    p.setName(name);
                    retval.getProperties().add(p);
                    register(clazz, retval);
                } catch (NoSuchMethodException ex) {
                    Logger.getLogger(NodeDescriptorRegistry.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        if (retval == null) {
            retval = new NodeDescriptor();
            retval.setType(clazz.getName());
            retval.setStyleClass("tree-item-node-unknownnode");
            register(clazz, retval);

        }
        return retval;
    }

    public NodeDescriptor getDescriptor__OLD(Object o) {
        if (o == null) {
            return null;
        }
        if (descriptors.isEmpty()) {
            //createDefaultDescriptors();
            loadDefaultDescriptors();
        }
        NodeDescriptor retval;
        if (o instanceof Class) {
            retval = find((Class) o);
        } else {
            retval = find(o.getClass());
        }
        if (retval == null && !(o instanceof Class)) {
            //
            // try to find DefaultProperty
            //
            Annotation annotation = o.getClass().getDeclaredAnnotation(DefaultProperty.class);
            if (annotation != null) {
                String name = ((DefaultProperty) annotation).value();
                retval = new NodeDescriptor();
                retval.setType(o.getClass().getName());
                try {

                    Method method = MethodUtil.getMethod(o.getClass(), "get" + name.substring(0, 1).toUpperCase() + name.substring(1), new Class[0]);
                    //Method method = o.getClass().getMethod("get" + name.substring(0, 1).toUpperCase() + name.substring(1), new Class[0]);
                    Class returnType = method.getReturnType();
                    NodeProperty p;
                    if (ObservableList.class.equals(returnType)) {
                        p = new NodeList();
                    } else {
                        p = new NodeContent();
                    }
                    p.setName(name);
                    retval.getProperties().add(p);
                } catch (NoSuchMethodException ex) {
                    Logger.getLogger(NodeDescriptorRegistry.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        if (retval == null && !(o instanceof Class)) {
            retval = new NodeDescriptor();
            retval.setType(o.getClass().getName());
            retval.setStyleClass("tree-item-node-unknownnode");
        }
        return retval;
    }
    
    protected NodeDescriptor find(Class clazz) {
        if (descriptors.isEmpty()) {
            //createDefaultDescriptors();
            loadDefaultDescriptors();
        }

        NodeDescriptor retval = null;
        Class c = clazz;

        while (c != null && !c.isPrimitive()) {
            if (descriptors.get(c) != null) {
                retval = descriptors.get(c);
                break;
            }
            c = c.getSuperclass();
        }

        if (retval == null) {
            c = clazz;
            while (c != null) {
                retval = find(c.getInterfaces());
                if (retval != null) {
                    break;
                }
                c = c.getSuperclass();
            }
        }
        if (retval == null) {
            retval = descriptors.get(Node.class);
            register(clazz, retval);
        }

        return retval;
    }

    protected NodeDescriptor find(Class[] interfaces) {
        NodeDescriptor retval = null;
        for (Class c : interfaces) {
            retval = findForInterface(c);
            if (retval != null) {
                break;
            }
        }

        return retval;
    }

    protected NodeDescriptor findForInterface(Class clazz) {
        return descriptors.get(clazz);
    }

    public void register(Class clazz, NodeDescriptor value) {
        descriptors.put(clazz, value);
    }

    public void unregister(Object key) {
        descriptors.remove(key.getClass());
    }

    public boolean exists(Class clazz) {
        return getDescriptor(clazz) != null;
    }

    protected void loadDefaultDescriptors() {
        FXMLLoader loader = new FXMLLoader();
        GraphDescriptor root;
        try {
            root = loader.load(getClass().getResourceAsStream("/org/vns/javafx/designer/resources/DesignFXML01.fxml"));
            root.getDescriptors().forEach(d -> {
                String className = d.getType();
                Class clazz;//
                try {
                    clazz = Class.forName(className);
                    register(clazz, d);
                } catch (ClassNotFoundException ex) {
                    System.err.println("ClassNotFoundException EXCEPTION: " + ex.getMessage());
                    Logger.getLogger(NodeDescriptorRegistry.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(NodeDescriptorRegistry.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static class SingletonInstance {
        private static final NodeDescriptorRegistry INSTANCE = new NodeDescriptorRegistry();
    }

}

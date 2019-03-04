package org.vns.javafx.dock.api.bean;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import java.lang.reflect.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.Property;

import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import org.vns.javafx.dock.api.bean.ReflectHelper.MethodUtil;
import static org.vns.javafx.dock.api.bean.ReflectHelper.checkPackageAccess;
import static org.vns.javafx.dock.api.bean.ReflectHelper.getField;
import org.vns.javafx.dock.api.demo.TestTrashTray1;

/**
 * Exposes Java Bean properties of an object via the {@link Map} interface. A
 * call to {@link Map#get(Object)} invokes the getter for the corresponding
 * property, and a call to {@link Map#put(Object, Object)} invokes the
 * property's setter. Appending a "Property" suffix to the key returns the
 * corresponding property model.
 */
public class BeanAdapter extends AbstractMap<String, Object> {

    private final Object bean;

    private static class MethodCache {

        private final Map<String, List<Method>> methods;
        private final MethodCache nextClassCache;

        private MethodCache(Map<String, List<Method>> methods, MethodCache nextClassCache) {
            this.methods = methods;
            this.nextClassCache = nextClassCache;
        }

        private Method getMethod(String name, Class<?>... parameterTypes) {
            List<Method> namedMethods = methods.get(name);
            if (namedMethods != null) {
                for (int i = 0; i < namedMethods.size(); i++) {
                    Method namedMethod = namedMethods.get(i);
                    if (namedMethod.getName().equals(name)
                            && Arrays.equals(namedMethod.getParameterTypes(), parameterTypes)) {
                        return namedMethod;
                    }
                }
            }

            return nextClassCache != null ? nextClassCache.getMethod(name, parameterTypes) : null;
        }

    }

    private static final HashMap<Class<?>, MethodCache> globalMethodCache
            = new HashMap<>();

    private final MethodCache localCache;

    public static final String GET_PREFIX = "get";
    public static final String IS_PREFIX = "is";
    public static final String SET_PREFIX = "set";
    public static final String PROPERTY_SUFFIX = "Property";

    public static final String VALUE_OF_METHOD_NAME = "valueOf";

    /**
     * Creates a new Bean adapter.
     *
     * @param bean The Bean object to wrap.
     */
    public BeanAdapter(Object bean) {
        this.bean = bean;

        localCache = getClassMethodCache(bean.getClass());
    }

    public BeanAdapter(Class<?> beanClass) {
        bean = null;
        localCache = getClassMethodCache(beanClass);
    }

    public Method fxPropertyMethod(String propName) {
        Method retval = null;
        try {
            if (propName == null) {
                return null;
            }
            String fxPropName = propName + "Property";
//            System.err.println("BeanAdapter fxPropName = " + fxPropName);
            retval = ReflectHelper.MethodUtil.getMethod(bean.getClass(), fxPropName, new Class[0]);

        } catch (NoSuchMethodException ex) {
//            System.err.println("RRRRRRRRRRRRRRRRRRRRRRRRRRR");
            //Logger.getLogger(BeanAdapter.class.getName()).log(Level.INFO, "");
        }
        return retval;

    }
   
    public Property getJavaFxObjectProperty(String propName) {
        Property retval = null;
        try {
            if (propName == null) {
                return null;
            }
            String fxPropName = propName + "Property";
            Method m = ReflectHelper.MethodUtil.getMethod(bean.getClass(), fxPropName, new Class[0]);
            retval = (Property) m.invoke(bean);
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(BeanAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retval;
    }
   public static Method getJavaFxPropertyMethod(Class<?> beanClass,String propName) {
        Method retval = null;
        try {
            if (propName == null) {
                return null;
            }
            String fxPropName = propName + "Property";
            retval = ReflectHelper.MethodUtil.getMethod(beanClass, fxPropName, new Class[0]);
        } catch (NoSuchMethodException | IllegalArgumentException ex) {
            //Logger.getLogger(BeanAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retval;
    }
    private static MethodCache getClassMethodCache(final Class<?> type) {
        if (type == Object.class) {
            return null;
        }
        MethodCache classMethodCache;
        synchronized (globalMethodCache) {
            if ((classMethodCache = globalMethodCache.get(type)) != null) {
                return classMethodCache;
            }
            Map<String, List<Method>> classMethods = new HashMap<>();
            checkPackageAccess(type);
            if (Modifier.isPublic(type.getModifiers())) {
                // only interested in public methods in public classes in
                // non-restricted packages
                final Method[] declaredMethods
                        = AccessController.doPrivileged((PrivilegedAction<Method[]>) () -> type.getDeclaredMethods());
                for (int i = 0; i < declaredMethods.length; i++) {
                    Method method = declaredMethods[i];
                    int modifiers = method.getModifiers();

                    if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)) {
                        String name = method.getName();
                        List<Method> namedMethods = classMethods.get(name);

                        if (namedMethods == null) {
                            namedMethods = new ArrayList<>();
                            classMethods.put(name, namedMethods);
                        }

                        namedMethods.add(method);
                    }
                }
            }
            MethodCache cache = new MethodCache(classMethods, getClassMethodCache(type.getSuperclass()));
            globalMethodCache.put(type, cache);
            return cache;
        }
    }

    /**
     * Returns the Bean object this adapter wraps.
     *
     * @return The Bean object, or <tt>null</tt> if no Bean has been set.
     */
    public Object getBean() {
        return bean;
    }

    private Method getGetterMethod(String key) {
        Method getterMethod = localCache.getMethod(getMethodName(GET_PREFIX, key));

        if (getterMethod == null) {
            getterMethod = localCache.getMethod(getMethodName(IS_PREFIX, key));
        }

        return getterMethod;
    }

    private Method getSetterMethod(String key) {
        Class<?> type = getType(key);
        if (type == null) {
            throw new UnsupportedOperationException("Cannot determine type for property.");
        }

        return localCache.getMethod(getMethodName(SET_PREFIX, key), type);
    }
    
     private Method getJavaFxSetterMethod(String key) {
        Class<?> type = getType(key);
        if (type == null) {
            return null;
        }

        return localCache.getMethod(getMethodName(SET_PREFIX, key), type);
    }


    private static String getMethodName(String prefix, String key) {
        return prefix + Character.toUpperCase(key.charAt(0)) + key.substring(1);
    }

    /**
     * Invokes the getter method for the given property.
     *
     * @param key The property name.
     *
     * @return The value returned by the method, or <tt>null</tt> if no such
     * method exists.
     */
    @Override
    public Object get(Object key) {
        if (key == null) {
            throw new NullPointerException();
        }

        return get(key.toString());
    }

    private Object get(String key) {
        Method getterMethod = key.endsWith(PROPERTY_SUFFIX) ? localCache.getMethod(key) : getGetterMethod(key);

        Object value;
        if (getterMethod != null) {
            try {
                value = MethodUtil.invoke(getterMethod, bean, (Object[]) null);
            } catch (IllegalAccessException | InvocationTargetException exception) {
                throw new RuntimeException(exception);
            }
        } else {
            value = null;
        }

        return value;
    }

    /**
     * Invokes a setter method for the given property. The
     * {@link #coerce(Object, Class)} method is used as needed to attempt to
     * convert a given value to the property type, as defined by the return
     * value of the getter method.
     *
     * @param key The property name.
     *
     * @param value The new property value.
     *
     * @return Returns <tt>null</tt>, since returning the previous value would
     * require an unnecessary call to the getter method.
     *
     * is read-only.
     */
    @Override
    public Object put(String key, Object value) {
        if (key == null) {
            throw new NullPointerException();
        }

        Method setterMethod = getSetterMethod(key);

        try {
            MethodUtil.invoke(setterMethod, bean, new Object[]{coerce(value, getType(key))});
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }

        return null;
    }

    /**
     * Verifies the existence of a property.
     *
     * @param key The property name.
     *
     * @return
     * <tt>true</tt> if the property exists; <tt>false</tt>, otherwise.
     */
    @Override
    public boolean containsKey(Object key) {
        if (key == null) {
            throw new NullPointerException();
        }

        return getType(key.toString()) != null;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        throw new UnsupportedOperationException();
    }

    public static Set<String> getPropertyNames(Class<?> beanClass) {
        BeanInfo info;
        Set<String> nameSet = new HashSet<>();
        try {
            info = Introspector.getBeanInfo(beanClass);
            //System.err.println("info.getBeanDescriptor().getName()=" + info.getBeanDescriptor().getName());
            PropertyDescriptor[] pds = info.getPropertyDescriptors();
            for (PropertyDescriptor pd : pds) {
                nameSet.add(pd.getName());
            }
        } catch (IntrospectionException | IllegalArgumentException ex) {
            Logger.getLogger(TestTrashTray1.class.getName()).log(Level.SEVERE, null, ex);
        }
        //System.err.println("BeanAdapter() getPropertiesNames size() =  " + nameSet.size());
        return nameSet;
    }
    public static Set<String> getPropertyNames(Class<?> beanClass, Class<?> superClass) {
        BeanInfo info;
        Set<String> nameSet = new HashSet<>();
        try {
            info = Introspector.getBeanInfo(beanClass, superClass);
            //System.err.println("info.getBeanDescriptor().getName()=" + info.getBeanDescriptor().getName());
            PropertyDescriptor[] pds = info.getPropertyDescriptors();
            for (PropertyDescriptor pd : pds) {
                nameSet.add(pd.getName());
            }
        } catch (IntrospectionException | IllegalArgumentException ex) {
            Logger.getLogger(TestTrashTray1.class.getName()).log(Level.SEVERE, null, ex);
        }
        //System.err.println("BeanAdapter() getPropertiesNames size() =  " + nameSet.size());
        return nameSet;
    }

    /**
     * Tests the mutability of a property.
     *
     * @param key The property name.
     *
     * @return
     * <tt>true</tt> if the property is read-only; <tt>false</tt>, otherwise.
     */
    public boolean isReadOnly(String key) {
        if (key == null) {
            throw new NullPointerException();
        }

        return getSetterMethod(key) == null;
    }
 /**
     * Tests the mutability of a property.
     *
     * @param key The property name.
     *
     * @return
     * <tt>true</tt> if the property is read-only; <tt>false</tt>, otherwise.
     */
    public boolean isFxReadOnly(String key) {
        if (key == null) {
            throw new NullPointerException();
        }

        return getJavaFxSetterMethod(key) == null;
    }
    /**
     * Returns the property model for the given property.
     *
     * @param <T> specifies
     * @param key The property name.
     *
     * @return The named property model, or <tt>null</tt> if no such property
     * exists.
     */
    @SuppressWarnings("unchecked")
    public <T> ObservableValue<T> getPropertyModel(String key) {
        if (key == null) {
            throw new NullPointerException();
        }

        return (ObservableValue<T>) get(key + BeanAdapter.PROPERTY_SUFFIX);
    }

    /**
     * Returns the type of a property.
     *
     * @param key The property name.
     * @return the type
     */
    public Class<?> getType(String key) {
        if (key == null) {
            throw new NullPointerException();
        }

        Method getterMethod = getGetterMethod(key);

        return (getterMethod == null) ? null : getterMethod.getReturnType();
    }

    /**
     * Returns the generic type of a property.
     *
     * @param key The property name.
     * @return the object
     *
     */
    public Type getGenericType(String key) {
        if (key == null) {
            throw new NullPointerException();
        }

        Method getterMethod = getGetterMethod(key);

        return (getterMethod == null) ? null : getterMethod.getGenericReturnType();
    }

    @Override
    public boolean equals(Object object) {
        boolean equals = false;

        if (object instanceof BeanAdapter) {
            BeanAdapter beanAdapter = (BeanAdapter) object;
            equals = (bean == beanAdapter.bean);
        }

        return equals;
    }

    @Override
    public int hashCode() {
        return (bean == null) ? -1 : bean.hashCode();
    }

    /**
     * Coerces a value to a given type.
     *
     * @param <T> ??
     * @param value??
     * @param type ??
     *
     * @return The coerced value.
     */
    @SuppressWarnings("unchecked")
    public static <T> T coerce(Object value, Class<? extends T> type) {
        if (type == null) {
            throw new NullPointerException();
        }

        Object coercedValue = null;

        if (value == null) {
            // Null values can only be coerced to null
            coercedValue = null;
        } else if (type.isAssignableFrom(value.getClass())) {
            // Value doesn't require coercion
            coercedValue = value;
        } else if (type == Boolean.class
                || type == Boolean.TYPE) {
            coercedValue = Boolean.valueOf(value.toString());
        } else if (type == Character.class
                || type == Character.TYPE) {
            coercedValue = value.toString().charAt(0);
        } else if (type == Byte.class
                || type == Byte.TYPE) {
            if (value instanceof Number) {
                coercedValue = ((Number) value).byteValue();
            } else {
                coercedValue = Byte.valueOf(value.toString());
            }
        } else if (type == Short.class
                || type == Short.TYPE) {
            if (value instanceof Number) {
                coercedValue = ((Number) value).shortValue();
            } else {
                coercedValue = Short.valueOf(value.toString());
            }
        } else if (type == Integer.class
                || type == Integer.TYPE) {
            if (value instanceof Number) {
                coercedValue = ((Number) value).intValue();
            } else {
                coercedValue = Integer.valueOf(value.toString());
            }
        } else if (type == Long.class
                || type == Long.TYPE) {
            if (value instanceof Number) {
                coercedValue = ((Number) value).longValue();
            } else {
                coercedValue = Long.valueOf(value.toString());
            }
        } else if (type == BigInteger.class) {
            if (value instanceof Number) {
                coercedValue = BigInteger.valueOf(((Number) value).longValue());
            } else {
                coercedValue = new BigInteger(value.toString());
            }
        } else if (type == Float.class
                || type == Float.TYPE) {
            if (value instanceof Number) {
                coercedValue = ((Number) value).floatValue();
            } else {
                coercedValue = Float.valueOf(value.toString());
            }
        } else if (type == Double.class
                || type == Double.TYPE) {
            if (value instanceof Number) {
                coercedValue = ((Number) value).doubleValue();
            } else {
                coercedValue = Double.valueOf(value.toString());
            }
        } else if (type == Number.class) {
            String number = value.toString();
            if (number.contains(".")) {
                coercedValue = Double.valueOf(number);
            } else {
                coercedValue = Long.valueOf(number);
            }
        } else if (type == BigDecimal.class) {
            if (value instanceof Number) {
                coercedValue = BigDecimal.valueOf(((Number) value).doubleValue());
            } else {
                coercedValue = new BigDecimal(value.toString());
            }
        } else if (type == Class.class) {
            try {
                final String className = value.toString();
                //ReflectUtil.checkPackageAccess(className);
                checkPackageAccess(className);
                final ClassLoader cl = Thread.currentThread().getContextClassLoader();
                coercedValue = Class.forName(
                        className,
                        false,
                        cl);
            } catch (ClassNotFoundException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else {
            Class<?> valueType = value.getClass();
            Method valueOfMethod = null;

            while (valueOfMethod == null
                    && valueType != null) {
                try {
                    checkPackageAccess(type);
                    valueOfMethod = type.getDeclaredMethod(VALUE_OF_METHOD_NAME, valueType);
                } catch (NoSuchMethodException exception) {
                    // No-op
                }

                if (valueOfMethod == null) {
                    valueType = valueType.getSuperclass();
                }
            }

            if (valueOfMethod == null) {
                throw new IllegalArgumentException("Unable to coerce " + value + " to " + type + ".");
            }

            if (type.isEnum()
                    && value instanceof String
                    && Character.isLowerCase(((String) value).charAt(0))) {
                value = toAllCaps((String) value);
            }

            try {
                coercedValue = MethodUtil.invoke(valueOfMethod, null, new Object[]{value});
            } catch (IllegalAccessException | InvocationTargetException | SecurityException exception) {
                throw new RuntimeException(exception);
            }
        }

        return (T) coercedValue;
    }

    /**
     * Invokes the static getter method for the given property.
     *
     * @param <T> ???
     * @param target The object to which the property is attached.
     *
     * @param sourceType The class that defines the property.
     *
     * @param key The property name.
     *
     * @return The value returned by the method, or <tt>null</tt> if no such
     * method exists.
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Object target, Class<?> sourceType, String key) {
        T value = null;

        Class<?> targetType = target.getClass();
        Method getterMethod = getStaticGetterMethod(sourceType, key, targetType);

        if (getterMethod != null) {
            try {
                value = (T) MethodUtil.invoke(getterMethod, null, new Object[]{target});
            } catch (InvocationTargetException exception) {
                throw new RuntimeException(exception);
            } catch (IllegalAccessException exception) {
                throw new RuntimeException(exception);
            }
        }

        return value;
    }

    /**
     * Invokes a static setter method for the given property. If the value is
     * <tt>null</tt> or there is no explicit setter for a given type, the
     * {@link #coerce(Object, Class)} method is used to attempt to convert the
     * value to the actual property type (defined by the return value of the
     * getter method). throws PropertyNotFoundException If the given static
     * property does not exist or is read-only.
     *
     *
     * @param target The object to which the property is or will be attached.
     *
     * @param sourceType The class that defines the property.
     *
     * @param key The property name.
     *
     * @param value The new property value.
     *
     *
     */
    public static void put(Object target, Class<?> sourceType, String key, Object value) {
        Class<?> targetType = target.getClass();

        Method setterMethod = null;
        if (value != null) {
            setterMethod = getStaticSetterMethod(sourceType, key, value.getClass(), targetType);
        }

        if (setterMethod == null) {
            // Get the property type and attempt to coerce the value to it
            Class<?> propertyType = getType(sourceType, key, targetType);

            if (propertyType != null) {
                setterMethod = getStaticSetterMethod(sourceType, key, propertyType, targetType);
                value = coerce(value, propertyType);
            }
        }

        if (setterMethod == null) {
            //My 25.05 throw new PropertyNotFoundException("Static property \"" + key + "\" does not exist"
            //    + " or is read-only.");
        }

        // Invoke the setter
        try {
            MethodUtil.invoke(setterMethod, null, new Object[]{target, value});
        } catch (InvocationTargetException | IllegalAccessException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Tests the existence of a static property.
     *
     * @param sourceType The class that defines the property.
     *
     * @param key The property name.
     *
     * @param targetType The type of the object to which the property applies.
     *
     * @return  <tt>true</tt> if the property exists; <tt>false</tt>, otherwise.
     */
    public static boolean isDefined(Class<?> sourceType, String key, Class<?> targetType) {
        return (getStaticGetterMethod(sourceType, key, targetType) != null);
    }

    /**
     * Returns the type of a static property.
     *
     * @param sourceType The class that defines the property.
     *
     * @param key The property name.
     *
     * @param targetType The type of the object to which the property applies.
     * @return ???
     */
    public static Class<?> getType(Class<?> sourceType, String key, Class<?> targetType) {
        Method getterMethod = getStaticGetterMethod(sourceType, key, targetType);
        return (getterMethod == null) ? null : getterMethod.getReturnType();
    }

    /**
     * Returns the generic type of a static property.
     *
     * @param sourceType The class that defines the property.
     *
     * @param key The property name.
     *
     * @param targetType The type of the object to which the property applies.
     * @return ???
     */
    public static Type getGenericType(Class<?> sourceType, String key, Class<?> targetType) {
        Method getterMethod = getStaticGetterMethod(sourceType, key, targetType);
        return (getterMethod == null) ? null : getterMethod.getGenericReturnType();
    }

    /**
     * Determines the type of a list item.
     *
     * @param listType ??
     * @return ???
     */
    public static Class<?> getListItemType(Type listType) {
        Type itemType = getGenericListItemType(listType);

        if (itemType instanceof ParameterizedType) {
            itemType = ((ParameterizedType) itemType).getRawType();
        }

        return (Class<?>) itemType;
    }

    /**
     * Determines the type of a map value.
     *
     * @param mapType ???
     * @return ???
     */
    public static Class<?> getMapValueType(Type mapType) {
        Type valueType = getGenericMapValueType(mapType);

        if (valueType instanceof ParameterizedType) {
            valueType = ((ParameterizedType) valueType).getRawType();
        }

        return (Class<?>) valueType;
    }

    /**
     * Determines the type of a list item.
     *
     * @param listType ??
     * @return ??
     */
    public static Type getGenericListItemType(Type listType) {
        Type itemType = null;

        Type parentType = listType;
        while (parentType != null) {
            if (parentType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) parentType;
                Class<?> rawType = (Class<?>) parameterizedType.getRawType();

                if (List.class.isAssignableFrom(rawType)) {
                    itemType = parameterizedType.getActualTypeArguments()[0];
                }

                break;
            }

            Class<?> classType = (Class<?>) parentType;
            Type[] genericInterfaces = classType.getGenericInterfaces();

            for (int i = 0; i < genericInterfaces.length; i++) {
                Type genericInterface = genericInterfaces[i];

                if (genericInterface instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                    Class<?> interfaceType = (Class<?>) parameterizedType.getRawType();

                    if (List.class.isAssignableFrom(interfaceType)) {
                        itemType = parameterizedType.getActualTypeArguments()[0];
                        break;
                    }
                }
            }

            if (itemType != null) {
                break;
            }

            parentType = classType.getGenericSuperclass();
        }

        if (itemType != null && itemType instanceof TypeVariable<?>) {
            itemType = Object.class;
        }

        return itemType;
    }

    /**
     * Determines the type of a map value.
     *
     * @param mapType ???
     * @return ???
     */
    public static Type getGenericMapValueType(Type mapType) {
        Type valueType = null;

        Type parentType = mapType;
        while (parentType != null) {
            if (parentType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) parentType;
                Class<?> rawType = (Class<?>) parameterizedType.getRawType();

                if (Map.class.isAssignableFrom(rawType)) {
                    valueType = parameterizedType.getActualTypeArguments()[1];
                }

                break;
            }

            Class<?> classType = (Class<?>) parentType;
            Type[] genericInterfaces = classType.getGenericInterfaces();

            for (int i = 0; i < genericInterfaces.length; i++) {
                Type genericInterface = genericInterfaces[i];

                if (genericInterface instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                    Class<?> interfaceType = (Class<?>) parameterizedType.getRawType();

                    if (Map.class.isAssignableFrom(interfaceType)) {
                        valueType = parameterizedType.getActualTypeArguments()[1];
                        break;
                    }
                }
            }

            if (valueType != null) {
                break;
            }

            parentType = classType.getGenericSuperclass();
        }

        if (valueType != null && valueType instanceof TypeVariable<?>) {
            valueType = Object.class;
        }

        return valueType;
    }

    /**
     * Returns the value of a named constant.
     *
     * @param type The type that defines the constant.
     *
     * @param name The name of the constant.
     * @return ???
     * @throws java.lang.NoSuchFieldException ???
     */
    public static Object getConstantValue(Class<?> type, String name) throws NoSuchFieldException {
        if (type == null) {
            throw new IllegalArgumentException();
        }

        if (name == null) {
            throw new IllegalArgumentException();
        }

        Field field;
        field = getField(type, name);

        int fieldModifiers = field.getModifiers();
        if ((fieldModifiers & Modifier.STATIC) == 0
                || (fieldModifiers & Modifier.FINAL) == 0) {
            throw new IllegalArgumentException("Field is not a constant.");
        }

        Object value;
        try {
            value = field.get(null);
        } catch (IllegalAccessException exception) {
            throw new IllegalArgumentException(exception);
        }

        return value;
    }

    private static Method getStaticGetterMethod(Class<?> sourceType, String key,
            Class<?> targetType) {
        if (sourceType == null) {
            throw new NullPointerException();
        }

        if (key == null) {
            throw new NullPointerException();
        }

        Method method = null;

        if (targetType != null) {
            key = Character.toUpperCase(key.charAt(0)) + key.substring(1);

            String getMethodName = GET_PREFIX + key;
            String isMethodName = IS_PREFIX + key;

            try {
                method = MethodUtil.getMethod(sourceType, getMethodName, new Class[]{targetType});
            } catch (NoSuchMethodException exception) {
                // No-op
            }

            if (method == null) {
                try {
                    method = MethodUtil.getMethod(sourceType, isMethodName, new Class[]{targetType});
                } catch (NoSuchMethodException exception) {
                    // No-op
                }
            }

            // Check for interfaces
            if (method == null) {
                Class<?>[] interfaces = targetType.getInterfaces();
                for (int i = 0; i < interfaces.length; i++) {
                    try {
                        method = MethodUtil.getMethod(sourceType, getMethodName, new Class[]{interfaces[i]});
                    } catch (NoSuchMethodException exception) {
                        // No-op
                    }

                    if (method == null) {
                        try {
                            method = MethodUtil.getMethod(sourceType, isMethodName, new Class[]{interfaces[i]});
                        } catch (NoSuchMethodException exception) {
                            // No-op
                        }
                    }

                    if (method != null) {
                        break;
                    }
                }
            }

            if (method == null) {
                method = getStaticGetterMethod(sourceType, key, targetType.getSuperclass());
            }
        }

        return method;
    }

    private static Method getStaticSetterMethod(Class<?> sourceType, String key,
            Class<?> valueType, Class<?> targetType) {
        if (sourceType == null) {
            throw new NullPointerException();
        }

        if (key == null) {
            throw new NullPointerException();
        }

        if (valueType == null) {
            throw new NullPointerException();
        }

        Method method = null;

        if (targetType != null) {
            key = Character.toUpperCase(key.charAt(0)) + key.substring(1);

            String setMethodName = SET_PREFIX + key;
            try {
                method = MethodUtil.getMethod(sourceType, setMethodName, new Class[]{targetType, valueType});
            } catch (NoSuchMethodException exception) {
                // No-op
            }

            // Check for interfaces
            if (method == null) {
                Class<?>[] interfaces = targetType.getInterfaces();
                for (int i = 0; i < interfaces.length; i++) {
                    try {
                        method = MethodUtil.getMethod(sourceType, setMethodName, new Class[]{interfaces[i], valueType});
                    } catch (NoSuchMethodException exception) {
                        // No-op
                    }

                    if (method != null) {
                        break;
                    }
                }
            }

            if (method == null) {
                method = getStaticSetterMethod(sourceType, key, valueType, targetType.getSuperclass());
            }
        }

        return method;
    }

    private static String toAllCaps(String value) {
        if (value == null) {
            throw new NullPointerException();
        }

        StringBuilder allCapsBuilder = new StringBuilder();

        for (int i = 0, n = value.length(); i < n; i++) {
            char c = value.charAt(i);

            if (Character.isUpperCase(c)) {
                allCapsBuilder.append('_');
            }

            allCapsBuilder.append(Character.toUpperCase(c));
        }

        return allCapsBuilder.toString();
    }
}
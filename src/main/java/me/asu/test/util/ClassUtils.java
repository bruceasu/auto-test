package me.asu.test.util;

/**
 *
 * @author suk
 * @version 1.0.0
 * @since 2017-10-21 9:44
 */

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassUtils {

    private static ExtClasspathLoader extClasspathLoader = new ExtClasspathLoader();
    private static ClassLoader cacheClassLoader = ClassUtils.class.getClassLoader();
    private static final Class<?>[] EMPTY_ARRAY;
    private static final Map<Class<?>, Constructor<?>> CONSTRUCTOR_CACHE;

    static {
        if (cacheClassLoader == null) {
            try {
                cacheClassLoader = ClassLoader.getSystemClassLoader();
            } catch (Throwable localThrowable) {
            }

        }

        EMPTY_ARRAY = new Class[0];

        CONSTRUCTOR_CACHE = new ConcurrentHashMap();
    }

    public static ClassLoader getDefaultClassLoader() {
        return cacheClassLoader;
    }


    public static ClassLoader getClassLoader() {
        return cacheClassLoader;
    }

    public static String getClassName(InputStream in) {
        DataInputStream dis;
        try {
            dis = new DataInputStream(new BufferedInputStream(in));

            Map strs = new HashMap();
            Map classes = new HashMap();
            dis.skipBytes(4);
            dis.skipBytes(2);
            dis.skipBytes(2);

            int constantPoolCount = dis.readUnsignedShort();
            for (int i = 0; i < constantPoolCount - 1; ++i) {
                byte flag = dis.readByte();
                switch (flag) {
                    case 7:
                        int index = dis.readUnsignedShort();
                        classes.put(Integer.valueOf(i + 1), Integer.valueOf(index));
                        break;
                    case 9:
                    case 10:
                    case 11:
                        dis.skipBytes(2);
                        dis.skipBytes(2);
                        break;
                    case 8:
                        dis.skipBytes(2);
                        break;
                    case 3:
                    case 4:
                        dis.skipBytes(4);
                        break;
                    case 5:
                    case 6:
                        dis.skipBytes(8);
                        ++i;
                        break;
                    case 12:
                        dis.skipBytes(2);
                        dis.skipBytes(2);
                        break;
                    case 1:
                        int len = dis.readUnsignedShort();
                        byte[] data = new byte[len];
                        dis.readFully(data);
                        strs.put(Integer.valueOf(i + 1), new String(data, "UTF-8"));
                        break;
                    case 15:
                        dis.skipBytes(1);
                        dis.skipBytes(2);
                        break;
                    case 16:
                        dis.skipBytes(2);
                        break;
                    case 18:
                        dis.skipBytes(2);
                        dis.skipBytes(2);
                        break;
                    case 2:
                    case 13:
                    case 14:
                    case 17:
                    default:
                        throw new RuntimeException("Impossible!! flag=" + flag);
                }
            }

            dis.skipBytes(2);
            int pos = dis.readUnsignedShort();
            String name = (String) strs.get(classes.get(Integer.valueOf(pos)));
            if (name != null) {
                name = name.replace('/', '.');
            }

            dis.close();
            return name;
        } catch (Throwable e) {
            System.err.println("Fail to read ClassName from class InputStream");
            e.printStackTrace();
        }
        return null;
    }

    public static Class<?> getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String objectId(Object o) {
        if (o == null) {
            return "null";
        }

        return o.getClass().getName() + "@" + System.identityHashCode(o);
    }

    public static <T> T newInstance(Class<T> theClass) {
        Object result;
        Constructor meth;
        try {
            meth = (Constructor) CONSTRUCTOR_CACHE.get(theClass);
            if (meth == null) {
                meth = theClass.getDeclaredConstructor(EMPTY_ARRAY);
                meth.setAccessible(true);
                CONSTRUCTOR_CACHE.put(theClass, meth);
            }
            result = meth.newInstance(new Object[0]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return (T) result;
    }

    public static <T> T newInstance(String className) {
        Class<T> c;
        try {
            c = (Class<T>) Class.forName(className);
            return newInstance(c);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void addToClasspath(File path) {
        extClasspathLoader.addURL(path);
    }

    public static void addToClasspath(String path) {
        extClasspathLoader.addURL(path);
    }
    public static void addJarToClasspath(String path) {
        extClasspathLoader.loadJarToClasspath(path);
    }

    public static void addToClasspath(URL path) {
        extClasspathLoader.addURL(path);
    }

    public static void addToClasspathRecursion(String path, boolean isDir) {
        if (isDir) {
            extClasspathLoader.loadResourceDirToClasspath(path);
        } else {
            extClasspathLoader.loadJarToClasspath(path);
        }
    }

}

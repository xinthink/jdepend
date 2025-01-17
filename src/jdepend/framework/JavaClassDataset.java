package jdepend.framework;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The <code>JavaClassDataset</code> class represents a collection of JavaClass instances.
 * It also contains a map of JavaClass to module name.
 *
 * @since 2.11
 */
public class JavaClassDataset {
    /**
     * Collection of JavaClass instances.
     */
    private final Map<String, JavaClass> javaClasses;

    /**
     * Map of JavaClass to module name.
      */
    private final Map<String, String> javaClassModule;

    public JavaClassDataset() {
        javaClasses = new HashMap<>();
        javaClassModule = new HashMap<>();
    }

    public Map<String, JavaClass> getJavaClasses() {
        return javaClasses;
    }

    public void addJavaClass(JavaClass javaClass) {
        javaClasses.put(javaClass.getName(), javaClass);
    }

    /**
     * @return Map of JavaClass to module name.
     */
    public Map<String, String> getJavaClassModule() {
        return javaClassModule;
    }

    public void putJavaClassModule(JavaClass javaClass, String moduleName) {
//        System.out.println("map " + javaClass.getName() + " to " + moduleName);
        javaClassModule.put(javaClass.getName(), moduleName);
    }
}

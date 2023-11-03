package jdepend.framework;

import java.util.*;

/**
 * The <code>JavaClass</code> class represents a Java 
 * class or interface.
 * 
 * @author <b>Mike Clark</b>
 * @author Clarkware Consulting, Inc.
 */

public class JavaClass {

    private String className;
    private String packageName;
    private boolean isAbstract;
    private final HashMap<String, JavaPackage> imports;
    private String sourceFile;

    /**
     * The collection of classes on which this class depends.
     */
    private final Map<String, Integer> dependencies;

    public JavaClass(String name) {
        className = name;
        packageName = "default";
        isAbstract = false;
        imports = new HashMap<>();
        dependencies = new HashMap<>();
        sourceFile = "Unknown";
    }

    public void setName(String name) {
        className = name;
    }

    public String getName() {
        return className;
    }

    public void setPackageName(String name) {
        packageName = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setSourceFile(String name) {
        sourceFile = name;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public Collection<JavaPackage> getImportedPackages() {
        return imports.values();
    }

    public void addImportedPackage(JavaPackage jPackage) {
        if (!jPackage.getName().equals(getPackageName())) {
            imports.put(jPackage.getName(), jPackage);
        }
    }

    /**
     * Increase the dependency to the specified class.
     *
     * @param className Name of the class on which this class depends.
     */
    public void addDependency(String className) {
        if (className == null || className.isBlank()) {
            return;
        }

        Integer count = dependencies.get(className);
        count = count == null ? 1 : count + 1;
        dependencies.put(className, count);
    }

    /**
     * Returns classes on which this class depends.
     */
    public Map<String, Integer> getDependencies() {
        return dependencies;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public void isAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }

    public boolean equals(Object other) {

        if (other instanceof JavaClass) {
            JavaClass otherClass = (JavaClass) other;
            return otherClass.getName().equals(getName());
        }

        return false;
    }

    public int hashCode() {
        return getName().hashCode();
    }

    public static class ClassComparator implements Comparator {

        public int compare(Object a, Object b) {
            JavaClass c1 = (JavaClass) a;
            JavaClass c2 = (JavaClass) b;

            return c1.getName().compareTo(c2.getName());
        }
    }
}

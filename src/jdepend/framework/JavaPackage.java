package jdepend.framework;

import java.util.*;

/**
 * The <code>JavaPackage</code> class represents a Java package.
 *
 * @author <b>Mike Clark</b>
 * @author Clarkware Consulting, Inc.
 */

public class JavaPackage {

    private final String name;
    private int volatility;
    private final HashSet<JavaClass> classes;
    private final Map<JavaPackage, Integer> afferents;
    private final Map<JavaPackage, Integer> efferents;


    public JavaPackage(String name) {
        this(name, 1);
    }

    public JavaPackage(String name, int volatility) {
        this.name = name;
        setVolatility(volatility);
        classes = new HashSet<>();
        afferents = new HashMap<>();
        efferents = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    /**
     * @return The package's volatility (0-1).
     */
    public int getVolatility() {
        return volatility;
    }

    /**
     * @param v Volatility (0-1).
     */
    public void setVolatility(int v) {
        volatility = v;
    }

    public boolean containsCycle() {
        return collectCycle(new ArrayList());
    }

    /**
     * Collects the packages participating in the first package dependency cycle
     * detected which originates from this package.
     *
     * @param list Collecting object to be populated with the list of
     *            JavaPackage instances in a cycle.
     * @return <code>true</code> if a cycle exist; <code>false</code>
     *         otherwise.
     */
    public boolean collectCycle(List list) {

        if (list.contains(this)) {
            list.add(this);
            return true;
        }

        list.add(this);

        for (Iterator i = getEfferents().iterator(); i.hasNext();) {
            JavaPackage efferent = (JavaPackage)i.next();
            if (efferent.collectCycle(list)) {
                return true;
            }
        }

        list.remove(this);

        return false;
    }

    /**
     * Collects all the packages participating in a package dependency cycle
     * which originates from this package.
     * <p>
     * This is a more exhaustive search than that employed by
     * <code>collectCycle</code>.
     *
     * @param list Collecting object to be populated with the list of
     *            JavaPackage instances in a cycle.
     * @return <code>true</code> if a cycle exist; <code>false</code>
     *         otherwise.
     */
    public boolean collectAllCycles(List list) {

        if (list.contains(this)) {
            list.add(this);
            return true;
        }

        list.add(this);

        boolean containsCycle = false;
        for (Iterator i = getEfferents().iterator(); i.hasNext();) {
            JavaPackage efferent = (JavaPackage)i.next();
            if (efferent.collectAllCycles(list)) {
                containsCycle = true;
            }
        }

        if (containsCycle) {
            return true;
        }

        list.remove(this);
        return false;
    }

    public void addClass(JavaClass clazz) {
        classes.add(clazz);
    }

    public Collection<JavaClass> getClasses() {
        return classes;
    }

    public int getClassCount() {
        return classes.size();
    }

    public int getAbstractClassCount() {
        int count = 0;

        for (Iterator i = classes.iterator(); i.hasNext();) {
            JavaClass clazz = (JavaClass)i.next();
            if (clazz.isAbstract()) {
                count++;
            }
        }

        return count;
    }

    public int getConcreteClassCount() {
        int count = 0;

        for (Iterator i = classes.iterator(); i.hasNext();) {
            JavaClass clazz = (JavaClass)i.next();
            if (!clazz.isAbstract()) {
                count++;
            }
        }

        return count;
    }

    /**
     * Adds the specified Java package as an `efferent` of this package
     * and adds this package as an `afferent` of it.
     *
     * @param imported Java package.
     * @param count Number of references.
     */
    public void dependsUpon(JavaPackage imported, int count) {
        addEfferent(imported, count);
        imported.addAfferent(this, count);
    }

    /**
     * Adds the specified Java package as an `afferent` of this package.
     *
     * @param pkg Java package.
     * @param count Number of references.
     */
    private void addAfferent(JavaPackage pkg, int count) {
        if (!equals(pkg)) {
            Integer c = afferents.get(pkg);
            afferents.put(pkg, count + (c == null ? 0 : c));
        }
    }

    public Collection<JavaPackage> getAfferents() {
        return afferents.keySet();
    }

    public Integer getAfferentCount(JavaPackage pkg) {
        return afferents.get(pkg);
    }

    public void setAfferents(Collection<JavaPackage> afferents) {
        this.afferents.clear();
        for (JavaPackage p : afferents) {
            this.afferents.put(p, 1);
        }
    }

    private void addEfferent(JavaPackage pkg, int count) {
        if (!equals(pkg)) {
            Integer c = efferents.get(pkg);
            efferents.put(pkg, count + (c == null ? 0 : c));
        }
    }

    public Collection<JavaPackage> getEfferents() {
        return efferents.keySet();
    }

    public Integer getEfferentCount(JavaPackage pkg) {
        return efferents.get(pkg);
    }

    public void setEfferents(Collection<JavaPackage> efferents) {
        this.efferents.clear();
        for (JavaPackage p : efferents) {
            this.efferents.put(p, 1);
        }
    }

    /**
     * @return The afferent coupling (Ca) of this package.
     */
    public int afferentCoupling() {
        return afferents.values().stream().reduce(0, Integer::sum);
    }

    /**
     * @return The efferent coupling (Ce) of this package.
     */
    public int efferentCoupling() {
        return efferents.values().stream().reduce(0, Integer::sum);
    }

    /**
     * @return Instability (0-1).
     */
    public float instability() {

        float totalCoupling = (float) efferentCoupling()
                + (float) afferentCoupling();

        if (totalCoupling > 0) {
            return efferentCoupling()/totalCoupling;
        }

        return 0;
    }

    /**
     * @return The package's abstractness (0-1).
     */
    public float abstractness() {

        if (getClassCount() > 0) {
            return (float) getAbstractClassCount() / (float) getClassCount();
        }

        return 0;
    }

    /**
     * @return The package's distance from the main sequence (D).
     */
    public float distance() {
        float d = Math.abs(abstractness() + instability() - 1);
        return d * volatility;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        return other instanceof JavaPackage &&
                ((JavaPackage) other).name.equals(name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
    	return name;
    }
}

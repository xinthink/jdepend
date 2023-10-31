package jdepend.framework;

/**
 * The <code>Module</code> class represents a Java module.
 *
 * @since 2.11
 */
public class Module extends JavaPackage {

    public Module(String name) {
        super(name);
    }

    public Module(String name, int volatility) {
        super(name, volatility);
    }
}

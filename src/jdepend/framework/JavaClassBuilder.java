package jdepend.framework;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.*;
import java.util.zip.*;

/**
 * The <code>JavaClassBuilder</code> builds <code>JavaClass</code> 
 * instances from .class, .jar, .war, or .zip files.
 * 
 * @author <b>Mike Clark</b>
 * @author Clarkware Consulting, Inc.
 */

public class JavaClassBuilder {

    private AbstractParser parser;
    private FileManager fileManager;

    private JavaClassDataset dataset;

    public JavaClassBuilder() {
        this(new ClassFileParser(), new FileManager());
    }

    public JavaClassBuilder(FileManager fm) {
        this(new ClassFileParser(), fm);
    }

    public JavaClassBuilder(AbstractParser parser, FileManager fm) {
        this.parser = parser;
        this.fileManager = fm;
    }

    public int countClasses() {
        AtomicInteger count = new AtomicInteger(0);
        AbstractParser counter = new AbstractParser() {

            public JavaClass parse(InputStream is) {
                count.incrementAndGet();
                return null;
            }
        };

        new JavaClassBuilder(counter, fileManager).build();
        return count.get();
    }

    /**
     * Builds the <code>JavaClass</code> instances.
     * 
     * @return A <code>JavaClassDataset</code> contains classes and their modules.
     */
    public JavaClassDataset build() {

        JavaClassDataset dataset = new JavaClassDataset();

        for (Iterator i = fileManager.extractFiles().iterator(); i.hasNext();) {

            File nextFile = (File)i.next();

            try {

                buildClasses(nextFile, dataset);

            } catch (IOException ioe) {
                System.err.println("\n" + ioe.getMessage());
            }
        }

        return dataset;
    }

    /**
     * Builds the <code>JavaClass</code> instances from the
     * specified file.
     *
     * @param file    Class or Jar file.
     * @param dataset The dataset to be populated with the classes.
     */
    public void buildClasses(File file, JavaClassDataset dataset) throws IOException {

        if (fileManager.acceptClassFile(file)) {
            try (InputStream is = new FileInputStream(file)) {
                JavaClass parsedClass = parser.parse(is);
                if (parsedClass != null) {
                    dataset.addJavaClass(parsedClass);
                }
            }
        } else if (fileManager.acceptJarFile(file)) {

            JarFile jarFile = new JarFile(file);
            buildClasses(jarFile, dataset);
            jarFile.close();

        } else {
            throw new IOException("File is not a valid " + 
                ".class, .jar, .war, or .zip file: " + 
                file.getPath());
        }
    }

    /**
     * Builds the <code>JavaClass</code> instances from the specified 
     * jar, war, or zip file.
     * 
     * @param file Jar, war, or zip file.
     * @param dataset The dataset to be populated with the classes.
     */
    public void buildClasses(JarFile file, JavaClassDataset dataset) throws IOException {

        Enumeration<JarEntry> entries = file.entries();
        while (entries.hasMoreElements()) {
            ZipEntry e = entries.nextElement();
            if (fileManager.acceptClassFileName(e.getName())) {
                try (InputStream is = new BufferedInputStream(file.getInputStream(e))) {
                    JavaClass jc = parser.parse(is);
                    if (jc != null) {
                        dataset.addJavaClass(jc);
                    }
                } catch (IOException ioe) {
                    System.out.println("Failed loading " + e.getName() + " in " + file.getName() + ": " + ioe);
                }
            } else if (fileManager.acceptJarFileName(e.getName())) {
                parseJarEntry(file, e, dataset);
            }
        }
    }

    private void parseJarEntry(JarFile file, ZipEntry jarEntry, JavaClassDataset dataset) {
        try (final ZipInputStream zip = new ZipInputStream(file.getInputStream(jarEntry))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (fileManager.acceptClassFileName(entry.getName())) {
                    JavaClass jc = parser.parse(zip);
                    if (jc != null) {
                        dataset.addJavaClass(jc);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Failed loading " + jarEntry.getName() + " in " + file.getName() + ": " + e);
        }
    }
}

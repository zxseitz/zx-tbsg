package ch.zxseitz.tbsg.server.games;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

public class JarLoader extends ClassLoader {
    private final Map<String, byte[]> classes;
    private final Map<String, byte[]> resources;
    private final Manifest manifest;

    public JarLoader(Path jar) throws IOException {
        this(jar, getSystemClassLoader());
    }

    public JarLoader(Path jar, ClassLoader parent) throws IOException {
        super(parent);
        if (jar == null) {
            throw new IllegalArgumentException("jar path is null");
        }

        this.classes = new HashMap<>();
        this.resources = new HashMap<>();

        try (var in = new JarInputStream(Files.newInputStream(jar));
             var out = new ByteArrayOutputStream()) {
            this.manifest = in.getManifest();
            var buf = new byte[1024];
            int bytesRead;
            var jarEntry = in.getNextJarEntry();
            while (jarEntry != null) {
                var name = jarEntry.getName();
                out.reset();
                while ((bytesRead = in.read(buf, 0, buf.length)) != -1) {
                    out.write(buf, 0, bytesRead);
                }
                out.flush();
                if (out.size() > 0) {
                    if (name.endsWith(".class")) {
                        classes.put(name.replace('/', '.'), out.toByteArray());
                    } else {
                        resources.put(name, out.toByteArray());
                    }
                }
                jarEntry = in.getNextJarEntry();
            }
        }
    }

    public Set<String> listAllClasses() {
        return classes.keySet();
    }

    public boolean containsClass(String name) {
        return classes.containsKey(name);
    }

    public Set<String> listAllResources() {
        return resources.keySet();
    }

    public boolean containsResource(String name) {
        return resources.containsKey(name);
    }

    /**
     * Find the class with the given name.
     *
     * @param name Class name.
     * @return Class object.
     * @throws ClassNotFoundException Thrown if the class could not be found.
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        var classBytes = classes.get(name);
        if (classBytes != null) {
            return defineClass(name, classBytes, 0, classBytes.length);
        }
        return this.getParent().loadClass(name);
    }

    // todo filter
    public Set<Class<?>> loadAllClasses() {
        var classes = new HashSet<Class<?>>();
        this.classes.forEach((name, classBytes) -> {
            var clazz = defineClass(name, classBytes, 0, classBytes.length);
            classes.add(clazz);
        });
        return classes;
    }

    public void loadEachClass(Consumer<Class<?>> action) {
        this.classes.forEach((name, classBytes) -> {
            var clazz = defineClass(name, classBytes, 0, classBytes.length);
            action.accept(clazz);
        });
    }

    /**
     * Get an input stream for reading the given resource.
     *
     * @param name Resource name.
     * @return Input stream, or null if the resource could not be found.
     */
    @Override
    public InputStream getResourceAsStream(String name) {
        var resource = resources.get(name);
        if (resource != null) {
            return new ByteArrayInputStream(resource);
        }
        return getParent().getResourceAsStream(name);
    }

    public Manifest getManifest() {
        return manifest;
    }
}

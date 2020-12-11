package ch.zxseitz.tbsg.server.games;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarInputStream;

public class GameLoader extends ClassLoader {
    private final Map<String, byte[]> resources;

    public GameLoader(Path jar) throws IOException {
        super();
        this.resources = new HashMap<>();
        if (jar == null) {
            throw new IllegalArgumentException("jar path is null");
        }
        try (var in = new JarInputStream(Files.newInputStream(jar));
             var out = new ByteArrayOutputStream()) {
            var buf = new byte[1024];
            int bytesRead;
            var jarEntry = in.getNextJarEntry();
            while (jarEntry != null) {
                out.reset();
                while ((bytesRead  = in.read(buf, 0, buf.length)) != -1) {
                    out.write(buf, 0, bytesRead );
                }
                out.flush();
                resources.put(jarEntry.getName().replace('/', '.'), out.toByteArray());
                jarEntry = in.getNextJarEntry();
            }
        }
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
        var resource = resources.get(name);
        if (resource != null) {
            return defineClass(name, resource, 0, resource.length);
        }
        return this.getParent().loadClass(name);
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

    public GameProxy createGame(Path jar) {
        //todo implement
        return null;
    }
}

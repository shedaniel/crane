package dev.architectury.crane.bootstrap;

import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.transformer.WhyIsThisPackagePrivate;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Enumeration;

public class BootstrapperClassLoader extends ClassLoader {
    public static BootstrapperClassLoader classLoader;
    public final ClassLoader parent;
    private final WhyIsThisPackagePrivate why;
    
    public BootstrapperClassLoader(ClassLoader parent) {
        this.parent = parent;
        classLoader = this;
        MixinBootstrap.init();
        why = new WhyIsThisPackagePrivate();
    }
    
    @Override
    public InputStream getResourceAsStream(String name) {
        return parent.getResourceAsStream(name);
    }
    
    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return parent.getResources(name);
    }
    
    @Override
    public URL getResource(String name) {
        return parent.getResource(name);
    }
    
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);
            if (c == null && !name.startsWith("java") && !name.startsWith("jdk") && !name.startsWith("com.google.")) {
                byte[] bytes = getClassBytes(name);
                if (bytes == null) return null;
                bytes = why.transformClassBytes(name, name, bytes);
                c = defineClass(name, bytes, 0, bytes.length);
            }
            if (c == null) {
                c = parent.loadClass(name);
            }
            return c;
        }
    }
    
    public byte[] getClassBytes(String name) {
        InputStream stream = getResourceAsStream(name.replace('.', '/') + ".class");
        if (stream == null) return null;
        try {
            return IOUtils.toByteArray(stream);
        } catch (IOException e) {
            throw new UncheckedIOException(name, e);
        }
    }
    
    public Class<?> findLoadedClassA(String name) {
        return findLoadedClass(name);
    }
}

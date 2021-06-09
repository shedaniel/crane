package dev.architectury.mojmapextensions.bootstrap;

import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

public class EnigmaBootstrapper {
    public static void main(String[] args) throws Throwable {
        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            BootstrapperPropertyService.PROPERTIES.put((String) entry.getKey(), entry.getValue());
        }
        BootstrapperClassLoader classLoader = new BootstrapperClassLoader(Thread.currentThread().getContextClassLoader());
        Mixins.addConfiguration("mojmapextensions.mixins.json");
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            Method m = MixinEnvironment.class.getDeclaredMethod("gotoPhase", MixinEnvironment.Phase.class);
            m.setAccessible(true);
            m.invoke(null, MixinEnvironment.Phase.INIT);
            m.invoke(null, MixinEnvironment.Phase.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        MethodHandles.lookup().findStatic(classLoader.loadClass(args[0]), "main", MethodType.methodType(Void.TYPE, String[].class))
                .invoke((Object) Arrays.stream(args).skip(1).toArray(String[]::new));
    }
}

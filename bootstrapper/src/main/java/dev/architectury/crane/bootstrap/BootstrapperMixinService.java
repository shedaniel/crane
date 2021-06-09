package dev.architectury.crane.bootstrap;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.platform.container.ContainerHandleURI;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.service.*;
import org.spongepowered.asm.util.ReEntranceLock;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;

/*
 * Copyright 2016 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class BootstrapperMixinService implements IMixinService, IClassProvider, IClassBytecodeProvider {
    private final ReEntranceLock lock = new ReEntranceLock(1);
    
    @Override
    public String getName() {
        return "EnigmaBootstrapper";
    }
    
    @Override
    public boolean isValid() {
        return true;
    }
    
    @Override
    public void prepare() {
        
    }
    
    @Override
    public MixinEnvironment.Phase getInitialPhase() {
        return MixinEnvironment.Phase.PREINIT;
    }
    
    @Override
    public void init() {
        
    }
    
    @Override
    public void beginPhase() {
        
    }
    
    @Override
    public void checkEnv(Object bootSource) {
        
    }
    
    @Override
    public ReEntranceLock getReEntranceLock() {
        return lock;
    }
    
    @Override
    public IClassProvider getClassProvider() {
        return this;
    }
    
    @Override
    public IClassBytecodeProvider getBytecodeProvider() {
        return this;
    }
    
    @Override
    public ITransformerProvider getTransformerProvider() {
        return null;
    }
    
    @Override
    public IClassTracker getClassTracker() {
        return null;
    }
    
    @Override
    public IMixinAuditTrail getAuditTrail() {
        return null;
    }
    
    @Override
    public Collection<String> getPlatformAgents() {
        return Collections.emptyList();
    }
    
    @Override
    public IContainerHandle getPrimaryContainer() {
        try {
            return new ContainerHandleURI(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public Collection<IContainerHandle> getMixinContainers() {
        return Collections.emptyList();
    }
    
    @Override
    public InputStream getResourceAsStream(String name) {
        return BootstrapperClassLoader.classLoader.getResourceAsStream(name);
    }
    
    @Override
    public String getSideName() {
        return "client";
    }
    
    @Override
    public MixinEnvironment.CompatibilityLevel getMinCompatibilityLevel() {
        return MixinEnvironment.CompatibilityLevel.JAVA_8;
    }
    
    @Override
    public MixinEnvironment.CompatibilityLevel getMaxCompatibilityLevel() {
        return MixinEnvironment.CompatibilityLevel.JAVA_17;
    }
    
    @Override
    public ClassNode getClassNode(String name) throws ClassNotFoundException, IOException {
        return getClassNode(name, true);
    }
    
    @Override
    public ClassNode getClassNode(String name, boolean runTransformers) throws ClassNotFoundException, IOException {
        ClassReader reader = new ClassReader(getClassBytes(name, runTransformers));
        ClassNode node = new ClassNode();
        reader.accept(node, 0);
        return node;
    }
    
    public byte[] getClassBytes(String name, boolean runTransformers) throws ClassNotFoundException, IOException {
        byte[] classBytes = BootstrapperClassLoader.classLoader.getClassBytes(name);
        
        if (classBytes != null) {
            return classBytes;
        } else {
            throw new ClassNotFoundException(name);
        }
    }
    
    
    @Override
    public URL[] getClassPath() {
        return new URL[0];
    }
    
    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return BootstrapperClassLoader.classLoader.loadClass(name);
    }
    
    @Override
    public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, BootstrapperClassLoader.classLoader);
    }
    
    @Override
    public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, BootstrapperClassLoader.class.getClassLoader());
    }
}

package dev.architectury.mojmapextensions.bootstrap;

import org.spongepowered.asm.service.IGlobalPropertyService;
import org.spongepowered.asm.service.IPropertyKey;

import java.util.HashMap;
import java.util.Map;

public class BootstrapperPropertyService implements IGlobalPropertyService {
    public static final Map<String, Object> PROPERTIES = new HashMap<>();
    
    @Override
    public IPropertyKey resolveKey(String name) {
        return new Property(name);
    }
    
    @Override
    public <T> T getProperty(IPropertyKey key) {
        return (T) PROPERTIES.get(((Property) key).key);
    }
    
    @Override
    public void setProperty(IPropertyKey key, Object value) {
        PROPERTIES.put(((Property) key).key, value);
    }
    
    @Override
    public <T> T getProperty(IPropertyKey key, T defaultValue) {
        return (T) PROPERTIES.getOrDefault(((Property) key).key, defaultValue);
    }
    
    @Override
    public String getPropertyString(IPropertyKey key, String defaultValue) {
        Object get = PROPERTIES.get(((Property) key).key);
        return get == null ? defaultValue : get.toString();
    }
    
    private record Property(String key) implements IPropertyKey {
        @Override
        public String toString() {
            return key;
        }
    }
}

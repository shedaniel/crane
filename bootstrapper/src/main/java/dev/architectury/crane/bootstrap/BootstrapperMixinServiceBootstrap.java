package dev.architectury.crane.bootstrap;

import org.spongepowered.asm.service.IMixinServiceBootstrap;

public class BootstrapperMixinServiceBootstrap implements IMixinServiceBootstrap {
    @Override
    public String getName() {
        return "EnigmaBootstrapper";
    }
    
    @Override
    public String getServiceClassName() {
        return "dev.architectury.crane.bootstrap.BootstrapperMixinService";
    }
    
    @Override
    public void bootstrap() {
        
    }
}

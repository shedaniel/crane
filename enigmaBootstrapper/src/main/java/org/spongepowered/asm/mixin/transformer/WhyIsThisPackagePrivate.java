package org.spongepowered.asm.mixin.transformer;

public class WhyIsThisPackagePrivate {
    private final MixinTransformer transformer = new MixinTransformer();
    
    public byte[] transformClassBytes(String name, String transformedName, byte[] basicClass) {
        return transformer.transformClassBytes(name, transformedName, basicClass);
    }
}

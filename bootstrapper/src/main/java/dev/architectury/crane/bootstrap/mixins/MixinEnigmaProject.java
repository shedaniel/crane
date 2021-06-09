package dev.architectury.crane.bootstrap.mixins;

import cuchaz.enigma.EnigmaProject;
import cuchaz.enigma.analysis.EntryReference;
import cuchaz.enigma.translation.representation.entry.Entry;
import cuchaz.enigma.translation.representation.entry.LocalVariableEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnigmaProject.class)
public class MixinEnigmaProject {
    @Inject(method = "isRenamable(Lcuchaz/enigma/analysis/EntryReference;)Z", at = @At("HEAD"), cancellable = true)
    public void isRenamable(EntryReference<Entry<?>, Entry<?>> obfReference, CallbackInfoReturnable<Boolean> cir) {
        if (obfReference.getNameableEntry() instanceof LocalVariableEntry entry && entry.isArgument()) return;
        cir.setReturnValue(false);
    }
}

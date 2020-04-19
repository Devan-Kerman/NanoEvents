package net.devtech.nanoevents.mixin;

import net.devtech.nanoevents.testing.Test;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class TestMixin {
	@Inject(method = "main", at = @At("HEAD"), cancellable = true)
	private static void testEvent(String[] args, CallbackInfo ci) {
		if(Test.invoker())
			ci.cancel();
	}
}

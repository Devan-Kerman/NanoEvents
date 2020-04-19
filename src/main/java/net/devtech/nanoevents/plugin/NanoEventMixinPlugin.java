package net.devtech.nanoevents.plugin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import java.util.List;
import java.util.Set;

import static net.devtech.nanoevents.api.NanoEventsAPI.wasApplied;

/**
 * The mixin plugin you must add to your mixin json for nano events to work
 */
public class NanoEventMixinPlugin implements IMixinConfigPlugin {

	@Override
	public void onLoad(String mixinPackage) {}

	@Override
	public String getRefMapperConfig() {return null;}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return wasApplied(mixinClassName);
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

	@Override
	public List<String> getMixins() {return null;}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}

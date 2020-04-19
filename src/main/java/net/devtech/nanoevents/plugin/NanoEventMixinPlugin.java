package net.devtech.nanoevents.plugin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static net.devtech.nanoevents.api.NanoEventsAPI.wasApplied;

/**
 * The mixin plugin you must add to your mixin json for nano events to work
 */
public class NanoEventMixinPlugin implements IMixinConfigPlugin {
	private static final Logger LOGGER = Logger.getLogger("NanoEventMixinPlugin");

	@Override
	public void onLoad(String mixinPackage) {}

	@Override
	public String getRefMapperConfig() {return null;}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if(wasApplied(mixinClassName)) {
			return true;
		} else {
			LOGGER.info(mixinClassName + " was not applied because there are no listeners for it's event");
			return false;
		}
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

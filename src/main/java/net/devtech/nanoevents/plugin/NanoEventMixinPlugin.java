package net.devtech.nanoevents.plugin;

import net.devtech.nanoevents.NanoEvents;
import net.devtech.nanoevents.evtparser.Id;
import net.devtech.nanoevents.evtparser.MixinPath;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import java.util.List;
import java.util.Set;

public class NanoEventMixinPlugin implements IMixinConfigPlugin {

	public static boolean contains(Id val) {
		return NanoEvents.LISTENERS.containsKey(val);
	}

	public static boolean contains(String mixinClassName) {
		for (MixinPath path : NanoEvents.ENABLED) {
			if (path.matches(mixinClassName)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onLoad(String mixinPackage) {}

	@Override
	public String getRefMapperConfig() {return null;}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return contains(mixinClassName);
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

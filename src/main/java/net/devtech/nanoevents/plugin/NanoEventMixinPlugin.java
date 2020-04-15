package net.devtech.nanoevents.plugin;

import net.devtech.nanoevents.plugin.util.Id;
import net.devtech.nanoevents.plugin.util.ListenerFinder;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class NanoEventMixinPlugin implements IMixinConfigPlugin {
	public static final Map<Id, String> EVENT_HANDLER_PACKAGES = ListenerFinder.findEventHandlers();
	public static final Map<String, Collection<String>> LISTENERS = ListenerFinder.findListeners(EVENT_HANDLER_PACKAGES);
	private static final Logger LOGGER = Logger.getLogger("NanoEventMixinPlugin");

	@Override
	public void onLoad(String mixinPackage) {}
	@Override
	public String getRefMapperConfig() {return null;}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		int index;
		String packageName = mixinClassName;
		while (true) {
			index = packageName.lastIndexOf('.');
			if(index == -1)
				break;
			packageName = packageName.substring(0, index);
			// if there's something listening to the event, apply it
			if(LISTENERS.containsKey(packageName))
				return true;
		}
		LOGGER.info(mixinClassName + " not applying because no listener exists for that event");
		return false;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) { /*alternative mixins soon?*/ }

	@Override
	public List<String> getMixins() {return null;}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		/*probably best to not use this*/
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {/*this is where the hackery belongs*/}
}

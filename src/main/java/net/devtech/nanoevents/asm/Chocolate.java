package net.devtech.nanoevents.asm;

import com.chocohead.mm.api.ClassTinkerers;
import com.sun.xml.internal.ws.util.StreamUtils;
import net.devtech.nanoevents.plugin.NanoEventMixinPlugin;
import net.devtech.nanoevents.plugin.util.ListenerFinder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.objectweb.asm.tree.MethodNode;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Chocolate implements Runnable {
	private static final Logger LOGGER = Logger.getLogger("Chocolate");
	private Stream<VoxelShape> getLazyGetEntityCollisionStream(World world, Entity entity_1, Box box_1, Predicate<Entity> predicate_1) {
		Stream<Supplier<Stream<VoxelShape>>> getCollisionsLazilyStream = Stream.of(() -> world.getEntityCollisions(entity_1, box_1, null));
		return getCollisionsLazilyStream.flatMap(Supplier::get);
	}

	@Override
	public void run() {
		Map<String, String> invokers = ListenerFinder.findInvokers(NanoEventMixinPlugin.EVENT_HANDLER_PACKAGES);
		for (Map.Entry<String, String> entry : invokers.entrySet()) {
			String namespace = entry.getKey();
			String methodSignature = entry.getValue();
			// transform invoker class
			String[] split = methodSignature.split(";");
			if(split.length != 3)
				LOGGER.severe("invalid method signature " + methodSignature + " should look like mymod/exists/help/BruhClass;myMethod;namespace");
			else {
				ClassTinkerers.addTransformation(split[0], c -> {
					for (MethodNode methodNode : c.methods) {
						if(methodNode.name.equals(split[1])) {

						}
					}
				});
			}
		}
	}

	private static String slice(String string, char character) {
		int index = string.indexOf(character);
		if(index == -1) {
			return null;
		}
		return string.substring(0, index);
	}
}

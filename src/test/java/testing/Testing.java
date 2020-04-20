package testing;

import net.devtech.nanoevents.asm.NanoTransformer;
import net.devtech.nanoevents.util.Id;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import sun.misc.Unsafe;
import testing.nano.NanoListeners;
import testing.nano.Nano;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Testing {
	public static void main(String[] args) throws IOException, ReflectiveOperationException {
		List<String> listeners = new ArrayList<>();
		String listenerType = Type.getInternalName(NanoListeners.class);
		listeners.add(listenerType+"#one");
		listeners.add(listenerType+"#two");
		listeners.add(listenerType+"#three");
		InputStream stream = Nano.class.getResourceAsStream('/' + Type.getInternalName(Nano.class) + ".class");
		ClassReader reader = new ClassReader(stream);
		ClassNode node = new ClassNode();
		reader.accept(node, 0);
		NanoTransformer.transformClass(listeners, node, new Id("nano:test"));

	}
}

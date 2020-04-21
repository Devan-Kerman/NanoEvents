package net.devtech.nanoevents.asm;

import com.chocohead.mm.api.ClassTinkerers;
import net.devtech.nanoevents.NanoEvents;
import net.devtech.nanoevents.api.annotations.Invoker;
import net.devtech.nanoevents.api.Logic;
import net.devtech.nanoevents.api.annotations.ListenerInvoker;
import net.devtech.nanoevents.api.annotations.SingleInvoker;
import net.devtech.nanoevents.evt.Evt;
import net.devtech.nanoevents.util.Id;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


public class NanoTransformer implements Runnable {
	private static final boolean DEBUG_TRANSFORMER = "true".equals(System.getProperty("nano_debug"));
	private static final String LOGIC_TYPE = Type.getInternalName(Logic.class);
	private static final String INVOKER_TYPE = 'L' + Type.getInternalName(Invoker.class) + ';';
	private static final String SINGLE_INVOKER_TYPE = 'L' + Type.getInternalName(SingleInvoker.class) + ';';
	private static final String LISTENER_INVOKER_TYPE = 'L' + Type.getInternalName(ListenerInvoker.class) + ';';

	private static final Logger LOGGER = Logger.getLogger("NanoTransformer");
	static {
		if(DEBUG_TRANSFORMER)
			LOGGER.info("Transformer debugging is enabled!");
	}

	@Override
	public void run() {
		for (Evt evt : NanoEvents.EVENTS.values()) {
			String invoker = evt.getInvokerClass();
			Id id = evt.getId();
			List<String> listeners = NanoEvents.LISTENERS.get(id);
			String invokerType = invoker.replace('.', '/');
			ClassTinkerers.addTransformation(invokerType, node -> transformClass(listeners, node, id));
		}
	}

	/**
	 * searches for the invoker method and extentions for the event and transforms the invoker
	 * @param listeners the listeners for this event
	 * @param node the class node
	 * @param id the id of the event
	 */
	public static void transformClass(List<String> listeners, ClassNode node, Id id) {
		Map<String, MethodNode> nodes = ASMUtil.methodFinder(node, m -> {
			List<AnnotationNode> annotations = m.invisibleAnnotations;
			if(annotations != null) for (AnnotationNode annotation : annotations) {
				if(annotation.values.size() == 2 && id.toString().equals(annotation.values.get(1))) {
					if (INVOKER_TYPE.equals(annotation.desc)) {
						return "invoker";
					} else if(listeners.size() == 1 && SINGLE_INVOKER_TYPE.equals(annotation.desc)) {
						return "single_invoker";
					} else if(LISTENER_INVOKER_TYPE.equals(annotation.desc)) {
						return "listener_invoker";
					}
				}
			}
			return null;
		});

		MethodNode invokerMethod = nodes.get("invoker");
		// if no single invoker found
		if(invokerMethod == null) {
			LOGGER.severe("No invoker found for " + id + " '" + node.name + "'!");
			return;
		}


		MethodNode single = nodes.get("single_invoker");
		MethodNode listenerInvoker = nodes.get("listener_invoker");

		String desc = listenerInvoker == null ? invokerMethod.desc : listenerInvoker.desc;
		if(single == null) {
			String name = listenerInvoker == null ? invokerMethod.name : listenerInvoker.name;
			transform(listeners, invokerMethod.instructions, name, desc, node.name);
		}

		// single transformation
		if(single != null) {
			String name = listenerInvoker == null ? single.name : listenerInvoker.name;
			invokerMethod.instructions = replace(single.instructions, name, node.name, desc, listeners.get(0));
		}

		if(DEBUG_TRANSFORMER) {
			File file = new File("nano_debug/"+node.name+".class");
			File parent = file.getParentFile();
			if(!parent.exists())
				parent.mkdirs();
			try (FileOutputStream output = new FileOutputStream(file)) {
				ClassWriter writer = new ClassWriter(0);
				node.accept(writer);
				output.write(writer.toByteArray());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * this method starts the cut and paste process of the listener, it first looks for a start
	 * and end method, then it copies all the instructions in between, and pastes them over and over
	 * again but replacing all <b>shallow</b> recursive invocations of the method with listener invocations
	 *
	 * @param invoker the invoker string (the owner class)
	 * @param desc the descriptor of the method to replace
	 * @param insns the bytecode of the method to transform
	 * @param name the name of the method to replace
	 */
	private static void transform(Collection<String> listeners, InsnList insns, String name, String desc, String invoker) {
		int startMethod = -1;
		for (int i = 0; i < insns.size(); i++) { // scan the whole method
			AbstractInsnNode node = insns.get(i);
			if (node instanceof MethodInsnNode) {
				MethodInsnNode methodNode = (MethodInsnNode) node;
				if (LOGIC_TYPE.equals(methodNode.owner) && methodNode.desc.equals("()V")) { // check if it's a method in the Invoker class
					if (methodNode.name.equals("start")) { // if it is a start
						if (startMethod != -1) {
							LOGGER.severe("You can only invoke start() once! please fix '" + invoker + "'");
							break;
						}
						startMethod = i; // store the starting point
					} else if (methodNode.name.equals("end")) { // if it's the end
						InsnList insnCopy = ASMUtil.cut(insns, startMethod, i); // cut all the instructions from the method
						pasteAndModify(listeners, insns, name, desc, insnCopy, invoker, startMethod); // and paste/modify them for every listener
						break;
					}
				}
			}
		}
	}



	/**
	 * replace all of shallow recursive call with a listener reference in a newly copied list
	 *
	 * @param list the original byecode
	 * @param nodeName the name of the method being transformed
	 * @param nodeOwner the class of the method being transformed
	 * @param nodeDescriptor the descriptor of the class being transformed
	 * @param listenerReference the method reference to the listener
	 * @return a newly created edited copy of the orignal bytecode
	 */
	private static InsnList replace(InsnList list, String nodeName, String nodeOwner, String nodeDescriptor, String listenerReference) {
		return ASMUtil.clone(list, 0, list.size(), a -> { // copy the copied instruction list, but replace the recursive call with a listener one
			if (a instanceof MethodInsnNode) {
				MethodInsnNode replacementNode = (MethodInsnNode) a;
				// check if method call is the right one
				if (replacementNode.name.equals(nodeName) && replacementNode.owner.equals(nodeOwner) && replacementNode.desc.equals(nodeDescriptor)) {
					// parse the listener reference
					int classIndex = listenerReference.indexOf('#');
					if (classIndex == -1) {
						LOGGER.severe("Bad method signature " + listenerReference);
						replacementNode.owner = "null";
						replacementNode.name = "READ_THE_LOGS";
					} else {
						replacementNode.owner = listenerReference.substring(0, classIndex).replace('.', '/');
						replacementNode.name = listenerReference.substring(classIndex + 1);
					}
				}
			}
			return a;
		});
	}

	/**
	 * paste the copied instruction list into the method once for every listener
	 *
	 * @param listenerReferences all the listener's method references
	 * @param insns the method's bytecode, sans the portion in between the first start/end block
	 * @param desc the descriptor of the method to replace
	 * @param name the name of the method to replace
	 * @param insnCopy the unmodified copied instruction list from the original method
	 * @param type the internal name of the invoker's class
	 * @param startIndex the index in the bytecode of the now removed start method
	 */
	private static void pasteAndModify(Collection<String> listenerReferences, InsnList insns, String name, String desc, InsnList insnCopy, String type, int startIndex) {
		AbstractInsnNode start = insns.get(startIndex);
		for (String listenerReference : listenerReferences) {
			InsnList modCopy = replace(insnCopy, name, type, desc, listenerReference);
			insns.insert(start, modCopy);
		}
	}

}

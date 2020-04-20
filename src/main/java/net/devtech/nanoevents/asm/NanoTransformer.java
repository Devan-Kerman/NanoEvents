package net.devtech.nanoevents.asm;

import com.chocohead.mm.api.ClassTinkerers;
import net.devtech.nanoevents.NanoEvents;
import net.devtech.nanoevents.api.Invoker;
import net.devtech.nanoevents.api.Logic;
import net.devtech.nanoevents.api.SingleInvoker;
import net.devtech.nanoevents.evt.Evt;
import net.devtech.nanoevents.util.Id;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;


public class NanoTransformer implements Runnable {
	private static final boolean DEBUG_TRANSFORMER = "true".equals(System.getProperty("nano_debug"));
	private static final String LOGIC_TYPE = Type.getInternalName(Logic.class);
	private static final String INVOKER_TYPE = 'L' + Type.getInternalName(Invoker.class) + ';';
	private static final String SINGLE_INVOKER_TYPE = 'L' + Type.getInternalName(SingleInvoker.class) + ';';
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

	public static void transformClass(List<String> listeners, ClassNode node, Id id) {
		MethodNode single = null; // if there's only one listener, check for single invokers
		MethodNode invokerMethod = null;
		for (MethodNode method : node.methods) {
			List<AnnotationNode> annotations = method.invisibleAnnotations;
			if (annotations != null && !annotations.isEmpty()) {
				for (AnnotationNode annotation : annotations) {
					List<Object> vals = annotation.values;
					if (INVOKER_TYPE.equals(annotation.desc) && "value".equals(vals.get(0)) && vals.get(1).equals(id.toString())) { // todo check namespace
						invokerMethod = method;
						if(listeners.size() != 1)
							break;
					} else if (listeners.size() == 1 && SINGLE_INVOKER_TYPE.equals(annotation.desc)) {
						// single invoker found
						single = method;
					}
				}
			}
		}

		// if no single invoker found
		if(invokerMethod == null) {
			LOGGER.severe("No invoker found for " + id + " '" + node.name + "'!");
			return;
		}

		if(single == null) {
			transform(listeners, invokerMethod, node.name);
		}

		// single transformation
		if(single != null) {
			singleTransform(single, invokerMethod, node.name, listeners.get(0));
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

	private static void singleTransform(MethodNode single, MethodNode invoker, String invokerType, String listener) {
		invoker.instructions = replace(single.instructions, single.name, invokerType, single.desc, listener);
	}

	/**
	 * this method starts the cut and paste process of the listener, it first looks for a start
	 * and end method, then it copies all the instructions in between, and pastes them over and over
	 * again but replacing all <b>shallow</b> recursive invocations of the method with listener invocations
	 *
	 * @param invoker the invoker string (the owner class)
	 * @param method the method node being transformed
	 */
	private static void transform(Collection<String> listeners, MethodNode method, String invoker) {
		int startMethod = -1;
		InsnList insns = method.instructions;
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
						InsnList insnCopy = cut(insns, startMethod, i); // cut all the instructions from the method
						paste(listeners, method, insnCopy, insns, invoker, startMethod); // and paste/modify them for every listener
						break;
					}
				}
			}
		}
	}

	/**
	 * clone and remove the instructions between the start and end index of the method
	 *
	 * @param list the original instructions
	 * @param startIndex the index-1 where to start
	 * @param endIndex the last index to copy + 1
	 * @return the newly copied list
	 */
	private static InsnList cut(InsnList list, int startIndex, int endIndex) {
		InsnList clone = clone(list, startIndex + 1, endIndex, a -> a);
		for (int idex = endIndex; idex >= startIndex; idex--) {
			AbstractInsnNode val = list.get(idex);
			list.remove(val);
		}
		return clone;
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
		return clone(list, 0, list.size(), a -> { // copy the copied instruction list, but replace the recursive call with a listener one
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
	 * @param node the method node
	 * @param insnCopy the unmodified copied instruction list from the original method
	 * @param list the method's bytecode, sans the portion in between the first start/end block
	 * @param type the internal name of the invoker's class
	 * @param startIndex the index in the bytecode of the now removed start method
	 */
	private static void paste(Collection<String> listenerReferences, MethodNode node, InsnList insnCopy, InsnList list, String type, int startIndex) {
		AbstractInsnNode start = list.get(startIndex);
		for (String listenerReference : listenerReferences) {
			InsnList modCopy = replace(insnCopy, node.name, type, node.desc, listenerReference);
			list.insert(start, modCopy);
		}
	}


	/**
	 * clones the list
	 * Copied from https://github.com/Chocohead/Merger under MPL
	 * Modified to suit my purposes
	 */
	private static InsnList clone(InsnList list, int fromIndex, int toIndex, Function<AbstractInsnNode, AbstractInsnNode> transformer) {
		Map<LabelNode, LabelNode> clonedLabels = new IdentityHashMap<>();
		Map<Label, Label> trueLabels = new IdentityHashMap<>();
		boolean seenFrame = false;
		for (int i = fromIndex; i < toIndex; i++) {
			AbstractInsnNode insn = list.get(i);
			switch (insn.getType()) {
				case AbstractInsnNode.LABEL:
					LabelNode node = (LabelNode) insn;
					clonedLabels.put(node, new LabelNode(trueLabels.computeIfAbsent(node.getLabel(), k -> new Label())));
					break;

				case AbstractInsnNode.FRAME:
					seenFrame = true;
					break;
			}
		}

		if (!seenFrame && clonedLabels.isEmpty()) return list; //Only clone the list if we have to

		InsnList out = new InsnList();
		for (int i = fromIndex; i < toIndex; i++) {
			AbstractInsnNode insn = list.get(i);
			AbstractInsnNode node = insn.clone(clonedLabels);
			node = transformer.apply(node);
			out.add(node);
		}

		return out;
	}

}

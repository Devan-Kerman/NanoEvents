package net.devtech.nanoevents.asm;

import com.chocohead.mm.api.ClassTinkerers;
import net.devtech.nanoevents.NanoEvents;
import net.devtech.nanoevents.evtparser.Evt;
import net.devtech.nanoevents.util.Id;
import net.devtech.nanoevents.api.Invoker;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

// todo add a throw exception at the top of event handler methods that suck ass
public class NanoTransformer implements Runnable {
	private static final String INVOKER_TYPE = Type.getInternalName(Invoker.class);
	private static final Logger LOGGER = Logger.getLogger("NanoTransformer");

	@Override
	public void run() {
		for (Map.Entry<Id, Evt> entry : NanoEvents.EVENTS.entrySet()) {
			Evt evt = entry.getValue();
			String invoker = evt.getInvoker();
			int semicolonIndex = invoker.indexOf(';');
			if (semicolonIndex == -1) {
				LOGGER.severe("Invalid method signature for " + evt.getId() + " '" + invoker + "'");
				continue;
			}
			String classSignature = invoker.substring(0, semicolonIndex);
			if (classSignature.charAt(0) == 'L') {
				classSignature = classSignature.substring(1);
				int nextSemicolon = invoker.indexOf(';', semicolonIndex + 1);
				if (nextSemicolon == -1) {
					LOGGER.severe("Invalid method name for " + evt.getId() + " '" + invoker + "'");
					continue;
				}
				String methodName = invoker.substring(semicolonIndex + 1, nextSemicolon);
				String methodDescriptor = invoker.substring(nextSemicolon + 1);
				String finalClassSignature = classSignature;
				ClassTinkerers.addTransformation(classSignature, invokerClass -> {
					for (MethodNode method : invokerClass.methods) {
						if (method.name.equals(methodName) && method.desc.equals(methodDescriptor)) {
							transform(NanoEvents.LISTENERS.get(entry.getKey()), method, invoker, finalClassSignature);
							return;
						}
					}
					LOGGER.severe("No invoker found for " + evt.getId() + " '" + invoker + "'!");
				});
			} else
				LOGGER.severe("Invalid class signature for " + evt.getId() + " '" + invoker + "'");
		}
	}

	/**
	 * this method starts the cut and paste process of the listener, it first looks for a start
	 * and end method, then it copies all the instructions in between, and pastes them over and over
	 * again but replacing all <b>shallow</b> recursive invocations of the method with listener invocations
	 *
	 * @param invoker the invoker string
	 * @param method the method node being transformed
	 */
	public static void transform(Collection<String> listeners, MethodNode method, String invoker, String type) {
		int startMethod = -1;
		InsnList insns = method.instructions;
		for (int i = 0; i < insns.size(); i++) { // scan the whole method
			AbstractInsnNode node = insns.get(i);
			if (node instanceof MethodInsnNode) {
				MethodInsnNode methodNode = (MethodInsnNode) node;
				if (INVOKER_TYPE.equals(methodNode.owner) && methodNode.desc.equals("()V")) { // check if it's a method in the Invoker class
					if (methodNode.name.equals("start")) { // if it is a start
						if (startMethod != -1) {
							LOGGER.severe("You can only invoke start() once! please fix '" + invoker + "'");
							break;
						}
						startMethod = i; // store the starting point
					} else if (methodNode.name.equals("end")) { // if it's the end
						InsnList insnCopy = cut(insns, startMethod, i); // cut all the instructions from the method
						paste(listeners, method, insnCopy, insns, type, startMethod); // and paste/modify them for every listener
						break;
					}
				}
			}
		}
	}

	/**
	 * paste the copied instruction list into the method once for every listener
	 * @param listenerReferences all the listener's method references
	 * @param node the method node
	 * @param insnCopy the unmodified copied instruction list from the original method
	 * @param list the method's bytecode, sans the portion in between the first start/end block
	 * @param type the internal name of the invoker's class
	 * @param startIndex the index in the bytecode of the now removed start method
	 */
	public static void paste(Collection<String> listenerReferences, MethodNode node, InsnList insnCopy, InsnList list, String type, int startIndex) {
		AbstractInsnNode start = list.get(startIndex);
		for (String listenerReference : listenerReferences) {
			InsnList modCopy = clone(insnCopy, 0, insnCopy.size(), a -> { // copy the copied instruction list, but replace the recursive call with a listener one
				if (a instanceof MethodInsnNode) {
					MethodInsnNode replacementNode = (MethodInsnNode) a;

					// check if method call is the right one
					if (replacementNode.name.equals(node.name) && replacementNode.owner.equals(type) && replacementNode.desc.equals(node.desc)) {
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
			list.insert(start, modCopy);
		}
	}

	/**
	 * clone and remove the instructions between the start and end index of the method
	 * @param list the original instructions
	 * @param startIndex the index-1 where to start
	 * @param endIndex the last index to copy + 1
	 * @return the newly copied list
	 */
	public static InsnList cut(InsnList list, int startIndex, int endIndex) {
		InsnList clone = clone(list, startIndex + 1, endIndex, a -> a);
		for (int idex = endIndex; idex >= startIndex; idex--) {
			AbstractInsnNode val = list.get(idex);
			list.remove(val);
		}
		return clone;
	}


	/**
	 * clones the list
	 * Copied from https://github.com/Chocohead/Merger under MPL
	 * Modified to suit my purposes
	 */
	public static InsnList clone(InsnList list, int fromIndex, int toIndex, Function<AbstractInsnNode, AbstractInsnNode> transformer) {
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

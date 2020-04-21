package net.devtech.nanoevents.asm;

import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

class ASMUtil {
	static Map<String, MethodNode> methodFinder(ClassNode node, Function<MethodNode, String> function) {
		Map<String, MethodNode> methods = new HashMap<>();
		for (MethodNode method : node.methods) {
			String key = function.apply(method);
			if(key != null) {
				methods.put(key, method);
			}
		}
		return methods;
	}

	/**
	 * clone and remove the instructions between the start and end index of the method
	 *
	 * @param list the original instructions
	 * @param startIndex the index-1 where to start
	 * @param endIndex the last index to copy + 1
	 * @return the newly copied list
	 */
	static InsnList cut(InsnList list, int startIndex, int endIndex) {
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
	static InsnList clone(InsnList list, int fromIndex, int toIndex, Function<AbstractInsnNode, AbstractInsnNode> transformer) {
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

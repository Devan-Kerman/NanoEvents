package net.devtech.asm.playground;

import net.devtech.nanoevents.api.Invoker;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

public class AsmPlayground {
	private static final String INVOKER_TYPE = Type.getInternalName(Invoker.class);
	private static final Logger LOGGER = Logger.getLogger("NanoTransformer");

	public static void main(String[] args) throws IOException {
		InputStream stream = AsmPlayground.class.getResourceAsStream("/" + AsmPlayground.class.getName().replace('.', '/') + ".class");
		System.out.println("parse");
		ClassReader reader = new ClassReader(stream);
		ClassNode node = new ClassNode();
		reader.accept(node, 0);
		System.out.println("red");
		for (MethodNode method : node.methods) {
			if(method.name.equals("invoker"))
				process(Arrays.asList("net.devtech.asm.playground.AsmPlayground#player0", "net.devtech.asm.playground.AsmPlayground#player1", "net.devtech.asm.playground.AsmPlayground#player2", "net.devtech.asm.playground.AsmPlayground#player3", "net.devtech.asm.playground.AsmPlayground#player4", "net.devtech.asm.playground.AsmPlayground#player5"), method, "???", "net/devtech/asm/playground/AsmPlayground");
		}

		OutputStream outputStream = new FileOutputStream(new File("Concern.class"));
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		node.accept(writer);
		outputStream.write(writer.toByteArray());
		outputStream.close();
	}



	public static boolean invoker(PlayerEntity one, BlockPos pos, BlockState state) {
		boolean cancelled = false;
		Invoker.start();
		int i = 0;
		Invoker.end();
		return cancelled;
	}

	public static boolean player(PlayerEntity one, BlockPos pos, BlockState state) {
		return false;
	}

	public static boolean player0(PlayerEntity one, BlockPos pos, BlockState state) {
		return false;
	}
	public static boolean player1(PlayerEntity one, BlockPos pos, BlockState state) {
		return false;
	}
	public static boolean player2(PlayerEntity one, BlockPos pos, BlockState state) {
		return false;
	}
	public static boolean player3(PlayerEntity one, BlockPos pos, BlockState state) {
		return false;
	}
	public static boolean player4(PlayerEntity one, BlockPos pos, BlockState state) {
		return false;
	}
	public static boolean player5(PlayerEntity one, BlockPos pos, BlockState state) {
		return false;
	}
	public static boolean player6(PlayerEntity one, BlockPos pos, BlockState state) {
		return false;
	}
	public static boolean player7(PlayerEntity one, BlockPos pos, BlockState state) {
		return false;
	}
	public static boolean player8(PlayerEntity one, BlockPos pos, BlockState state) {
		return false;
	}
	public static boolean player9(PlayerEntity one, BlockPos pos, BlockState state) {
		return false;
	}
	public static boolean player10(PlayerEntity one, BlockPos pos, BlockState state) {
		return false;
	}

	/**
	 * @param invoker the invoker string
	 * @param method the method node being transformed
	 */
	public static void process(Collection<String> listeners, MethodNode method, String invoker, String type) {
		int startMethod = -1;
		InsnList insns = method.instructions;
		for (int i = 0; i < insns.size(); i++) {
			AbstractInsnNode node = insns.get(i);
			if (node instanceof MethodInsnNode) {
				MethodInsnNode methodNode = (MethodInsnNode) node;
				if (INVOKER_TYPE.equals(methodNode.owner) && methodNode.desc.equals("()V")) {
					if (methodNode.name.equals("start")) {
						if (startMethod != -1) {
							LOGGER.severe("You can only invoke start() once! please fix '" + invoker + "'");
						}
						startMethod = i;
					} else if (methodNode.name.equals("end")) {
						InsnList insnCopy = cut(insns, startMethod, i);
						paste(listeners, method, insnCopy, insns, type, startMethod);
						break;
					}
				}
			}
		}

	}


	public static void paste(Collection<String> values, MethodNode node, InsnList insnCopy, InsnList list, String type, int startIndex) {
		AbstractInsnNode start = list.get(startIndex);
		for (String listenerMethods : values) {
			InsnList modCopy = clone(insnCopy, 0, insnCopy.size(), a -> {
				System.out.println("input");
				if (a instanceof MethodInsnNode) {
					MethodInsnNode replacementNode = (MethodInsnNode) a;
					if (replacementNode.name.equals(node.name) && replacementNode.owner.equals(type) && replacementNode.desc.equals(node.desc)) {

						int classIndex = listenerMethods.indexOf('#');
						if (classIndex == -1) {
							LOGGER.severe("Bad method signature " + listenerMethods);
						} else {
							replacementNode.owner = listenerMethods.substring(0, classIndex).replace('.', '/');
							replacementNode.name = listenerMethods.substring(classIndex + 1);
						}
					}
				}
				return a;
			});
			list.insert(start, modCopy);
		}
	}

	public static InsnList cut(InsnList list, int startIndex, int endIndex) {
		InsnList clone = clone(list, startIndex + 1, endIndex, a -> a);
		for (int idex = endIndex; idex >= startIndex; idex--) {
			AbstractInsnNode val = list.get(idex);
			list.remove(val);
		}
		return clone;
	}


	/**
	 * Copied from https://github.com/Chocohead/Merger under MPL
	 * i changed it but pls no sue kthnksbai
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

package dyvil.tools.compiler.util;

import jdk.internal.org.objectweb.asm.Opcodes;

public class OpcodeUtil
{
	public static boolean isReturnOpcode(int op) {
		return op == Opcodes.RETURN || op == Opcodes.ARETURN || op == Opcodes.IRETURN || op == Opcodes.LRETURN || op == Opcodes.FRETURN || op == Opcodes.DRETURN;
	}
}

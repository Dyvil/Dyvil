package dyvil.tools.compiler.util;

import static dyvil.reflect.Opcodes.*;

public class OpcodeUtil
{
	public static boolean isReturnOpcode(int op)
	{
		return op == RETURN || op == ARETURN || op == IRETURN || op == LRETURN || op == FRETURN || op == DRETURN;
	}
	
	public static boolean isInvokeOpcode(int op)
	{
		return op == INVOKEVIRTUAL || op == INVOKEINTERFACE || op == INVOKESPECIAL || op == INVOKESTATIC;
	}
	
	public static boolean isLoadOpcode(int opcode)
	{
		return opcode == ALOAD || opcode == ILOAD || opcode == LLOAD || opcode == FLOAD || opcode == DLOAD;
	}
	
	public static boolean isStoreOpcode(int opcode)
	{
		return opcode == ASTORE || opcode == ISTORE || opcode == LSTORE || opcode == FSTORE || opcode == DSTORE || opcode == RET;
	}
	
	public static boolean isFieldOpcode(int opcode)
	{
		return opcode == PUTFIELD || opcode == GETFIELD || opcode == PUTSTATIC || opcode == GETSTATIC;
	}
	
	public static boolean isJumpOpcode(int opcode)
	{
		return opcode >= IFEQ && opcode <= JSR || opcode == GOTO || opcode == IFNULL || opcode == IFNONNULL;
	}
}

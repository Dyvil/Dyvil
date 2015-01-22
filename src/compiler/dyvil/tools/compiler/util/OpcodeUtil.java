package dyvil.tools.compiler.util;

import static dyvil.reflect.Opcodes.*;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.ast.type.PrimitiveType;
import dyvil.tools.compiler.backend.MethodWriter;

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
	
	public static void writePrimitiveCast(PrimitiveType value, PrimitiveType cast, MethodWriter writer)
	{
		switch (value.typecode)
		{
		case Opcodes.T_BYTE:
		case Opcodes.T_SHORT:
		case Opcodes.T_BOOLEAN:
		case Opcodes.T_CHAR:
		case Opcodes.T_INT:
			writeIntCast(cast, writer);
			break;
		case Opcodes.T_LONG:
			writeLongCast(cast, writer);
			break;
		case Opcodes.T_FLOAT:
			writeFloatCast(cast, writer);
			break;
		case Opcodes.T_DOUBLE:
			writeDoubleCast(cast, writer);
			break;
		}
	}
	
	private static void writeIntCast(PrimitiveType cast, MethodWriter writer)
	{
		switch (cast.typecode)
		{
		case Opcodes.T_BOOLEAN:
		case Opcodes.T_BYTE:
		case Opcodes.T_SHORT:
		case Opcodes.T_CHAR:
		case Opcodes.T_INT:
			break;
		case Opcodes.T_LONG:
			writer.visitInsn(I2L);
			break;
		case Opcodes.T_FLOAT:
			writer.visitInsn(I2F);
			break;
		case Opcodes.T_DOUBLE:
			writer.visitInsn(I2D);
			break;
		}
	}
	
	private static void writeLongCast(PrimitiveType cast, MethodWriter writer)
	{
		switch (cast.typecode)
		{
		case Opcodes.T_BOOLEAN:
			writer.visitInsn(L2I);
			break;
		case Opcodes.T_BYTE:
			writer.visitInsn(L2B);
			break;
		case Opcodes.T_SHORT:
			writer.visitInsn(L2S);
			break;
		case Opcodes.T_CHAR:
			writer.visitInsn(L2C);
			break;
		case Opcodes.T_INT:
			writer.visitInsn(L2I);
			break;
		case Opcodes.T_LONG:
			break;
		case Opcodes.T_FLOAT:
			writer.visitInsn(L2F);
			break;
		case Opcodes.T_DOUBLE:
			writer.visitInsn(L2D);
			break;
		}
	}
	
	private static void writeFloatCast(PrimitiveType cast, MethodWriter writer)
	{
		switch (cast.typecode)
		{
		case Opcodes.T_BOOLEAN:
			writer.visitInsn(F2I);
			break;
		case Opcodes.T_BYTE:
			writer.visitInsn(F2B);
			break;
		case Opcodes.T_SHORT:
			writer.visitInsn(F2S);
			break;
		case Opcodes.T_CHAR:
			writer.visitInsn(F2C);
			break;
		case Opcodes.T_INT:
			writer.visitInsn(F2I);
			break;
		case Opcodes.T_LONG:
			writer.visitInsn(F2L);
			break;
		case Opcodes.T_FLOAT:
			break;
		case Opcodes.T_DOUBLE:
			writer.visitInsn(F2D);
			break;
		}
	}
	
	private static void writeDoubleCast(PrimitiveType cast, MethodWriter writer)
	{
		switch (cast.typecode)
		{
		case Opcodes.T_BOOLEAN:
			writer.visitInsn(D2I);
			break;
		case Opcodes.T_BYTE:
			writer.visitInsn(D2B);
			break;
		case Opcodes.T_SHORT:
			writer.visitInsn(D2S);
			break;
		case Opcodes.T_CHAR:
			writer.visitInsn(D2C);
			break;
		case Opcodes.T_INT:
			writer.visitInsn(D2I);
			break;
		case Opcodes.T_LONG:
			writer.visitInsn(D2L);
			break;
		case Opcodes.T_FLOAT:
			writer.visitInsn(D2F);
			break;
		case Opcodes.T_DOUBLE:
			break;
		}
	}
}

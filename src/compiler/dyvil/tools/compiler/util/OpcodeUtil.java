package dyvil.tools.compiler.util;

import static dyvil.reflect.Opcodes.*;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.PrimitiveType;
import dyvil.tools.compiler.ast.type.Type;
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
		return opcode >= IFEQ && opcode <= JSR || opcode == GOTO || opcode == IFNULL || opcode == IFNONNULL || opcode >= IF_LCMPEQ && opcode <= IF_DCMPLE;
	}
	
	public static int getInverseOpcode(int opcode)
	{
		switch (opcode)
		{
		case IFEQ:
			return IFNE;
		case IFNE:
			return IFEQ;
		case IFLT:
			return IFGE;
		case IFGE:
			return IFLT;
		case IFGT:
			return IFLE;
		case IFLE:
			return IFGT;
			
		case IF_ICMPEQ:
			return IF_ICMPNE;
		case IF_ICMPNE:
			return IF_ICMPEQ;
		case IF_ICMPLT:
			return IF_ICMPGE;
		case IF_ICMPGE:
			return IF_ICMPLT;
		case IF_ICMPGT:
			return IF_ICMPLE;
		case IF_ICMPLE:
			return IF_ICMPGT;
			
		case IF_ACMPEQ:
			return IF_ACMPNE;
		case IF_ACMPNE:
			return IF_ACMPEQ;
		case IFNULL:
			return IFNONNULL;
		case IFNONNULL:
			return IFNULL;
			
		case IF_LCMPEQ:
			return IF_LCMPNE;
		case IF_LCMPNE:
			return IF_LCMPEQ;
		case IF_LCMPLT:
			return IF_LCMPGE;
		case IF_LCMPGE:
			return IF_LCMPLT;
		case IF_LCMPGT:
			return IF_LCMPLE;
		case IF_LCMPLE:
			return IF_LCMPGT;
			
		case IF_FCMPEQ:
			return IF_FCMPNE;
		case IF_FCMPNE:
			return IF_FCMPEQ;
		case IF_FCMPLT:
			return IF_FCMPGE;
		case IF_FCMPGE:
			return IF_FCMPLT;
		case IF_FCMPGT:
			return IF_FCMPLE;
		case IF_FCMPLE:
			return IF_FCMPGT;
			
		case IF_DCMPEQ:
			return IF_DCMPNE;
		case IF_DCMPNE:
			return IF_DCMPEQ;
		case IF_DCMPLT:
			return IF_DCMPGE;
		case IF_DCMPGE:
			return IF_DCMPLT;
		case IF_DCMPGT:
			return IF_DCMPLE;
		case IF_DCMPLE:
			return IF_DCMPGT;
		}
		return 0;
	}
	
	public static void writePrimitiveCast(IType value, PrimitiveType cast, MethodWriter writer)
	{
		IClass iclass = value.getTheClass();
		if (iclass == Type.BYTE_CLASS || iclass == Type.SHORT_CLASS || iclass == Type.CHAR_CLASS || iclass == Type.INT_CLASS)
		{
			writeIntCast(cast, writer);
			return;
		}
		if (iclass == Type.LONG_CLASS)
		{
			writeLongCast(cast, writer);
			return;
		}
		if (iclass == Type.FLOAT_CLASS)
		{
			writeFloatCast(cast, writer);
			return;
		}
		if (iclass == Type.DOUBLE_CLASS)
		{
			writeDoubleCast(cast, writer);
			return;
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

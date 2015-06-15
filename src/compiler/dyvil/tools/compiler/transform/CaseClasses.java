package dyvil.tools.compiler.transform;

import static dyvil.reflect.Opcodes.*;

import org.objectweb.asm.Label;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.PrimitiveType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;

public interface CaseClasses
{
	public static void writeEquals(MethodWriter writer, IClass theClass) throws BytecodeException
	{
		Label label;
		// Write check 'if (this == obj)'
		writer.writeVarInsn(ALOAD, 0);
		writer.writeVarInsn(ALOAD, 1);
		// if
		writer.writeJumpInsn(IF_ACMPNE, label = new Label());
		// then
		writer.writeLDC(1);
		writer.writeInsn(IRETURN); // return true
		// else
		writer.writeLabel(label);
		
		// Write check 'if (obj == null)'
		writer.writeVarInsn(ALOAD, 1);
		// if
		writer.writeJumpInsn(IFNONNULL, label = new Label());
		// then
		writer.writeLDC(0);
		writer.writeInsn(IRETURN); // return false
		// else
		writer.writeLabel(label);
		
		// Write check 'if (this.getClass() != obj.getClass())'
		// this.getClass()
		writer.writeVarInsn(ALOAD, 0);
		writer.writeInvokeInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
		// obj.getClass()
		writer.writeVarInsn(ALOAD, 1);
		writer.writeInvokeInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
		// if
		writer.writeJumpInsn(IF_ACMPEQ, label = new Label());
		// then
		writer.writeLDC(0);
		writer.writeInsn(IRETURN);
		// else
		writer.writeLabel(label);
		
		// var = (ClassName) obj
		writer.writeVarInsn(ALOAD, 1);
		writer.writeTypeInsn(CHECKCAST, theClass.getInternalName());
		// 'var' variable that stores the casted 'obj' parameter
		writer.writeVarInsn(ASTORE, 2);
		
		int len = theClass.parameterCount();
		for (int i = 0; i < len; i++)
		{
			writeEquals(writer, theClass.getParameter(i));
		}
		
		writer.writeLDC(1);
		writer.writeInsn(IRETURN);
	}
	
	public static void writeEquals(MethodWriter writer, IField field) throws BytecodeException
	{
		IType type = field.getType();
		
		if (type.isPrimitive())
		{
			// Push 'this'
			writer.writeVarInsn(ALOAD, 0);
			field.writeGet(writer, null);
			// Push 'var'
			writer.writeVarInsn(ALOAD, 2);
			field.writeGet(writer, null);
			
			Label label = new Label();
			switch (((PrimitiveType) type).typecode)
			{
			case ClassFormat.T_BOOLEAN:
			case ClassFormat.T_BYTE:
			case ClassFormat.T_SHORT:
			case ClassFormat.T_CHAR:
			case ClassFormat.T_INT:
				writer.writeJumpInsn(IF_ICMPEQ, label);
				break;
			case ClassFormat.T_LONG:
				writer.writeJumpInsn(IF_LCMPEQ, label);
				break;
			case ClassFormat.T_FLOAT:
				writer.writeJumpInsn(IF_FCMPEQ, label);
				break;
			case ClassFormat.T_DOUBLE:
				writer.writeJumpInsn(IF_FCMPEQ, label);
				break;
			}
			writer.writeLDC(0);
			writer.writeInsn(IRETURN);
			writer.writeLabel(label);
			return;
		}
		
		// if (this.f == null) { if (var.f != null) return true }
		// else if (!this.f.equals(var.f)) return false;
		// Code generated using ASMifier
		
		Label elseLabel = new Label();
		Label endLabel = new Label();
		writer.writeVarInsn(ALOAD, 0);
		field.writeGet(writer, null);
		writer.writeJumpInsn(IFNONNULL, elseLabel);
		writer.writeVarInsn(ALOAD, 2);
		field.writeGet(writer, null);
		writer.writeJumpInsn(IFNULL, endLabel);
		writer.writeLDC(0);
		writer.writeInsn(IRETURN);
		writer.writeLabel(elseLabel);
		writer.writeVarInsn(ALOAD, 0);
		field.writeGet(writer, null);
		writer.writeVarInsn(ALOAD, 2);
		field.writeGet(writer, null);
		
		if (type.isArrayType())
		{
			writeArrayEquals(writer, type.getElementType());
		}
		else
		{
			writer.writeInvokeInsn(INVOKEVIRTUAL, "java/lang/Object", "equals", "(Ljava/lang/Object;)Z", false);
		}
		
		writer.writeJumpInsn(IFNE, endLabel);
		writer.writeLDC(0);
		writer.writeInsn(IRETURN);
		writer.writeLabel(endLabel);
	}
	
	public static void writeArrayEquals(MethodWriter writer, IType type) throws BytecodeException
	{
		switch (type.typeTag())
		{
		case IType.PRIMITIVE_TYPE:
			switch (((PrimitiveType) type).typecode)
			{
			case ClassFormat.T_BOOLEAN:
				writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/BooleanArray", "equals", "([Z[Z)Z", true);
				return;
			case ClassFormat.T_BYTE:
				writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/ByteArray", "equals", "([B[B)Z", true);
				return;
			case ClassFormat.T_SHORT:
				writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/ShortArray", "equals", "([S[S)Z", true);
				return;
			case ClassFormat.T_CHAR:
				writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/CharArray", "equals", "([C[C)Z", true);
				return;
			case ClassFormat.T_INT:
				writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/IntArray", "equals", "([I[I)Z", true);
				return;
			case ClassFormat.T_LONG:
				writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/LongArray", "equals", "([J[J)Z", true);
				return;
			case ClassFormat.T_FLOAT:
				writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/FloatArray", "equals", "([F[F)Z", true);
				return;
			case ClassFormat.T_DOUBLE:
				writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/DoubleArray", "equals", "([D[D)Z", true);
				return;
			default:
				return;
			}
		case IType.ARRAY_TYPE:
			writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/ObjectArray", "deepEquals", "([Ljava/lang/Object;[Ljava/lang/Object;)Z", true);
			return;
		default:
			writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/ObjectArray", "equals", "([Ljava/lang/Object;[Ljava/lang/Object;)Z", true);
			return;
		}
	}
	
	public static void writeHashCode(MethodWriter writer, IClass theClass) throws BytecodeException
	{
		writer.writeLDC(31);
		
		int len = theClass.parameterCount();
		for (int i = 0; i < len; i++)
		{
			IField field = theClass.getParameter(i);
			// Load the value of the field
			writer.writeVarInsn(ALOAD, 0);
			field.writeGet(writer, null);
			// Write the hashing strategy for the field
			writeHashCode(writer, field.getType());
			// Add the hash to the previous result
			writer.writeInsn(IADD);
			writer.writeLDC(31);
			// Multiply the result by 31
			writer.writeInsn(IMUL);
		}
		
		writer.writeInsn(IRETURN);
	}
	
	public static void writeHashCode(MethodWriter writer, IType type) throws BytecodeException
	{
		
		if (type.isPrimitive())
		{
			switch (((PrimitiveType) type).typecode)
			{
			case ClassFormat.T_BOOLEAN:
			{
				// Write boolean hashing by using 1231 if the value is true and
				// 1237 if the value is false
				Label elseLabel = new Label();
				Label endLabel = new Label();
				// if
				writer.writeJumpInsn(IFEQ, elseLabel);
				// then
				writer.writeLDC(1231);
				writer.writeJumpInsn(GOTO, endLabel);
				// else
				writer.writeLabel(elseLabel);
				writer.writeLDC(1237);
				writer.writeLabel(endLabel);
				return;
			}
			case ClassFormat.T_BYTE:
				return;
			case ClassFormat.T_SHORT:
				return;
			case ClassFormat.T_CHAR:
				return;
			case ClassFormat.T_INT:
				return;
			case ClassFormat.T_LONG:
				// Write a long hashing snippet by XORing the value by the value
				// bit-shifted 32 bits to the right, and then converting the
				// result to an integer. l1 = (int) (l ^ (l >>> 32))
				writer.writeInsn(DUP2);
				writer.writeLDC(32);
				writer.writeInsn(LUSHR);
				writer.writeInsn(LOR);
				writer.writeInsn(L2I);
				return;
			case ClassFormat.T_FLOAT:
				// Write a float hashing snippet using Float.floatToIntBits
				writer.writeInvokeInsn(INVOKESTATIC, "java/lang/Float", "floatToIntBits", "(F)I", false);
				return;
			case ClassFormat.T_DOUBLE:
				// Write a double hashing snippet using Double.doubleToLongBits
				// and long hashing
				writer.writeInvokeInsn(INVOKESTATIC, "java/lang/Double", "doubleToLongBits", "(D)L", false);
				writer.writeInsn(DUP2);
				writer.writeLDC(32);
				writer.writeInsn(LUSHR);
				writer.writeInsn(LOR);
				writer.writeInsn(L2I);
				return;
			}
		}
		
		Label elseLabel = new Label();
		Label endLabel = new Label();
		
		// Write an Object hashing snippet
		
		// if
		writer.writeInsn(DUP);
		writer.writeJumpInsn(IFNULL, elseLabel);
		// then
		if (type.isArrayType())
		{
			writeArrayHashCode(writer, type.getElementType());
		}
		else
		{
			writer.writeInvokeInsn(INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I", false);
		}
		writer.writeJumpInsn(GOTO, endLabel);
		// else
		writer.writeLabel(elseLabel);
		writer.writeInsn(POP);
		writer.writeLDC(0);
		writer.writeLabel(endLabel);
	}
	
	public static void writeArrayHashCode(MethodWriter writer, IType type) throws BytecodeException
	{
		switch (type.typeTag())
		{
		case IType.PRIMITIVE_TYPE:
			switch (((PrimitiveType) type).typecode)
			{
			case ClassFormat.T_BOOLEAN:
				writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/BooleanArray", "hashCode", "([Z)Z", true);
				return;
			case ClassFormat.T_BYTE:
				writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/ByteArray", "hashCode", "([B)I", true);
				return;
			case ClassFormat.T_SHORT:
				writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/ShortArray", "hashCode", "([S)I", true);
				return;
			case ClassFormat.T_CHAR:
				writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/CharArray", "hashCode", "([C)I", true);
				return;
			case ClassFormat.T_INT:
				writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/IntArray", "hashCode", "([I)I", true);
				return;
			case ClassFormat.T_LONG:
				writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/LongArray", "hashCode", "([J)I", true);
				return;
			case ClassFormat.T_FLOAT:
				writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/FloatArray", "hashCode", "([F)I", true);
				return;
			case ClassFormat.T_DOUBLE:
				writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/DoubleArray", "hashCode", "([D)I", true);
				return;
			default:
				return;
			}
		case IType.ARRAY_TYPE:
			writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/ObjectArray", "deepHashCode", "([Ljava/lang/Object;)I", true);
			return;
		default:
			writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/ObjectArray", "hashCode", "([Ljava/lang/Object;)I", true);
			return;
		}
	}
	
	public static void writeToString(MethodWriter writer, IClass theClass) throws BytecodeException
	{
		// ----- StringBuilder Constructor -----
		writer.writeTypeInsn(NEW, "java/lang/StringBuilder");
		writer.writeInsn(DUP);
		// Call the StringBuilder(String) constructor with the "[ClassName]("
		// argument
		writer.writeLDC(theClass.getName() + "(");
		writer.writeInvokeInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
		
		// ----- Fields -----
		int params = theClass.parameterCount();
		for (int i = 0; i < params; i++)
		{
			IField field = theClass.getParameter(i);
			IType type = field.getType();
			
			// Get the field
			writer.writeVarInsn(ALOAD, 0);
			field.writeGet(writer, null);
			
			writeToString(writer, type);
			if (i + 1 < params)
			{
				// Separator Comma
				writer.writeLDC(", ");
				writer.writeInvokeInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
			}
		}
		
		// ----- Append Closing Parenthesis -----
		writer.writeLDC(")");
		// Write the call to the StringBuilder#append(String) method
		writer.writeInvokeInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
		
		// ----- ToString -----
		// Write the call to the StringBuilder#toString() method
		writer.writeInvokeInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
		// Write the return
		writer.writeInsn(ARETURN);
	}
	
	public static void writeToString(MethodWriter writer, IType type) throws BytecodeException
	{
		// Write the call to the StringBuilder#append() method that
		// corresponds to the type of the field
		
		if (type.isArrayType())
		{
			writer.writeInsn(Opcodes.SWAP);
			writer.writeInsn(Opcodes.DUP_X1);
			writeArrayToString(writer, type.getElementType());
			return;
		}
		
		StringBuilder desc = new StringBuilder().append('(');
		if (type.isPrimitive())
		{
			type.appendExtendedName(desc);
		}
		else if (type.equals(Types.STRING))
		{
			desc.append("Ljava/lang/String;");
		}
		else
		{
			desc.append("Ljava/lang/Object;");
		}
		desc.append(")Ljava/lang/StringBuilder;");
		
		writer.writeInvokeInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", desc.toString(), false);
		return;
	}
	
	public static void writeArrayToString(MethodWriter writer, IType type) throws BytecodeException
	{
		switch (type.typeTag())
		{
		case IType.PRIMITIVE_TYPE:
			switch (((PrimitiveType) type).typecode)
			{
			case ClassFormat.T_BOOLEAN:
				writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/BooleanArray", "toString", "([ZLjava/lang/StringBuilder;)V", true);
				return;
			case ClassFormat.T_BYTE:
				writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/ByteArray", "toString", "([BLjava/lang/StringBuilder;)V", true);
				return;
			case ClassFormat.T_SHORT:
				writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/ShortArray", "toString", "([SLjava/lang/StringBuilder;)V", true);
				return;
			case ClassFormat.T_CHAR:
				writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/CharArray", "toString", "([CLjava/lang/StringBuilder;)V", true);
				return;
			case ClassFormat.T_INT:
				writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/IntArray", "toString", "([ILjava/lang/StringBuilder;)V", true);
				return;
			case ClassFormat.T_LONG:
				writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/LongArray", "toString", "([JLjava/lang/StringBuilder;)V", true);
				return;
			case ClassFormat.T_FLOAT:
				writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/FloatArray", "toString", "([FLjava/lang/StringBuilder;)V", true);
				return;
			case ClassFormat.T_DOUBLE:
				writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/DoubleArray", "toString", "([DLjava/lang/StringBuilder;)V", true);
				return;
			default:
				return;
			}
		case IType.ARRAY_TYPE:
			writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/ObjectArray", "deepToString", "([Ljava/lang/Object;Ljava/lang/StringBuilder;)V", true);
			return;
		default:
			writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/array/ObjectArray", "toString", "([Ljava/lang/Object;Ljava/lang/StringBuilder;)V", true);
			return;
		}
	}
}

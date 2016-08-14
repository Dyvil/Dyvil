package dyvil.tools.compiler.transform;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.parameter.IParameterList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.PrimitiveType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;

import static dyvil.reflect.Opcodes.*;

public interface CaseClasses
{
	static void writeEquals(MethodWriter writer, IClass theClass) throws BytecodeException
	{
		Label label;
		// Write check 'if (this == obj)'
		writer.visitVarInsn(ALOAD, 0);
		writer.visitVarInsn(ALOAD, 1);
		// if
		writer.visitJumpInsn(IF_ACMPNE, label = new Label());
		// then
		writer.visitLdcInsn(1);
		writer.visitInsn(IRETURN); // return true
		// else
		writer.visitLabel(label);

		// Write check 'if (obj == null)'
		writer.visitVarInsn(ALOAD, 1);
		// if
		writer.visitJumpInsn(IFNONNULL, label = new Label());
		// then
		writer.visitLdcInsn(0);
		writer.visitInsn(IRETURN); // return false
		// else
		writer.visitLabel(label);

		// Write check 'if (this.getClass() != obj.getClass())'
		// this.getClass()
		writer.visitVarInsn(ALOAD, 0);
		writer.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
		// obj.getClass()
		writer.visitVarInsn(ALOAD, 1);
		writer.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
		// if
		writer.visitJumpInsn(IF_ACMPEQ, label = new Label());
		// then
		writer.visitLdcInsn(0);
		writer.visitInsn(IRETURN);
		// else
		writer.visitLabel(label);

		// var = (ClassName) obj
		writer.visitVarInsn(ALOAD, 1);
		writer.visitTypeInsn(CHECKCAST, theClass.getInternalName());
		// 'var' variable that stores the casted 'obj' parameter
		writer.visitVarInsn(ASTORE, 2);

		final IParameterList parameters = theClass.getParameterList();
		for (int i = 0, count = parameters.size(); i < count; i++)
		{
			writeEquals(writer, parameters.get(i));
		}

		writer.visitLdcInsn(1);
		writer.visitInsn(IRETURN);
	}

	static void writeEquals(MethodWriter writer, IDataMember field) throws BytecodeException
	{
		IType type = field.getType();

		if (type.isPrimitive())
		{
			// Push 'this'
			writer.visitVarInsn(ALOAD, 0);
			field.writeGet(writer, null, 0);
			// Push 'var'
			writer.visitVarInsn(ALOAD, 2);
			field.writeGet(writer, null, 0);

			Label label = new Label();
			switch (type.getTypecode())
			{
			case PrimitiveType.BOOLEAN_CODE:
			case PrimitiveType.BYTE_CODE:
			case PrimitiveType.SHORT_CODE:
			case PrimitiveType.CHAR_CODE:
			case PrimitiveType.INT_CODE:
				writer.visitJumpInsn(IF_ICMPEQ, label);
				break;
			case PrimitiveType.LONG_CODE:
				writer.visitJumpInsn(IF_LCMPEQ, label);
				break;
			case PrimitiveType.FLOAT_CODE:
				writer.visitJumpInsn(IF_FCMPEQ, label);
				break;
			case PrimitiveType.DOUBLE_CODE:
				writer.visitJumpInsn(IF_DCMPEQ, label);
				break;
			}
			writer.visitLdcInsn(0);
			writer.visitInsn(IRETURN);
			writer.visitLabel(label);
			return;
		}

		// if (this.f == null) { if (var.f != null) return true }
		// else if (!this.f.equals(var.f)) return false;
		// Code generated using ASMifier

		Label elseLabel = new Label();
		Label endLabel = new Label();
		writer.visitVarInsn(ALOAD, 0);
		field.writeGet(writer, null, 0);
		writer.visitJumpInsn(IFNONNULL, elseLabel);
		writer.visitVarInsn(ALOAD, 2);
		field.writeGet(writer, null, 0);
		writer.visitJumpInsn(IFNULL, endLabel);
		writer.visitLdcInsn(0);
		writer.visitInsn(IRETURN);
		writer.visitLabel(elseLabel);
		writer.visitVarInsn(ALOAD, 0);
		field.writeGet(writer, null, 0);
		writer.visitVarInsn(ALOAD, 2);
		field.writeGet(writer, null, 0);

		writer.visitLineNumber(0);
		if (type.isArrayType())
		{
			writeArrayEquals(writer, type.getElementType());
		}
		else
		{
			writer.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "equals", "(Ljava/lang/Object;)Z", false);
		}

		writer.visitJumpInsn(IFNE, endLabel);
		writer.visitLdcInsn(0);
		writer.visitInsn(IRETURN);
		writer.visitLabel(endLabel);
	}

	static void writeArrayEquals(MethodWriter writer, IType type) throws BytecodeException
	{
		switch (type.typeTag())
		{
		case IType.PRIMITIVE:
			switch (type.getTypecode())
			{
			case PrimitiveType.BOOLEAN_CODE:
				writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/BooleanArray", "equals", "([Z[Z)Z", true);
				return;
			case PrimitiveType.BYTE_CODE:
				writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/ByteArray", "equals", "([B[B)Z", true);
				return;
			case PrimitiveType.SHORT_CODE:
				writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/ShortArray", "equals", "([S[S)Z", true);
				return;
			case PrimitiveType.CHAR_CODE:
				writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/CharArray", "equals", "([C[C)Z", true);
				return;
			case PrimitiveType.INT_CODE:
				writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/IntArray", "equals", "([I[I)Z", true);
				return;
			case PrimitiveType.LONG_CODE:
				writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/LongArray", "equals", "([J[J)Z", true);
				return;
			case PrimitiveType.FLOAT_CODE:
				writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/FloatArray", "equals", "([F[F)Z", true);
				return;
			case PrimitiveType.DOUBLE_CODE:
				writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/DoubleArray", "equals", "([D[D)Z", true);
				return;
			default:
				return;
			}
		case IType.ARRAY:
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/ObjectArray", "deepEquals",
			                       "([Ljava/lang/Object;[Ljava/lang/Object;)Z", true);
			return;
		default:
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/ObjectArray", "equals",
			                       "([Ljava/lang/Object;[Ljava/lang/Object;)Z", true);
		}
	}

	static void writeHashCode(MethodWriter writer, IClass theClass) throws BytecodeException
	{
		writer.visitLdcInsn(31);

		final IParameterList parameters = theClass.getParameterList();
		for (int i = 0, count = parameters.size(); i < count; i++)
		{
			final IDataMember parameter = parameters.get(i);

			// Load the value of the field
			writer.visitVarInsn(ALOAD, 0);
			parameter.writeGet(writer, null, 0);
			// Write the hashing strategy for the parameter
			writeHashCode(writer, parameter.getType());
			// Add the hash to the previous result
			writer.visitInsn(IADD);
			writer.visitLdcInsn(31);
			// Multiply the result by 31
			writer.visitInsn(IMUL);
		}

		writer.visitInsn(IRETURN);
	}

	static void writeHashCode(MethodWriter writer, IType type) throws BytecodeException
	{

		if (type.isPrimitive())
		{
			switch (type.getTypecode())
			{
			case PrimitiveType.BOOLEAN_CODE:
			{
				// Write boolean hashing by using 1231 if the value is true and
				// 1237 if the value is false
				Label elseLabel = new Label();
				Label endLabel = new Label();
				// if
				writer.visitJumpInsn(IFEQ, elseLabel);
				// then
				writer.visitLdcInsn(1231);
				writer.visitJumpInsn(GOTO, endLabel);
				// else
				writer.visitLabel(elseLabel);
				writer.visitLdcInsn(1237);
				writer.visitLabel(endLabel);
				return;
			}
			case PrimitiveType.BYTE_CODE:
				return;
			case PrimitiveType.SHORT_CODE:
				return;
			case PrimitiveType.CHAR_CODE:
				return;
			case PrimitiveType.INT_CODE:
				return;
			case PrimitiveType.LONG_CODE:
				// Write a long hashing snippet by XORing the value by the value
				// bit-shifted 32 bits to the right, and then converting the
				// result to an integer. l1 = (int) (l ^ (l >>> 32))
				writer.visitInsn(DUP2);
				writer.visitLdcInsn(32);
				writer.visitInsn(LUSHR);
				writer.visitInsn(LOR);
				writer.visitInsn(L2I);
				return;
			case PrimitiveType.FLOAT_CODE:
				// Write a float hashing snippet using Float.floatToIntBits
				writer.visitLineNumber(0);
				writer.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "floatToIntBits", "(F)I", false);
				return;
			case PrimitiveType.DOUBLE_CODE:
				// Write a double hashing snippet using Double.doubleToLongBits
				// and long hashing
				writer.visitLineNumber(0);
				writer.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "doubleToLongBits", "(D)J", false);
				writer.visitInsn(DUP2);
				writer.visitLdcInsn(32);
				writer.visitInsn(LUSHR);
				writer.visitInsn(LOR);
				writer.visitInsn(L2I);
				return;
			}
		}

		Label elseLabel = new Label();
		Label endLabel = new Label();

		// Write an Object hashing snippet

		// if
		writer.visitInsn(DUP);
		writer.visitJumpInsn(IFNULL, elseLabel);
		// then
		writer.visitLineNumber(0);
		if (type.isArrayType())
		{
			writeArrayHashCode(writer, type.getElementType());
		}
		else
		{
			writer.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I", false);
		}
		writer.visitJumpInsn(GOTO, endLabel);
		// else
		writer.visitLabel(elseLabel);
		writer.visitInsn(POP);
		writer.visitLdcInsn(0);
		writer.visitLabel(endLabel);
	}

	static void writeArrayHashCode(MethodWriter writer, IType type) throws BytecodeException
	{
		switch (type.typeTag())
		{
		case IType.PRIMITIVE:
			switch (type.getTypecode())
			{
			case PrimitiveType.BOOLEAN_CODE:
				writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/BooleanArray", "hashCode", "([Z)Z", true);
				return;
			case PrimitiveType.BYTE_CODE:
				writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/ByteArray", "hashCode", "([B)I", true);
				return;
			case PrimitiveType.SHORT_CODE:
				writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/ShortArray", "hashCode", "([S)I", true);
				return;
			case PrimitiveType.CHAR_CODE:
				writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/CharArray", "hashCode", "([C)I", true);
				return;
			case PrimitiveType.INT_CODE:
				writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/IntArray", "hashCode", "([I)I", true);
				return;
			case PrimitiveType.LONG_CODE:
				writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/LongArray", "hashCode", "([J)I", true);
				return;
			case PrimitiveType.FLOAT_CODE:
				writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/FloatArray", "hashCode", "([F)I", true);
				return;
			case PrimitiveType.DOUBLE_CODE:
				writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/DoubleArray", "hashCode", "([D)I", true);
				return;
			default:
				return;
			}
		case IType.ARRAY:
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/ObjectArray", "deepHashCode",
			                       "([Ljava/lang/Object;)I", true);
			return;
		default:
			writer
				.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/ObjectArray", "hashCode", "([Ljava/lang/Object;)I",
				                 true);
		}
	}

	static void writeToString(MethodWriter writer, IClass theClass) throws BytecodeException
	{
		// ----- StringBuilder Constructor -----
		writer.visitTypeInsn(NEW, "java/lang/StringBuilder");
		writer.visitInsn(DUP);
		// Call the StringBuilder(String) constructor with the "[ClassName]("
		// argument
		writer.visitLdcInsn(theClass.getName() + "(");
		writer.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);

		// ----- Class Parameters -----
		final IParameterList parameters = theClass.getParameterList();
		for (int i = 0, count = parameters.size(); i < count; i++)
		{
			final IDataMember parameter = parameters.get(i);
			IType type = parameter.getType();

			// Get the parameter value
			writer.visitVarInsn(ALOAD, 0);
			parameter.writeGet(writer, null, 0);

			writeStringAppend(writer, type);
			if (i + 1 < count)
			{
				// Separator Comma
				writer.visitLdcInsn(", ");
				writer.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
				                       "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
			}
		}

		// ----- Append Closing Parenthesis -----
		writer.visitLdcInsn(")");
		// Write the call to the StringBuilder#append(String) method
		writer.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
		                       "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);

		// ----- ToString -----
		// Write the call to the StringBuilder#toString() method
		writer.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
		// Write the return
		writer.visitInsn(ARETURN);
	}

	static void writeToString(MethodWriter writer, IValue value) throws BytecodeException
	{
		final IType type = value.getType();
		// If someValue is a String, write it as is
		if (Types.isSuperType(Types.STRING, type))
		{
			value.writeExpression(writer, Types.STRING);
			return;
		}

		// Otherwise, generate an appropriate call to String.valueOf or ObjectArray.toString
		value.writeExpression(writer, type);

		switch (type.getTypecode())
		{
		case PrimitiveType.BOOLEAN_CODE:
			writer
				.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "toString", "(Z)Ljava/lang/String;", false);
			return;
		case PrimitiveType.BYTE_CODE:
		case PrimitiveType.SHORT_CODE:
		case PrimitiveType.CHAR_CODE:
		case PrimitiveType.INT_CODE:
			writer
				.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;", false);
			return;
		case PrimitiveType.LONG_CODE:
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "toString", "(J)Ljava/lang/String;", false);
			return;
		case PrimitiveType.FLOAT_CODE:
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "toString", "(F)Ljava/lang/String;", false);
			return;
		case PrimitiveType.DOUBLE_CODE:
			writer
				.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "toString", "(D)Ljava/lang/String;", false);
			return;
		}
		if (type.isArrayType())
		{
			writeArrayToString(writer, type);
			return;
		}

		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/String", "valueOf",
		                       "(Ljava/lang/Object;)Ljava/lang/String;", false);
	}

	static void writeStringAppend(MethodWriter writer, String string) throws BytecodeException
	{
		switch (string.length())
		{
		case 0:
			return;
		case 1:
			writer.visitLdcInsn(string.charAt(0));
			writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
			                       "(C)Ljava/lang/StringBuilder;", false);
			return;
		default:
			writer.visitLdcInsn(string);
			writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
			                       "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
		}
	}

	static void writeStringAppend(MethodWriter writer, IType type) throws BytecodeException
	{
		// Write the call to the StringBuilder#append() method that
		// corresponds to the type of the field

		writer.visitLineNumber(0);

		if (type.isArrayType())
		{
			writer.visitInsn(Opcodes.SWAP);
			writer.visitInsn(Opcodes.DUP_X1);
			writeArrayStringAppend(writer, type.getElementType());
			return;
		}

		StringBuilder desc = new StringBuilder().append('(');
		if (type.isPrimitive())
		{
			type.appendExtendedName(desc);
		}
		else if (Types.isExactType(type, Types.STRING))
		{
			desc.append("Ljava/lang/String;");
		}
		else
		{
			desc.append("Ljava/lang/Object;");
		}
		desc.append(")Ljava/lang/StringBuilder;");

		writer.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", desc.toString(), false);
	}

	static void writeArrayToString(MethodWriter writer, IType type) throws BytecodeException
	{
		switch (type.getTypecode())
		{
		case PrimitiveType.BOOLEAN_CODE:
			writer
				.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/BooleanArray", "toString", "([Z)Ljava/lang/String;",
				                 true);
			return;
		case PrimitiveType.BYTE_CODE:
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/ByteArray", "toString", "([B)Ljava/lang/String;",
			                       true);
			return;
		case PrimitiveType.SHORT_CODE:
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/ShortArray", "toString", "([S)Ljava/lang/String;",
			                       true);
			return;
		case PrimitiveType.CHAR_CODE:
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/CharArray", "toString", "([C)Ljava/lang/String;",
			                       true);
			return;
		case PrimitiveType.INT_CODE:
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/IntArray", "toString", "([I)Ljava/lang/String;",
			                       true);
			return;
		case PrimitiveType.LONG_CODE:
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/LongArray", "toString", "([J)Ljava/lang/String;",
			                       true);
			return;
		case PrimitiveType.FLOAT_CODE:
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/FloatArray", "toString", "([F)Ljava/lang/String;",
			                       true);
			return;
		case PrimitiveType.DOUBLE_CODE:
			writer
				.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/DoubleArray", "toString", "([D)Ljava/lang/String;",
				                 true);
			return;
		}
		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/ObjectArray", "deepToString",
		                       "([Ljava/lang/Object;)Ljava/lang/String;", true);
	}

	static void writeArrayStringAppend(MethodWriter writer, IType type) throws BytecodeException
	{
		switch (type.getTypecode())
		{
		case PrimitiveType.BOOLEAN_CODE:
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/BooleanArray", "toString",
			                       "([ZLjava/lang/StringBuilder;)V", true);
			return;
		case PrimitiveType.BYTE_CODE:
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/ByteArray", "toString",
			                       "([BLjava/lang/StringBuilder;)V", true);
			return;
		case PrimitiveType.SHORT_CODE:
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/ShortArray", "toString",
			                       "([SLjava/lang/StringBuilder;)V", true);
			return;
		case PrimitiveType.CHAR_CODE:
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/CharArray", "toString",
			                       "([CLjava/lang/StringBuilder;)V", true);
			return;
		case PrimitiveType.INT_CODE:
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/IntArray", "toString",
			                       "([ILjava/lang/StringBuilder;)V", true);
			return;
		case PrimitiveType.LONG_CODE:
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/LongArray", "toString",
			                       "([JLjava/lang/StringBuilder;)V", true);
			return;
		case PrimitiveType.FLOAT_CODE:
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/FloatArray", "toString",
			                       "([FLjava/lang/StringBuilder;)V", true);
			return;
		case PrimitiveType.DOUBLE_CODE:
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/DoubleArray", "toString",
			                       "([DLjava/lang/StringBuilder;)V", true);
			return;
		}
		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/array/ObjectArray", "deepToString",
		                       "([Ljava/lang/Object;Ljava/lang/StringBuilder;)V", true);
	}
}

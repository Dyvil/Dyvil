package dyvil.tools.compiler.transform;

import static dyvil.reflect.Opcodes.*;

import org.objectweb.asm.Label;

import dyvil.tools.compiler.ast.classes.CodeClass;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.PrimitiveType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.MethodWriter;

public class CaseClasses
{
	public static void writeEquals(MethodWriter writer, CodeClass theClass)
	{
		Label label;
		String extended = "L" + theClass.getInternalName() + ";";
		
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
		writer.writeTypeInsn(CHECKCAST, extended);
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
	
	private static void writeEquals(MethodWriter writer, IField field)
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
		writer.writeInvokeInsn(INVOKEVIRTUAL, "java/lang/Object", "equals", "(Ljava/lang/Object;)Z", false);
		writer.writeJumpInsn(IFNE, endLabel);
		writer.writeLDC(0);
		writer.writeInsn(IRETURN);
		writer.writeLabel(endLabel);
	}
	
	public static void writeHashCode(MethodWriter writer, CodeClass theClass)
	{
		writer.writeLDC(31);
		
		int len = theClass.parameterCount();
		for (int i = 0; i < len; i++)
		{
			// Write the hashing strategy for the field
			writeHashCode(writer, theClass.getParameter(i));
			// Add the hash to the previous result
			writer.writeInsn(IADD);
			writer.writeLDC(31);
			// Multiply the result by 31
			writer.writeInsn(IMUL);
		}
		
		writer.writeInsn(IRETURN);
	}
	
	private static void writeHashCode(MethodWriter writer, IField field)
	{
		writer.writeVarInsn(ALOAD, 0);
		field.writeGet(writer, null);
		
		IType type = field.getType();
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
		writer.writeInvokeInsn(INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I", false);
		writer.writeJumpInsn(GOTO, endLabel);
		// else
		writer.writeLabel(elseLabel);
		writer.writeInsn(POP);
		writer.writeLDC(0);
		writer.writeLabel(endLabel);
	}
	
	public static void writeToString(MethodWriter writer, CodeClass theClass)
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
			writeToString(writer, theClass.getParameter(i));
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
	
	private static void writeToString(MethodWriter writer, IField field)
	{
		IType type = field.getType();
		
		// Get the field
		writer.writeVarInsn(ALOAD, 0);
		field.writeGet(writer, null);
		
		// Write the call to the StringBuilder#append() method that
		// corresponds to the type of the field
		StringBuilder desc = new StringBuilder().append('(');
		
		if (type.isPrimitive())
		{
			type.appendExtendedName(desc);
		}
		else if (type == Types.STRING)
		{
			desc.append("Ljava/lang/String;");
		}
		else
		{
			desc.append("Ljava/lang/Object;");
		}
		desc.append(")Ljava/lang/StringBuilder;");
		
		writer.writeInvokeInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", desc.toString(), false);
	}
}

package dyvil.tools.compiler.transform;

import static dyvil.reflect.Opcodes.*;

import java.util.Iterator;
import java.util.List;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.classes.CodeClass;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.PrimitiveType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;

public class CaseClasses
{
	public static void writeEquals(MethodWriter writer, CodeClass theClass, List<IField> fields)
	{
		Label label;
		String extended = "L" + theClass.getInternalName() + ";";
		
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
		writer.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false, 1, "Ljava/lang/Class;");
		// obj.getClass()
		writer.visitVarInsn(ALOAD, 1);
		writer.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false, 1, "Ljava/lang/Class;");
		// if
		writer.visitJumpInsn(IF_ACMPEQ, label = new Label());
		// then
		writer.visitLdcInsn(0);
		writer.visitInsn(IRETURN);
		// else
		writer.visitLabel(label);
		
		// var = (ClassName) obj
		writer.visitVarInsn(ALOAD, 1);
		writer.visitTypeInsn(CHECKCAST, extended);
		// 'var' variable that stores the casted 'obj' parameter
		writer.addLocal(extended);
		writer.visitVarInsn(ASTORE, 2);
		
		Iterator<IField> iterator = fields.iterator();
		while (iterator.hasNext())
		{
			IField f = iterator.next();
			
			if (f.hasModifier(Modifiers.STATIC))
			{
				if (iterator.hasNext())
				{
					continue;
				}
				break;
			}
			
			writeEquals(writer, f);
		}
		
		writer.visitLdcInsn(1);
		writer.visitInsn(IRETURN);
	}
	
	private static void writeEquals(MethodWriter writer, IField field)
	{
		IType type = field.getType();
		if (type.isPrimitive())
		{
			// Push 'this'
			writer.visitVarInsn(ALOAD, 0);
			field.writeGet(writer, null);
			// Push 'var'
			writer.visitVarInsn(ALOAD, 2);
			field.writeGet(writer, null);
			
			Label label = new Label();
			switch (((PrimitiveType) type).typecode)
			{
			case Opcodes.T_BOOLEAN:
			case Opcodes.T_BYTE:
			case Opcodes.T_SHORT:
			case Opcodes.T_CHAR:
			case Opcodes.T_INT:
				writer.visitJumpInsn(IF_ICMPEQ, label);
				break;
			case Opcodes.T_LONG:
				writer.visitJumpInsn(IF_LCMPEQ, label);
				break;
			case Opcodes.T_FLOAT:
				writer.visitJumpInsn(IF_FCMPEQ, label);
				break;
			case Opcodes.T_DOUBLE:
				writer.visitJumpInsn(IF_FCMPEQ, label);
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
		field.writeGet(writer, null);
		writer.visitJumpInsn(IFNONNULL, elseLabel);
		writer.visitVarInsn(ALOAD, 2);
		field.writeGet(writer, null);
		writer.visitJumpInsn(IFNULL, endLabel);
		writer.visitLdcInsn(0);
		writer.visitInsn(IRETURN);
		writer.visitLabel(elseLabel);
		writer.visitVarInsn(ALOAD, 0);
		field.writeGet(writer, null);
		writer.visitVarInsn(ALOAD, 2);
		field.writeGet(writer, null);
		writer.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "equals", "(Ljava/lang/Object;)Z", false, 2, MethodWriter.INT);
		writer.visitJumpInsn(IFNE, endLabel);
		writer.visitLdcInsn(0);
		writer.visitInsn(IRETURN);
		writer.visitLabel(endLabel);
	}
	
	public static void writeHashCode(MethodWriter writer, CodeClass theClass, List<IField> fields)
	{
		Iterator<IField> iterator = fields.iterator();
		writer.visitLdcInsn(31);
		while (iterator.hasNext())
		{
			IField f = iterator.next();
			
			if (f.hasModifier(Modifiers.STATIC))
			{
				if (iterator.hasNext())
				{
					continue;
				}
				break;
			}
			
			// Write the hashing strategy for the field
			writeHashCode(writer, f);
			// Add the hash to the previous result
			writer.visitInsn(IADD);
			writer.visitLdcInsn(31);
			// Multiply the result by 31
			writer.visitInsn(IMUL);
		}
		
		writer.visitInsn(IRETURN);
	}
	
	private static void writeHashCode(MethodWriter writer, IField field)
	{
		writer.visitVarInsn(ALOAD, 0);
		field.writeGet(writer, null);
		
		IType type = field.getType();
		if (type.isPrimitive())
		{
			switch (((PrimitiveType) type).typecode)
			{
			case Opcodes.T_BOOLEAN:
			{
				// Write boolean hashing by using 1231 if the value is true and
				// 1237 if the value is false
				Label elseLabel = new Label();
				Label endLabel = new Label();
				// if
				writer.visitJumpInsn(IFEQ, elseLabel);
				// then
				writer.visitLdcInsn(1231);
				writer.pop();
				writer.visitJumpInsn(GOTO, endLabel);
				// else
				writer.visitLabel(elseLabel);
				writer.visitLdcInsn(1237);
				writer.visitLabel(endLabel);
				return;
			}
			case Opcodes.T_BYTE:
				return;
			case Opcodes.T_SHORT:
				return;
			case Opcodes.T_CHAR:
				return;
			case Opcodes.T_INT:
				return;
			case Opcodes.T_LONG:
				// Write a long hashing snippet by XORing the value by the value
				// bit-shifted 32 bits to the right, and then converting the
				// result to an integer. l1 = (int) (l ^ (l >>> 32))
				writer.visitInsn(DUP2);
				writer.visitLdcInsn(32);
				writer.visitInsn(LUSHR);
				writer.visitInsn(LOR);
				writer.visitInsn(L2I);
				return;
			case Opcodes.T_FLOAT:
				// Write a float hashing snippet using Float.floatToIntBits
				writer.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "floatToIntBits", "(F)I", false, 1, MethodWriter.FLOAT);
				return;
			case Opcodes.T_DOUBLE:
				// Write a double hashing snippet using Double.doubleToLongBits
				// and long hashing
				writer.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "doubleToLongBits", "(D)L", false, 1, MethodWriter.DOUBLE);
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
		writer.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I", false, 0, null);
		writer.visitJumpInsn(GOTO, endLabel);
		// else
		writer.visitLabel(elseLabel);
		writer.visitInsn(POP);
		writer.visitLdcInsn(0);
		writer.visitLabel(endLabel);
	}
	
	public static void writeToString(MethodWriter writer, CodeClass theClass, List<IField> fields)
	{
		// ----- StringBuilder Constructor -----
		writer.visitTypeInsn(NEW, "java/lang/StringBuilder");
		writer.visitInsn(DUP);
		// Call the StringBuilder(String) constructor with the "[ClassName]("
		// argument
		writer.visitLdcInsn(theClass.getName() + "(");
		writer.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false, 2, (String) null);
		
		// ----- Fields -----
		if (!fields.isEmpty())
		{
			Iterator<IField> iterator = fields.iterator();
			while (true)
			{
				IField f = iterator.next();
				
				if (f.hasModifier(Modifiers.STATIC))
				{
					if (iterator.hasNext())
					{
						continue;
					}
					break;
				}
				
				IType type = f.getType();
				
				// Get the field
				writer.visitVarInsn(ALOAD, 0);
				f.writeGet(writer, null);
				
				// Write the call to the StringBuilder#append() method that
				// corresponds to the type of the field
				StringBuilder desc = new StringBuilder().append('(');
				
				if (type.isPrimitive())
				{
					type.appendExtendedName(desc);
				}
				else if (type == Type.STRING)
				{
					desc.append("Ljava/lang/String;");
				}
				else
				{
					desc.append("Ljava/lang/Object;");
				}
				desc.append(")Ljava/lang/StringBuilder;");
				
				writer.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", desc.toString(), false, 2, "Ljava/lang/StringBuilder;");
				
				if (iterator.hasNext())
				{
					// Separator Comma
					writer.visitLdcInsn(", ");
					writer.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false, 2,
							"Ljava/lang/StringBuilder;");
				}
				else
				{
					break;
				}
			}
		}
		
		// ----- Append Closing Parenthesis -----
		writer.visitLdcInsn(")");
		// Write the call to the StringBuilder#append(String) method
		writer.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false, 2,
				"Ljava/lang/StringBuilder;");
		
		// ----- ToString -----
		// Write the call to the StringBuilder#toString() method
		writer.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false, 1, "Ljava/lang/String;");
		// Write the return
		writer.visitInsn(ARETURN);
	}
}

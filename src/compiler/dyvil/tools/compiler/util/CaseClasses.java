package dyvil.tools.compiler.util;

import static dyvil.reflect.Opcodes.*;

import java.util.Iterator;
import java.util.List;

import dyvil.tools.compiler.ast.classes.CodeClass;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;

public class CaseClasses
{
	public static void writeEquals(MethodWriter writer, CodeClass theClass, List<IField> fields)
	{
		// TODO Case Class equals
		writer.visitLdcInsn(0);
		writer.visitInsn(IRETURN);
	}
	
	public static void writeHashCode(MethodWriter writer, CodeClass theClass, List<IField> fields)
	{
		// TODO Case Class hashCode
		writer.visitLdcInsn(0);
		writer.visitInsn(IRETURN);
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
			f.writeGet(writer);
			
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
				// Seperator Comma
				writer.visitLdcInsn(", ");
				writer.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false, 2,
						"Ljava/lang/StringBuilder;");
			}
			else
			{
				break;
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

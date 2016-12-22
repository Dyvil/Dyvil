package dyvil.tools.compiler.ast.type.builtin;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.parsing.Name;

public class AnyType implements IBuiltinType
{
	public static final String OBJECT_INTERNAL = "java/lang/Object";

	@Override
	public int typeTag()
	{
		return ANY;
	}

	@Override
	public Name getName()
	{
		return Names.any;
	}

	@Override
	public IClass getTheClass()
	{
		return Types.OBJECT_CLASS;
	}

	// Subtyping

	@Override
	public boolean isSuperTypeOf(IType subType)
	{
		return true;
	}

	@Override
	public boolean isSuperClassOf(IType subType)
	{
		return true;
	}

	// Compilation

	@Override
	public String getInternalName()
	{
		return OBJECT_INTERNAL;
	}

	@Override
	public void appendDescriptor(StringBuilder buffer, int type)
	{
		if (type == NAME_FULL)
		{
			buffer.append("a");
			return;
		}
		buffer.append("L" + OBJECT_INTERNAL + ";");
	}

	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.visitFieldInsn(Opcodes.GETSTATIC, "dyvilx/lang/model/type/AnyType", "instance",
		                      "Ldyvilx/lang/model/type/AnyType;");
	}

	@Override
	public String toString()
	{
		return "any";
	}
}

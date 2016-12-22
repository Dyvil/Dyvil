package dyvil.tools.compiler.ast.type.builtin;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.parsing.Name;

public class NoneType implements IBuiltinType
{
	public static final String NONE_INTERNAL = "dyvil/lang/None";

	@Override
	public int typeTag()
	{
		return NONE;
	}

	@Override
	public Name getName()
	{
		return Names.none;
	}

	@Override
	public IClass getTheClass()
	{
		return Types.NONE_CLASS;
	}

	// Subtyping

	@Override
	public int subTypeCheckLevel()
	{
		return SUBTYPE_NULL;
	}

	@Override
	public boolean isSubTypeOf(IType superType)
	{
		return true;
	}

	@Override
	public boolean isSubClassOf(IType superType)
	{
		return true;
	}

	// Compilation

	@Override
	public String getInternalName()
	{
		return NONE_INTERNAL;
	}

	@Override
	public void appendDescriptor(StringBuilder buffer, int type)
	{
		if (type == NAME_FULL)
		{
			buffer.append('b');
			return;
		}
		buffer.append("L" + NONE_INTERNAL + ";");
	}

	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.visitFieldInsn(Opcodes.GETSTATIC, "dyvilx/lang/model/type/NoneType", "instance",
		                      "Ldyvilx/lang/model/type/NoneType;");
	}

	@Override
	public String toString()
	{
		return "none";
	}
}

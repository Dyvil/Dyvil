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
	public static final String NONE_INTERNAL = "dyvil/lang/internal/None";

	public static final char NONE_DESC = 'b';

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

	@Override
	public boolean useNonNullAnnotation()
	{
		return false;
	}

	// Subtyping

	@Override
	public int subTypeCheckLevel()
	{
		return SUBTYPE_NULL;
	}

	@Override
	public boolean isSuperTypeOf(IType subType)
	{
		return subType.hasTag(NONE);
	}

	@Override
	public boolean isSuperClassOf(IType subType)
	{
		return subType.hasTag(NONE);
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
			buffer.append(NONE_DESC);
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

package dyvilx.tools.compiler.ast.type.builtin;

import dyvil.reflect.Opcodes;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.compound.NullableType;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.transform.Names;
import dyvil.lang.Name;

public class NullType implements IBuiltinType
{
	public static final String NULL_INTERNAL = "dyvil/lang/internal/Null";

	public static final char NULL_DESC = 'n';

	@Override
	public int typeTag()
	{
		return NULL;
	}

	@Override
	public Name getName()
	{
		return Names._null;
	}

	@Override
	public IClass getTheClass()
	{
		return Types.NULL_CLASS;
	}

	@Override
	public boolean useNonNullAnnotation()
	{
		return false;
	}

	@Override
	public int subTypeCheckLevel()
	{
		return IType.SUBTYPE_NULL;
	}

	@Override
	public boolean isSubClassOf(IType superType)
	{
		return true;
	}

	@Override
	public boolean isSubTypeOf(IType superType)
	{
		return superType.canExtract(NullType.class) || NullableType.isNullable(superType);
	}

	// Compilation

	@Override
	public String getInternalName()
	{
		return NULL_INTERNAL;
	}

	@Override
	public void appendDescriptor(StringBuilder buffer, int type)
	{
		if (type == NAME_FULL)
		{
			buffer.append(NULL_DESC);
			return;
		}
		buffer.append("L" + NULL_INTERNAL + ";");
	}

	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.visitFieldInsn(Opcodes.GETSTATIC, "dyvil/reflect/types/NullType", "instance",
		                      "Ldyvil/reflect/types/NullType;");
	}

	@Override
	public void writeCast(MethodWriter writer, IType target, int lineNumber) throws BytecodeException
	{
		if (!target.hasTag(IType.NULL))
		{
			writer.visitInsn(Opcodes.POP);
			writer.visitInsn(Opcodes.ACONST_NULL);
		}
	}

	@Override
	public String toString()
	{
		return "null";
	}
}

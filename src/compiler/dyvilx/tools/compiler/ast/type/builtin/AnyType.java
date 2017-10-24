package dyvilx.tools.compiler.ast.type.builtin;

import dyvil.reflect.Opcodes;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.transform.Names;
import dyvil.lang.Name;

public class AnyType implements IBuiltinType
{
	public static final String OBJECT_INTERNAL = "java/lang/Object";

	public static final char ANY_DESC = 'a';

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

	// Resolution

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
		Types.OBJECT_CLASS.getMethodMatches(list, receiver, name, arguments);
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
			buffer.append(ANY_DESC);
			return;
		}
		buffer.append("L" + OBJECT_INTERNAL + ";");
	}

	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.visitFieldInsn(Opcodes.GETSTATIC, "dyvil/reflect/types/AnyType", "instance",
		                      "Ldyvil/reflect/types/AnyType;");
	}

	@Override
	public String toString()
	{
		return "any";
	}
}

package dyvil.tools.compiler.util;

import dyvil.tools.compiler.ast.access.FieldAccess;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.operator.BooleanAnd;
import dyvil.tools.compiler.ast.operator.BooleanOr;
import dyvil.tools.compiler.ast.operator.SwapOperator;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;

public class Operators
{
	public static IValue get(IValue arg1, String name, IValue arg2)
	{
		boolean isOr = false;
		if (":=:".equals(name))
		{
			if (arg1.getValueType() == IValue.FIELD_ACCESS && arg2.getValueType() == IValue.FIELD_ACCESS)
			{
				return new SwapOperator((FieldAccess) arg1, (FieldAccess) arg2);
			}
			return null;
		}
		else if ("&&".equals(name) || (isOr = "||".equals(name)))
		{
			IType t1 = arg1.getType();
			IType t2 = arg2.getType();
			if (t1.classEquals(Type.BOOLEAN) && t2.classEquals(Type.BOOLEAN))
			{
				return isOr ? new BooleanOr(arg1, arg2) : new BooleanAnd(arg1, arg2);
			}
			return null;
		}
		return null;
	}
}

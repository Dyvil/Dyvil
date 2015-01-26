package dyvil.tools.compiler.util;

import dyvil.tools.compiler.ast.access.ClassAccess;
import dyvil.tools.compiler.ast.access.FieldAccess;
import dyvil.tools.compiler.ast.operator.*;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;

public class Operators
{
	public static IValue get(IValue arg1, String name)
	{
		return null;
	}
	
	public static IValue get(String name, IValue arg1)
	{
		return null;
	}
	
	public static IValue get(IValue arg1, String name, IValue arg2)
	{
		// Swap Operator
		if (":=:".equals(name))
		{
			if (arg1.getValueType() == IValue.FIELD_ACCESS && arg2.getValueType() == IValue.FIELD_ACCESS)
			{
				return new SwapOperator((FieldAccess) arg1, (FieldAccess) arg2);
			}
			return null;
		}
		// Cast Operator
		if (":>".equals(name))
		{
			if (arg2.getValueType() == IValue.CLASS_ACCESS)
			{
				return new CastOperator(arg1, ((ClassAccess) arg2).type);
			}
			return null;
		}
		// Instanceof Operator
		if ("<:".equals(name))
		{
			if (arg2.getValueType() == IValue.CLASS_ACCESS)
			{
				return new InstanceOfOperator(arg1, ((ClassAccess) arg2).type);
			}
			return null;
		}
		IType t1 = arg1.getType();
		IType t2 = arg2.getType();
		if ("&&".equals(name))
		{
			if (t1.classEquals(Type.BOOLEAN) && t2.classEquals(Type.BOOLEAN))
			{
				return new BooleanAnd(arg1, arg2);
			}
			return null;
		}
		if ("||".equals(name))
		{
			if (t1.classEquals(Type.BOOLEAN) && t2.classEquals(Type.BOOLEAN))
			{
				return new BooleanOr(arg1, arg2);
			}
			return null;
		}
		return null;
	}
}

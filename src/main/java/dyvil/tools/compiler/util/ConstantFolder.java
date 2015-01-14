package dyvil.tools.compiler.util;

import static dyvil.tools.compiler.ast.api.IValue.*;
import dyvil.tools.compiler.ast.api.IValue;
import dyvil.tools.compiler.ast.value.*;

public class ConstantFolder
{
	public static IValue apply(IValue v1, String op)
	{
		switch (v1.getValueType())
		{
		case BOOLEAN:
			return applyBoolean((BooleanValue) v1, op);
		case CHAR:
			return applyChar((CharValue) v1, op);
		case INT:
			return applyInt((IntValue) v1, op);
		case LONG:
			return applyLong((LongValue) v1, op);
		case FLOAT:
			return applyFloat((FloatValue) v1, op);
		case DOUBLE:
			return applyDouble((DoubleValue) v1, op);
		}
		return null;
	}
	
	public static IValue apply(IValue v1, String op, IValue v2)
	{
		return null;
	}
	
	private static IValue applyBoolean(BooleanValue v, String op)
	{
		if ("$bang".equals(op))
		{
			return new BooleanValue(!v.value);
		}
		return null;
	}
	
	private static IValue applyChar(CharValue v, String op)
	{
		if ("$tilde".equals(op))
		{
			return new CharValue((char) ~v.value);
		}
		else if ("$minus".equals(op))
		{
			return new CharValue((char) -v.value);
		}
		return null;
	}
	
	private static IValue applyInt(IntValue v, String op)
	{
		if ("$tilde".equals(op))
		{
			return new IntValue(~v.value);
		}
		else if ("$minus".equals(op))
		{
			return new IntValue(-v.value);
		}
		return null;
	}
	
	private static IValue applyLong(LongValue v, String op)
	{
		if ("$tilde".equals(op))
		{
			return new LongValue(~v.value);
		}
		else if ("$minus".equals(op))
		{
			return new LongValue(-v.value);
		}
		return null;
	}
	
	private static IValue applyFloat(FloatValue v, String op)
	{
		if ("$minus".equals(op))
		{
			return new FloatValue(-v.value);
		}
		return null;
	}
	
	private static IValue applyDouble(DoubleValue v, String op)
	{
		if ("$minus".equals(op))
		{
			return new DoubleValue(-v.value);
		}
		return null;
	}
}

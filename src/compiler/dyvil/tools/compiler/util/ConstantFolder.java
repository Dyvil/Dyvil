package dyvil.tools.compiler.util;

import static dyvil.tools.compiler.ast.expression.IValue.*;
import dyvil.tools.compiler.ast.expression.IValue;
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
		int t1 = v1.getValueType();
		int t2 = v2.getValueType();
		if (isNumeric(t1) && isNumeric(t2))
		{
			int type = Math.max(t1, t2);
			switch (type)
			{
			case CHAR:
			case INT:
				return applyInt((INumericValue) v1, op, (INumericValue) v2);
			case LONG:
				return applyLong((INumericValue) v1, op, (INumericValue) v2);
			case FLOAT:
				return applyFloat((INumericValue) v1, op, (INumericValue) v2);
			case DOUBLE:
				return applyDouble((INumericValue) v1, op, (INumericValue) v2);
			}
		}
		else if (t1 == STRING && t2 == STRING && "$plus".equals(op))
		{
			return new StringValue(((StringValue) v1).value + ((StringValue) v2).value);
		}
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
	
	private static IValue applyInt(INumericValue v1, String op, INumericValue v2)
	{
		switch (op)
		{
		case "$plus":
			return new IntValue(v1.intValue() + v2.intValue());
		case "$minus":
			return new IntValue(v1.intValue() - v2.intValue());
		case "$times":
			return new IntValue(v1.intValue() * v2.intValue());
		case "$div":
			return new IntValue(v1.intValue() / v2.intValue());
		case "$percent":
			return new IntValue(v1.intValue() % v2.intValue());
		case "$amp":
			return new IntValue(v1.intValue() & v2.intValue());
		case "$bar":
			return new IntValue(v1.intValue() | v2.intValue());
		case "$up":
			return new IntValue(v1.intValue() ^ v2.intValue());
		case "$less$less":
			return new IntValue(v1.intValue() << v2.intValue());
		case "$greater$greater":
			return new IntValue(v1.intValue() >> v2.intValue());
		case "$greater$greater$greater":
			return new IntValue(v1.intValue() >>> v2.intValue());
		}
		return null;
	}
	
	private static IValue applyLong(INumericValue v1, String op, INumericValue v2)
	{
		switch (op)
		{
		case "$plus":
			return new LongValue(v1.longValue() + v2.longValue());
		case "$minus":
			return new LongValue(v1.longValue() - v2.longValue());
		case "$times":
			return new LongValue(v1.longValue() * v2.longValue());
		case "$div":
			return new LongValue(v1.longValue() / v2.longValue());
		case "$percent":
			return new LongValue(v1.longValue() % v2.longValue());
		case "$amp":
			return new LongValue(v1.longValue() & v2.longValue());
		case "$bar":
			return new LongValue(v1.longValue() | v2.longValue());
		case "$up":
			return new LongValue(v1.longValue() ^ v2.longValue());
		case "$less$less":
			return new LongValue(v1.longValue() << v2.longValue());
		case "$greater$greater":
			return new LongValue(v1.longValue() >> v2.longValue());
		case "$greater$greater$greater":
			return new LongValue(v1.longValue() >>> v2.longValue());
		}
		return null;
	}
	
	private static IValue applyFloat(INumericValue v1, String op, INumericValue v2)
	{
		switch (op)
		{
		case "$plus":
			return new FloatValue(v1.floatValue() + v2.floatValue());
		case "$minus":
			return new FloatValue(v1.floatValue() - v2.floatValue());
		case "$times":
			return new FloatValue(v1.floatValue() * v2.floatValue());
		case "$div":
			return new FloatValue(v1.floatValue() / v2.floatValue());
		case "$percent":
			return new FloatValue(v1.floatValue() % v2.floatValue());
		}
		return null;
	}
	
	private static IValue applyDouble(INumericValue v1, String op, INumericValue v2)
	{
		switch (op)
		{
		case "$plus":
			return new DoubleValue(v1.doubleValue() + v2.doubleValue());
		case "$minus":
			return new DoubleValue(v1.doubleValue() - v2.doubleValue());
		case "$times":
			return new DoubleValue(v1.doubleValue() * v2.doubleValue());
		case "$div":
			return new DoubleValue(v1.doubleValue() / v2.doubleValue());
		case "$percent":
			return new DoubleValue(v1.doubleValue() % v2.doubleValue());
		}
		return null;
	}
}

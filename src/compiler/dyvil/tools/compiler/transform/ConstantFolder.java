package dyvil.tools.compiler.transform;

import static dyvil.tools.compiler.ast.expression.IValue.*;
import dyvil.tools.compiler.ast.constant.*;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.Name;

public class ConstantFolder
{
	public static IValue apply(Name op, IValue v1)
	{
		switch (v1.valueTag())
		{
		case BOOLEAN:
			return applyBoolean((BooleanValue) v1, op);
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
	
	public static IValue apply(IValue v1, Name op, IValue v2)
	{
		int t1 = v1.valueTag();
		int t2 = v2.valueTag();
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
		if (t1 == STRING && t2 == STRING && Name.plus.equals(op))
		{
			return new StringValue(((StringValue) v1).value + ((StringValue) v2).value);
		}
		return null;
	}
	
	private static IValue applyBoolean(BooleanValue v, Name op)
	{
		if (op == Name.bang)
		{
			return new BooleanValue(!v.value);
		}
		return null;
	}
	
	private static IValue applyInt(IntValue v, Name op)
	{
		if (op == Name.tilde)
		{
			return new IntValue(~v.value);
		}
		if (op == Name.minus)
		{
			return new IntValue(-v.value);
		}
		return null;
	}
	
	private static IValue applyLong(LongValue v, Name op)
	{
		if (op == Name.tilde)
		{
			return new LongValue(~v.value);
		}
		if (op == Name.minus)
		{
			return new LongValue(-v.value);
		}
		return null;
	}
	
	private static IValue applyFloat(FloatValue v, Name op)
	{
		if (op == Name.minus)
		{
			return new FloatValue(-v.value);
		}
		return null;
	}
	
	private static IValue applyDouble(DoubleValue v, Name op)
	{
		if (op == Name.minus)
		{
			return new DoubleValue(-v.value);
		}
		return null;
	}
	
	private static IValue applyInt(INumericValue v1, Name op, INumericValue v2)
	{
		if (op == Name.plus)
		{
			return new IntValue(v1.intValue() + v2.intValue());
		}
		if (op == Name.minus)
		{
			return new IntValue(v1.intValue() - v2.intValue());
		}
		if (op == Name.times)
		{
			return new IntValue(v1.intValue() * v2.intValue());
		}
		if (op == Name.div)
		{
			float i2 = v2.floatValue();
			return i2 == 0F ? null : new FloatValue(v1.floatValue() / i2);
		}
		if (op == Name.bslash)
		{
			int i2 = v2.intValue();
			return i2 == 0 ? null : new IntValue(v1.intValue() / i2);
		}
		if (op == Name.percent)
		{
			int i2 = v2.intValue();
			return i2 == 0 ? null : new IntValue(v1.intValue() % i2);
		}
		if (op == Name.amp)
		{
			return new IntValue(v1.intValue() & v2.intValue());
		}
		if (op == Name.bar)
		{
			return new IntValue(v1.intValue() | v2.intValue());
		}
		if (op == Name.up)
		{
			return new IntValue(v1.intValue() ^ v2.intValue());
		}
		if (op == Name.ltlt)
		{
			return new IntValue(v1.intValue() << v2.intValue());
		}
		if (op == Name.gtgt)
		{
			return new IntValue(v1.intValue() >> v2.intValue());
		}
		if (op == Name.gtgtgt)
		{
			return new IntValue(v1.intValue() >>> v2.intValue());
		}
		return null;
	}
	
	private static IValue applyLong(INumericValue v1, Name op, INumericValue v2)
	{
		if (op == Name.plus)
		{
			return new LongValue(v1.longValue() + v2.longValue());
		}
		if (op == Name.minus)
		{
			return new LongValue(v1.longValue() - v2.longValue());
		}
		if (op == Name.times)
		{
			return new LongValue(v1.longValue() * v2.longValue());
		}
		if (op == Name.div)
		{
			double l2 = v2.doubleValue();
			return l2 == 0D ? null : new DoubleValue(v1.longValue() / l2);
		}
		if (op == Name.bslash)
		{
			long l2 = v2.longValue();
			return l2 == 0L ? null : new LongValue(v1.longValue() / l2);
		}
		if (op == Name.percent)
		{
			long l2 = v2.longValue();
			return l2 == 0L ? null : new LongValue(v1.longValue() % l2);
		}
		if (op == Name.amp)
		{
			return new LongValue(v1.longValue() & v2.longValue());
		}
		if (op == Name.bar)
		{
			return new LongValue(v1.longValue() | v2.longValue());
		}
		if (op == Name.up)
		{
			return new LongValue(v1.longValue() ^ v2.longValue());
		}
		if (op == Name.ltlt)
		{
			return new LongValue(v1.longValue() << v2.longValue());
		}
		if (op == Name.gtgt)
		{
			return new LongValue(v1.longValue() >> v2.longValue());
		}
		if (op == Name.gtgtgt)
		{
			return new LongValue(v1.longValue() >>> v2.longValue());
		}
		return null;
	}
	
	private static IValue applyFloat(INumericValue v1, Name op, INumericValue v2)
	{
		if (op == Name.plus)
		{
			return new FloatValue(v1.floatValue() + v2.floatValue());
		}
		if (op == Name.minus)
		{
			return new FloatValue(v1.floatValue() - v2.floatValue());
		}
		if (op == Name.times)
		{
			return new FloatValue(v1.floatValue() * v2.floatValue());
		}
		if (op == Name.div)
		{
			return new FloatValue(v1.floatValue() / v2.floatValue());
		}
		if (op == Name.percent)
		{
			return new FloatValue(v1.floatValue() % v2.floatValue());
		}
		return null;
	}
	
	private static IValue applyDouble(INumericValue v1, Name op, INumericValue v2)
	{
		if (op == Name.plus)
		{
			return new DoubleValue(v1.doubleValue() + v2.doubleValue());
		}
		if (op == Name.minus)
		{
			return new DoubleValue(v1.doubleValue() - v2.doubleValue());
		}
		if (op == Name.times)
		{
			return new DoubleValue(v1.doubleValue() * v2.doubleValue());
		}
		if (op == Name.div)
		{
			return new DoubleValue(v1.doubleValue() / v2.doubleValue());
		}
		if (op == Name.percent)
		{
			return new DoubleValue(v1.doubleValue() % v2.doubleValue());
		}
		return null;
	}
}

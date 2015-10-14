package dyvil.tools.compiler.transform;

import dyvil.tools.compiler.ast.constant.*;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.parsing.Name;

import static dyvil.tools.compiler.ast.expression.IValue.*;

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
				return applyInt(v1, op, v2);
			case LONG:
				return applyLong(v1, op, v2);
			case FLOAT:
				return applyFloat(v1, op, v2);
			case DOUBLE:
				return applyDouble(v1, op, v2);
			}
		}
		if (t1 == STRING && t2 == STRING && Names.plus.equals(op))
		{
			return new StringValue(v1.stringValue() + v2.stringValue());
		}
		return null;
	}
	
	private static IValue applyBoolean(BooleanValue v, Name op)
	{
		if (op == Names.bang)
		{
			return new BooleanValue(!v.booleanValue());
		}
		return null;
	}
	
	private static IValue applyInt(IntValue v, Name op)
	{
		if (op == Names.tilde)
		{
			return new IntValue(~v.intValue());
		}
		if (op == Names.minus)
		{
			return new IntValue(-v.intValue());
		}
		return null;
	}
	
	private static IValue applyLong(LongValue v, Name op)
	{
		if (op == Names.tilde)
		{
			return new LongValue(~v.longValue());
		}
		if (op == Names.minus)
		{
			return new LongValue(-v.longValue());
		}
		return null;
	}
	
	private static IValue applyFloat(FloatValue v, Name op)
	{
		if (op == Names.minus)
		{
			return new FloatValue(-v.floatValue());
		}
		return null;
	}
	
	private static IValue applyDouble(DoubleValue v, Name op)
	{
		if (op == Names.minus)
		{
			return new DoubleValue(-v.doubleValue());
		}
		return null;
	}
	
	private static IValue applyInt(IValue v1, Name op, IValue v2)
	{
		if (op == Names.plus)
		{
			return new IntValue(v1.intValue() + v2.intValue());
		}
		if (op == Names.minus)
		{
			return new IntValue(v1.intValue() - v2.intValue());
		}
		if (op == Names.times)
		{
			return new IntValue(v1.intValue() * v2.intValue());
		}
		if (op == Names.div)
		{
			float i2 = v2.floatValue();
			return i2 == 0F ? null : new FloatValue(v1.floatValue() / i2);
		}
		if (op == Names.bslash)
		{
			int i2 = v2.intValue();
			return i2 == 0 ? null : new IntValue(v1.intValue() / i2);
		}
		if (op == Names.percent)
		{
			int i2 = v2.intValue();
			return i2 == 0 ? null : new IntValue(v1.intValue() % i2);
		}
		if (op == Names.amp)
		{
			return new IntValue(v1.intValue() & v2.intValue());
		}
		if (op == Names.bar)
		{
			return new IntValue(v1.intValue() | v2.intValue());
		}
		if (op == Names.up)
		{
			return new IntValue(v1.intValue() ^ v2.intValue());
		}
		if (op == Names.ltlt)
		{
			return new IntValue(v1.intValue() << v2.intValue());
		}
		if (op == Names.gtgt)
		{
			return new IntValue(v1.intValue() >> v2.intValue());
		}
		if (op == Names.gtgtgt)
		{
			return new IntValue(v1.intValue() >>> v2.intValue());
		}
		return null;
	}
	
	private static IValue applyLong(IValue v1, Name op, IValue v2)
	{
		if (op == Names.plus)
		{
			return new LongValue(v1.longValue() + v2.longValue());
		}
		if (op == Names.minus)
		{
			return new LongValue(v1.longValue() - v2.longValue());
		}
		if (op == Names.times)
		{
			return new LongValue(v1.longValue() * v2.longValue());
		}
		if (op == Names.div)
		{
			double l2 = v2.doubleValue();
			return l2 == 0D ? null : new DoubleValue(v1.longValue() / l2);
		}
		if (op == Names.bslash)
		{
			long l2 = v2.longValue();
			return l2 == 0L ? null : new LongValue(v1.longValue() / l2);
		}
		if (op == Names.percent)
		{
			long l2 = v2.longValue();
			return l2 == 0L ? null : new LongValue(v1.longValue() % l2);
		}
		if (op == Names.amp)
		{
			return new LongValue(v1.longValue() & v2.longValue());
		}
		if (op == Names.bar)
		{
			return new LongValue(v1.longValue() | v2.longValue());
		}
		if (op == Names.up)
		{
			return new LongValue(v1.longValue() ^ v2.longValue());
		}
		if (op == Names.ltlt)
		{
			return new LongValue(v1.longValue() << v2.longValue());
		}
		if (op == Names.gtgt)
		{
			return new LongValue(v1.longValue() >> v2.longValue());
		}
		if (op == Names.gtgtgt)
		{
			return new LongValue(v1.longValue() >>> v2.longValue());
		}
		return null;
	}
	
	private static IValue applyFloat(IValue v1, Name op, IValue v2)
	{
		if (op == Names.plus)
		{
			return new FloatValue(v1.floatValue() + v2.floatValue());
		}
		if (op == Names.minus)
		{
			return new FloatValue(v1.floatValue() - v2.floatValue());
		}
		if (op == Names.times)
		{
			return new FloatValue(v1.floatValue() * v2.floatValue());
		}
		if (op == Names.div)
		{
			return new FloatValue(v1.floatValue() / v2.floatValue());
		}
		if (op == Names.percent)
		{
			return new FloatValue(v1.floatValue() % v2.floatValue());
		}
		return null;
	}
	
	private static IValue applyDouble(IValue v1, Name op, IValue v2)
	{
		if (op == Names.plus)
		{
			return new DoubleValue(v1.doubleValue() + v2.doubleValue());
		}
		if (op == Names.minus)
		{
			return new DoubleValue(v1.doubleValue() - v2.doubleValue());
		}
		if (op == Names.times)
		{
			return new DoubleValue(v1.doubleValue() * v2.doubleValue());
		}
		if (op == Names.div)
		{
			return new DoubleValue(v1.doubleValue() / v2.doubleValue());
		}
		if (op == Names.percent)
		{
			return new DoubleValue(v1.doubleValue() % v2.doubleValue());
		}
		return null;
	}
}

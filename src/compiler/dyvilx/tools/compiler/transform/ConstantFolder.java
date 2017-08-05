package dyvilx.tools.compiler.transform;

import dyvilx.tools.compiler.ast.expression.constant.*;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvil.lang.Name;

import static dyvilx.tools.compiler.ast.expression.IValue.*;

public final class ConstantFolder
{
	private ConstantFolder()
	{
		// no instances
	}

	public static IValue applyUnary(Name op, IValue operand)
	{
		switch (operand.valueTag())
		{
		case BOOLEAN:
			return applyPrefixBoolean(op, (BooleanValue) operand);
		case INT:
			return applyPrefixInt(op, (IntValue) operand);
		case LONG:
			return applyPrefixLong(op, (LongValue) operand);
		case FLOAT:
			return applyPrefixFloat(op, (FloatValue) operand);
		case DOUBLE:
			return applyPrefixDouble(op, (DoubleValue) operand);
		case STRING:
			if (op == Names.length)
			{
				return new IntValue(operand.stringSize());
			}
		}
		return null;
	}
	
	public static IValue applyInfix(IValue lhs, Name op, IValue rhs)
	{
		final int lhsType = lhs.valueTag();
		final int rhsType = rhs.valueTag();
		if (isNumeric(lhsType) && isNumeric(rhsType))
		{
			final int maxType = Math.max(rhsType, lhsType);
			switch (maxType)
			{
			case CHAR:
			case INT:
				return applyInfixInt(lhs, op, rhs);
			case LONG:
				return applyInfixLong(lhs, op, rhs);
			case FLOAT:
				return applyInfixFloat(lhs, op, rhs);
			case DOUBLE:
				return applyInfixDouble(lhs, op, rhs);
			}
		}
		if (rhsType == STRING && lhsType == STRING && op == Names.plus)
		{
			// Use concat instead of + because the strings are known to be non-null
			return new StringValue(lhs.stringValue().concat(rhs.stringValue()));
		}
		return null;
	}
	
	private static IValue applyPrefixBoolean(Name op, BooleanValue operand)
	{
		if (op == Names.bang)
		{
			return new BooleanValue(!operand.booleanValue());
		}
		return null;
	}
	
	private static IValue applyPrefixInt(Name op, IntValue operand)
	{
		if (op == Names.tilde)
		{
			return new IntValue(~operand.intValue());
		}
		if (op == Names.minus)
		{
			return new IntValue(-operand.intValue());
		}
		return null;
	}
	
	private static IValue applyPrefixLong(Name op, LongValue operand)
	{
		if (op == Names.tilde)
		{
			return new LongValue(~operand.longValue());
		}
		if (op == Names.minus)
		{
			return new LongValue(-operand.longValue());
		}
		return null;
	}
	
	private static IValue applyPrefixFloat(Name op, FloatValue operand)
	{
		if (op == Names.minus)
		{
			return new FloatValue(-operand.floatValue());
		}
		return null;
	}
	
	private static IValue applyPrefixDouble(Name op, DoubleValue operand)
	{
		if (op == Names.minus)
		{
			return new DoubleValue(-operand.doubleValue());
		}
		return null;
	}
	
	private static IValue applyInfixInt(IValue lhs, Name op, IValue rhs)
	{
		if (op == Names.plus)
		{
			return new IntValue(lhs.intValue() + rhs.intValue());
		}
		if (op == Names.minus)
		{
			return new IntValue(lhs.intValue() - rhs.intValue());
		}
		if (op == Names.times)
		{
			return new IntValue(lhs.intValue() * rhs.intValue());
		}
		if (op == Names.div)
		{
			final float rhsFloat = rhs.floatValue();
			return rhsFloat == 0F ? null : new FloatValue(lhs.floatValue() / rhsFloat);
		}
		if (op == Names.bslash)
		{
			final int rhsInt = rhs.intValue();
			return rhsInt == 0 ? null : new IntValue(lhs.intValue() / rhsInt);
		}
		if (op == Names.percent)
		{
			final int rhsInt = rhs.intValue();
			return rhsInt == 0 ? null : new IntValue(lhs.intValue() % rhsInt);
		}
		if (op == Names.amp)
		{
			return new IntValue(lhs.intValue() & rhs.intValue());
		}
		if (op == Names.bar)
		{
			return new IntValue(lhs.intValue() | rhs.intValue());
		}
		if (op == Names.up)
		{
			return new IntValue(lhs.intValue() ^ rhs.intValue());
		}
		if (op == Names.ltlt)
		{
			return new IntValue(lhs.intValue() << rhs.intValue());
		}
		if (op == Names.gtgt)
		{
			return new IntValue(lhs.intValue() >> rhs.intValue());
		}
		if (op == Names.gtgtgt)
		{
			return new IntValue(lhs.intValue() >>> rhs.intValue());
		}
		return null;
	}
	
	private static IValue applyInfixLong(IValue lhs, Name op, IValue rhs)
	{
		if (op == Names.plus)
		{
			return new LongValue(lhs.longValue() + rhs.longValue());
		}
		if (op == Names.minus)
		{
			return new LongValue(lhs.longValue() - rhs.longValue());
		}
		if (op == Names.times)
		{
			return new LongValue(lhs.longValue() * rhs.longValue());
		}
		if (op == Names.div)
		{
			final double rhsDouble = rhs.doubleValue();
			return rhsDouble == 0D ? null : new DoubleValue(lhs.longValue() / rhsDouble);
		}
		if (op == Names.bslash)
		{
			final long rhsLong = rhs.longValue();
			return rhsLong == 0L ? null : new LongValue(lhs.longValue() / rhsLong);
		}
		if (op == Names.percent)
		{
			final long rhsLong = rhs.longValue();
			return rhsLong == 0L ? null : new LongValue(lhs.longValue() % rhsLong);
		}
		if (op == Names.amp)
		{
			return new LongValue(lhs.longValue() & rhs.longValue());
		}
		if (op == Names.bar)
		{
			return new LongValue(lhs.longValue() | rhs.longValue());
		}
		if (op == Names.up)
		{
			return new LongValue(lhs.longValue() ^ rhs.longValue());
		}
		if (op == Names.ltlt)
		{
			return new LongValue(lhs.longValue() << rhs.longValue());
		}
		if (op == Names.gtgt)
		{
			return new LongValue(lhs.longValue() >> rhs.longValue());
		}
		if (op == Names.gtgtgt)
		{
			return new LongValue(lhs.longValue() >>> rhs.longValue());
		}
		return null;
	}
	
	private static IValue applyInfixFloat(IValue lhs, Name op, IValue rhs)
	{
		if (op == Names.plus)
		{
			return new FloatValue(lhs.floatValue() + rhs.floatValue());
		}
		if (op == Names.minus)
		{
			return new FloatValue(lhs.floatValue() - rhs.floatValue());
		}
		if (op == Names.times)
		{
			return new FloatValue(lhs.floatValue() * rhs.floatValue());
		}
		if (op == Names.div)
		{
			return new FloatValue(lhs.floatValue() / rhs.floatValue());
		}
		if (op == Names.percent)
		{
			return new FloatValue(lhs.floatValue() % rhs.floatValue());
		}
		return null;
	}
	
	private static IValue applyInfixDouble(IValue lhs, Name op, IValue rhs)
	{
		if (op == Names.plus)
		{
			return new DoubleValue(lhs.doubleValue() + rhs.doubleValue());
		}
		if (op == Names.minus)
		{
			return new DoubleValue(lhs.doubleValue() - rhs.doubleValue());
		}
		if (op == Names.times)
		{
			return new DoubleValue(lhs.doubleValue() * rhs.doubleValue());
		}
		if (op == Names.div)
		{
			return new DoubleValue(lhs.doubleValue() / rhs.doubleValue());
		}
		if (op == Names.percent)
		{
			return new DoubleValue(lhs.doubleValue() % rhs.doubleValue());
		}
		return null;
	}
}

package dyvil.tools.compiler.ast.operator;

import dyvil.tools.compiler.ast.access.FieldAccess;
import dyvil.tools.compiler.ast.access.FieldAssignment;
import dyvil.tools.compiler.ast.constant.IntValue;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.StringConcatExpr;
import dyvil.tools.compiler.ast.reference.ReferenceOperator;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.parsing.Name;

import static dyvil.tools.compiler.ast.operator.Operator.INFIX_LEFT;

public interface Operators
{
	Operator DEFAULT = new Operator(null, 100000, INFIX_LEFT);
	
	static IValue getPrefix(Name name, IValue arg1)
	{
		if (name == Names.bang)
		{
			if (arg1.isType(Types.BOOLEAN))
			{
				return new NotOperator(arg1);
			}
		}
		if (name == Names.times)
		{
			return new ReferenceOperator(arg1);
		}
		return getIncOperator(name, arg1, true);
	}

	static IncOperator getIncOperator(Name name, IValue arg1, boolean prefix)
	{
		if (arg1.valueTag() == IValue.FIELD_ACCESS && IncOperator.isIncConvertible(arg1.getType()))
		{
			final FieldAccess fieldAccess = (FieldAccess) arg1;
			if (name == Names.plusplus)
			{
				return new IncOperator(fieldAccess.getReceiver(), fieldAccess.getField(), 1, prefix);
			}
			else if (name == Names.minusminus)
			{
				return new IncOperator(fieldAccess.getReceiver(), fieldAccess.getField(), -1, prefix);
			}
		}
		return null;
	}

	static IValue getPostfix(IValue arg1, Name name)
	{
		return getIncOperator(name, arg1, false);
	}
	
	static IValue getInfix_Priority(IValue arg1, Name name, IValue arg2)
	{
		if (name == Names.eqeq || name == Names.eqeqeq)
		{
			if (arg2.valueTag() == IValue.NULL)
			{
				return new NullCheckOperator(arg1, true);
			}
			if (arg1.valueTag() == IValue.NULL)
			{
				return new NullCheckOperator(arg2, true);
			}
			return null;
		}
		if (name == Names.bangeq || name == Names.bangeqeq)
		{
			if (arg2.valueTag() == IValue.NULL)
			{
				return new NullCheckOperator(arg1, false);
			}
			if (arg1.valueTag() == IValue.NULL)
			{
				return new NullCheckOperator(arg2, false);
			}
		}
		return null;
	}
	
	static IValue getInfix(IValue arg1, Name name, IValue arg2)
	{
		if (name == Names.plus)
		{
			if (arg1.valueTag() == IValue.STRINGBUILDER)
			{
				StringConcatExpr sbe = (StringConcatExpr) arg1;
				sbe.addValue(arg2);
				return sbe;
			}
			if (isStringBuilderElement(arg1) || isStringBuilderElement(arg2))
			{
				StringConcatExpr sbe = new StringConcatExpr();
				sbe.addValue(arg1);
				sbe.addValue(arg2);
				return sbe;
			}
		}
		if (name == Names.pluseq)
		{
			if (arg1.valueTag() != IValue.FIELD_ACCESS || !arg1.isType(Types.STRING))
			{
				return null;
			}
			if (arg2.valueTag() == IValue.STRINGBUILDER)
			{
				StringConcatExpr sbe = (StringConcatExpr) arg2;
				sbe.addFirstValue(arg1);
			}
			else
			{
				StringConcatExpr sbe = new StringConcatExpr();
				sbe.addValue(arg1);
				sbe.addValue(arg2);
				arg2 = sbe;
			}
			
			FieldAccess fa = (FieldAccess) arg1;
			return new FieldAssignment(null, fa.getInstance(), fa.getField(), arg2);
		}
		boolean openRange = false;
		if (name == Names.dotdot || (openRange = name == Names.dotdotlt))
		{
			final RangeOperator rangeOperator = new RangeOperator(arg1, arg2);
			rangeOperator.setHalfOpen(openRange);
			return rangeOperator;
		}
		if (name == Names.ampamp)
		{
			if (arg1.isType(Types.BOOLEAN) && arg2.isType(Types.BOOLEAN))
			{
				return new AndOperator(arg1, arg2);
			}
			return null;
		}
		if (name == Names.barbar)
		{
			if (arg1.isType(Types.BOOLEAN) && arg2.isType(Types.BOOLEAN))
			{
				return new OrOperator(arg1, arg2);
			}
			return null;
		}
		return null;
	}

	static boolean isStringBuilderElement(IValue arg1)
	{
		if (!arg1.isType(Types.STRING))
		{
			return false;
		}
		switch (arg1.valueTag())
		{
		case IValue.NULL:
		case IntValue.WILDCARD:
			return false;
		}
		return true;
	}
}

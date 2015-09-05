package dyvil.tools.compiler.ast.operator;

import dyvil.tools.compiler.ast.access.FieldAccess;
import dyvil.tools.compiler.ast.access.FieldAssign;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.StringBuilderExpression;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.type.Types;

import static dyvil.tools.compiler.ast.member.Name.*;
import static dyvil.tools.compiler.ast.operator.Operator.INFIX_LEFT;

public interface Operators
{
	public static final int PREFIX = 1000;
	
	public static final Operator DEFAULT = new Operator(null, 100000, INFIX_LEFT);
	
	public static IValue getPriority(Name name, IValue arg1)
	{
		if (name == bang)
		{
			if (arg1.isType(Types.BOOLEAN))
			{
				return new NotOperator(arg1);
			}
		}
		return null;
	}
	
	public static IValue getPriority(IValue arg1, Name name, IValue arg2)
	{
		if (name == eqeq || name == eqeqeq)
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
		if (name == bangeq || name == bangeqeq)
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
	
	public static IValue get(IValue arg1, Name name, IValue arg2)
	{
		if (name == plus)
		{
			if (arg1.valueTag() == IValue.STRINGBUILDER)
			{
				StringBuilderExpression sbe = (StringBuilderExpression) arg1;
				sbe.addValue(arg2);
				return sbe;
			}
			if (arg1.isType(Types.STRING) && arg1.valueTag() != IValue.NULL || arg2.isType(Types.STRING) && arg2.valueTag() != IValue.NULL)
			{
				StringBuilderExpression sbe = new StringBuilderExpression();
				sbe.addValue(arg1);
				sbe.addValue(arg2);
				return sbe;
			}
		}
		if (name == pluseq)
		{
			if (arg1.valueTag() != IValue.FIELD_ACCESS || !arg1.isType(Types.STRING))
			{
				return null;
			}
			if (arg2.valueTag() == IValue.STRINGBUILDER)
			{
				StringBuilderExpression sbe = (StringBuilderExpression) arg2;
				sbe.addFirstValue(arg1);
			}
			else
			{
				StringBuilderExpression sbe = new StringBuilderExpression();
				sbe.addValue(arg1);
				sbe.addValue(arg2);
				arg2 = sbe;
			}
			
			FieldAccess fa = (FieldAccess) arg1;
			return new FieldAssign(null, fa.getInstance(), fa.getField(), arg2);
		}
		boolean openRange = false;
		if (name == dotdot || (openRange = name == dotdotlt))
		{
			RangeOperator rangeOperator = null;
			if (arg1.isType(RangeOperator.LazyFields.ORDERED) && arg2.isType(RangeOperator.LazyFields.ORDERED))
			{
				rangeOperator = new RangeOperator(arg1, arg2);
				rangeOperator.setHalfOpen(openRange);
			}
			return rangeOperator;
		}
		// Swap Operator
		if (name == coloneqcolon)
		{
			if (arg1.valueTag() == IValue.FIELD_ACCESS && arg2.valueTag() == IValue.FIELD_ACCESS)
			{
				return new SwapOperator((FieldAccess) arg1, (FieldAccess) arg2);
			}
			return null;
		}
		if (name == ampamp)
		{
			if (arg1.isType(Types.BOOLEAN) && arg2.isType(Types.BOOLEAN))
			{
				return new AndOperator(arg1, arg2);
			}
			return null;
		}
		if (name == barbar)
		{
			if (arg1.isType(Types.BOOLEAN) && arg2.isType(Types.BOOLEAN))
			{
				return new OrOperator(arg1, arg2);
			}
			return null;
		}
		return null;
	}
}

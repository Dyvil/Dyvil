package dyvil.tools.compiler.ast.operator;

import java.util.IdentityHashMap;
import java.util.Map;

import dyvil.tools.compiler.ast.access.FieldAccess;
import dyvil.tools.compiler.ast.expression.*;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.type.Types;

import static dyvil.tools.compiler.ast.member.Name.*;
import static dyvil.tools.compiler.ast.operator.Operator.INFIX_LEFT;
import static dyvil.tools.compiler.ast.operator.Operator.INFIX_NONE;

public interface Operators
{
	public static final int					PREFIX			= 1000;
	
	public static final Map<Name, Operator>	map				= new IdentityHashMap();
	
	public static final Operator			PREFIX_MINUS	= new Operator(Name.minus, PREFIX);
	public static final Operator			PREFIX_TILDE	= new Operator(Name.tilde, PREFIX);
	public static final Operator			PREFIX_BANG		= new Operator(Name.bang, PREFIX);
	
	public static final Operator			BOOL_OR			= new Operator(Name.barbar, 10, INFIX_LEFT);
	public static final Operator			BOOL_AND		= new Operator(Name.ampamp, 20, INFIX_LEFT);
	public static final Operator			OR				= new Operator(Name.bar, 30, INFIX_LEFT);
	public static final Operator			XOR				= new Operator(Name.up, 40, INFIX_LEFT);
	public static final Operator			AND				= new Operator(Name.amp, 50, INFIX_LEFT);
	
	public static final Operator			EQ				= new Operator(Name.eqeq, 60, INFIX_LEFT);
	public static final Operator			NE				= new Operator(Name.bangeq, 60, INFIX_LEFT);
	public static final Operator			IS				= new Operator(Name.eqeqeq, 60, INFIX_LEFT);
	public static final Operator			ISNOT			= new Operator(Name.bangeqeq, 60, INFIX_LEFT);
	public static final Operator			SWAP			= new Operator(Name.coloneqcolon, 60, INFIX_LEFT);
	
	public static final Operator			LESS			= new Operator(Name.lt, 70, INFIX_LEFT);
	public static final Operator			LESSEQ			= new Operator(Name.lteq, 70, INFIX_LEFT);
	public static final Operator			GREATER			= new Operator(Name.gt, 70, INFIX_LEFT);
	public static final Operator			GREATEREQ		= new Operator(Name.gteq, 70, INFIX_LEFT);
	
	public static final Operator			LSHIFT			= new Operator(Name.ltlt, 100, INFIX_LEFT);
	public static final Operator			RSHIFT			= new Operator(Name.gtgt, 100, INFIX_LEFT);
	public static final Operator			URSHIFT			= new Operator(Name.gtgtgt, 100, INFIX_LEFT);
	
	public static final Operator			PLUS			= new Operator(Name.plus, 90, INFIX_LEFT);
	public static final Operator			MINUS			= new Operator(Name.minus, 90, INFIX_LEFT);
	public static final Operator			TIMES			= new Operator(Name.times, 100, INFIX_LEFT);
	public static final Operator			DIV				= new Operator(Name.div, 100, INFIX_LEFT);
	public static final Operator			MOD				= new Operator(Name.percent, 100, INFIX_LEFT);
	public static final Operator			BSLASH			= new Operator(Name.bslash, 100, INFIX_LEFT);
	
	public static final Operator			RARROW			= new Operator(Name.minusgt, 200, INFIX_LEFT);
	public static final Operator			LARROW			= new Operator(Name.ltminus, 200, INFIX_LEFT);
	public static final Operator			DOTDOT			= new Operator(Name.dotdot, 200, INFIX_NONE);
	
	public static int index(Name name)
	{
		return 0;
	}
	
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
		if (name == dotdot)
		{
			if (arg1.isType(RangeOperator.ORDERED) && arg2.isType(RangeOperator.ORDERED))
			{
				return new RangeOperator(arg1, arg2);
			}
			if (arg1.isType(Types.STRING) && arg2.isType(Types.STRING))
			{
				return new RangeOperator(arg1, arg2, Types.STRING);
			}
			return null;
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

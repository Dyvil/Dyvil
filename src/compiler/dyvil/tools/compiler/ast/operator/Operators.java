package dyvil.tools.compiler.ast.operator;

import static dyvil.tools.compiler.ast.member.Name.*;
import static dyvil.tools.compiler.ast.operator.Operator.INFIX_LEFT;

import java.util.IdentityHashMap;
import java.util.Map;

import dyvil.tools.compiler.ast.access.ClassAccess;
import dyvil.tools.compiler.ast.access.FieldAccess;
import dyvil.tools.compiler.ast.expression.CaseStatement;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValueList;
import dyvil.tools.compiler.ast.expression.MatchExpression;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.type.Types;

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
	
	public static final Operator			CAST			= new Operator(Name.colongt, 150, INFIX_LEFT);
	public static final Operator			INSTANCEOF		= new Operator(Name.ltcolon, 150, INFIX_LEFT);
	public static final Operator			RARROW			= new Operator(Name.minusgt, 200, INFIX_LEFT);
	public static final Operator			LARROW			= new Operator(Name.ltminus, 200, INFIX_LEFT);
	
	public static int index(Name name)
	{
		return 0;
	}
	
	public static IValue get(Name name, IValue arg1)
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
	
	public static IValue get(IValue arg1, Name name, IValue arg2)
	{
		// Null check
		if (name == eqeq || name == eqeqeq)
		{
			if (arg2.getValueType() == IValue.NULL)
			{
				return new NullCheckOperator(arg1, true);
			}
			if (arg1.getValueType() == IValue.NULL)
			{
				return new NullCheckOperator(arg2, true);
			}
			return null;
		}
		if (name == bangeq || name == bangeqeq)
		{
			if (arg2.getValueType() == IValue.NULL)
			{
				return new NullCheckOperator(arg1, false);
			}
			if (arg1.getValueType() == IValue.NULL)
			{
				return new NullCheckOperator(arg2, false);
			}
			return null;
		}
		// Swap Operator
		if (name == coloneqcolon)
		{
			if (arg1.getValueType() == IValue.FIELD_ACCESS && arg2.getValueType() == IValue.FIELD_ACCESS)
			{
				return new SwapOperator((FieldAccess) arg1, (FieldAccess) arg2);
			}
			return null;
		}
		// Cast Operator
		if (name == colongt)
		{
			if (arg2.getValueType() == IValue.CLASS_ACCESS)
			{
				return new CastOperator(arg1, ((ClassAccess) arg2).type);
			}
			return null;
		}
		// Instanceof Operator
		if (name == ltcolon)
		{
			if (arg2.getValueType() == IValue.CLASS_ACCESS)
			{
				return new InstanceOfOperator(arg1, ((ClassAccess) arg2).type);
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
	
	public static MatchExpression getMatchExpression(IValue arg1, IValue arg2)
	{
		if (arg2.getValueType() == IValue.ARRAY)
		{
			IValueList list = (IValueList) arg2;
			int len = list.valueCount();
			CaseStatement[] cases = new CaseStatement[len];
			for (int i = 0; i < len; i++)
			{
				IValue v = list.getValue(i);
				if (v.getValueType() != IValue.CASE_STATEMENT)
				{
					// All values have to be patterns.
					return null;
				}
				
				cases[i] = (CaseStatement) v;
			}
			
			return new MatchExpression(arg1, cases);
		}
		if (arg2.getValueType() == IValue.CASE_STATEMENT)
		{
			return new MatchExpression(arg1, new CaseStatement[] { (CaseStatement) arg2 });
		}
		return null;
	}
}

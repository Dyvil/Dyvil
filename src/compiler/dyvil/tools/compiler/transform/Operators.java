package dyvil.tools.compiler.transform;

import static dyvil.tools.compiler.ast.member.Name.*;
import dyvil.tools.compiler.ast.access.ClassAccess;
import dyvil.tools.compiler.ast.access.FieldAccess;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.operator.*;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.ast.value.CaseStatement;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValueList;
import dyvil.tools.compiler.ast.value.MatchExpression;

public class Operators
{
	public static final int	PREFIX	= 1000;
	
	public static int index(Name name)
	{
		if (name == barBar)
		{
			return 10;
		}
		if (name == ampAmp)
		{
			return 20;
		}
		if (name == bar)
		{
			return 30;
		}
		if (name == up)
		{
			return 40;
		}
		if (name == amp)
		{
			return 50;
		}
		if (name == eqEq || name == bangEq || name == colonEqColon)
		{
			return 60;
		}
		if (name == less || name == greater || name == lessEq || name == greaterEq)
		{
			return 70;
		}
		if (name == lessLess || name == greaterGreater || name == greaterGreaterGreater)
		{
			return 80;
		}
		if (name == plus || name == minus)
		{
			return 90;
		}
		if (name == times || name == div || name == bslash || name == percent)
		{
			return 100;
		}
		if (name == minusGreater || name == lessMinus)
		{
			return 110;
		}
		if (name == colonGreater || name == lessColon)
		{
			return 200;
		}
		if (name.qualified.endsWith("$eq"))
		{
			return 5;
		}
		return 0;
	}
	
	public static IValue get(Name name, IValue arg1)
	{
		if (name == bang)
		{
			if (arg1.isType(Types.BOOLEAN))
			{
				return new BooleanNot(arg1);
			}
		}
		return null;
	}
	
	public static IValue get(IValue arg1, Name name, IValue arg2)
	{
		// Swap Operator
		if (name == colonEqColon)
		{
			if (arg1.getValueType() == IValue.FIELD_ACCESS && arg2.getValueType() == IValue.FIELD_ACCESS)
			{
				return new SwapOperator((FieldAccess) arg1, (FieldAccess) arg2);
			}
			return null;
		}
		// Cast Operator
		if (name == colonGreater)
		{
			if (arg2.getValueType() == IValue.CLASS_ACCESS)
			{
				return new CastOperator(arg1, ((ClassAccess) arg2).type);
			}
			return null;
		}
		// Instanceof Operator
		if (name == lessColon)
		{
			if (arg2.getValueType() == IValue.CLASS_ACCESS)
			{
				return new InstanceOfOperator(arg1, ((ClassAccess) arg2).type);
			}
			return null;
		}
		if (name == ampAmp)
		{
			if (arg1.isType(Types.BOOLEAN) && arg2.isType(Types.BOOLEAN))
			{
				return new BooleanAnd(arg1, arg2);
			}
			return null;
		}
		if (name == barBar)
		{
			if (arg1.isType(Types.BOOLEAN) && arg2.isType(Types.BOOLEAN))
			{
				return new BooleanOr(arg1, arg2);
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

package dyvil.tools.compiler.transform;

import dyvil.tools.compiler.ast.access.ClassAccess;
import dyvil.tools.compiler.ast.access.FieldAccess;
import dyvil.tools.compiler.ast.match.CaseStatement;
import dyvil.tools.compiler.ast.match.MatchExpression;
import dyvil.tools.compiler.ast.operator.*;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValueList;

public class Operators
{
	public static final int	PREFIX	= 1000;
	
	public static int index(String name)
	{
		switch (name)
		{
		case "||":
			return 10;
		case "&&":
			return 20;
		case "|":
			return 30;
		case "^":
			return 40;
		case "&":
			return 50;
		case "==":
		case "!=":
		case ":=:":
			return 60;
		case "<":
		case ">":
		case "<=":
		case ">=":
			return 70;
		case "<<":
		case ">>":
		case ">>>":
			return 80;
		case "+":
		case "-":
			return 90;
		case "*":
		case "/":
		case "%":
			return 100;
		case "->":
		case "<-":
			return 110;
		case "<:":
		case ":>":
			return 200;
		}
		if (name.charAt(name.length() - 1) == '=')
		{
			return 5;
		}
		return 0;
	}
	
	public static IValue get(String name, IValue arg1)
	{
		if ("!".equals(name))
		{
			if (arg1.isType(Type.BOOLEAN))
			{
				return new BooleanNot(arg1);
			}
		}
		return null;
	}
	
	public static IValue get(IValue arg1, String name, IValue arg2)
	{
		// Swap Operator
		if (":=:".equals(name))
		{
			if (arg1.getValueType() == IValue.FIELD_ACCESS && arg2.getValueType() == IValue.FIELD_ACCESS)
			{
				return new SwapOperator((FieldAccess) arg1, (FieldAccess) arg2);
			}
			return null;
		}
		// Cast Operator
		if (":>".equals(name))
		{
			if (arg2.getValueType() == IValue.CLASS_ACCESS)
			{
				return new CastOperator(arg1, ((ClassAccess) arg2).type);
			}
			return null;
		}
		// Instanceof Operator
		if ("<:".equals(name))
		{
			if (arg2.getValueType() == IValue.CLASS_ACCESS)
			{
				return new InstanceOfOperator(arg1, ((ClassAccess) arg2).type);
			}
			return null;
		}
		if ("&&".equals(name))
		{
			if (arg1.isType(Type.BOOLEAN) && arg2.isType(Type.BOOLEAN))
			{
				return new BooleanAnd(arg1, arg2);
			}
			return null;
		}
		if ("||".equals(name))
		{
			if (arg1.isType(Type.BOOLEAN) && arg2.isType(Type.BOOLEAN))
			{
				return new BooleanOr(arg1, arg2);
			}
			return null;
		}
		return null;
	}
	
	public static MatchExpression getMatchExpression(IValue arg1, IValue arg2)
	{
		if (arg2.getValueType() == IValue.VALUE_LIST)
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

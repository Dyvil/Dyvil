package dyvil.tools.compiler.transform;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import dyvil.tools.compiler.ast.access.MethodCall;
import dyvil.tools.compiler.ast.expression.IValue;

public class OperatorComparator implements Comparator<MethodCall>
{
	public static OperatorComparator	instance	= new OperatorComparator();
	
	// private static String[] UNARY = new String[] { "++", "--", "+", "-", "~",
	// "!" };
	
	public static int index(String name)
	{
		switch (name)
		{
		case "||":
			return 1;
		case "&&":
			return 2;
		case "|":
			return 3;
		case "^":
			return 4;
		case "&":
			return 5;
		case "==":
		case "!=":
		case ":=:":
			return 6;
		case "<:":
		case ":>":
			return 7;
		case "<":
		case ">":
		case "<=":
		case ">=":
			return 8;
		case "<<":
		case ">>":
		case ">>>":
			return 9;
		case "+":
		case "-":
			return 10;
		case "*":
		case "/":
		case "%":
			return 11;
		}
		return 0;
	}
	
	@Override
	public int compare(MethodCall o1, MethodCall o2)
	{
		int index1 = index(o1.method.getName());
		int index2 = index(o2.method.getName());
		return Integer.compare(index1, index2);
	}
	
	public static MethodCall run(MethodCall call)
	{
		LinkedList<MethodCall> list = new LinkedList();
		MethodCall c = call;
		while (true)
		{
			list.addFirst(c);
			
			IValue v = c.getValue();
			if (v instanceof MethodCall)
			{
				c = (MethodCall) v;
				continue;
			}
			break;
		}
		
		Collections.sort(list, instance);
		
		return list.getLast();
	}
}

package dyvil.tools.compiler.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import dyvil.tools.compiler.ast.api.IValue;
import dyvil.tools.compiler.ast.expression.MethodCall;

public class OperatorComparator implements Comparator<MethodCall>
{
	public static OperatorComparator	instance	= new OperatorComparator();
	
	// private static String[] UNARY = new String[] { "++", "--", "+", "-", "~",
	// "!" };
	
	private static Object[]				MAP			= { new String[] { "$times", "$div", "$percent" }, // multiplicative
			new String[] { "$plus", "$minus" }, // additive
			new String[] { "$less$less", "$greater$greater", "$greater$greater$greater" }, // shift
			new String[] { "$less", "$greater", "$less$eq", "$greater$eq" }, // relational
			new String[] { "$less$colon", "$colon$greater" }, // type check
			new String[] { "$eq$eq", "$bang$eq" }, // equality
			"$amp", // bitwise AND
			"$up", // bitwise XOR
			"$bar", // bitwise OR
			"$amp$amp", // logical AND
			"$bar$bar", // logical OR
													};
	
	private static int index(String name)
	{
		for (int i = 0; i < MAP.length; i++)
		{
			Object o = MAP[i];
			Class c = o.getClass();
			
			if (c == String.class)
			{
				if (name.equals(c))
				{
					return i;
				}
			}
			else if (c == String[].class)
			{
				for (String s : (String[]) o)
				{
					if (name.equals(s))
					{
						return i;
					}
				}
			}
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

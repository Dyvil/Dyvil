package dyvil.tools.compiler.util;

import java.util.Comparator;

import dyvil.tools.compiler.ast.expression.MethodCall;

public class OperatorComparator implements Comparator<MethodCall>
{
	public static OperatorComparator	instance	= new OperatorComparator();
	
	private static Object[]				MAP			= { new String[] { "++", "--", "+", "-", "~", "!" }, // unary
			new String[] { "*", "/", "%" }, // multiplicative
			new String[] { "+", "-" }, // additive
			new String[] { "<<", ">>", ">>>" }, // shift
			new String[] { "<", ">", "<=", ">=" }, // relational
			new String[] { "<:", ":>" }, // type check
			new String[] { "==", "!=" }, // equality
			"&", // bitwise AND
			"^", // bitwise XOR
			"|", // bitwise OR
			"&&", // logical AND
			"||", // logical OR
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
				for (String s : ((String[]) o))
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
		int index1 = index(o1.getName());
		int index2 = index(o2.getName());
		return -Integer.compare(index1, index2);
	}
	
}

package dyvil.tools.compiler.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;

public class ParserUtil
{
	public static boolean isEscapeChar(String value)
	{
		if (value.length() != 1)
		{
			return false;
		}
		return ",;)}]>".indexOf(value.charAt(0)) != -1;
	}
	
	public static boolean isSeperatorChar(String value)
	{
		if (value.length() != 1)
		{
			return false;
		}
		char c = value.charAt(0);
		return c == ',' || c == ';';
	}
	
	public static Type[] getTypes(List<IValue> values)
	{
		int len = values.size();
		Type[] types = new Type[len];
		for (int i = 0; i < len; i++)
		{
			types[i] = values.get(i).getType();
		}
		return types;
	}
	
	public static <T> void toString(Collection<T> list, Function<T, String> function, String seperator, StringBuilder buffer)
	{
		if (!list.isEmpty())
		{
			Iterator<T> iterator = list.iterator();
			while (true)
			{
				T o = iterator.next();
				buffer.append(function.apply(o));
				
				if (iterator.hasNext())
				{
					buffer.append(seperator);
				}
				else
				{
					break;
				}
			}
		}
	}
}

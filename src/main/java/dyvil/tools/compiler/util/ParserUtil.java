package dyvil.tools.compiler.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import dyvil.tools.compiler.Dyvilc;
import dyvil.tools.compiler.ast.api.IASTObject;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.config.Formatting;

public class ParserUtil
{
	public static Predicate<?>	ISNULL	= a -> a == null;
	
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
		return c == ',' || c == ';' || c == ':';
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
	
	public static void listToString(Collection<String> list, String seperator, StringBuilder buffer)
	{
		if (!list.isEmpty())
		{
			Iterator<String> iterator = list.iterator();
			while (true)
			{
				String o = iterator.next();
				buffer.append(o);
				
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
	
	public static void astToString(Collection list, String seperator, StringBuilder buffer)
	{
		if (!list.isEmpty())
		{
			Iterator iterator = list.iterator();
			while (true)
			{
				IASTObject o = (IASTObject) iterator.next();
				o.toString("", buffer);
				
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
	
	public static void parametersToString(Collection<? extends IASTObject> parameters, StringBuilder buffer, boolean writeEmpty)
	{
		parametersToString(parameters, buffer, writeEmpty, Formatting.Method.emptyParameters, Formatting.Method.parametersStart, Formatting.Method.parameterSeperator, Formatting.Method.parametersEnd);
	}
	
	public static void parametersToString(Collection<? extends IASTObject> parameters, StringBuilder buffer, boolean writeEmpty, String empty, String start, String seperator, String end)
	{
		if (parameters.isEmpty())
		{
			if (writeEmpty)
			{
				buffer.append(empty);
			}
		}
		else
		{
			buffer.append(start);
			Iterator<? extends IASTObject> iterator = parameters.iterator();
			while (true)
			{
				IASTObject value = iterator.next();
				value.toString("", buffer);
				if (iterator.hasNext())
				{
					buffer.append(seperator);
				}
				else
				{
					break;
				}
			}
			buffer.append(end);
		}
	}
	
	public static void logProfile(long now, int operations, String format)
	{
		now = System.nanoTime() - now;
		float n = now / 1000000F;
		float f = (float) n / operations;
		Dyvilc.logger.info(String.format(format, n, f, 1000F / f));
	}
}

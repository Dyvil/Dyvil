package dyvil.tools.compiler.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import dyvil.strings.CharUtils;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.statement.StatementList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValueList;

public class Util
{
	public static final List<ITyped>	EMPTY_TYPES	= Collections.EMPTY_LIST;
	
	public static final Predicate<?>	ISNULL		= a -> a == null;
	
	public static IType[] getTypes(List<IValue> values)
	{
		int len = values.size();
		IType[] types = new IType[len];
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
	
	public static void astToString(String prefix, Collection list, String seperator, StringBuilder buffer)
	{
		if (!list.isEmpty())
		{
			Iterator iterator = list.iterator();
			while (true)
			{
				IASTNode o = (IASTNode) iterator.next();
				o.toString(prefix, buffer);
				
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
	
	public static void astToString(String prefix, IASTNode[] array, int size, String seperator, StringBuilder buffer)
	{
		for (int i = 0; i < size; i++)
		{
			IASTNode o = array[i];
			o.toString(prefix, buffer);
			if (i + 1 == size)
			{
				break;
			}
			buffer.append(seperator);
		}
	}
	
	public static void typesToString(String prefix, Iterable<? extends ITyped> list, String seperator, StringBuilder buffer)
	{
		Iterator<? extends ITyped> iterator = list.iterator();
		while (true)
		{
			IType type = iterator.next().getType();
			if (type == null)
			{
				buffer.append("unknown");
			}
			else
			{
				type.toString(prefix, buffer);
			}
			
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
	
	public static String getAdder(String methodName)
	{
		StringBuilder builder = new StringBuilder("add");
		int len = methodName.length() - 1;
		builder.append(CharUtils.toUpperCase(methodName.charAt(0)));
		for (int i = 1; i < len; i++)
		{
			builder.append(methodName.charAt(i));
		}
		return builder.toString();
	}
	
	public static String getSetter(String methodName)
	{
		StringBuilder builder = new StringBuilder("set");
		int len = methodName.length();
		builder.append(CharUtils.toUpperCase(methodName.charAt(0)));
		for (int i = 1; i < len; i++)
		{
			builder.append(methodName.charAt(i));
		}
		return builder.toString();
	}
	
	public static String getGetter(String methodName)
	{
		StringBuilder builder = new StringBuilder("get");
		int len = methodName.length();
		builder.append(CharUtils.toUpperCase(methodName.charAt(0)));
		for (int i = 1; i < len; i++)
		{
			builder.append(methodName.charAt(i));
		}
		return builder.toString();
	}
	
	public static void prependValue(IMethod method, IValue value)
	{
		IValue value1 = method.getValue();
		if (value1 instanceof IValueList)
		{
			((IValueList) value1).addValue(0, value);
		}
		else if (value1 != null)
		{
			StatementList list = new StatementList(null);
			list.addValue(value1);
			list.addValue(value);
			method.setValue(list);
		}
		else
		{
			method.setValue(value);
		}
	}
	
	public static void logProfile(long now, int operations, String format)
	{
		now = System.nanoTime() - now;
		float n = now / 1000000F;
		float f = n / operations;
		DyvilCompiler.logger.info(String.format(format, n, f, 1000F / f));
	}
}

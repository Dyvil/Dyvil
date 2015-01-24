package dyvil.tools.compiler.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValueList;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.statement.StatementList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.config.Formatting;

public class Util
{
	public static Predicate<?>	ISNULL	= a -> a == null;
	
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
	
	public static void astToString(Collection list, String seperator, StringBuilder buffer)
	{
		if (!list.isEmpty())
		{
			Iterator iterator = list.iterator();
			while (true)
			{
				IASTNode o = (IASTNode) iterator.next();
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
	
	public static void typesToString(Collection<? extends ITyped> list, String seperator, StringBuilder buffer)
	{
		if (!list.isEmpty())
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
					type.toString("", buffer);
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
	}
	
	public static void parametersToString(Collection<? extends IASTNode> parameters, StringBuilder buffer, boolean writeEmpty)
	{
		parametersToString(parameters, buffer, writeEmpty, Formatting.Method.emptyParameters, Formatting.Method.parametersStart,
				Formatting.Method.parameterSeperator, Formatting.Method.parametersEnd);
	}
	
	public static void parametersToString(Collection<? extends IASTNode> parameters, StringBuilder buffer, boolean writeEmpty, String empty, String start,
			String seperator, String end)
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
			Iterator<? extends IASTNode> iterator = parameters.iterator();
			while (true)
			{
				IASTNode value = iterator.next();
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
	
	public static void prependValue(IMethod method, IValue value)
	{
		IValue value1 = method.getValue();
		if (value1 instanceof IValueList)
		{
			List<IValue> list = ((IValueList) value1).getValues();
			list.add(0, value);
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

package dyvil.tools.compiler.util;

import dyvil.string.CharUtils;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValueList;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.statement.StatementList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;

public class Util
{
	public static void fieldSignatureToString(IField field, StringBuilder buf)
	{
		field.getModifiers().toString(buf);
		field.getType().toString("", buf);
		buf.append(' ').append(field.getName());
	}
	
	public static void propertySignatureToString(IProperty property, StringBuilder buf)
	{
		fieldSignatureToString(property, buf);
	}
	
	public static void methodSignatureToString(IMethod method, StringBuilder buf)
	{
		method.getModifiers().toString(buf);
		method.getType().toString("", buf);
		buf.append(' ').append(method.getName());
		
		int typeVariables = method.typeParameterCount();
		if (typeVariables > 0)
		{
			buf.append('[');
			method.getTypeParameter(0).toString("", buf);
			for (int i = 1; i < typeVariables; i++)
			{
				buf.append(", ");
				method.getTypeParameter(i).toString("", buf);
			}
			buf.append(']');
		}
		
		buf.append('(');
		
		int params = method.parameterCount();
		if (params > 0)
		{
			method.getParameter(0).getType().toString("", buf);
			for (int i = 1; i < params; i++)
			{
				buf.append(", ");
				method.getParameter(i).getType().toString("", buf);
			}
		}
		
		buf.append(')');
	}
	
	public static void classSignatureToString(IClass iclass, StringBuilder buf)
	{
		iclass.getModifiers().toString(buf);
		buf.append(iclass.getName());
		
		int typeVariables = iclass.typeParameterCount();
		if (typeVariables > 0)
		{
			buf.append('[');
			
			iclass.getTypeParameter(0).toString("", buf);
			for (int i = 1; i < typeVariables; i++)
			{
				buf.append(", ");
				iclass.getTypeParameter(i).toString("", buf);
			}
			
			buf.append(']');
		}
		
		int params = iclass.parameterCount();
		if (params > 0)
		{
			buf.append('(');
			
			iclass.getParameter(0).getType().toString("", buf);
			for (int i = 1; i < params; i++)
			{
				buf.append(", ");
				iclass.getParameter(i).getType().toString("", buf);
			}
			
			buf.append(')');
		}
	}
	
	public static void astToString(String prefix, IASTNode[] array, int size, String separator, StringBuilder buffer)
	{
		if (size <= 0)
		{
			return;
		}
		
		array[0].toString(prefix, buffer);
		for (int i = 1; i < size; i++)
		{
			buffer.append(separator);
			array[i].toString(prefix, buffer);
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
	
	public static IValue prependValue(IValue prepend, IValue value)
	{
		if (value instanceof IValueList)
		{
			((IValueList) value).addValue(0, prepend);
			return value;
		}
		else if (value != null)
		{
			StatementList list = new StatementList(null);
			list.addValue(prepend);
			list.addValue(value);
			return list;
		}
		
		return prepend;
	}
	
	public static IValue constant(IValue value, MarkerList markers)
	{
		IValue value1 = value.toConstant(markers);
		if (value1 == null)
		{
			markers.add(MarkerMessages.createMarker(value.getPosition(), "value.constant", DyvilCompiler.maxConstantDepth));
			return value.getType().getDefaultValue();
		}
		
		return value1;
	}
	
	public static String toTime(long nanos)
	{
		if (nanos < 1000L)
		{
			return nanos + " ns";
		}
		if (nanos < 1000000L)
		{
			return nanos / 1000000D + " ms";
		}
		
		long l = 0L;
		StringBuilder builder = new StringBuilder();
		if (nanos >= 60_000_000_000L) // minutes
		{
			l = nanos / 60_000_000_000L;
			builder.append(l).append(" min ");
			nanos -= l * 60_000_000_000L;
		}
		if (nanos >= 1_000_000_000L) // seconds
		{
			l = nanos / 1_000_000_000L;
			builder.append(l).append(" s ");
			nanos -= l * 1_000_000_000L;
		}
		if (nanos >= 1_000_000L)
		{
			l = nanos / 1_000_000L;
			builder.append(l).append(" ms ");
			nanos -= l * 1_000_000L;
		}
		return builder.deleteCharAt(builder.length() - 1).toString();
	}
	
	public static void createTypeError(MarkerList markers, IValue value, IType type, ITypeContext typeContext, String key, Object... args)
	{
		Marker marker = MarkerMessages.createMarker(value.getPosition(), key, args);
		marker.addInfo(MarkerMessages.getMarker("type.expected", type.getConcreteType(typeContext)));
		marker.addInfo(MarkerMessages.getMarker("value.type", value.getType()));
		markers.add(marker);
	}
	
	public static final Name stripEq(Name name)
	{
		String qualified = name.qualified.substring(0, name.qualified.length() - 3);
		String unqualified = name.unqualified.substring(0, name.unqualified.length() - 1);
		return Name.get(qualified, unqualified);
	}

	public static String toString(IClassMember member, String type)
	{
		return MarkerMessages.getMarker("member.named", MarkerMessages.getMarker("member." + type), member.getName());
	}

	public static boolean formatStatementList(String prefix, StringBuilder buffer, IValue value)
	{
		if (value.valueTag() == IValue.STATEMENT_LIST)
		{
			if (Formatting.getBoolean("statement.open_brace.newline_before"))
			{
				buffer.append('\n').append(prefix);
			}
			else
			{
				buffer.append(' ');
			}

			value.toString(prefix, buffer);
			return true;
		}
		return false;
	}
}

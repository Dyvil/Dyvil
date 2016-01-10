package dyvil.tools.compiler.util;

import dyvil.string.CharUtils;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValueList;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.statement.StatementList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.lexer.LexerUtil;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;

public class Util
{
	public static void memberSignatureToString(IMember member, StringBuilder buf)
	{
		member.getType().toString("", buf);
		buf.append(' ').append(member.getName());
	}

	public static String methodSignatureToString(IMethod method)
	{
		final StringBuilder stringBuilder = new StringBuilder();
		methodSignatureToString(method, stringBuilder);
		return stringBuilder.toString();
	}
	
	public static void methodSignatureToString(IMethod method, StringBuilder buf)
	{
		memberSignatureToString(method, buf);
		
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
			markers.add(
					MarkerMessages.createMarker(value.getPosition(), "value.constant", DyvilCompiler.maxConstantDepth));
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

	public static boolean hasEq(Name name)
	{
		return name.unqualified.endsWith("=");
	}

	public static Name addEq(Name name)
	{
		final int unqualifiedLength = name.unqualified.length();
		final char lastChar = name.unqualified.charAt(unqualifiedLength - 1);

		if (LexerUtil.isIdentifierSymbol(lastChar))
		{
			// Last character is a symbol -> add = without _
			return Name.get(name.unqualified.concat("="), name.qualified.concat("$eq"));
		}
		// Last character is NOT a symbol -> add _=
		return Name.get(name.unqualified.concat("_="), name.qualified.concat("_$eq"));

		// We use 'concat' above to avoid implicit StringBuilders to be created
	}

	public static Name removeEq(Name name)
	{
		final String unqualified = name.unqualified;
		final String qualified = name.qualified;

		if (unqualified.endsWith("_="))
		{
			final String newQualified = qualified.substring(0, qualified.length() - 4); // 4 = "_$eq".length
			final String newUnqualified = unqualified.substring(0, unqualified.length() - 2); // 2 = "_=".length
			return Name.get(newQualified, newUnqualified);
		}
		if (unqualified.endsWith("="))
		{
			final String newQualified = qualified.substring(0, qualified.length() - 3); // 3 = "$eq".length
			final String newUnqualified = unqualified.substring(0, unqualified.length() - 1); // 1 = "=".length
			return Name.get(newQualified, newUnqualified);
		}
		return name;
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

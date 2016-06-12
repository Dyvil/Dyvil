package dyvil.tools.compiler.util;

import dyvil.string.CharUtils;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.parameter.IParameterList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.lexer.LexerUtil;

public final class Util
{
	// region Member & AST toString

	public static String memberSignatureToString(IMember member, ITypeContext typeContext)
	{
		final StringBuilder stringBuilder = new StringBuilder();
		memberSignatureToString(member, typeContext, stringBuilder);
		return stringBuilder.toString();
	}

	public static void memberSignatureToString(IMember member, ITypeContext typeContext, StringBuilder stringBuilder)
	{
		stringBuilder.append(member.getName()).append(": ");

		ITypeContext.apply(typeContext, member.getType()).toString("", stringBuilder);
	}

	public static String methodSignatureToString(IMethod method, ITypeContext typeContext)
	{
		final StringBuilder stringBuilder = new StringBuilder();
		methodSignatureToString(method, typeContext, stringBuilder);
		return stringBuilder.toString();
	}

	public static void methodSignatureToString(IMethod method, ITypeContext typeContext, StringBuilder stringBuilder)
	{
		stringBuilder.append(method.getName());

		final int typeParams = method.typeParameterCount();
		if (typeParams > 0)
		{
			stringBuilder.append(' ').append('<');
			method.getTypeParameter(0).toString("", stringBuilder);
			for (int i1 = 1; i1 < typeParams; i1++)
			{
				stringBuilder.append(", ");
				method.getTypeParameter(i1).toString("", stringBuilder);
			}
			stringBuilder.append('>');
		}

		method.getParameterList().signatureToString(stringBuilder, typeContext);

		stringBuilder.append(": ");
		ITypeContext.apply(typeContext, method.getType()).toString("", stringBuilder);
	}

	public static String constructorSignatureToString(IConstructor constructor, ITypeContext typeContext)
	{
		final StringBuilder stringBuilder = new StringBuilder();
		constructorSignatureToString(constructor, typeContext, stringBuilder);
		return stringBuilder.toString();
	}

	public static void constructorSignatureToString(IConstructor constructor, ITypeContext typeContext, StringBuilder stringBuilder)
	{
		stringBuilder.append("init");
		constructor.getParameterList().signatureToString(stringBuilder, typeContext);
	}

	public static void typeToString(IType type, ITypeContext typeContext, StringBuilder stringBuilder)
	{
		if (type == null)
		{
			stringBuilder.append("auto");
			return;
		}

		ITypeContext.apply(typeContext, type).toString("", stringBuilder);
	}

	public static String classSignatureToString(IClass iClass)
	{
		final StringBuilder stringBuilder = new StringBuilder();
		classSignatureToString(iClass, stringBuilder);
		return stringBuilder.toString();
	}

	public static void classSignatureToString(IClass iClass, StringBuilder stringBuilder)
	{
		ModifierUtil.writeClassType(iClass.getModifiers().toFlags(), stringBuilder);

		stringBuilder.append(iClass.getName());

		final int typeParams = iClass.typeParameterCount();
		if (typeParams > 0)
		{
			if (endsWithSymbol(stringBuilder))
			{
				stringBuilder.append(' ');
			}

			stringBuilder.append('<');
			iClass.getTypeParameter(0).toString("", stringBuilder);
			for (int i1 = 1; i1 < typeParams; i1++)
			{
				stringBuilder.append(", ");
				iClass.getTypeParameter(i1).toString("", stringBuilder);
			}
			stringBuilder.append('>');
		}

		final IParameterList parameterList = iClass.getParameterList();
		if (!parameterList.isEmpty())
		{
			parameterList.signatureToString(stringBuilder, null);
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

	public static boolean formatStatementList(String prefix, StringBuilder buffer, IValue value)
	{
		if (value.valueTag() != IValue.STATEMENT_LIST)
		{
			return false;
		}

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

	public static String memberNamed(IMember member)
	{
		return Markers.getSemantic("member.named", Markers.getSemantic("member." + member.getKind().getName()),
		                           member.getName());
	}

	// endregion

	// region Name transformations

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

	// endregion

	public static boolean startsWithSymbol(Name name)
	{
		return LexerUtil.isIdentifierSymbol(name.unqualified.codePointAt(0));
	}

	public static boolean endsWithSymbol(Name name)
	{
		return LexerUtil.isIdentifierSymbol(name.unqualified.codePointBefore(name.unqualified.length()));
	}

	public static boolean endsWithSymbol(StringBuilder buffer)
	{
		return LexerUtil.isIdentifierSymbol(buffer.codePointBefore(buffer.length()));
	}

	public static String toTime(long nanos)
	{
		if (nanos < 1_000_000L)
		{
			return nanos + " ns";
		}

		long l;
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
		if (nanos >= 1_000_000L) // milliseconds
		{
			l = nanos / 1_000_000L;
			builder.append(l).append(" ms ");
			// nanos -= l * 1_000_000L;
		}

		return builder.deleteCharAt(builder.length() - 1).toString();
	}
}

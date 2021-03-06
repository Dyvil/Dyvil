package dyvilx.tools.compiler.util;

import dyvil.lang.Name;
import dyvilx.tools.compiler.ast.attribute.modifiers.ModifierUtil;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.member.Member;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.parameter.ParameterList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.parsing.ASTNode;
import dyvilx.tools.parsing.lexer.CharacterTypes;

import java.io.File;

// TODO get rid of this class
public final class Util
{
	// region Member & AST toString

	public static String memberSignatureToString(Member member, ITypeContext typeContext)
	{
		final StringBuilder stringBuilder = new StringBuilder();
		memberSignatureToString(member, typeContext, stringBuilder);
		return stringBuilder.toString();
	}

	public static void memberSignatureToString(Member member, ITypeContext typeContext, StringBuilder stringBuilder)
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

		if (method.isTypeParametric())
		{
			method.getTypeParameters().toString("", stringBuilder);
		}

		method.getParameters().signatureToString(stringBuilder, typeContext);

		stringBuilder.append(" -> ");
		ITypeContext.apply(typeContext, method.getType()).toString("", stringBuilder);
	}

	public static String constructorSignatureToString(IConstructor constructor, ITypeContext typeContext)
	{
		final StringBuilder stringBuilder = new StringBuilder();
		constructorSignatureToString(constructor, typeContext, stringBuilder);
		return stringBuilder.toString();
	}

	public static void constructorSignatureToString(IConstructor constructor, ITypeContext typeContext,
		StringBuilder stringBuilder)
	{
		stringBuilder.append("init");
		constructor.getParameters().signatureToString(stringBuilder, typeContext);
	}

	public static void typeToString(IType type, ITypeContext typeContext, StringBuilder stringBuilder)
	{
		if (type == null)
		{
			stringBuilder.append(Types.UNKNOWN);
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
		ModifierUtil.writeClassType(iClass.getAttributes().flags(), stringBuilder);

		stringBuilder.append(iClass.getName());

		if (iClass.isTypeParametric())
		{
			iClass.getTypeParameters().toString("", stringBuilder);
		}

		final ParameterList parameterList = iClass.getParameters();
		if (!parameterList.isEmpty())
		{
			parameterList.signatureToString(stringBuilder, null);
		}
	}

	public static void astToString(String prefix, ASTNode[] array, int size, String separator, StringBuilder buffer)
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

	public static String memberNamed(Member member)
	{
		return Markers.getSemantic("member.named", Markers.getSemantic("member." + member.getKind().getName()),
		                           member.getName());
	}

	// endregion

	// region Name transformations

	private static String capitalizeAndPrepend(String prefix, String string)
	{
		final int length = string.length();
		final StringBuilder builder = new StringBuilder(length + prefix.length() + 1).append(prefix);
		builder.appendCodePoint(Character.toUpperCase(string.codePointAt(0)));
		builder.append(string, Character.charCount(Character.toUpperCase(string.codePointAt(0))), length);
		return builder.toString();
	}

	public static String getGetter(String methodName)
	{
		return capitalizeAndPrepend("get", methodName);
	}

	public static String getSetter(String methodName)
	{
		return capitalizeAndPrepend("set", methodName);
	}

	public static boolean hasEq(Name name)
	{
		return name.unqualified.endsWith("=");
	}

	public static Name addEq(Name name)
	{
		final int lastCodePoint = name.unqualified.codePointBefore(name.unqualified.length());
		if (CharacterTypes.isIdentifierSymbol(lastCodePoint) || lastCodePoint == '.')
		{
			// Last character is a symbol -> add = without _
			return Name.from(name.unqualified.concat("="), name.qualified.concat("$eq"));
		}
		// Last character is NOT a symbol -> add _=
		return Name.from(name.unqualified.concat("_="), name.qualified.concat("_$eq"));

		// We use 'concat' above to avoid implicit StringBuilders to be created
	}

	public static Name removeEq(Name name)
	{
		final String unqualified = name.unqualified;
		final String qualified = name.qualified;

		if (unqualified.endsWith("_="))
		{
			final String newUnqualified = unqualified.substring(0, unqualified.length() - 2); // 2 = "_=".length
			final String newQualified = qualified.substring(0, qualified.length() - 4); // 4 = "_$eq".length
			return Name.from(newUnqualified, newQualified);
		}
		if (unqualified.endsWith("="))
		{
			final String newUnqualified = unqualified.substring(0, unqualified.length() - 1); // 1 = "=".length
			final String newQualified = qualified.substring(0, qualified.length() - 3); // 3 = "$eq".length
			return Name.from(newUnqualified, newQualified);
		}
		return name;
	}

	public static Name getHeaderName(File input)
	{
		final String name = input.getAbsolutePath();
		final int start = name.lastIndexOf(File.separatorChar);
		final int end = name.lastIndexOf('.');
		return Name.fromQualified(name.substring(start + 1, end).replace('.', '_'));
	}

	// endregion

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

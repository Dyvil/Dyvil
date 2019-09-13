package dyvilx.tools.repl.command;

import dyvil.io.StringBuilderWriter;
import dyvil.reflect.Modifiers;
import dyvil.source.LineSource;
import dyvil.util.Qualifier;
import dyvilx.tools.compiler.ast.classes.ClassBody;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IField;
import dyvilx.tools.compiler.ast.field.IProperty;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.member.Member;
import dyvilx.tools.compiler.ast.method.Candidate;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ParameterList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.parser.DyvilSymbols;
import dyvilx.tools.compiler.parser.expression.ExpressionParser;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.compiler.util.MemberSorter;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.ParserManager;
import dyvilx.tools.parsing.TokenList;
import dyvilx.tools.parsing.lexer.DyvilLexer;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvilx.tools.parsing.marker.MarkerPrinter;
import dyvilx.tools.parsing.marker.MarkerStyle;
import dyvilx.tools.repl.DyvilREPL;
import dyvilx.tools.repl.context.REPLContext;
import dyvilx.tools.repl.lang.I18n;

import java.io.PrintStream;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.TreeSet;

public class CompleteCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "complete";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "c" };
	}

	@Override
	public String getUsage()
	{
		return ":c[omplete] <identifier>[.]";
	}

	@Override
	public void execute(DyvilREPL repl, String argument)
	{
		final IContext context = repl.getContext().getContext();

		if (argument == null)
		{
			// REPL Variables

			this.printREPLMembers(repl, "");
			return;
		}

		final int dotIndex = argument.lastIndexOf('.');
		if (dotIndex <= 0)
		{
			// REPL Variable Completions

			this.printREPLMembers(repl, Qualifier.qualify(argument));
			return;
		}

		final String expression = argument.substring(0, dotIndex);
		final String memberStart = Qualifier.qualify(argument.substring(dotIndex + 1));

		final MarkerList markers = new MarkerList(Markers.INSTANCE);
		final TokenList tokens = new DyvilLexer(markers, DyvilSymbols.INSTANCE).tokenize(expression);

		new ParserManager(DyvilSymbols.INSTANCE, tokens.iterator(), markers)
			.parse(new ExpressionParser((IValue value) -> {
				value.resolveTypes(markers, context);
				value = value.resolve(markers, context);
				value.checkTypes(markers, context);

				if (!markers.isEmpty())
				{
					// Print Errors, if any
					final boolean colors = repl.getCompiler().config.useAnsiColors();
					final MarkerStyle style = repl.getCompiler().config.getMarkerStyle();
					final StringBuilder buffer = new StringBuilder();

					new MarkerPrinter(new LineSource(expression), style, colors)
						.print(markers, new StringBuilderWriter(buffer));

					repl.getOutput().println(buffer);
				}

				try
				{
					this.printCompletions(repl, memberStart, value);
				}
				catch (Throwable throwable)
				{
					throwable.printStackTrace(repl.getErrorOutput());
				}
			}));
	}

	private void printREPLMembers(DyvilREPL repl, String start)
	{
		final REPLContext replContext = repl.getContext();

		final Set<IField> variables = new TreeSet<>(MemberSorter.MEMBER_COMPARATOR);
		final Set<IMethod> methods = new TreeSet<>(MemberSorter.METHOD_COMPARATOR);

		for (IField variable : replContext.getFields().values())
		{
			checkMember(variables, variable, start);
		}
		for (IMethod method : replContext.getMethods())
		{
			checkMember(methods, method, start);
		}

		final PrintStream output = repl.getOutput();

		boolean hasOutput = false;
		if (!variables.isEmpty())
		{
			hasOutput = true;
			output.println(I18n.get("command.complete.variables"));
			printMembers(output, variables, null);
		}
		if (!methods.isEmpty())
		{
			hasOutput = true;
			output.println(I18n.get("command.complete.methods"));
			printMethods(output, methods, null);
		}

		if (!hasOutput)
		{
			output.println(I18n.get("command.complete.none"));
		}
	}

	private void printCompletions(DyvilREPL repl, String memberStart, IValue value)
	{
		final IType type = value.getType();
		final boolean statics = value.isClassAccess();
		final IContext context = repl.getContext().getContext();

		final Set<IField> fields = new TreeSet<>(MemberSorter.MEMBER_COMPARATOR);
		final Set<IProperty> properties = new TreeSet<>(MemberSorter.MEMBER_COMPARATOR);
		final Set<IMethod> methods = new TreeSet<>(MemberSorter.METHOD_COMPARATOR);
		final Set<IMethod> extensionMethods = new TreeSet<>(MemberSorter.METHOD_COMPARATOR);
		final Set<IMethod> conversionMethods = new TreeSet<>(MemberSorter.METHOD_COMPARATOR);

		final PrintStream output = repl.getOutput();
		if (statics)
		{
			output.println(I18n.get("command.complete.type", type));

			findMembers(type, fields, properties, methods, memberStart, true);
			findExtensions(context, type, value, extensionMethods, memberStart);
		}
		else
		{
			output.println(I18n.get("command.complete.expression", value, type));

			findInstanceMembers(type, fields, properties, methods, memberStart,
			                    Collections.newSetFromMap(new IdentityHashMap<>()));
			findExtensions(context, type, value, extensionMethods, memberStart);
			findConversions(context, type, value, conversionMethods);
		}

		boolean hasOutput = false;
		if (!fields.isEmpty())
		{
			hasOutput = true;
			output.println(I18n.get("command.complete.fields"));
			printMembers(output, fields, type);
		}
		if (!properties.isEmpty())
		{
			hasOutput = true;
			output.println(I18n.get("command.complete.properties"));
			printMembers(output, properties, type);
		}
		if (!methods.isEmpty())
		{
			hasOutput = true;
			output.println(I18n.get("command.complete.methods"));
			printMethods(output, methods, type);
		}
		if (!extensionMethods.isEmpty())
		{
			hasOutput = true;
			output.println(I18n.get("command.complete.extensions"));
			printMethods(output, extensionMethods, type);
		}
		if (!conversionMethods.isEmpty())
		{
			hasOutput = true;
			output.println(I18n.get("command.complete.conversions"));
			printMethods(output, conversionMethods, type);
		}

		if (!hasOutput)
		{
			output.println(I18n.get("command.complete.none"));
		}
	}

	private static void printMembers(PrintStream out, Set<? extends Member> members, ITypeContext typeContext)
	{
		for (Member member : members)
		{
			out.print('\t');
			out.println(Util.memberSignatureToString(member, typeContext));
		}
	}

	private static void printMethods(PrintStream out, Set<IMethod> methods, ITypeContext typeContext)
	{
		for (IMethod method : methods)
		{
			out.print('\t');
			out.println(Util.methodSignatureToString(method, typeContext));
		}
	}

	private static void findMembers(IType type, Set<IField> fields, Set<IProperty> properties, Set<IMethod> methods,
		String start, boolean statics)
	{
		final IClass theClass = type.getTheClass();
		if (theClass == null)
		{
			return;
		}

		final ClassBody body = theClass.getBody();
		if (body == null)
		{
			return;
		}

		for (int i = 0, count = body.fieldCount(); i < count; i++)
		{
			checkMember(fields, body.getField(i), start, statics);
		}

		for (int i = 0, count = body.propertyCount(); i < count; i++)
		{
			checkMember(properties, body.getProperty(i), start, statics);
		}

		for (int i = 0, count = body.methodCount(); i < count; i++)
		{
			checkMember(methods, body.getMethod(i), start, statics);
		}
	}

	private static void findInstanceMembers(IType type, Set<IField> fields, Set<IProperty> properties,
		Set<IMethod> methods, String start, Set<IClass> dejaVu)
	{
		final IClass iclass = type.getTheClass();
		if (iclass == null || dejaVu.contains(iclass))
		{
			return;
		}
		dejaVu.add(iclass);

		// Add members
		final ParameterList parameterList = iclass.getParameters();
		for (int i = 0, count = parameterList.size(); i < count; i++)
		{
			checkMember(fields, (IField) parameterList.get(i), start, false);
		}

		findMembers(type, fields, properties, methods, start, false);

		// Recursively scan super types
		final IType superType = iclass.getSuperType();
		if (superType != null)
		{
			findInstanceMembers(superType.getConcreteType(type), fields, properties, methods, start, dejaVu);
		}

		for (IType interfaceType : iclass.getInterfaces())
		{
			findInstanceMembers(interfaceType.getConcreteType(type), fields, properties, methods, start, dejaVu);
		}
	}

	private static void findExtensions(IContext context, IType type, IValue value, Set<IMethod> methods, String start)
	{
		final MatchList<IMethod> matchList = new MatchList<>(context, true);

		type.getMethodMatches(matchList, value, null, null);
		context.getMethodMatches(matchList, value, null, null);
		Types.BASE_CONTEXT.getMethodMatches(matchList, value, null, null);

		for (Candidate<IMethod> candidate : matchList)
		{
			final IMethod member = candidate.getMember();
			if (member.hasModifier(Modifiers.INFIX) || member.hasModifier(Modifiers.EXTENSION)
			    || member.getEnclosingClass() != type.getTheClass())
			{
				checkMember(methods, member, start, true);
			}
		}
	}

	private static void findConversions(IContext context, IType type, IValue value, Set<IMethod> methods)
	{
		MatchList<IMethod> matchList = new MatchList<>(null, true);
		type.getImplicitMatches(matchList, value, null);
		context.getImplicitMatches(matchList, value, null);
		Types.BASE_CONTEXT.getImplicitMatches(matchList, value, null);

		for (Candidate<IMethod> candidate : matchList)
		{
			checkMember(methods, candidate.getMember(), "", true);
		}
	}

	private static <T extends Member> void checkMember(Set<T> set, T member, String start)
	{
		if (member.getName().startsWith(start))
		{
			set.add(member);
		}
	}

	private static <T extends Member> void checkMember(Set<T> set, T member, String start, boolean statics)
	{
		if (!member.getName().startsWith(start))
		{
			return;
		}

		final long modifiers = member.getAttributes().flags();
		if ((modifiers & Modifiers.PUBLIC) != 0 && statics == ((modifiers & Modifiers.STATIC) != 0))
		{
			set.add(member);
		}
	}
}

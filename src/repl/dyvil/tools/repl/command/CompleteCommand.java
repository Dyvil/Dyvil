package dyvil.tools.repl.command;

import dyvil.collection.Set;
import dyvil.collection.mutable.IdentityHashSet;
import dyvil.collection.mutable.TreeSet;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassBody;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.IParameterList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.MemberSorter;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.ParserManager;
import dyvil.tools.parsing.TokenIterator;
import dyvil.tools.parsing.lexer.DyvilLexer;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.name.Qualifier;
import dyvil.tools.repl.DyvilREPL;
import dyvil.tools.repl.context.REPLContext;
import dyvil.tools.repl.lang.I18n;

import java.io.PrintStream;

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
		final REPLContext context = repl.getContext();

		if (argument == null)
		{
			// REPL Variables

			this.printREPLMembers(repl, context, "");
			return;
		}

		final int dotIndex = argument.lastIndexOf('.');
		if (dotIndex <= 0)
		{
			// REPL Variable Completions

			this.printREPLMembers(repl, context, Qualifier.qualify(argument));
			return;
		}

		final String expression = argument.substring(0, dotIndex);
		final String memberStart = Qualifier.qualify(argument.substring(dotIndex + 1));

		final MarkerList markers = new MarkerList(Markers.INSTANCE);
		final TokenIterator tokenIterator = new DyvilLexer(markers, DyvilSymbols.INSTANCE).tokenize(expression);

		new ParserManager(DyvilSymbols.INSTANCE, tokenIterator, markers).parse(new ExpressionParser((IValue value) -> {
			value.resolveTypes(markers, context);
			value = value.resolve(markers, context);
			value.checkTypes(markers, context);

			if (!markers.isEmpty())
			{
				// Print Errors, if any
				final StringBuilder builder = new StringBuilder();
				final boolean colors = repl.getCompiler().config.useAnsiColors();

				markers.sort();
				for (Marker marker : markers)
				{
					marker.log(expression, builder, colors);
				}

				repl.getOutput().println(builder);
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

	private void printREPLMembers(DyvilREPL repl, REPLContext context, String start)
	{
		final Set<IField> variables = new TreeSet<>(MemberSorter.MEMBER_COMPARATOR);
		final Set<IMethod> methods = new TreeSet<>(MemberSorter.METHOD_COMPARATOR);

		for (IField variable : context.getFields().values())
		{
			checkMember(variables, variable, start);
		}
		for (IMethod method : context.getMethods())
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
		}
		else
		{
			output.println(I18n.get("command.complete.expression", value, type));

			findInstanceMembers(type, fields, properties, methods, memberStart, new IdentityHashSet<>());
			findExtensions(repl, type, value, extensionMethods, memberStart);
			findConversions(repl, type, value, conversionMethods);
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

	private static void printMembers(PrintStream out, Set<? extends IMember> members, ITypeContext typeContext)
	{
		for (IMember member : members)
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

	private static void findMembers(IType type, Set<IField> fields, Set<IProperty> properties, Set<IMethod> methods, String start, boolean statics)
	{
		final IClassBody body = type.getTheClass().getBody();
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

	private static void findInstanceMembers(IType type, Set<IField> fields, Set<IProperty> properties, Set<IMethod> methods, String start, Set<IClass> dejaVu)
	{
		final IClass iclass = type.getTheClass();
		if (dejaVu.contains(iclass))
		{
			return;
		}
		dejaVu.add(iclass);

		// Add members
		final IParameterList parameterList = iclass.getParameterList();
		for (int i = 0, count = parameterList.size(); i < count; i++)
		{
			// TODO IClassParameter interface
			checkMember(fields, (IField) parameterList.get(i), start, false);
		}

		findMembers(type, fields, properties, methods, start, false);

		// Recursively scan super types
		final IType superType = iclass.getSuperType();
		if (superType != null)
		{
			findInstanceMembers(superType.getConcreteType(type), fields, properties, methods, start, dejaVu);
		}

		for (int i = 0, count = iclass.interfaceCount(); i < count; i++)
		{
			final IType superInterface = iclass.getInterface(i);
			if (superInterface != null)
			{
				findInstanceMembers(superInterface.getConcreteType(type), fields, properties, methods, start, dejaVu);
			}
		}
	}

	private static void findExtensions(DyvilREPL repl, IType type, IValue value, Set<IMethod> methods, String start)
	{
		MatchList<IMethod> matchList = new MatchList<>(repl.getContext());
		type.getMethodMatches(matchList, value, null, null);
		repl.getContext().getMethodMatches(matchList, value, null, null);
		Types.BASE_CONTEXT.getMethodMatches(matchList, value, null, null);

		for (int i = 0, count = matchList.size(); i < count; i++)
		{
			checkMember(methods, matchList.getCandidate(i).getMember(), start, true);
		}
	}

	private static void findConversions(DyvilREPL repl, IType type, IValue value, Set<IMethod> methods)
	{
		MatchList<IMethod> matchList = new MatchList<>(null);
		type.getImplicitMatches(matchList, value, null);
		repl.getContext().getImplicitMatches(matchList, value, null);
		Types.BASE_CONTEXT.getImplicitMatches(matchList, value, null);

		for (int i = 0, count = matchList.size(); i < count; i++)
		{
			checkMember(methods, matchList.getCandidate(i).getMember(), "", true);
		}
	}

	private static <T extends IMember> void checkMember(Set<T> set, T member, String start)
	{
		if (member.getName().startWith(start))
		{
			set.add(member);
		}
	}

	private static <T extends IMember> void checkMember(Set<T> set, T member, String start, boolean statics)
	{
		if (!member.getName().startWith(start))
		{
			return;
		}

		final int modifiers = member.getModifiers().toFlags();
		if ((modifiers & Modifiers.PUBLIC) != 0 && statics == ((modifiers & Modifiers.STATIC) != 0))
		{
			set.add(member);
		}
	}
}

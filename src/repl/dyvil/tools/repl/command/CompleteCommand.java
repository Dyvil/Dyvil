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
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.IParameterList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.ParserManager;
import dyvil.tools.parsing.TokenIterator;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.DyvilLexer;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.repl.DyvilREPL;
import dyvil.tools.repl.context.REPLContext;
import dyvil.tools.repl.lang.I18n;

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

			this.printREPLMembers(repl, context, BaseSymbols.qualify(argument));
			return;
		}

		final String expression = argument.substring(0, dotIndex);
		final String memberStart = BaseSymbols.qualify(argument.substring(dotIndex + 1));

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
		final Set<String> fields = new TreeSet<>();
		final Set<String> methods = new TreeSet<>();

		for (IField variable : context.getFields().values())
		{
			if (variable.getName().startWith(start))
			{
				fields.add(Util.memberSignatureToString(variable, null));
			}
		}
		for (IMethod method : context.getMethods())
		{
			if (method.getName().startWith(start))
			{
				methods.add(Util.methodSignatureToString(method, null));
			}
		}

		boolean output = false;
		if (!fields.isEmpty())
		{
			output = true;
			repl.getOutput().println(I18n.get("command.complete.variables"));
			printAll(repl, fields);
		}
		if (!methods.isEmpty())
		{
			output = true;
			repl.getOutput().println(I18n.get("command.complete.methods"));
			printAll(repl, methods);
		}

		if (!output)
		{
			repl.getOutput().println(I18n.get("command.complete.none"));
		}
	}

	private void printCompletions(DyvilREPL repl, String memberStart, IValue value)
	{
		final IType type = value.getType();
		final boolean statics = value.valueTag() == IValue.CLASS_ACCESS;
		final Set<String> fields = new TreeSet<>();
		final Set<String> properties = new TreeSet<>();
		final Set<String> methods = new TreeSet<>();
		final Set<String> extensionMethods = new TreeSet<>();

		if (statics)
		{
			repl.getOutput().println(I18n.get("command.complete.type", type));

			findMembers(type, fields, properties, methods, memberStart, true);
		}
		else
		{
			repl.getOutput().println(I18n.get("command.complete.expression", value, type));

			findCompletions(type, fields, properties, methods, memberStart, new IdentityHashSet<>());
			findExtensions(repl, memberStart, type, value, extensionMethods);
		}

		boolean output = false;
		if (!fields.isEmpty())
		{
			output = true;
			repl.getOutput().println(I18n.get("command.complete.fields"));
			printAll(repl, fields);
		}
		if (!properties.isEmpty())
		{
			output = true;
			repl.getOutput().println(I18n.get("command.complete.properties"));
			printAll(repl, properties);
		}
		if (!methods.isEmpty())
		{
			output = true;
			repl.getOutput().println(I18n.get("command.complete.methods"));
			printAll(repl, methods);
		}
		if (!extensionMethods.isEmpty())
		{
			output = true;
			repl.getOutput().println(I18n.get("command.complete.extensions"));
			printAll(repl, extensionMethods);
		}

		if (!output)
		{
			repl.getOutput().println(I18n.get("command.complete.none"));
		}
	}

	private static void printAll(DyvilREPL repl, Set<String> members)
	{
		for (String field : members)
		{
			repl.getOutput().print('\t');
			repl.getOutput().println(field);
		}
	}

	private static void findCompletions(IType type, Set<String> fields, Set<String> properties, Set<String> methods, String start, Set<IClass> dejaVu)
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
			final IParameter parameter = parameterList.get(i);
			if (matches(start, parameter, false))
			{
				fields.add(Util.memberSignatureToString(parameter, type));
			}
		}

		findMembers(type, fields, properties, methods, start, false);

		// Recursively scan super types
		final IType superType = iclass.getSuperType();
		if (superType != null)
		{
			findCompletions(superType.getConcreteType(type), fields, properties, methods, start, dejaVu);
		}

		for (int i = 0, count = iclass.interfaceCount(); i < count; i++)
		{
			final IType superInterface = iclass.getInterface(i);
			if (superInterface != null)
			{
				findCompletions(superInterface.getConcreteType(type), fields, properties, methods, start, dejaVu);
			}
		}
	}

	private static void findMembers(IType type, Set<String> fields, Set<String> properties, Set<String> methods, String start, boolean statics)
	{
		final IClassBody body = type.getTheClass().getBody();
		if (body == null)
		{
			return;
		}

		for (int i = 0, count = body.fieldCount(); i < count; i++)
		{
			final IField field = body.getField(i);
			if (matches(start, field, statics))
			{
				fields.add(Util.memberSignatureToString(field, type));
			}
		}

		for (int i = 0, count = body.propertyCount(); i < count; i++)
		{
			final IProperty property = body.getProperty(i);
			if (matches(start, property, statics))
			{
				properties.add(Util.memberSignatureToString(property, type));
			}
		}

		for (int i = 0, count = body.methodCount(); i < count; i++)
		{
			final IMethod method = body.getMethod(i);
			if (matches(start, method, statics))
			{
				methods.add(Util.methodSignatureToString(method, type));
			}
		}
	}

	private static void findExtensions(DyvilREPL repl, String memberStart, IType type, IValue value, Set<String> methods)
	{
		MethodMatchList matchList = new MethodMatchList();
		type.getMethodMatches(matchList, value, null, null);
		repl.getContext().getMethodMatches(matchList, value, null, null);
		Types.LANG_HEADER.getMethodMatches(matchList, value, null, null);

		for (int i = 0, count = matchList.size(); i < count; i++)
		{
			final IMethod method = matchList.getMethod(i);
			if (matches(memberStart, method, true))
			{
				methods.add(Util.methodSignatureToString(method, null));
			}
		}
	}

	private static boolean matches(String start, IMember member, boolean statics)
	{
		if (!member.getName().startWith(start))
		{
			return false;
		}

		int modifiers = member.getModifiers().toFlags();
		return (modifiers & Modifiers.PUBLIC) != 0 && statics == ((modifiers & Modifiers.STATIC) != 0);
	}
}

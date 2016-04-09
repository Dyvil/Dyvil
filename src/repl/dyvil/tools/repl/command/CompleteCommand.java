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
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.ParserManager;
import dyvil.tools.parsing.TokenIterator;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.DyvilLexer;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.repl.DyvilREPL;
import dyvil.tools.repl.context.REPLContext;

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
	public String getDescription()
	{
		return "Prints a list of possible completions";
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

			this.printCompletions(repl, memberStart, value);

			if (markers.isEmpty())
			{
				return;
			}

			// Print Errors, if any
			final StringBuilder builder = new StringBuilder();
			final boolean colors = repl.getCompiler().config.useAnsiColors();
			markers.sort();
			for (Marker marker : markers)
			{
				marker.log(expression, builder, colors);
			}

			repl.getOutput().print(builder);
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
				fields.add(getSignature(variable, Types.UNKNOWN));
			}
		}
		for (IMethod method : context.getMethods())
		{
			if (method.getName().startWith(start))
			{
				methods.add(getSignature(method, Types.UNKNOWN));
			}
		}

		boolean output = false;
		if (!fields.isEmpty())
		{
			output = true;
			repl.getOutput().println("Fields:");
			printAll(repl, fields);
		}
		if (!methods.isEmpty())
		{
			output = true;
			repl.getOutput().println("Methods:");
			printAll(repl, methods);
		}

		if (!output)
		{
			repl.getOutput().println("No completions available");
		}
	}

	private void printCompletions(DyvilREPL repl, String memberStart, IValue value)
	{
		final IType type = value.getType();
		repl.getOutput().println("Available completions for '" + value + "' of type '" + type + "':");

		final boolean statics = value.valueTag() == IValue.CLASS_ACCESS;

		final Set<String> fields = new TreeSet<>();
		final Set<String> properties = new TreeSet<>();
		final Set<String> methods = new TreeSet<>();
		final Set<String> extensionMethods = new TreeSet<>();

		findCompletions(type, fields, properties, methods, memberStart, statics, new IdentityHashSet<>());
		findExtensions(repl, memberStart, type, value, extensionMethods);

		boolean output = false;
		if (!fields.isEmpty())
		{
			output = true;
			repl.getOutput().println("Fields:");
			printAll(repl, fields);
		}
		if (!properties.isEmpty())
		{
			output = true;
			repl.getOutput().println("Properties:");
			printAll(repl, properties);
		}
		if (!methods.isEmpty())
		{
			output = true;
			repl.getOutput().println("Methods:");
			printAll(repl, methods);
		}
		if (!extensionMethods.isEmpty())
		{
			output = true;
			repl.getOutput().println("Extension Methods:");
			printAll(repl, extensionMethods);
		}

		if (!output)
		{
			if (statics)
			{
				repl.getOutput().println("No static completions available for type " + type);
			}
			else
			{
				repl.getOutput().println("No completions available for type " + type);
			}
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

	private static void findExtensions(DyvilREPL repl, String memberStart, IType type,  IValue value, Set<String> methods)
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
				methods.add(getSignature(method, null));
			}
		}
	}

	private static void findCompletions(IType type, Set<String> fields, Set<String> properties, Set<String> methods, String start, boolean statics, Set<IClass> dejaVu)
	{
		IClass iclass = type.getTheClass();
		if (dejaVu.contains(iclass))
		{
			return;
		}
		dejaVu.add(iclass);

		// Add members
		for (int i = 0, count = iclass.parameterCount(); i < count; i++)
		{
			final IParameter parameter = iclass.getParameter(i);
			if (matches(start, parameter, statics))
			{
				fields.add(getSignature(parameter, type));
			}
		}

		final IClassBody body = iclass.getBody();
		if (body != null)
		{
			for (int i = 0, count = body.fieldCount(); i < count; i++)
			{
				final IField field = body.getField(i);
				if (matches(start, field, statics))
				{
					fields.add(getSignature(field, type));
				}
			}

			for (int i = 0, count = body.propertyCount(); i < count; i++)
			{
				final IProperty property = body.getProperty(i);
				if (matches(start, property, statics))
				{
					properties.add(getSignature(property, type));
				}
			}

			for (int i = 0, count = body.methodCount(); i < count; i++)
			{
				final IMethod method = body.getMethod(i);
				if (matches(start, method, statics))
				{
					methods.add(getSignature(method, type));
				}
			}
		}

		if (statics)
		{
			return;
		}

		// Recursively scan super types
		final IType superType = iclass.getSuperType();
		if (superType != null)
		{
			findCompletions(superType.getConcreteType(type), fields, properties, methods, start, false, dejaVu);
		}

		for (int i = 0, count = iclass.interfaceCount(); i < count; i++)
		{
			final IType superInterface = iclass.getInterface(i);
			if (superInterface != null)
			{
				findCompletions(superInterface.getConcreteType(type), fields, properties, methods, start, false,
				                dejaVu);
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

	private static String getSignature(IMember member, ITypeContext typeContext)
	{
		final StringBuilder builder = new StringBuilder().append(member.getName()).append(" : ");
		member.getType().getConcreteType(typeContext).toString("", builder);
		return builder.toString();
	}

	private static String getSignature(IMethod method, ITypeContext typeContext)
	{
		final StringBuilder builder = new StringBuilder().append(method.getName()).append('(');

		int paramCount = method.parameterCount();
		if (paramCount > 0)
		{
			method.getParameter(0).getType().getConcreteType(typeContext).toString("", builder);
			for (int i = 1; i < paramCount; i++)
			{
				builder.append(", ");
				method.getParameter(i).getType().getConcreteType(typeContext).toString("", builder);
			}
		}

		builder.append(") : ");
		method.getType().getConcreteType(typeContext).toString("", builder);
		return builder.toString();
	}
}

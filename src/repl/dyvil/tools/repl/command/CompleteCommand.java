package dyvil.tools.repl.command;

import dyvil.collection.Set;
import dyvil.collection.mutable.IdentityHashSet;
import dyvil.collection.mutable.TreeSet;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassBody;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.parsing.ParserManager;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.TokenIterator;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.DyvilLexer;
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

		final IValueConsumer valueConsumer = value -> {
			value.resolveTypes(markers, context);
			value = value.resolve(markers, context);
			value.checkTypes(markers, context);

			final IType type = value.getType();
			repl.getOutput().println("Available completions for '" + value + "' of type '" + type + "':");

			this.printCompletions(repl, memberStart, type, value.valueTag() == IValue.CLASS_ACCESS);
		};
		new ParserManager(DyvilSymbols.INSTANCE, tokenIterator, markers).parse(new ExpressionParser(valueConsumer));
	}
	
	private void printREPLMembers(DyvilREPL repl, REPLContext context, String start)
	{
		final Set<String> fields = new TreeSet<>();
		final Set<String> methods = new TreeSet<>();

		for (IField variable : context.getFields().values())
		{
			if (variable.getName().startWith(start))
			{
				fields.add(getSignature(Types.UNKNOWN, variable));
			}
		}
		for (IMethod method : context.getMethods())
		{
			if (method.getName().startWith(start))
			{
				methods.add(getSignature(Types.UNKNOWN, method));
			}
		}

		boolean output = false;
		if (!fields.isEmpty())
		{
			output = true;
			repl.getOutput().println("Fields:");
			for (String field : fields)
			{
				repl.getOutput().print('\t');
				repl.getOutput().println(field);
			}
		}
		if (!methods.isEmpty())
		{
			output = true;
			repl.getOutput().println("Methods:");
			for (String method : methods)
			{
				repl.getOutput().print('\t');
				repl.getOutput().println(method);
			}
		}

		if (!output)
		{
			repl.getOutput().println("No completions available");
		}
	}

	private void printCompletions(DyvilREPL repl, String memberStart, IType type, boolean statics)
	{
		final Set<String> fields = new TreeSet<>();
		final Set<String> properties = new TreeSet<>();
		final Set<String> methods = new TreeSet<>();

		this.findCompletions(type, fields, properties, methods, memberStart, statics, new IdentityHashSet<>());

		boolean output = false;
		if (!fields.isEmpty())
		{
			output = true;
			repl.getOutput().println("Fields:");
			for (String field : fields)
			{
				repl.getOutput().print('\t');
				repl.getOutput().println(field);
			}
		}
		if (!properties.isEmpty())
		{
			output = true;
			repl.getOutput().println("Properties:");
			for (String property : properties)
			{
				repl.getOutput().print('\t');
				repl.getOutput().println(property);
			}
		}
		if (!methods.isEmpty())
		{
			output = true;
			repl.getOutput().println("Methods:");
			for (String method : methods)
			{
				repl.getOutput().print('\t');
				repl.getOutput().println(method);
			}
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

	private void findCompletions(IType type, Set<String> fields, Set<String> properties, Set<String> methods, String start, boolean statics, Set<IClass> dejaVu)
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
				fields.add(getSignature(type, parameter));
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
					fields.add(getSignature(type, field));
				}
			}

			for (int i = 0, count = body.propertyCount(); i < count; i++)
			{
				final IProperty property = body.getProperty(i);
				if (matches(start, property, statics))
				{
					properties.add(getSignature(type, property));
				}
			}

			for (int i = 0, count = body.methodCount(); i < count; i++)
			{
				final IMethod method = body.getMethod(i);
				if (matches(start, method, statics))
				{
					methods.add(getSignature(type, method));
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
			this.findCompletions(superType.getConcreteType(type), fields, properties, methods, start, false, dejaVu);
		}

		for (int i = 0, count = iclass.interfaceCount(); i < count; i++)
		{
			final IType superInterface = iclass.getInterface(i);
			if (superInterface != null)
			{
				this.findCompletions(superInterface.getConcreteType(type), fields, properties, methods, start, false,
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
	
	private static String getSignature(IType type, IMember member)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(member.getName());
		sb.append(" : ");
		member.getType().getConcreteType(type).toString("", sb);
		return sb.toString();
	}
	
	private static String getSignature(IType type, IMethod method)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(method.getName());
		sb.append('(');
		
		int paramCount = method.parameterCount();
		if (paramCount > 0)
		{
			method.getParameter(0).getType().getConcreteType(type).toString("", sb);
			for (int i = 1; i < paramCount; i++)
			{
				sb.append(", ");
				method.getParameter(i).getType().getConcreteType(type).toString("", sb);
			}
		}
		
		sb.append(") : ");
		method.getType().getConcreteType(type).toString("", sb);
		return sb.toString();
	}
}

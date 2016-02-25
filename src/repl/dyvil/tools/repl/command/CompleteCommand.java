package dyvil.tools.repl.command;

import dyvil.collection.Set;
import dyvil.collection.mutable.IdentityHashSet;
import dyvil.collection.mutable.TreeSet;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassBody;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.repl.DyvilREPL;
import dyvil.tools.repl.REPLContext;

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
	public void execute(DyvilREPL repl, String... args)
	{
		REPLContext context = repl.getContext();
		
		if (args.length == 0)
		{
			// REPL Variables
			
			this.printMembers(repl, context, "");
			return;
		}
		
		String argument = args[0];
		int index = args[0].indexOf('.');
		if (index <= 0)
		{
			// REPL Variable Completions
			
			this.printMembers(repl, context, BaseSymbols.qualify(argument));
			return;
		}
		
		Name varName = Name.get(argument.substring(0, index));
		String memberStart = BaseSymbols.qualify(argument.substring(index + 1));
		IDataMember variable = context.resolveField(varName);
		
		if (variable != null)
		{
			// Field Completions
			
			IType type = variable.getType();
			repl.getOutput().println("Available completions for '" + varName + "' of type '" + type + "':");
			
			this.printCompletions(repl, memberStart, type, false);
			return;
		}
		
		IType type = IContext.resolveType(context, varName);
		if (type != null)
		{
			// Type Completions
			repl.getOutput().println("Available completions for type '" + type + "':");
			this.printCompletions(repl, memberStart, type, true);
			return;
		}
		
		// No Completions available
		repl.getOutput().println("'" + varName + "' could not be resolved");
		return;
	}
	
	private void printCompletions(DyvilREPL repl, String memberStart, IType type, boolean statics)
	{
		Set<String> fields = new TreeSet<>();
		Set<String> properties = new TreeSet<>();
		Set<String> methods = new TreeSet<>();
		
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
	
	private void printMembers(DyvilREPL repl, REPLContext context, String start)
	{
		Set<String> fields = new TreeSet<>();
		Set<String> methods = new TreeSet<>();
		
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

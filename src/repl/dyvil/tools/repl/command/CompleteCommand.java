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
	public String getDescription()
	{
		return "Prints a list of possible completions";
	}
	
	@Override
	public void execute(DyvilREPL repl, String... args)
	{
		REPLContext context = repl.getContext();
		
		if (args.length == 0)
		{
			// REPL Variables
			
			this.printMembers(context, "");
			return;
		}
		
		String argument = args[0];
		int index = args[0].indexOf('.');
		if (index <= 0)
		{
			// REPL Variable Completions
			
			this.printMembers(context, BaseSymbols.qualify(argument));
			return;
		}
		
		Name varName = Name.get(argument.substring(0, index));
		String memberStart = BaseSymbols.qualify(argument.substring(index + 1));
		IDataMember variable = context.resolveField(varName);
		
		if (variable != null)
		{
			// Field Completions
			
			IType type = variable.getType();
			System.out.println("Available completions for '" + varName + "' of type '" + type + "':");
			
			this.printCompletions(memberStart, type);
			return;
		}
		
		IType type = IContext.resolveType(context, varName);
		if (type != null)
		{
			// Type Completions
			System.out.println("Available completions for type '" + type + "':");
			this.printCompletions(memberStart, type);
			return;
		}
		
		// No Completions available
		System.out.println("'" + varName + "' could not be resolved");
		return;
	}
	
	private void printCompletions(String memberStart, IType type)
	{
		Set<String> fields = new TreeSet();
		Set<String> properties = new TreeSet();
		Set<String> methods = new TreeSet();
		
		this.findCompletions(type, fields, properties, methods, memberStart, true, new IdentityHashSet());
		
		if (!fields.isEmpty())
		{
			System.out.println("Fields:");
			for (String field : fields)
			{
				System.out.print('\t');
				System.out.println(field);
			}
		}
		if (!properties.isEmpty())
		{
			System.out.println("Properties:");
			for (String property : properties)
			{
				System.out.print('\t');
				System.out.println(property);
			}
		}
		if (!methods.isEmpty())
		{
			System.out.println("Methods:");
			for (String method : methods)
			{
				System.out.print('\t');
				System.out.println(method);
			}
		}
	}
	
	private void printMembers(REPLContext context, String start)
	{
		Set<String> fields = new TreeSet();
		Set<String> methods = new TreeSet();
		
		for (IField variable : context.fields.values())
		{
			if (variable.getName().startWith(start))
			{
				fields.add(getSignature(Types.UNKNOWN, variable));
			}
		}
		for (IMethod method : context.methods)
		{
			if (method.getName().startWith(start))
			{
				methods.add(getSignature(Types.UNKNOWN, method));
			}
		}
		
		if (!fields.isEmpty())
		{
			System.out.println("Fields:");
			for (String field : fields)
			{
				System.out.print('\t');
				System.out.println(field);
			}
		}
		if (!methods.isEmpty())
		{
			System.out.println("Methods:");
			for (String method : methods)
			{
				System.out.print('\t');
				System.out.println(method);
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
		
		IClassBody body = iclass.getBody();
		if (body != null)
		{
			int count = body.fieldCount();
			for (int i = 0; i < count; i++)
			{
				IField field = body.getField(i);
				if (matches(start, field, statics))
				{
					properties.add(getSignature(type, field));
				}
			}
			
			count = body.propertyCount();
			for (int i = 0; i < count; i++)
			{
				IProperty property = body.getProperty(i);
				if (matches(start, property, statics))
				{
					properties.add(getSignature(type, property));
				}
			}
			
			count = body.methodCount();
			for (int i = 0; i < count; i++)
			{
				IMethod method = body.getMethod(i);
				if (matches(start, method, statics))
				{
					methods.add(getSignature(type, method));
				}
			}
		}
		
		IType superType = iclass.getSuperType();
		if (superType != null)
		{
			this.findCompletions(superType.getConcreteType(type), fields, properties, methods, start, false, dejaVu);
		}
		
		int itfCount = iclass.interfaceCount();
		for (int i = 0; i < itfCount; i++)
		{
			IType superInterface = iclass.getInterface(i);
			if (superInterface != null)
			{
				this.findCompletions(superInterface.getConcreteType(type), fields, properties, methods, start, false, dejaVu);
			}
		}
	}
	
	private boolean matches(String start, IMember member, boolean statics)
	{
		if (!member.getName().startWith(start))
		{
			return false;
		}
		
		int modifiers = member.getModifiers();
		if ((modifiers & Modifiers.PUBLIC) == 0)
		{
			return false;
		}
		if (!statics && (modifiers & Modifiers.STATIC) != 0)
		{
			return false;
		}
		
		return true;
	}
	
	private static String getSignature(IType type, IField field)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(field.getName());
		sb.append(" : ");
		field.getType().getConcreteType(type).toString("", sb);
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

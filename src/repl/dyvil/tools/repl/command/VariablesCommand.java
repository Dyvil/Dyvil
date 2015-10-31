package dyvil.tools.repl.command;

import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.repl.DyvilREPL;
import dyvil.tools.repl.REPLContext;

public class VariablesCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "variables";
	}
	
	@Override
	public String getDescription()
	{
		return "Prints all available variables";
	}
	
	@Override
	public void execute(DyvilREPL repl, String... args)
	{
		REPLContext context = repl.getContext();
		for (IField v : context.fields.values())
		{
			System.out.println(v);
		}
	}
}

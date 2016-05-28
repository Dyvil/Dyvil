package dyvil.tools.repl.command;

import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.repl.DyvilREPL;
import dyvil.tools.repl.context.REPLContext;

public class VariablesCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "variables";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "v", "vars" };
	}

	@Override
	public String getUsage()
	{
		return ":variables";
	}

	@Override
	public void execute(DyvilREPL repl, String args)
	{
		final REPLContext context = repl.getContext();

		for (IField field : context.getFields().values())
		{
			repl.getOutput().println(field);
		}
	}
}

package dyvil.tools.repl.command;

import dyvil.collection.Map;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.parsing.Name;
import dyvil.tools.repl.DyvilREPL;

public class RenameCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "rename";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "r" };
	}

	@Override
	public String getDescription()
	{
		return "Renames a field or method defined in the current context";
	}

	@Override
	public String getUsage()
	{
		return ":r[ename] <oldname> <newname>";
	}

	@Override
	public void execute(DyvilREPL repl, String argument)
	{
		if (argument == null)
		{
			repl.getErrorOutput().println("Missing Arguments. Usage: " + this.getUsage());
			return;
		}

		final String[] split = argument.split(" ", 2);
		if (split.length < 2)
		{
			repl.getErrorOutput().println("Invalid Number of Arguments. Usage: " + this.getUsage());
			return;
		}

		final Name memberName = Name.get(split[0]);
		final Name newName = Name.get(split[1]);
		final Map<Name, IField> fields = repl.getContext().getFields();
		final IField field = fields.remap(memberName, newName);

		if (field != null)
		{
			field.setName(newName);
			repl.getOutput().println("Renamed field '" + memberName + "' to '" + newName + "'");
		}
		else
		{
			repl.getOutput().println("No field named '" + memberName + "' was found");
		}
	}
}

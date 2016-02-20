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
	public String getDescription()
	{
		return "Renames a field or method defined in the current context";
	}

	@Override
	public void execute(DyvilREPL repl, String... args)
	{
		if (args.length < 2)
		{
			repl.getErrorOutput().println("Invalid number of arguments - member name and new name expected");
			return;
		}

		final Name memberName = Name.get(args[0]);
		final Name newName = Name.get(args[1]);
		final Map<Name, IField> fields = repl.getContext().getFields();
		final IField field = fields.remap(memberName, newName);

		if (field != null)
		{
			field.setName(newName);
			repl.getOutput().println("Renamed field '" + memberName + "' to '" + newName + "'");
		}
		else
		{
			repl.getOutput().println("No field named '" + memberName + "' were found");
		}
	}
}

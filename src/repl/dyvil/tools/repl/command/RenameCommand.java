package dyvil.tools.repl.command;

import dyvil.collection.Map;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.parsing.Name;
import dyvil.tools.repl.DyvilREPL;
import dyvil.tools.repl.lang.I18n;

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
	public String getUsage()
	{
		return ":r[ename] <oldname> <newname>";
	}

	@Override
	public void execute(DyvilREPL repl, String argument)
	{
		final String[] split;
		if (argument == null || (split = argument.split(" ", 2)).length < 2)
		{
			repl.getErrorOutput().println(I18n.get("command.argument_list.invalid", this.getUsage()));
			return;
		}

		final Name memberName = Name.from(split[0]);
		final Name newName = Name.from(split[1]);
		final Map<Name, IField> fields = repl.getContext().getFields();
		final IField field = fields.remap(memberName, newName);

		if (field != null)
		{
			field.setName(newName);
			repl.getOutput().println(I18n.get("command.rename.success", memberName, newName));
		}
		else
		{
			repl.getOutput().println(I18n.get("command.rename.not_found", memberName));
		}
	}
}

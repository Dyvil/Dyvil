package dyvil.tools.repl.command;

import dyvil.collection.Map;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.util.MemberSorter;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.repl.DyvilREPL;
import dyvil.tools.repl.lang.I18n;

import java.util.Arrays;

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
		final Map<Name, IField> fieldMap = repl.getContext().getFields();

		if (fieldMap.isEmpty())
		{
			repl.getOutput().println(I18n.get("command.variables.none"));
			return;
		}

		final IField[] fields = fieldMap.toValueArray(IField.class);
		Arrays.sort(fields, MemberSorter.MEMBER_COMPARATOR);

		for (IField field : fields)
		{
			repl.getOutput().println(Util.memberSignatureToString(field, null));
		}
	}
}

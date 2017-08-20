package dyvilx.tools.repl.command;

import dyvil.collection.Map;
import dyvilx.tools.compiler.ast.field.IField;
import dyvilx.tools.compiler.util.MemberSorter;
import dyvilx.tools.compiler.util.Util;
import dyvil.lang.Name;
import dyvilx.tools.repl.DyvilREPL;
import dyvilx.tools.repl.lang.I18n;

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

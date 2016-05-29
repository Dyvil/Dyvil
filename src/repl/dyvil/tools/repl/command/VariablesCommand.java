package dyvil.tools.repl.command;

import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.util.MemberSorter;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.repl.DyvilREPL;
import dyvil.tools.repl.context.REPLContext;

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
		final REPLContext context = repl.getContext();

		final IField[] fields = context.getFields().toValueArray(IField.class);
		Arrays.sort(fields, MemberSorter.MEMBER_COMPARATOR);

		for (IField field : fields)
		{
			repl.getOutput().println(Util.memberSignatureToString(field, null));
		}
	}
}

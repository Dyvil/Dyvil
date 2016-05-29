package dyvil.tools.repl.command;

import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.util.MemberSorter;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.repl.DyvilREPL;
import dyvil.tools.repl.context.REPLContext;

import java.util.Arrays;

public class MethodsCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "methods";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "m" };
	}

	@Override
	public String getUsage()
	{
		return ":methods";
	}

	@Override
	public void execute(DyvilREPL repl, String args)
	{
		final REPLContext context = repl.getContext();

		final IMethod[] methods = context.getMethods().toArray(IMethod.class);
		Arrays.sort(methods, MemberSorter.METHOD_COMPARATOR);

		for (IMethod method : methods)
		{
			repl.getOutput().println(Util.methodSignatureToString(method, null));
		}
	}
}

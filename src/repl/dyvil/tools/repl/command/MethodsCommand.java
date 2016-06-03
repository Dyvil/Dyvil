package dyvil.tools.repl.command;

import dyvil.collection.List;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.util.MemberSorter;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.repl.DyvilREPL;
import dyvil.tools.repl.lang.I18n;

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
		final List<IMethod> methodList = repl.getContext().getMethods();

		if (methodList.isEmpty())
		{
			repl.getOutput().println(I18n.get("command.methods.none"));
			return;
		}

		final IMethod[] methods = methodList.toArray(IMethod.class);
		Arrays.sort(methods, MemberSorter.METHOD_COMPARATOR);

		for (IMethod method : methods)
		{
			repl.getOutput().println(Util.methodSignatureToString(method, null));
		}
	}
}

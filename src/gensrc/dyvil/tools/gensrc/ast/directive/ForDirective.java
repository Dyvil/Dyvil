package dyvil.tools.gensrc.ast.directive;

import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.scope.LazyScope;
import dyvil.tools.gensrc.ast.scope.Scope;
import dyvil.tools.gensrc.ast.Util;

import java.io.PrintStream;

public class ForDirective implements Directive
{
	private final String varName;
	private final String start;
	private final String end;

	private Directive action;

	public ForDirective(String varName, String start, String end)
	{
		this.varName = varName;
		this.start = start;
		this.end = end;
	}

	public Directive getAction()
	{
		return this.action;
	}

	public void setAction(Directive action)
	{
		this.action = action;
	}

	@Override
	public void specialize(GenSrc gensrc, Scope scope, PrintStream output)
	{
		final int start;
		final int end;

		try
		{
			start = Integer.parseInt(Util.processLine(this.start, scope));
			end = Integer.parseInt(Util.processLine(this.end, scope));
		}
		catch (NumberFormatException ignored)
		{
			// TODO Invalid For Directive Error
			return;
		}

		final LazyScope forScope = new LazyScope(scope);

		for (int i = start; i <= end; i++)
		{
			forScope.define(this.varName, Integer.toString(i));
			this.action.specialize(gensrc, forScope, output);
		}
	}
}

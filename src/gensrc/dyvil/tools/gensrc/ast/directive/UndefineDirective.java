package dyvil.tools.gensrc.ast.directive;

import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.scope.LazyScope;
import dyvil.tools.gensrc.ast.scope.Scope;

import java.io.PrintStream;

public class UndefineDirective implements Directive
{
	private final boolean local;

	private final String varName;

	public UndefineDirective(boolean local, String varName)
	{
		this.local = local;
		this.varName = varName;
	}

	@Override
	public void specialize(GenSrc gensrc, Scope scope, PrintStream output)
	{
		if (!this.local)
		{
			scope = scope.getGlobalParent();
		}

		if (scope instanceof LazyScope)
		{
			((LazyScope) scope).undefine(this.varName);
		}
	}

	@Override
	public String toString()
	{
		return Directive.toString(this);
	}

	@Override
	public void toString(String indent, StringBuilder builder)
	{
		builder.append(indent).append(this.local ? "#delete " : "#undefine ").append(this.varName).append('\n');
	}
}

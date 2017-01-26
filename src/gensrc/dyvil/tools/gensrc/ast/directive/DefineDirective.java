package dyvil.tools.gensrc.ast.directive;

import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.Util;
import dyvil.tools.gensrc.ast.scope.LazyScope;
import dyvil.tools.gensrc.ast.scope.Scope;

import java.io.PrintStream;

public class DefineDirective implements Directive
{
	private boolean local;

	private final String key;
	private final String value;

	public DefineDirective(boolean local, String key, String value)
	{
		this.local = local;
		this.key = key;
		this.value = value;
	}

	@Override
	public void specialize(GenSrc gensrc, Scope scope, PrintStream output)
	{
		final String processed = Util.processLine(this.value, scope);

		if (!this.local)
		{
			scope = scope.getGlobalParent();
		}

		if (scope instanceof LazyScope)
		{
			((LazyScope) scope).define(this.key, processed);
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
		builder.append(indent).append(this.local ? "#local " : "#define ").append(this.key).append(' ')
		       .append(this.value).append('\n');
	}
}

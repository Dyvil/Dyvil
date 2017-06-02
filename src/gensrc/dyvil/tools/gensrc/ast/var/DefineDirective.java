package dyvil.tools.gensrc.ast.var;

import dyvil.lang.Formattable;
import dyvil.source.position.SourcePosition;
import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.directive.BasicDirective;
import dyvil.tools.gensrc.ast.scope.LazyScope;
import dyvil.tools.gensrc.ast.scope.Scope;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.PrintStream;

public class DefineDirective extends VarDirective
{
	private boolean local;

	public DefineDirective(boolean local, SourcePosition position)
	{
		this.local = local;
		this.position = position;
	}

	@Override
	public void specialize(GenSrc gensrc, Scope scope, MarkerList markers, PrintStream output)
	{
		if (!this.local)
		{
			scope = scope.getGlobalParent();
		}

		if (scope instanceof LazyScope)
		{
			((LazyScope) scope).define(this.name.qualified, this.body);
		}
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(String indent, StringBuilder builder)
	{
		builder.append(this.local ? "#local" : "#define").append('(').append(this.name).append(')');

		if (this.body != null)
		{
			BasicDirective.appendBody(indent, builder, this.body);
		}
		else
		{
			builder.append('\n');
		}
	}
}

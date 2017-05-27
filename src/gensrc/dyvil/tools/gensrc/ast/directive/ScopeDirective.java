package dyvil.tools.gensrc.ast.directive;

import dyvil.source.position.SourcePosition;
import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.scope.LazyScope;
import dyvil.tools.gensrc.ast.scope.Scope;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.PrintStream;

public class ScopeDirective extends BasicDirective
{
	public ScopeDirective(SourcePosition position)
	{
		this.position = position;
	}

	@Override
	public Name getName()
	{
		return null;
	}

	@Override
	public void specialize(GenSrc gensrc, Scope scope, MarkerList markers, PrintStream output)
	{
		final LazyScope lazyScope = new LazyScope(scope);
		this.body.specialize(gensrc, lazyScope, markers, output);
	}

	@Override
	public String specialize(Scope scope)
	{
		return this.body.specialize(scope);
	}

	@Override
	public void toString(String indent, StringBuilder builder)
	{
		builder.append(indent).append("#(");
		this.arguments.toString(indent, builder);
		builder.append(") {");
		this.body.toString(indent + '\t', builder);
		builder.append(indent).append('}');
	}
}

package dyvil.tools.gensrc.ast.directive;

import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.scope.LazyScope;
import dyvil.tools.gensrc.ast.scope.Scope;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.PrintStream;

public class ScopeDirective implements Directive
{
	private Directive block;

	public Directive getBlock()
	{
		return this.block;
	}

	public void setBlock(Directive block)
	{
		this.block = block;
	}

	@Override
	public void specialize(GenSrc gensrc, Scope scope, MarkerList markers, PrintStream output)
	{
		final LazyScope lazyScope = new LazyScope(scope);
		this.block.specialize(gensrc, lazyScope, markers, output);
	}

	@Override
	public void toString(String indent, StringBuilder builder)
	{
		builder.append(indent).append("#block\n");
		this.block.toString(indent + '\t', builder);
		builder.append(indent).append("#end\n");
	}
}

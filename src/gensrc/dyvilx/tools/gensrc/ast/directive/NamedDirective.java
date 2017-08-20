package dyvilx.tools.gensrc.ast.directive;

import dyvil.source.position.SourcePosition;
import dyvilx.tools.gensrc.GenSrc;
import dyvilx.tools.gensrc.ast.scope.Scope;
import dyvil.lang.Name;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.PrintStream;

public class NamedDirective extends BasicDirective
{
	private Name name;

	public NamedDirective(SourcePosition position, Name name)
	{
		this.position = position;
		this.name = name;
	}

	@Override
	public Name getName()
	{
		return this.name;
	}

	public void setName(Name name)
	{
		this.name = name;
	}

	@Override
	public void specialize(GenSrc gensrc, Scope scope, MarkerList markers, PrintStream output)
	{

	}
}

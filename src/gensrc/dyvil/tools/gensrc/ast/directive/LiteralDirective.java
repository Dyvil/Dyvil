package dyvil.tools.gensrc.ast.directive;

import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.scope.Scope;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.PrintStream;

public class LiteralDirective implements Directive
{
	private final String text;

	public LiteralDirective(String text)
	{
		this.text = text;
	}

	@Override
	public void specialize(GenSrc gensrc, Scope scope, MarkerList markers, PrintStream output)
	{
		output.println(this.text);
	}

	@Override
	public String toString()
	{
		return Directive.toString(this);
	}

	@Override
	public void toString(String indent, StringBuilder builder)
	{
		builder.append(indent).append("#literal ").append(this.text).append('\n');
	}
}

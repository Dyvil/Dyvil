package dyvil.tools.gensrc.ast.directive;

import dyvil.lang.Formattable;
import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.scope.Scope;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.PrintStream;

public class LiteralText implements Directive
{
	private final String text;

	public LiteralText(String text)
	{
		this.text = text;
	}

	@Override
	public void specialize(GenSrc gensrc, Scope scope, MarkerList markers, PrintStream output)
	{
		output.print(this.text);
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(String indent, StringBuilder builder)
	{
		builder.append("#literal{").append(this.text).append('}');
	}
}

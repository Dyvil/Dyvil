package dyvilx.tools.gensrc.ast.directive;

import dyvil.lang.Formattable;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvilx.tools.gensrc.GenSrc;
import dyvilx.tools.gensrc.ast.scope.Scope;

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

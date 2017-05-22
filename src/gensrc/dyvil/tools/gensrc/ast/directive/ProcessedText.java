package dyvil.tools.gensrc.ast.directive;

import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.scope.Scope;
import dyvil.tools.gensrc.ast.Util;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.PrintStream;

public class ProcessedText implements Directive
{
	private final String text;

	public ProcessedText(String text)
	{
		this.text = text;
	}

	@Override
	public void specialize(GenSrc gensrc, Scope scope, MarkerList markers, PrintStream output)
	{
		output.print(Util.processLine(this.text, scope));
	}

	@Override
	public String specialize(Scope scope)
	{
		return Util.processLine(this.text, scope);
	}

	@Override
	public String toString()
	{
		return this.text;
	}

	@Override
	public void toString(String indent, StringBuilder builder)
	{
		builder.append(this.text);
	}
}

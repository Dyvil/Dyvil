package dyvil.tools.gensrc.ast.directive;

import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.scope.Scope;
import dyvil.tools.gensrc.ast.Util;

import java.io.PrintStream;

public class ProcessedLine implements Directive
{
	private final String text;

	public ProcessedLine(String text)
	{
		this.text = text;
	}

	@Override
	public void specialize(GenSrc gensrc, Scope scope, PrintStream output)
	{
		output.println(Util.processLine(this.text, scope));
	}

	@Override
	public String toString()
	{
		return this.text;
	}

	@Override
	public void toString(String indent, StringBuilder builder)
	{
		builder.append(this.text).append('\n');
	}
}

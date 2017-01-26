package dyvil.tools.gensrc.ast.directive;

import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.scope.Scope;

import java.io.PrintStream;

public class LiteralDirective implements Directive
{
	private final String text;

	public LiteralDirective(String text)
	{
		this.text = text;
	}

	@Override
	public void specialize(GenSrc gensrc, Scope scope, PrintStream output)
	{
		output.println(this.text);
	}
}

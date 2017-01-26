package dyvil.tools.gensrc.ast.directive;

import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.scope.Scope;

import java.io.PrintStream;

public interface Directive
{
	void specialize(GenSrc gensrc, Scope scope, PrintStream output);

	public static String toString(Directive dir)
	{
		final StringBuilder builder = new StringBuilder();
		dir.toString("", builder);
		return builder.toString();
	}

	void toString(String indent, StringBuilder builder);
}

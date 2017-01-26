package dyvil.tools.gensrc.ast.directive;

import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.scope.Scope;

import java.io.PrintStream;

public interface Directive
{
	void specialize(GenSrc gensrc, Scope scope, PrintStream output);
}

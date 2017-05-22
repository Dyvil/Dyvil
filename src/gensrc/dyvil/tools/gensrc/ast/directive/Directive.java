package dyvil.tools.gensrc.ast.directive;

import dyvil.source.position.SourcePosition;
import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.scope.Scope;
import dyvil.tools.parsing.ASTNode;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.PrintStream;

public interface Directive extends ASTNode
{
	@Override
	default SourcePosition getPosition()
	{
		return null;
	}

	@Override
	default void setPosition(SourcePosition sourcePosition)
	{
	}

	void specialize(GenSrc gensrc, Scope scope, MarkerList markers, PrintStream output);

	default String specialize(Scope scope)
	{
		return null;
	}

	@Override
	void toString(String indent, StringBuilder builder);
}

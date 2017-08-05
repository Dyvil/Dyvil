package dyvilx.tools.gensrc.ast.directive;

import dyvil.source.position.SourcePosition;
import dyvilx.tools.gensrc.GenSrc;
import dyvilx.tools.gensrc.ast.scope.Scope;
import dyvilx.tools.parsing.ASTNode;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.PrintStream;

public interface Directive extends ASTNode
{
	Directive LITERAL_HASH = new Directive()
	{
		@Override
		public void specialize(GenSrc gensrc, Scope scope, MarkerList markers, PrintStream output)
		{
			output.print('#');
		}

		@Override
		public void toString(String indent, StringBuilder builder)
		{
			builder.append("##");
		}
	};

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

	@Override
	void toString(String indent, StringBuilder builder);
}

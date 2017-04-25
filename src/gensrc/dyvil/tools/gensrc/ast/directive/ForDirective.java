package dyvil.tools.gensrc.ast.directive;

import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.Util;
import dyvil.tools.gensrc.ast.scope.LazyScope;
import dyvil.tools.gensrc.ast.scope.Scope;
import dyvil.tools.gensrc.lang.I18n;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.marker.SemanticError;
import dyvil.source.position.SourcePosition;

import java.io.PrintStream;

public class ForDirective implements Directive
{
	private final SourcePosition position;
	private final String        varName;
	private final String        start;
	private final String        end;

	private Directive action;

	public ForDirective(SourcePosition position, String varName, String start, String end)
	{
		this.position = position;
		this.varName = varName;
		this.start = start;
		this.end = end;
	}

	public Directive getAction()
	{
		return this.action;
	}

	public void setAction(Directive action)
	{
		this.action = action;
	}

	@Override
	public void specialize(GenSrc gensrc, Scope scope, MarkerList markers, PrintStream output)
	{
		final int start;
		final int end;

		try
		{
			start = Integer.parseInt(Util.processLine(this.start, scope));
			end = Integer.parseInt(Util.processLine(this.end, scope));
		}
		catch (NumberFormatException ignored)
		{
			markers.add(new SemanticError(this.position, I18n.get("for.start_end.invalid")));
			return;
		}

		final LazyScope forScope = new LazyScope(scope);

		for (int i = start; i <= end; i++)
		{
			forScope.define(this.varName, Integer.toString(i));
			this.action.specialize(gensrc, forScope, markers, output);
		}
	}

	@Override
	public String toString()
	{
		return Directive.toString(this);
	}

	@Override
	public void toString(String indent, StringBuilder builder)
	{
		builder.append(indent).append("#for ").append(this.varName).append(' ').append(this.start).append(' ')
		       .append(this.end).append('\n');

		this.action.toString(indent + '\t', builder);

		builder.append(indent).append("#end\n");
	}
}

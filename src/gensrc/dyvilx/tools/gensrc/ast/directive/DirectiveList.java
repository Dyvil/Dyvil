package dyvilx.tools.gensrc.ast.directive;

import dyvil.lang.Formattable;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvilx.tools.gensrc.GenSrc;
import dyvilx.tools.gensrc.ast.scope.Scope;

import java.io.PrintStream;
import java.util.Arrays;

public class DirectiveList implements Directive
{
	private Directive[] directives;
	private int         directiveCount;

	public DirectiveList()
	{
		this.directives = new Directive[8];
	}

	public DirectiveList(int capacity)
	{
		this.directives = new Directive[capacity];
	}

	public int size()
	{
		return this.directiveCount;
	}

	public void add(Directive dir)
	{
		final int index = this.directiveCount++;
		if (index >= this.directives.length)
		{
			this.directives = Arrays.copyOf(this.directives, index << 1);
		}
		this.directives[index] = dir;
	}

	@Override
	public void specialize(GenSrc gensrc, Scope scope, MarkerList markers, PrintStream output)
	{
		for (int i = 0; i < this.directiveCount; i++)
		{
			this.directives[i].specialize(gensrc, scope, markers, output);
		}
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(String indent, StringBuilder builder)
	{
		if (this.directiveCount == 0)
		{
			return;
		}

		for (int i = 0; i < this.directiveCount; i++)
		{
			this.indent(indent, builder);

			this.directives[i].toString(indent, builder);
		}

		this.indent(indent, builder);
	}

	protected void indent(String indent, StringBuilder builder)
	{
		if (builder.length() > 0 && builder.charAt(builder.length() - 1) == '\n')
		{
			builder.append(indent);
		}
	}
}

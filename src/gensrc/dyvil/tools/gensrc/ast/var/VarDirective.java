package dyvil.tools.gensrc.ast.var;

import dyvil.lang.Formattable;
import dyvil.source.position.SourcePosition;
import dyvil.tools.gensrc.ast.directive.Directive;
import dyvil.tools.gensrc.ast.directive.DirectiveList;
import dyvil.lang.Name;

public abstract class VarDirective implements Directive
{
	protected Name name;

	protected DirectiveList body;

	// Metadata
	protected SourcePosition position;

	@Override
	public SourcePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
		this.position = position;
	}

	public Name getName()
	{
		return this.name;
	}

	public void setName(Name name)
	{
		this.name = name;
	}

	public DirectiveList getBody()
	{
		return this.body;
	}

	public void setBody(DirectiveList body)
	{
		this.body = body;
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}
}

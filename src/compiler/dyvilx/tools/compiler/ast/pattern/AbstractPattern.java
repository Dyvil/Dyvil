package dyvilx.tools.compiler.ast.pattern;

import dyvil.lang.Formattable;
import dyvil.source.position.SourcePosition;

public abstract class AbstractPattern implements Pattern
{
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

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}
}

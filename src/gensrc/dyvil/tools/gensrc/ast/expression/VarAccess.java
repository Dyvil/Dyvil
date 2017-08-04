package dyvil.tools.gensrc.ast.expression;

import dyvil.source.position.SourcePosition;
import dyvil.tools.gensrc.ast.scope.Scope;
import dyvil.lang.Name;

public class VarAccess implements Expression
{
	private SourcePosition position;
	private final Name name;

	public VarAccess(SourcePosition position, Name name)
	{
		this.position = position;
		this.name = name;
	}

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
	public String evaluateString(Scope scope)
	{
		return scope.getString(this.name.qualified);
	}

	@Override
	public void toString(String indent, StringBuilder builder)
	{
		builder.append(this.name);
	}
}

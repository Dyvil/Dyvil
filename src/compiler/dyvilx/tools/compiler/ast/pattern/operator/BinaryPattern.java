package dyvilx.tools.compiler.ast.pattern.operator;

import dyvil.lang.Formattable;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.pattern.Pattern;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.parsing.marker.MarkerList;

public abstract class BinaryPattern implements Pattern
{
	protected Pattern        left;
	protected Pattern        right;
	protected SourcePosition position;
	// Metadata
	private   IType          commonType;

	public BinaryPattern(Pattern left, SourcePosition token, Pattern right)
	{
		this.right = right;
		this.left = left;
		this.position = token;
	}

	public Pattern getLeft()
	{
		return this.left;
	}

	public void setLeft(Pattern left)
	{
		this.left = left;
	}

	public Pattern getRight()
	{
		return this.right;
	}

	public void setRight(Pattern right)
	{
		this.right = right;
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
	public boolean isWildcard()
	{
		return this.left.isWildcard() && this.right.isWildcard();
	}

	@Override
	public IType getType()
	{
		if (this.commonType == null)
		{
			return this.commonType = Types.combine(this.left.getType(), this.right.getType());
		}
		return this.commonType;
	}

	@Override
	public boolean isType(IType type)
	{
		return this.left.isType(type) && this.right.isType(type);
	}

	@Override
	public Pattern withType(IType type, MarkerList markers)
	{
		final Pattern left = this.left.withType(type, markers);
		if (left == null)
		{
			return null;
		}

		final Pattern right = this.right.withType(type, markers);
		if (right == null)
		{
			return null;
		}

		this.left = left;
		this.right = right;
		return this.withType();
	}

	protected abstract Pattern withType();

	@Override
	public Pattern resolve(MarkerList markers, IContext context)
	{
		this.left = this.left.resolve(markers, context);
		this.right = this.right.resolve(markers, context);
		return this;
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}
}

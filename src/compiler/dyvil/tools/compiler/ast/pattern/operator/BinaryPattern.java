package dyvil.tools.compiler.ast.pattern.operator;

import dyvil.lang.Formattable;
import dyvil.source.position.SourcePosition;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.pattern.IPattern;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.parsing.marker.MarkerList;

public abstract class BinaryPattern implements IPattern
{
	protected IPattern      left;
	protected IPattern      right;
	protected SourcePosition position;
	// Metadata
	private   IType         commonType;

	public BinaryPattern(IPattern left, SourcePosition token, IPattern right)
	{
		this.right = right;
		this.left = left;
		this.position = token;
	}

	public IPattern getLeft()
	{
		return this.left;
	}

	public void setLeft(IPattern left)
	{
		this.left = left;
	}

	public IPattern getRight()
	{
		return this.right;
	}

	public void setRight(IPattern right)
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
	public IPattern withType(IType type, MarkerList markers)
	{
		final IPattern left = this.left.withType(type, markers);
		if (left == null)
		{
			return null;
		}

		final IPattern right = this.right.withType(type, markers);
		if (right == null)
		{
			return null;
		}

		this.left = left;
		this.right = right;
		return this.withType();
	}

	protected abstract IPattern withType();

	@Override
	public IPattern resolve(MarkerList markers, IContext context)
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

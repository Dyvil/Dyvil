package dyvil.tools.compiler.ast.generic;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITypeList;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public abstract class BaseBounded implements IASTNode, IBounded, ITypeList
{
	protected ICodePosition	position;
	
	protected IType[]		upperBounds	= new IType[1];
	protected int			upperBoundCount;
	protected IType			lowerBound;
	
	public BaseBounded()
	{
	}
	
	public BaseBounded(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}
	
	@Override
	public int typeCount()
	{
		return this.upperBoundCount;
	}
	
	@Override
	public void setType(int index, IType type)
	{
		this.upperBounds[index] = type;
	}
	
	@Override
	public void addType(IType type)
	{
		int index = this.upperBoundCount++;
		if (index >= this.upperBounds.length)
		{
			IType[] temp = new IType[this.upperBoundCount];
			System.arraycopy(this.upperBounds, 0, temp, 0, index);
			this.upperBounds = temp;
		}
		this.upperBounds[index] = type;
	}
	
	@Override
	public IType getType(int index)
	{
		return this.upperBounds[index];
	}
	
	@Override
	public int upperBoundCount()
	{
		return this.upperBoundCount;
	}
	
	@Override
	public void setUpperBound(int index, IType bound)
	{
		this.upperBounds[index] = bound;
	}
	
	@Override
	public void addUpperBound(IType bound)
	{
		int index = this.upperBoundCount++;
		if (index >= this.upperBounds.length)
		{
			IType[] temp = new IType[this.upperBoundCount];
			System.arraycopy(this.upperBounds, 0, temp, 0, index);
			this.upperBounds = temp;
		}
		this.upperBounds[index] = bound;
	}
	
	@Override
	public IType getUpperBound(int index)
	{
		return this.upperBounds[index];
	}
	
	@Override
	public IType[] getUpperBounds()
	{
		return this.upperBounds;
	}
	
	@Override
	public void setLowerBound(IType bound)
	{
		this.lowerBound = bound;
	}
	
	@Override
	public IType getLowerBound()
	{
		return this.lowerBound;
	}
	
	public IClass getTheClass()
	{
		if (this.lowerBound != null || this.upperBoundCount == 0)
		{
			return Types.OBJECT_CLASS;
		}
		return this.upperBounds[0].getTheClass();
	}
	
	public boolean isSuperTypeOf(IType type)
	{
		if (this.upperBoundCount > 0)
		{
			for (int i = 0; i < this.upperBoundCount; i++)
			{
				if (!this.upperBounds[i].isSuperTypeOf(type))
				{
					return false;
				}
			}
		}
		if (this.lowerBound != null)
		{
			if (!type.isSuperTypeOf(this.lowerBound))
			{
				return false;
			}
		}
		return true;
	}
	
	// Misc
	
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.lowerBound != null)
		{
			this.lowerBound = this.lowerBound.resolve(markers, context);
		}
		
		if (this.upperBoundCount > 0)
		{
			// The first upper bound is meant to be a class bound.
			IType type = this.upperBounds[0] = this.upperBounds[0].resolve(markers, context);
			IClass iclass = type.getTheClass();
			if (iclass != null)
			{
				// If the first upper bound is an interface...
				if (iclass.hasModifier(Modifiers.INTERFACE_CLASS))
				{
					// shift the entire array one to the right and insert
					// Type.OBJECT at index 0
					if (++this.upperBoundCount > this.upperBounds.length)
					{
						IType[] temp = new IType[this.upperBoundCount];
						temp[0] = Types.OBJECT;
						System.arraycopy(this.upperBounds, 0, temp, 1, this.upperBoundCount - 1);
						this.upperBounds = temp;
					}
					else
					{
						System.arraycopy(this.upperBounds, 0, this.upperBounds, 1, this.upperBoundCount - 1);
						this.upperBounds[0] = Types.OBJECT;
					}
				}
			}
			
			// Check if the remaining upper bounds are interfaces, and remove if
			// not.
			for (int i = 0; i < this.upperBoundCount; i++)
			{
				type = this.upperBounds[i] = this.upperBounds[i].resolve(markers, context);
				iclass = type.getTheClass();
				if (iclass != null && !iclass.hasModifier(Modifiers.INTERFACE_CLASS))
				{
					System.arraycopy(this.upperBounds, i + 1, this.upperBounds, i, this.upperBoundCount - i - 1);
					this.upperBoundCount--;
					i--;
				}
			}
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		this.toString("", builder);
		return builder.toString();
	}
}

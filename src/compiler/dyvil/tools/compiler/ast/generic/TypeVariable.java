package dyvil.tools.compiler.ast.generic;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.CaptureClass;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class TypeVariable extends ASTNode implements ITypeVariable
{
	public String			name;
	
	protected IType[]		upperBounds;
	protected int			upperBoundCount;
	protected IType			lowerBound;
	
	protected CaptureClass	captureClass;
	
	public TypeVariable()
	{
	}
	
	public TypeVariable(String name)
	{
		this.name = name;
	}
	
	public TypeVariable(ICodePosition position)
	{
		this.position = position;
	}
	
	public TypeVariable(ICodePosition position, String name)
	{
		this.position = position;
		this.name = name;
	}
	
	@Override
	public void setName(String name, String qualifiedName)
	{
		this.name = name;
	}
	
	@Override
	public void setName(String name)
	{
		this.name = name;
	}
	
	@Override
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public void setQualifiedName(String name)
	{
		this.name = name;
	}
	
	@Override
	public String getQualifiedName()
	{
		return this.name;
	}
	
	@Override
	public boolean isName(String name)
	{
		return this.name.equals(name);
	}
	
	@Override
	public CaptureClass getCaptureClass()
	{
		return this.captureClass;
	}
	
	@Override
	public void addUpperBound(IType bound)
	{
		// FIXME
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
	
	@Override
	public boolean isSuperTypeOf(IType type)
	{
		if (this.upperBounds != null)
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
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.lowerBound != null)
		{
			this.lowerBound = this.lowerBound.resolve(markers, context);
		}
		
		if (this.upperBoundCount > 0)
		{
			// The first upper bound is meant to be a class bound.
			IType type = this.upperBounds[0];
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
						temp[0] = Type.OBJECT;
						System.arraycopy(this.upperBounds, 0, temp, 1, this.upperBoundCount - 1);
						this.upperBounds = temp;
					}
					else
					{
						System.arraycopy(this.upperBounds, 0, this.upperBounds, 1, this.upperBoundCount - 1);
						this.upperBounds[0] = Type.OBJECT;
					}
				}
			}
			
			// Check if the remaining upper bounds are interfaces, and remove if
			// not.
			for (int i = 1; i < this.upperBoundCount; i++)
			{
				type = this.upperBounds[i];
				iclass = type.getTheClass();
				if (iclass != null && !iclass.hasModifier(Modifiers.INTERFACE_CLASS))
				{
					System.arraycopy(this.upperBounds, i + 1, this.upperBounds, i, this.upperBoundCount - i - 1);
					this.upperBoundCount--;
					i--;
				}
			}
		}
		this.captureClass = new CaptureClass(this, this.upperBounds, this.upperBoundCount, this.lowerBound);
	}
	
	@Override
	public void appendSignature(StringBuilder buffer)
	{
		buffer.append(this.name).append(':');
		if (this.upperBoundCount > 0)
		{
			if (this.upperBounds[0] != Type.OBJECT)
			{
				this.upperBounds[0].appendSignature(buffer);
			}
			
			for (int i = 1; i < this.upperBoundCount; i++)
			{
				buffer.append(':');
				this.upperBounds[i].appendSignature(buffer);
			}
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.name == null)
		{
			buffer.append('_');
		}
		else
		{
			buffer.append(this.name);
		}
		
		if (this.lowerBound != null)
		{
			buffer.append(Formatting.Type.genericLowerBound);
			this.lowerBound.toString(prefix, buffer);
		}
		if (this.upperBoundCount > 0)
		{
			buffer.append(Formatting.Type.genericUpperBound);
			this.upperBounds[0].toString(prefix, buffer);
			for (int i = 1; i < this.upperBoundCount; i++)
			{
				buffer.append(Formatting.Type.genericBoundSeperator);
				this.upperBounds[i].toString(prefix, buffer);
			}
		}
	}
}

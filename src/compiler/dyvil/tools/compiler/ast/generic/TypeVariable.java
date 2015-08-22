package dyvil.tools.compiler.ast.generic;

import dyvil.reflect.Modifiers;
import dyvil.tools.asm.ClassWriter;
import dyvil.tools.asm.TypeReference;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class TypeVariable implements ITypeVariable
{
	protected ICodePosition position;
	
	protected Variance	variance	= Variance.INVARIANT;
	protected Name		name;
	protected IType[]	upperBounds	= new IType[1];
	protected int		upperBoundCount;
	protected IType		lowerBound;
	
	private int			index;
	private IGeneric	generic;
	
	public TypeVariable(IGeneric generic)
	{
		this.generic = generic;
	}
	
	public TypeVariable(IGeneric generic, Name name)
	{
		this.name = name;
		this.generic = generic;
	}
	
	public TypeVariable(ICodePosition position, IGeneric generic)
	{
		this.position = position;
		this.generic = generic;
	}
	
	public TypeVariable(ICodePosition position, IGeneric generic, Name name, Variance variance)
	{
		this.position = position;
		this.name = name;
		this.generic = generic;
		this.variance = variance;
	}
	
	@Override
	public IGeneric getGeneric()
	{
		return this.generic;
	}
	
	@Override
	public void setIndex(int index)
	{
		this.index = index;
	}
	
	@Override
	public int getIndex()
	{
		return this.index;
	}
	
	@Override
	public void setVariance(Variance variance)
	{
		this.variance = variance;
	}
	
	@Override
	public Variance getVariance()
	{
		return this.variance;
	}
	
	@Override
	public void setName(Name name)
	{
		this.name = name;
	}
	
	@Override
	public Name getName()
	{
		return this.name;
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
	
	@Override
	public IClass getTheClass()
	{
		if (this.lowerBound != null || this.upperBoundCount == 0)
		{
			return Types.OBJECT_CLASS;
		}
		return this.upperBounds[0].getTheClass();
	}
	
	@Override
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
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.lowerBound != null)
		{
			this.lowerBound = this.lowerBound.resolveType(markers, context);
		}
		
		if (this.upperBoundCount > 0)
		{
			// The first upper bound is meant to be a class bound.
			IType type = this.upperBounds[0] = this.upperBounds[0].resolveType(markers, context);
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
			for (int i = 1; i < this.upperBoundCount; i++)
			{
				type = this.upperBounds[i] = this.upperBounds[i].resolveType(markers, context);
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
	public void resolve(MarkerList markers, IContext context)
	{
		if (this.lowerBound != null)
		{
			this.lowerBound.resolve(markers, context);
		}
		
		for (int i = 0; i < this.upperBoundCount; i++)
		{
			this.upperBounds[i].resolve(markers, context);
		}
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.lowerBound != null)
		{
			this.lowerBound.checkType(markers, context, TypePosition.SUPER_TYPE_ARGUMENT);
		}
		
		for (int i = 0; i < this.upperBoundCount; i++)
		{
			this.upperBounds[i].checkType(markers, context, TypePosition.SUPER_TYPE_ARGUMENT);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.lowerBound != null)
		{
			this.lowerBound.check(markers, context);
		}
		
		for (int i = 0; i < this.upperBoundCount; i++)
		{
			this.upperBounds[i].check(markers, context);
		}
	}
	
	@Override
	public void foldConstants()
	{
		if (this.lowerBound != null)
		{
			this.lowerBound.foldConstants();
		}
		
		for (int i = 0; i < this.upperBoundCount; i++)
		{
			this.upperBounds[i].foldConstants();
		}
	}
	
	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		if (this.lowerBound != null)
		{
			this.lowerBound.cleanup(context, compilableList);
		}
		
		for (int i = 0; i < this.upperBoundCount; i++)
		{
			this.upperBounds[i].cleanup(context, compilableList);
		}
	}
	
	@Override
	public void appendSignature(StringBuilder buffer)
	{
		buffer.append(this.name).append(':');
		if (this.upperBoundCount > 0)
		{
			if (this.upperBounds[0] != Types.OBJECT || this.upperBoundCount == 1)
			{
				this.upperBounds[0].appendSignature(buffer);
			}
			
			for (int i = 1; i < this.upperBoundCount; i++)
			{
				buffer.append(':');
				this.upperBounds[i].appendSignature(buffer);
			}
		}
		else
		{
			buffer.append("Ljava/lang/Object;");
		}
	}
	
	@Override
	public void write(ClassWriter writer)
	{
		if (this.variance != Variance.INVARIANT)
		{
			int typeRef = TypeReference.newTypeParameterReference(TypeReference.CLASS_TYPE_PARAMETER, this.index);
			String type = this.variance == Variance.CONTRAVARIANT ? "Ldyvil/annotation/Contravariant;" : "Ldyvil/annotation/Covariant;";
			writer.visitTypeAnnotation(typeRef, null, type, true);
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		this.toString("", builder);
		return builder.toString();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.variance.appendPrefix(buffer);
		buffer.append(this.name);
		
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

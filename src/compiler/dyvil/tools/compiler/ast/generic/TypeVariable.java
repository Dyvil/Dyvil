package dyvil.tools.compiler.ast.generic;

import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class TypeVariable extends BaseBounded implements ITypeVariable
{
	private int index;
	private IGeneric	generic;
	public Name			name;
	
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
	
	public TypeVariable(ICodePosition position, IGeneric generic, Name name)
	{
		this.position = position;
		this.name = name;
		this.generic = generic;
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
	public void toString(String prefix, StringBuilder buffer)
	{
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

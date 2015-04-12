package dyvil.tools.compiler.ast.generic;

import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITypeList;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.util.Util;

public final class GenericData implements ITypeList
{
	public IType[]	generics;
	public int		genericCount;
	
	public int		computedGenerics	= -1;
	
	public GenericData()
	{
	}
	
	public GenericData(int count)
	{
		this.genericCount = count;
		this.generics = new IType[count];
		this.computedGenerics = count;
	}
	
	@Override
	public int typeCount()
	{
		return this.genericCount;
	}
	
	public void setTypeCount(int count)
	{
		this.computedGenerics = count;
		
		if (this.generics == null)
		{
			this.generics = new IType[count];
		}
		
		if (count > this.generics.length)
		{
			IType[] temp = new IType[count];
			System.arraycopy(this.generics, 0, temp, 0, this.generics.length);
			this.generics = temp;
		}
	}
	
	@Override
	public void setType(int index, IType type)
	{
		this.generics[index] = type;
	}
	
	@Override
	public void addType(IType type)
	{
		if (this.generics == null)
		{
			this.generics = new IType[3];
			this.generics[0] = type;
			this.genericCount = 1;
			return;
		}
		
		int index = this.genericCount++;
		if (this.genericCount > this.generics.length)
		{
			IType[] temp = new IType[this.genericCount];
			System.arraycopy(this.generics, 0, temp, 0, index);
			this.generics = temp;
		}
		this.generics[index] = type;
	}
	
	@Override
	public IType getType(int index)
	{
		return this.generics[index];
	}
	
	public void resolveTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.typeCount(); i++)
		{
			this.generics[i] = this.generics[i].resolve(markers, context);
		}
	}
	
	public void toString(String prefix, StringBuilder buffer)
	{
		int len = this.genericCount;
		if (this.computedGenerics != -1)
		{
			len -= this.computedGenerics;
		}
		
		if (len > 0)
		{
			buffer.append('[');
			Util.astToString(prefix, this.generics, len, Formatting.Type.genericSeperator, buffer);
			buffer.append(']');
		}
	}
}

package dyvil.tools.compiler.ast.generic;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITypeList;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.typevar.TypeVarType;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.marker.MarkerList;

public final class GenericData implements ITypeList, ITypeContext
{
	public IMethod method;
	public IType[] generics;
	public int     genericCount;
	public IValue  instance;
	
	public GenericData()
	{
	}
	
	public GenericData(IMethod method, int count)
	{
		this.method = method;
		this.generics = new IType[count];
	}
	
	@Override
	public int typeCount()
	{
		return this.genericCount;
	}
	
	public void setTypeCount(int count)
	{
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
	
	private boolean isMethodTypeVariable(ITypeParameter typeVar)
	{
		int index = typeVar.getIndex();
		if (index >= this.method.typeParameterCount())
		{
			return false;
		}
		return this.method.getTypeParameter(index) == typeVar;
	}
	
	@Override
	public IType resolveType(ITypeParameter typeParameter)
	{
		if (this.isMethodTypeVariable(typeParameter))
		{
			int index = typeParameter.getIndex();
			if (index >= this.genericCount)
			{
				return new TypeVarType(typeParameter);
			}
			return this.generics[index];
		}
		return this.instance.getType().resolveType(typeParameter);
	}
	
	@Override
	public void addMapping(ITypeParameter typeVar, IType type)
	{
		if (type == Types.UNKNOWN)
		{
			return;
		}
		
		int index = typeVar.getIndex();
		if (!this.isMethodTypeVariable(typeVar))
		{
			return;
		}
		
		if (index < this.genericCount)
		{
			if (this.generics[index] == null)
			{
				this.generics[index] = type;
				return;
			}
			this.generics[index] = Types.combine(this.generics[index], type);
			return;
		}
		
		this.genericCount = index + 1;
		if (index >= this.generics.length)
		{
			IType[] temp = new IType[index + 1];
			System.arraycopy(this.generics, 0, temp, 0, this.generics.length);
			this.generics = temp;
		}
		this.generics[typeVar.getIndex()] = type;
	}
	
	public void resolveTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.genericCount; i++)
		{
			this.generics[i] = this.generics[i].resolveType(markers, context);
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		this.toString("", builder);
		return builder.toString();
	}
	
	public void toString(String prefix, StringBuilder buffer)
	{
		int len = this.genericCount;
		if (len > 0)
		{
			buffer.append('.');
			Formatting.appendSeparator(buffer, "generics.open_bracket", '[');
			Util.astToString(prefix, this.generics, len, Formatting.getSeparator("generics.separator", ','), buffer);
			Formatting.appendSeparator(buffer, "generics.close_bracket", ']');
		}
	}
}

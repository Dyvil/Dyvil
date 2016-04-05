package dyvil.tools.compiler.ast.generic;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITypeList;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.marker.MarkerList;

public final class GenericData implements ITypeList, ITypeContext
{
	public    IMethod method;
	protected IType[] generics;
	protected int     genericCount;
	protected int     lockedCount;
	public    IType   receiverType;

	public GenericData()
	{
	}

	public GenericData(IMethod method, int count)
	{
		this.method = method;
		this.generics = new IType[count];
	}

	public GenericData(IMethod method, IType... generics)
	{
		this.method = method;
		this.genericCount = generics.length;
		this.generics = generics;
	}

	public IType getReceiverType()
	{
		return this.receiverType;
	}

	public void setReceiverType(IType receiverType)
	{
		this.receiverType = receiverType;
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
		if (typeVar.getGeneric() == this.method)
		{
			return true;
		}

		final int index = typeVar.getIndex();
		return index < this.method.typeParameterCount() && this.method.getTypeParameter(index) == typeVar;
	}

	@Override
	public IType resolveType(ITypeParameter typeParameter)
	{
		if (this.isMethodTypeVariable(typeParameter))
		{
			int index = typeParameter.getIndex();
			if (index >= this.genericCount)
			{
				return null;
			}
			return this.generics[index];
		}
		return this.receiverType.resolveType(typeParameter);
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
			if (index >= this.lockedCount)
			{
				this.generics[index] = Types.combine(this.generics[index], type);
			}
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

		this.lockedCount = this.genericCount;
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
		if (this.genericCount > 0)
		{
			Formatting.appendSeparator(buffer, "generics.open_bracket", '<');
			Util.astToString(prefix, this.generics, this.genericCount,
			                 Formatting.getSeparator("generics.separator", ','), buffer);
			Formatting.appendSeparator(buffer, "generics.close_bracket", '>');
		}
	}
}

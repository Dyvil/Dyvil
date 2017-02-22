package dyvil.tools.compiler.ast.generic;

import dyvil.tools.compiler.phase.IResolvable;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITypeList;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.marker.MarkerList;

public final class GenericData implements IResolvable, ITypeList, ITypeContext
{
	protected ITypeParametric typeParametric;
	protected IType[]         generics;
	protected int             genericCount;
	protected int             lockedCount;
	protected ITypeContext    fallbackTypeContext;

	public GenericData()
	{
	}

	public GenericData(ITypeParametric typeParametric, int capacity)
	{
		this.typeParametric = typeParametric;
		this.generics = new IType[capacity];
	}

	public GenericData(ITypeParametric typeParametric, IType... generics)
	{
		this.typeParametric = typeParametric;
		this.genericCount = generics.length;
		this.generics = generics;
	}

	public ITypeParametric getTypeParametric()
	{
		return this.typeParametric;
	}

	public void setTypeParametric(ITypeParametric typeParametric)
	{
		this.typeParametric = typeParametric;
	}

	public ITypeContext getFallbackTypeContext()
	{
		return this.fallbackTypeContext;
	}

	public void setFallbackTypeContext(ITypeContext fallbackTypeContext)
	{
		this.fallbackTypeContext = fallbackTypeContext;
	}

	public void lockAvailable()
	{
		this.lock(this.genericCount);
	}

	public void lock(int lockedCount)
	{
		if (lockedCount > this.lockedCount)
		{
			this.lockedCount = lockedCount;
		}
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
		if (typeVar.getGeneric() == this.typeParametric)
		{
			return true;
		}

		final int index = typeVar.getIndex();
		return index < this.typeParametric.typeParameterCount()
			       && this.typeParametric.getTypeParameter(index) == typeVar;
	}

	@Override
	public IType resolveType(ITypeParameter typeParameter)
	{
		if (this.isMethodTypeVariable(typeParameter))
		{
			int index = typeParameter.getIndex();
			if (index >= this.genericCount || index >= this.lockedCount)
			{
				return null;
			}
			return this.generics[index];
		}
		return this.fallbackTypeContext == null ? null : this.fallbackTypeContext.resolveType(typeParameter);
	}

	@Override
	public boolean isReadonly()
	{
		return false;
	}

	@Override
	public void addMapping(ITypeParameter typeVar, IType type)
	{
		if (type == Types.UNKNOWN)
		{
			return;
		}

		final int index = typeVar.getIndex();
		if (!this.isMethodTypeVariable(typeVar))
		{
			return;
		}

		type = type.asReturnType();

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

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.genericCount; i++)
		{
			this.generics[i] = this.generics[i].resolveType(markers, context);
		}

		this.lockedCount = this.genericCount;
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.genericCount; i++)
		{
			this.generics[i].resolve(markers, context);
		}
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.genericCount; i++)
		{
			this.generics[i].checkType(markers, context, IType.TypePosition.GENERIC_ARGUMENT);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.genericCount; i++)
		{
			this.generics[i].check(markers, context);
		}
	}

	@Override
	public void foldConstants()
	{
		for (int i = 0; i < this.genericCount; i++)
		{
			this.generics[i].foldConstants();
		}
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		for (int i = 0; i < this.genericCount; i++)
		{
			this.generics[i].cleanup(compilableList, classCompilableList);
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
		if (this.genericCount > 0)
		{
			Formatting.appendSeparator(buffer, "generics.open_bracket", '<');
			Util.astToString(prefix, this.generics, this.genericCount,
			                 Formatting.getSeparator("generics.separator", ','), buffer);
			Formatting.appendSeparator(buffer, "generics.close_bracket", '>');
		}
	}
}

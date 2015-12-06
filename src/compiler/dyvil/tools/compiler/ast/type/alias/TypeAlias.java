package dyvil.tools.compiler.ast.type.alias;

import dyvil.tools.compiler.ast.context.CombiningContext;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IDefaultContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class TypeAlias implements ITypeAlias, IDefaultContext
{
	protected Name  name;
	protected IType type;

	protected ITypeVariable[] typeVariables;
	protected int             typeVariableCount;
	
	public TypeAlias()
	{
	}

	public TypeAlias(Name name)
	{
		this.name = name;
	}

	public TypeAlias(Name name, IType type)
	{
		this.name = name;
		this.type = type;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return null;
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
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
	public void setType(IType type)
	{
		this.type = type;
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}

	@Override
	public void addTypeVariable(ITypeVariable var)
	{
		if (this.typeVariables == null)
		{
			this.setGeneric();
			this.typeVariables[0] = var;
			this.typeVariableCount = 1;
			return;
		}

		int index = this.typeVariableCount++;
		if (index >= this.typeVariables.length)
		{
			ITypeVariable[] temp = new ITypeVariable[index + 1];
			System.arraycopy(this.typeVariables, 0, temp, 0, index);
			this.typeVariables = temp;
		}
		this.typeVariables[index] = var;
	}

	@Override
	public int genericCount()
	{
		return this.typeVariableCount;
	}

	@Override
	public ITypeVariable getTypeVariable(int index)
	{
		return this.typeVariables[index];
	}

	@Override
	public ITypeVariable[] getTypeVariables()
	{
		return this.typeVariables;
	}

	@Override
	public boolean isGeneric()
	{
		return this.typeVariables != null;
	}

	@Override
	public void setGeneric()
	{
		this.typeVariables = new ITypeVariable[2];
	}

	@Override
	public void setTypeVariable(int index, ITypeVariable var)
	{
		this.typeVariables[index] = var;
	}

	@Override
	public void setTypeVariables(ITypeVariable[] typeVars, int count)
	{
		this.typeVariables = typeVars;
		this.typeVariableCount = count;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		IContext combinedContext = new CombiningContext(this, context);

		this.type = this.type.resolveType(markers, combinedContext);

		for (int i = 0; i < this.typeVariableCount; i++)
		{
			this.typeVariables[i].resolveTypes(markers, combinedContext);
		}
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		IContext combinedContext = new CombiningContext(this, context);

		this.type.resolve(markers, combinedContext);

		for (int i = 0; i < this.typeVariableCount; i++)
		{
			this.typeVariables[i].resolve(markers, combinedContext);
		}
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		IContext combinedContext = new CombiningContext(this, context);

		this.type.checkType(markers, combinedContext, TypePosition.TYPE);

		for (int i = 0; i < this.typeVariableCount; i++)
		{
			this.typeVariables[i].checkTypes(markers, combinedContext);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		IContext combinedContext = new CombiningContext(this, context);

		this.type.check(markers, combinedContext);

		for (int i = 0; i < this.typeVariableCount; i++)
		{
			this.typeVariables[i].check(markers, context);
		}
	}
	
	@Override
	public void foldConstants()
	{
		this.type.foldConstants();

		for (int i = 0; i < this.typeVariableCount; i++)
		{
			this.typeVariables[i].foldConstants();
		}
	}
	
	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		IContext combinedContext = new CombiningContext(this, context);

		this.type.cleanup(combinedContext, compilableList);

		for (int i = 0; i < this.typeVariableCount; i++)
		{
			this.typeVariables[i].cleanup(combinedContext, compilableList);
		}
	}
	
	@Override
	public void write(DataOutput dos) throws IOException
	{
		dos.writeUTF(this.name.qualified);
		IType.writeType(this.type, dos);
	}
	
	@Override
	public void read(DataInput dis) throws IOException
	{
		this.name = Name.getQualified(dis.readUTF());
		this.type = IType.readType(dis);
	}
	
	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("type ").append(this.name);

		if (this.typeVariableCount > 0)
		{
			Formatting.appendSeparator(buffer, "generics.open_bracket", '[');
			this.typeVariables[0].toString(prefix, buffer);

			for (int i = 1; i < this.typeVariableCount; i++)
			{
				Formatting.appendSeparator(buffer, "generics.separator", ',');
				this.typeVariables[i].toString(prefix, buffer);
			}

			if (Formatting.getBoolean("generics.close_bracket.space_before"))
			{
				buffer.append(' ');
			}
			buffer.append(']');
		}

		Formatting.appendSeparator(buffer, "field.assignment", '=');
		this.type.toString(prefix, buffer);
	}
}

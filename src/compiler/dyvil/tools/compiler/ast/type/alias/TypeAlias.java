package dyvil.tools.compiler.ast.type.alias;

import dyvil.annotation.internal.NonNull;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IDefaultContext;
import dyvil.tools.compiler.ast.external.ExternalTypeParameter;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.header.IHeaderUnit;
import dyvil.tools.compiler.ast.header.ISourceHeader;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Markers;
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

	protected ITypeParameter[] typeVariables;
	protected int              typeVariableCount;

	// Metadata
	protected IHeaderUnit   enclosingHeader;
	protected ICodePosition position;
	protected boolean       resolved;

	public TypeAlias()
	{
	}

	public TypeAlias(Name name)
	{
		this.name = name;
	}

	public TypeAlias(Name name, ICodePosition position)
	{
		this.name = name;
		this.position = position;
	}

	public TypeAlias(Name name, IType type)
	{
		this.name = name;
		this.type = type;
	}

	@Override
	public IHeaderUnit getEnclosingHeader()
	{
		return this.enclosingHeader;
	}

	@Override
	public void setEnclosingHeader(IHeaderUnit header)
	{
		this.enclosingHeader = header;
	}

	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}

	@Override
	public Name getName()
	{
		return this.name;
	}

	@Override
	public void setName(Name name)
	{
		this.name = name;
	}

	@Override
	public IType getType()
	{
		this.ensureResolved();
		return this.type;
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
	}

	@Override
	public boolean isTypeParametric()
	{
		return this.typeVariables != null;
	}

	@Override
	public void setTypeParametric()
	{
		this.typeVariables = new ITypeParameter[2];
	}

	@Override
	public int typeParameterCount()
	{
		return this.typeVariableCount;
	}

	@Override
	public ITypeParameter[] getTypeParameters()
	{
		return this.typeVariables;
	}

	@Override
	public void setTypeParameters(ITypeParameter[] typeParameters, int count)
	{
		this.typeVariables = typeParameters;
		this.typeVariableCount = count;
	}

	@Override
	public ITypeParameter getTypeParameter(int index)
	{
		return this.typeVariables[index];
	}

	@Override
	public void setTypeParameter(int index, ITypeParameter typeParameter)
	{
		typeParameter.setIndex(index);
		this.typeVariables[index] = typeParameter;
	}

	@Override
	public void addTypeParameter(ITypeParameter typeParameter)
	{
		if (this.typeVariables == null)
		{
			this.setTypeParametric();
			typeParameter.setIndex(0);
			this.typeVariables[0] = typeParameter;
			this.typeVariableCount = 1;
			return;
		}

		int index = this.typeVariableCount++;
		if (index >= this.typeVariables.length)
		{
			ITypeParameter[] temp = new ITypeParameter[index + 1];
			System.arraycopy(this.typeVariables, 0, temp, 0, index);
			this.typeVariables = temp;
		}

		typeParameter.setIndex(index);
		this.typeVariables[index] = typeParameter;
	}

	@Override
	public ITypeParameter resolveTypeParameter(Name name)
	{
		for (int i = 0; i < this.typeVariableCount; i++)
		{
			final ITypeParameter typeVariable = this.typeVariables[i];
			if (typeVariable.getName() == name)
			{
				return typeVariable;
			}
		}

		return null;
	}

	private void ensureResolved()
	{
		if (this.resolved)
		{
			return;
		}

		final MarkerList markers = this.enclosingHeader instanceof ISourceHeader ?
			                           ((ISourceHeader) this.enclosingHeader).getMarkers() :
			                           null;
		this.resolveTypes(markers, this.enclosingHeader.getContext());
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		context = context.push(this);

		if (this.type == null)
		{
			this.type = Types.UNKNOWN;
			markers.add(Markers.semanticError(this.position, "typealias.invalid"));
		}

		this.resolved = true;
		this.type = this.type.resolveType(markers, context);

		for (int i = 0; i < this.typeVariableCount; i++)
		{
			this.typeVariables[i].resolveTypes(markers, context);
		}

		context.pop();
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		context = context.push(this);

		this.type.resolve(markers, context);

		for (int i = 0; i < this.typeVariableCount; i++)
		{
			this.typeVariables[i].resolve(markers, context);
		}

		context.pop();
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		context = context.push(this);

		this.type.checkType(markers, context, TypePosition.GENERIC_ARGUMENT);

		for (int i = 0; i < this.typeVariableCount; i++)
		{
			this.typeVariables[i].checkTypes(markers, context);
		}

		context.pop();
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		context = context.push(this);

		this.type.check(markers, context);

		for (int i = 0; i < this.typeVariableCount; i++)
		{
			this.typeVariables[i].check(markers, context);
		}

		context.pop();
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
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.type.cleanup(compilableList, classCompilableList);

		for (int i = 0; i < this.typeVariableCount; i++)
		{
			this.typeVariables[i].cleanup(compilableList, classCompilableList);
		}
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		out.writeUTF(this.name.qualified);
		IType.writeType(this.type, out);

		out.writeShort(this.typeVariableCount);

		for (int i = 0; i < this.typeVariableCount; i++)
		{
			this.typeVariables[i].write(out);
		}
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.name = Name.fromRaw(in.readUTF());
		this.type = IType.readType(in);

		this.typeVariableCount = in.readShort();
		this.typeVariables = new ITypeParameter[this.typeVariableCount];

		for (int i = 0; i < this.typeVariableCount; i++)
		{
			ITypeParameter typeVariable = new ExternalTypeParameter(this);
			typeVariable.read(in);
			this.typeVariables[i] = typeVariable;
		}
	}

	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}

	@Override
	public void toString(@NonNull String prefix, @NonNull StringBuilder buffer)
	{
		buffer.append("type ").append(this.name);

		if (this.typeVariableCount > 0)
		{
			Formatting.appendSeparator(buffer, "generics.open_bracket", '<');
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
			buffer.append('>');
		}

		Formatting.appendSeparator(buffer, "field.assignment", '=');
		this.type.toString(prefix, buffer);
	}
}

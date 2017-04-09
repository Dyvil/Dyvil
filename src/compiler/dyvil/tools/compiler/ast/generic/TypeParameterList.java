package dyvil.tools.compiler.ast.generic;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.external.ExternalTypeParameter;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.phase.IResolvable;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class TypeParameterList implements IResolvable
{
	private static final int DEFAULT_CAPACITY = 3;

	private int              size;
	private ITypeParameter[] typeParams;

	public TypeParameterList()
	{
		this.typeParams = new ITypeParameter[DEFAULT_CAPACITY];
	}

	// List Manipulation

	public int size()
	{
		return this.size;
	}

	public ITypeParameter get(int i)
	{
		return this.typeParams[i];
	}

	public ITypeParameter get(Name name)
	{
		for (int i = 0; i < this.size; i++)
		{
			final ITypeParameter typeParameter = this.typeParams[i];
			if (typeParameter.getName() == name)
			{
				return typeParameter;
			}
		}

		return null;
	}

	public void add(ITypeParameter parameter)
	{
		final int index = this.size++;
		if (index >= this.typeParams.length)
		{
			final ITypeParameter[] temp = new ITypeParameter[index + 1];
			System.arraycopy(this.typeParams, 0, temp, 0, index);
			this.typeParams = temp;
		}
		this.typeParams[index] = parameter;
		parameter.setIndex(index);
	}

	public void addAll(TypeParameterList list)
	{
		final int newSize = this.size + list.size;
		if (newSize >= this.typeParams.length)
		{
			final ITypeParameter[] temp = new ITypeParameter[newSize];
			System.arraycopy(this.typeParams, 0, temp, 0, this.size);
			this.typeParams = temp;
		}
		System.arraycopy(list.typeParams, 0, this.typeParams, this.size, list.size);
		this.size = newSize;
	}

	public boolean isMember(IDataMember member)
	{
		for (int i = 0; i < this.size; i++)
		{
			if (this.typeParams[i].getReifyParameter() == member)
			{
				return true;
			}
		}
		return false;
	}

	// Resolution

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.typeParams[i].resolveTypes(markers, context);
		}
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.typeParams[i].resolve(markers, context);
		}
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.typeParams[i].checkTypes(markers, context);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.typeParams[i].check(markers, context);
		}
	}

	@Override
	public void foldConstants()
	{
		for (int i = 0; i < this.size; i++)
		{
			this.typeParams[i].foldConstants();
		}
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.typeParams[i].cleanup(compilableList, classCompilableList);
		}
	}

	// Compilation

	public void appendSignature(StringBuilder buffer)
	{
		if (this.size > 0)
		{
			buffer.append('<');
			for (int i = 0; i < this.size; i++)
			{
				this.typeParams[i].appendSignature(buffer);
			}
			buffer.append('>');
		}
	}

	public void appendParameterDescriptors(StringBuilder buffer)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.typeParams[i].appendParameterDescriptor(buffer);
		}
	}

	public void appendParameterSignatures(StringBuilder buffer)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.typeParams[i].appendParameterSignature(buffer);
		}
	}

	public void write(TypeAnnotatableVisitor visitor)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.typeParams[i].write(visitor);
		}
	}

	public void writeParameters(MethodWriter writer)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.typeParams[i].writeParameter(writer);
		}
	}

	public void writeArguments(MethodWriter writer, ITypeContext typeContext)
	{
		for (int i = 0; i < this.size; i++)
		{
			final ITypeParameter typeParameter = this.typeParams[i];
			typeParameter.writeArgument(writer, typeContext.resolveType(typeParameter));
		}
	}

	public static void write(@Nullable TypeParameterList typeParameters, DataOutput out) throws IOException
	{
		if (typeParameters == null)
		{
			out.writeInt(0);
			return;
		}
		typeParameters.write(out);
	}

	private void write(DataOutput out) throws IOException
	{
		out.writeInt(this.size);

		for (int i = 0; i < this.size; i++)
		{
			this.typeParams[i].write(out);
		}
	}

	public static void read(ITypeParametric generic, DataInput in) throws IOException
	{
		final int size = in.readInt();
		if (size <= 0)
		{
			return;
		}

		final TypeParameterList list = generic.getTypeParameters();
		list.typeParams = new ITypeParameter[size];

		for (int i = 0; i < size; i++)
		{
			final ITypeParameter typeParameter = new ExternalTypeParameter(generic);
			typeParameter.read(in);
			list.add(typeParameter);
		}
	}

	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		if (Util.endsWithSymbol(buffer) || Formatting.getBoolean("generics.open_bracket.space_before"))
		{
			buffer.append(' ');
		}
		buffer.append('<');
		if (Formatting.getBoolean("generics.open_bracket.space_after"))
		{
			buffer.append(' ');
		}

		Util.astToString(indent, this.typeParams, this.size, Formatting.getSeparator("generics.separator", ','),
		                 buffer);
		Formatting.appendSeparator(buffer, "generics.close_bracket", '>');
	}
}

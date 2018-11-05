package dyvilx.tools.compiler.ast.generic;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.lang.Name;
import dyvilx.tools.asm.TypeAnnotatableVisitor;
import dyvilx.tools.compiler.ast.external.ExternalTypeParameter;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.phase.ResolvableList;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.ASTNode;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;

public class TypeParameterList extends ArrayList<ITypeParameter> implements ResolvableList<ITypeParameter>
{
	// =============== Constructors ===============

	public TypeParameterList()
	{
	}

	public TypeParameterList(int capacity)
	{
		super(capacity);
	}

	// =============== Methods ===============

	// --------------- Member Resolution ---------------

	public ITypeParameter get(Name name)
	{
		for (ITypeParameter typeParameter : this)
		{
			if (typeParameter.getName() == name)
			{
				return typeParameter;
			}
		}

		return null;
	}

	public boolean isMember(IDataMember member)
	{
		for (ITypeParameter typeParameter : this)
		{
			if (typeParameter.getReifyParameter() == member)
			{
				return true;
			}
		}
		return false;
	}

	// --------------- Signatures and Descriptors ---------------

	public void appendSignature(StringBuilder buffer)
	{
		if (this.isEmpty())
		{
			return;
		}

		buffer.append('<');
		for (ITypeParameter typeParameter : this)
		{
			typeParameter.appendSignature(buffer);
		}
		buffer.append('>');
	}

	public void appendParameterDescriptors(StringBuilder buffer)
	{
		for (ITypeParameter typeParameter : this)
		{
			typeParameter.appendParameterDescriptor(buffer);
		}
	}

	public void appendParameterSignatures(StringBuilder buffer)
	{
		for (ITypeParameter typeParameter : this)
		{
			typeParameter.appendParameterSignature(buffer);
		}
	}

	// --------------- Compilation ---------------

	public void write(TypeAnnotatableVisitor visitor)
	{
		for (ITypeParameter typeParameter : this)
		{
			typeParameter.write(visitor);
		}
	}

	public void writeParameters(MethodWriter writer)
	{
		for (ITypeParameter typeParameter : this)
		{
			typeParameter.writeParameter(writer);
		}
	}

	public void writeArguments(MethodWriter writer, ITypeContext typeContext)
	{
		for (ITypeParameter typeParameter : this)
		{
			typeParameter.writeArgument(writer, typeContext.resolveType(typeParameter));
		}
	}

	// --------------- Serialization ---------------

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
		final int size = this.size();

		out.writeInt(size);

		//noinspection ForLoopReplaceableByForEach to avoid concurrency problems
		for (int i = 0; i < size; i++)
		{
			this.get(i).write(out);
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
		list.clear();
		list.ensureCapacity(size);

		for (int i = 0; i < size; i++)
		{
			final ITypeParameter typeParameter = new ExternalTypeParameter(generic);
			typeParameter.read(in);
			list.add(typeParameter);
		}
	}

	// --------------- Copying ---------------

	public TypeParameterList elementCopy()
	{
		final TypeParameterList copy = new TypeParameterList(this.size());
		for (ITypeParameter typeParameter : this)
		{
			copy.add(typeParameter.copy());
		}
		return copy;
	}

	// --------------- Formatting ---------------

	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		if (Formatting.endsWithSymbol(buffer) || Formatting.getBoolean("generics.open_bracket.space_before"))
		{
			buffer.append(' ');
		}
		buffer.append('<');
		if (Formatting.getBoolean("generics.open_bracket.space_after"))
		{
			buffer.append(' ');
		}

		Util.astToString(indent, this.toArray(new ASTNode[0]), this.size(),
		                 Formatting.getSeparator("generics.separator", ','), buffer);
		Formatting.appendSeparator(buffer, "generics.close_bracket", '>');
	}
}

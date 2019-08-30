package dyvilx.tools.compiler.ast.type;

import dyvil.annotation.internal.NonNull;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

public class TypeList extends ArrayList<IType> implements Consumer<IType>
{
	// =============== Constructors ===============

	public TypeList()
	{
	}

	public TypeList(int capacity)
	{
		super(capacity);
	}

	public TypeList(IType @NonNull ... elements)
	{
		super(Arrays.asList(elements));
	}

	public TypeList(Collection<? extends IType> elements)
	{
		super(elements);
	}

	@Deprecated
	public TypeList(IType[] types, int size)
	{
		super(size);
		this.addAll(Arrays.asList(types).subList(0, size));
	}

	// =============== Methods ===============

	// --------------- List Operations ---------------

	@Deprecated
	public IType[] getTypes()
	{
		return this.toArray(new IType[0]);
	}

	@Deprecated
	public void setTypes(IType[] types, int size)
	{
		this.clear();
		this.ensureCapacity(size);
		this.addAll(Arrays.asList(types).subList(0, size));
	}

	public TypeList copy()
	{
		return new TypeList(this);
	}

	// --------------- Resolution Phases ---------------

	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.replaceAll(t -> t.resolveType(markers, context));
	}

	public void resolve(MarkerList markers, IContext context)
	{
		for (final IType type : this)
		{
			type.resolve(markers, context);
		}
	}

	public void checkTypes(MarkerList markers, IContext context, int position)
	{
		for (final IType type : this)
		{
			type.checkType(markers, context, position);
		}
	}

	public void check(MarkerList markers, IContext context)
	{
		for (final IType type : this)
		{
			type.check(markers, context);
		}
	}

	public void foldConstants()
	{
		for (final IType type : this)
		{
			type.foldConstants();
		}
	}

	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		for (final IType type : this)
		{
			type.cleanup(compilableList, classCompilableList);
		}
	}

	// --------------- Compilation ---------------

	public String[] getInternalTypeNames()
	{
		return this.stream().map(IType::getInternalName).toArray(String[]::new);
	}

	public void appendDescriptors(StringBuilder buffer, int kind)
	{
		for (final IType type : this)
		{
			type.appendDescriptor(buffer, kind);
		}
	}

	// --------------- Serialization ---------------

	public void write(DataOutput out) throws IOException
	{
		out.writeInt(this.size());
		for (final IType type : this)
		{
			IType.writeType(type, out);
		}
	}

	public void read(DataInput in) throws IOException
	{
		final int size = in.readInt();

		this.clear();
		this.ensureCapacity(size);

		for (int i = 0; i < size; i++)
		{
			this.add(IType.readType(in));
		}
	}

	// --------------- Formatting ---------------

	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		this.toString(indent, buffer, '<', '>');
	}

	public void toString(@NonNull String indent, @NonNull StringBuilder buffer, char open, char close)
	{
		Formatting.appendSeparator(buffer, "type.list.open_paren", open);

		final String sep = Formatting.getSeparator("type.list.separator", ',');
		Util.astToString(indent, this.getTypes(), this.size(), sep, buffer);

		Formatting.appendClose(buffer, "type.list.close_paren", close);
	}

	// --------------- Consumer<IType> Implementation ---------------

	@Override
	public void accept(IType type)
	{
		this.add(type);
	}
}

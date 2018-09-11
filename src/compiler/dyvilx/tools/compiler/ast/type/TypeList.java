package dyvilx.tools.compiler.ast.type;

import dyvil.annotation.internal.NonNull;
import dyvil.collection.iterator.ArrayIterator;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;
import java.util.function.Consumer;

public class TypeList implements Iterable<IType>, Consumer<IType>
{
	// --------------- Constants ---------------

	private static final int DEFAULT_CAPACITY = 3;

	public static final TypeList EMPTY = new TypeList(null, 0);

	// --------------- Instance Fields ---------------

	private int     size;
	private IType[] types;

	// --------------- Constructors ---------------

	public TypeList()
	{
		this(DEFAULT_CAPACITY);
	}

	public TypeList(int capacity)
	{
		this.types = new IType[capacity];
	}

	public TypeList(IType @NonNull ... elements)
	{
		this(elements, elements.length);
	}

	public TypeList(IType[] types, int size)
	{
		this.size = size;
		this.types = types;
	}

	// --------------- List Operations ---------------

	public int size()
	{
		return this.size;
	}

	public IType get(int index)
	{
		return index >= this.size ? null : this.types[index];
	}

	public IType[] getTypes()
	{
		return this.types;
	}

	public void set(int index, IType type)
	{
		if (index >= this.types.length)
		{
			IType[] temp = new IType[index + 1];
			System.arraycopy(this.types, 0, temp, 0, index);
			this.types = temp;
		}
		if (index >= this.size)
		{
			this.size = index + 1;
		}

		this.types[index] = type;
	}

	public void setTypes(IType[] types, int size)
	{
		this.types = types;
		this.size = size;
	}

	public void add(IType type)
	{
		this.set(this.size, type);
	}

	public TypeList copy()
	{
		return new TypeList(this.types.clone(), this.size);
	}

	// --------------- Resolution Phases ---------------

	public void resolveTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.types[i] = this.types[i].resolveType(markers, context);
		}
	}

	public void resolve(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.types[i].resolve(markers, context);
		}
	}

	public void checkTypes(MarkerList markers, IContext context, int position)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.types[i].checkType(markers, context, position);
		}
	}

	public void check(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.types[i].check(markers, context);
		}
	}

	public void foldConstants()
	{
		for (int i = 0; i < this.size; i++)
		{
			this.types[i].foldConstants();
		}
	}

	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.types[i].cleanup(compilableList, classCompilableList);
		}
	}

	// --------------- Compilation ---------------

	public String[] getInternalTypeNames()
	{
		final String[] array = new String[this.size];
		for (int i = 0; i < this.size; i++)
		{
			array[i] = this.types[i].getInternalName();
		}
		return array;
	}

	public void appendDescriptors(StringBuilder buffer, int type)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.types[i].appendDescriptor(buffer, type);
		}
	}

	// --------------- Serialization ---------------

	public void write(DataOutput out) throws IOException
	{
		out.writeInt(this.size);
		for (int i = 0; i < this.size; i++)
		{
			IType.writeType(this.types[i], out);
		}
	}

	public void read(DataInput in) throws IOException
	{
		final int size = this.size = in.readInt();

		this.types = new IType[size];
		for (int i = 0; i < size; i++)
		{
			this.types[i] = IType.readType(in);
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

		Util.astToString(indent, this.types, this.size, Formatting.getSeparator("type.list.separator", ','), buffer);

		Formatting.appendClose(buffer, "type.list.close_paren", close);
	}

	// ------------------------------ Iterable<IType> Implementation ------------------------------

	@Override
	public Iterator<IType> iterator()
	{
		return new ArrayIterator<>(this.types, 0, this.size);
	}

	// ------------------------------ Consumer<IType> Implementation ------------------------------

	@Override
	public void accept(IType type)
	{
		this.add(type);
	}
}

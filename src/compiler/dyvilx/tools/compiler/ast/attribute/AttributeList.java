package dyvilx.tools.compiler.ast.attribute;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.collection.iterator.ArrayIterator;
import dyvilx.tools.asm.AnnotatableVisitor;
import dyvilx.tools.asm.TypeAnnotatableVisitor;
import dyvilx.tools.asm.TypePath;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.attribute.annotation.ExternalAnnotation;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.util.Iterator;

public class AttributeList implements Iterable<Attribute>
{
	protected Attribute @Nullable [] data;

	protected int size;
	protected long flags;

	public AttributeList()
	{
	}

	public AttributeList(int capacity)
	{
		this.data = new Attribute[capacity];
	}

	public static AttributeList of(long flags)
	{
		AttributeList list = new AttributeList();
		list.flags = flags;
		return list;
	}

	public int size()
	{
		return this.size;
	}

	public boolean isEmpty()
	{
		return this.size == 0;
	}

	@Override
	public Iterator<Attribute> iterator()
	{
		return new ArrayIterator<>(this.data, 0, this.size);
	}

	// Flags

	public long flags()
	{
		return this.flags;
	}

	public boolean hasFlag(long flag)
	{
		return (this.flags & flag) == flag;
	}

	public boolean hasAnyFlag(long flag)
	{
		return (this.flags & flag) != 0;
	}

	// Attributes and Annotations

	public Attribute get(int index)
	{
		if (this.data == null || index >= this.size)
		{
			return null;
		}
		return this.data[index];
	}

	public Annotation getAnnotation(IClass type)
	{
		for (int i = 0; i < this.size; i++)
		{
			final Attribute attribute = this.data[i];
			final IType attributeType = attribute.getType();

			if (attributeType != null && attributeType.getTheClass() == type)
			{
				return (Annotation) attribute;
			}
		}
		return null;
	}

	public final void add(Attribute attribute)
	{
		this.ensureCapacity(this.size + 1);
		this.data[this.size++] = attribute;
		this.flags |= attribute.flags();
	}

	public void addFlag(long modifier)
	{
		this.flags |= modifier;
	}

	private void ensureCapacity(int capacity)
	{
		if (this.data == null)
		{
			this.data = new Attribute[capacity];
			return;
		}
		if (capacity > this.data.length)
		{
			final Attribute[] temp = new Attribute[capacity];
			System.arraycopy(this.data, 0, temp, 0, this.size);
			this.data = temp;
		}
	}

	public void addAll(AttributeList list)
	{
		this.ensureCapacity(this.size + list.size);
		System.arraycopy(list.data, 0, this.data, this.size, list.size);
		this.flags |= list.flags;
	}

	public void addAnnotations(AttributeList list)
	{
		this.ensureCapacity(this.size + list.size);
		for (int i = 0; i < list.size; i++)
		{
			final Attribute attribute = list.data[i];
			if (attribute.getType() != null)
			{
				this.add(attribute);
			}
		}
	}

	private void removeAt(int index)
	{
		if (this.data == null || index >= this.size)
		{
			return;
		}

		final int moved = this.size - index - 1;
		if (moved > 0)
		{
			System.arraycopy(this.data, index + 1, this.data, index, moved);
		}
		this.data[--this.size] = null;
	}

	public AttributeList filtered(long mask)
	{
		AttributeList copy = new AttributeList(this.size);
		for (int i = 0; i < this.size; i++)
		{
			final Attribute attribute = this.data[i];
			final long flags = attribute.flags();
			if (flags == 0 || (flags & mask) != 0)
			{
				copy.add(attribute);
			}
		}
		copy.flags |= this.flags & mask;
		return copy;
	}

	public AttributeList annotations()
	{
		AttributeList copy = new AttributeList();
		copy.addAnnotations(this);
		return copy;
	}

	// Phases

	public void resolveTypes(MarkerList markers, IContext context, Attributable annotated)
	{
		for (int i = 0; i < this.size; i++)
		{
			final Attribute attribute = this.data[i];
			attribute.resolveTypes(markers, context);

			final IType type = attribute.getType();
			if (type != null)
			{
				final String internalName = type.getInternalName();
				if (internalName != null && annotated.skipAnnotation(internalName, (Annotation) attribute))
				{
					this.removeAt(i--);
				}
			}
		}
	}

	public void resolve(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.data[i].resolve(markers, context);
		}
	}

	public void checkTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.data[i].checkTypes(markers, context);
		}
	}

	public void check(MarkerList markers, IContext context, ElementType target)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.data[i].check(markers, context, target);
		}
	}

	public void foldConstants()
	{
		for (int i = 0; i < this.size; i++)
		{
			this.data[i].foldConstants();
		}
	}

	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.data[i].cleanup(compilableList, classCompilableList);
		}
	}

	// Compilation

	public void write(AnnotatableVisitor writer)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.data[i].write(writer);
		}
	}

	public void write(TypeAnnotatableVisitor visitor, int typeRef, TypePath typePath)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.data[i].write(visitor, typeRef, typePath);
		}
	}

	public static void write(AttributeList list, DataOutput out) throws IOException
	{
		if (list == null)
		{
			out.writeInt(0);
			out.writeShort(0);
			return;
		}

		int annotations = 0;
		// count the number of annotations
		for (int i = 0; i < list.size; i++)
		{
			if (list.data[i].getType() != null)
			{
				annotations++;
			}
		}

		out.writeInt(Math.toIntExact(list.flags)); // FIXME
		out.writeShort(annotations);
		for (int i = 0; i < annotations; i++)
		{
			list.data[i].write(out);
		}
	}

	public static AttributeList read(DataInput in) throws IOException
	{
		final int flags = in.readInt(); // FIXME
		final int annotations = in.readShort();
		final AttributeList list = new AttributeList(annotations);

		list.flags = flags;
		list.size = annotations;

		for (int i = 0; i < annotations; i++)
		{
			final Annotation annotation = new ExternalAnnotation();
			list.data[i] = annotation;
			annotation.read(in);
		}

		return list;
	}

	public void toInlineString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.data[i].toString(indent, buffer);
			buffer.append(' ');
		}
	}

	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		int i = 0;

		// add a newline after all leading attributes without an int flag
		// as soon as we encounter one, we use spaces.

		for (; i < this.size; i++)
		{
			final Attribute attribute = this.data[i];
			attribute.toString(indent, buffer);

			if (attribute.flags() != 0)
			{
				buffer.append(' ');
				i++;
				break; // continue in the other loop, that does not use newlines
			}

			buffer.append('\n').append(indent);
		}

		for (; i < this.size; i++)
		{
			this.data[i].toString(indent, buffer);
			buffer.append(' ');
		}
	}
}

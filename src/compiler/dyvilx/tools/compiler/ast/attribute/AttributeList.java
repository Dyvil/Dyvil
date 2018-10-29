package dyvilx.tools.compiler.ast.attribute;

import dyvil.annotation.internal.NonNull;
import dyvilx.tools.asm.AnnotatableVisitor;
import dyvilx.tools.asm.TypeAnnotatableVisitor;
import dyvilx.tools.asm.TypePath;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.attribute.annotation.ExternalAnnotation;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.phase.ResolvableList;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.util.ArrayList;

public class AttributeList extends ArrayList<Attribute> implements ResolvableList<Attribute>
{
	// =============== Fields ===============

	private long customFlags;

	private int  cachedAttributeFlagsModCount;
	private long cachedAttributeFlags;

	// =============== Constructors ===============

	public AttributeList()
	{
	}

	public AttributeList(int capacity)
	{
		super(capacity);
	}

	// =============== Static Methods ===============

	public static AttributeList of(long flags)
	{
		AttributeList list = new AttributeList();
		list.customFlags = flags;
		return list;
	}

	// =============== Properties ===============

	public long flags()
	{
		return this.customFlags | this.getAttributeFlags();
	}

	public long getAttributeFlags()
	{
		if (this.modCount == this.cachedAttributeFlagsModCount)
		{
			return this.cachedAttributeFlags;
		}

		long attributeFlags = 0;
		for (Attribute attribute : this)
		{
			attributeFlags |= attribute.flags();
		}

		this.cachedAttributeFlagsModCount = this.modCount;
		this.cachedAttributeFlags = attributeFlags;
		return attributeFlags;
	}

	public long getCustomFlags()
	{
		return this.customFlags;
	}

	// =============== Methods ===============

	// --------------- Flag Queries ---------------

	public boolean hasFlag(long flag)
	{
		return (this.flags() & flag) == flag;
	}

	public boolean hasAnyFlag(long flag)
	{
		return (this.flags() & flag) != 0;
	}

	// --------------- Custom Flags ---------------

	public void addFlag(long flag)
	{
		this.customFlags |= flag;
	}

	public void removeFlag(long flag)
	{
		this.customFlags &= ~flag;
	}

	// --------------- Annotations ---------------

	public Annotation getAnnotation(IClass type)
	{
		for (Attribute attribute : this)
		{
			final IType attributeType = attribute.getType();
			if (attributeType != null && attributeType.getTheClass() == type)
			{
				return (Annotation) attribute;
			}
		}
		return null;
	}

	public void addAnnotations(AttributeList list)
	{
		for (Attribute attr : list)
		{
			if (attr.getType() != null)
			{
				this.add(attr);
			}
		}
	}

	// --------------- Filtered Copies ---------------

	public AttributeList annotations()
	{
		AttributeList copy = new AttributeList();
		copy.addAnnotations(this);
		return copy;
	}

	public AttributeList filtered(long mask)
	{
		AttributeList copy = new AttributeList(this.size());
		for (Attribute attribute : this)
		{
			final long flags = attribute.flags();
			if (flags == 0 || (flags & mask) != 0)
			{
				copy.add(attribute);
			}
		}
		copy.customFlags |= this.customFlags & mask;
		return copy;
	}

	// --------------- Resolution Phases ---------------

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		throw new UnsupportedOperationException("use resolveTypes(MarkerList, IContext, Attributable) instead");
	}

	public void resolveTypes(MarkerList markers, IContext context, Attributable annotated)
	{
		ResolvableList.super.resolveTypes(markers, context);

		this.removeIf(attribute -> {
			final IType type = attribute.getType();
			if (type != null)
			{
				final String internalName = type.getInternalName();
				return internalName != null && annotated.skipAnnotation(internalName, (Annotation) attribute);
			}
			return false;
		});
	}

	// --------------- Diagnostic Phases ---------------

	@Override
	public void check(MarkerList markers, IContext context)
	{
		throw new UnsupportedOperationException("use check(MarkerList, IContext, ElementType) instead");
	}

	public void check(MarkerList markers, IContext context, ElementType target)
	{
		for (Attribute attribute : this)
		{
			attribute.check(markers, context, target);
		}
	}

	// --------------- Compilation ---------------

	public void write(AnnotatableVisitor writer)
	{
		for (Attribute attribute : this)
		{
			attribute.write(writer);
		}
	}

	public void write(TypeAnnotatableVisitor visitor, int typeRef, TypePath typePath)
	{
		for (Attribute attribute : this)
		{
			attribute.write(visitor, typeRef, typePath);
		}
	}

	// --------------- Serialization ---------------

	public static void write(AttributeList list, DataOutput out) throws IOException
	{
		if (list == null)
		{
			out.writeLong(0);
			out.writeShort(0);
			return;
		}

		final AttributeList annotations = list.annotations();
		final int numAnnotations = annotations.size();

		out.writeLong(list.customFlags);
		out.writeShort(numAnnotations);

		//noinspection ForLoopReplaceableByForEach to avoid concurrency problems
		for (int i = 0; i < numAnnotations; i++)
		{
			annotations.get(i).write(out);
		}
	}

	public static AttributeList read(DataInput in) throws IOException
	{
		final long flags = in.readLong();
		final int annotations = in.readShort();
		final AttributeList list = new AttributeList(annotations);

		list.customFlags = flags;
		list.ensureCapacity(annotations);

		for (int i = 0; i < annotations; i++)
		{
			final Annotation annotation = new ExternalAnnotation();
			list.add(annotation);
			annotation.read(in);
		}

		return list;
	}

	// --------------- Formatting ---------------

	public void toInlineString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		for (Attribute attribute : this)
		{
			attribute.toString(indent, buffer);
			buffer.append(' ');
		}
	}

	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		int i = 0;
		final int size = this.size();

		// add a newline after all leading attributes without flags
		// as soon as we encounter one, we use spaces.

		while (i < size)
		{
			final Attribute attribute = this.get(i++);
			attribute.toString(indent, buffer);

			if (attribute.flags() != 0)
			{
				buffer.append(' ');
				break; // continue in the other loop, that does not use newlines
			}

			buffer.append('\n').append(indent);
		}

		while (i < size)
		{
			this.get(i++).toString(indent, buffer);
			buffer.append(' ');
		}
	}
}

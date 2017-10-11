package dyvilx.tools.compiler.ast.attribute;

import dyvil.annotation.internal.NonNull;
import dyvilx.tools.asm.AnnotatableVisitor;
import dyvilx.tools.compiler.ast.annotation.Annotation;
import dyvilx.tools.compiler.ast.annotation.IAnnotation;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.consumer.IAnnotationConsumer;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;

public class AttributeList implements IAnnotationConsumer
{
	protected IAnnotation[] annotations;
	protected int           size;

	public AttributeList()
	{
		this.annotations = new IAnnotation[2];
	}

	public AttributeList(int capacity)
	{
		this.annotations = new IAnnotation[capacity];
	}

	public int size()
	{
		return this.size;
	}

	public IAnnotation[] getArray()
	{
		return this.annotations;
	}

	public IAnnotation get(int index)
	{
		if (this.annotations == null)
		{
			return null;
		}
		return this.annotations[index];
	}

	public IAnnotation get(IClass type)
	{
		if (this.annotations == null)
		{
			return null;
		}
		for (int i = 0; i < this.size; i++)
		{
			IAnnotation a = this.annotations[i];
			if (a.getType().getTheClass() == type)
			{
				return a;
			}
		}
		return null;
	}

	public final void add(IAnnotation annotation)
	{
		this.ensureCapacity(this.size + 1);
		this.annotations[this.size++] = annotation;
	}

	private void ensureCapacity(int capacity)
	{
		if (capacity > this.annotations.length)
		{
			final IAnnotation[] temp = new IAnnotation[capacity];
			System.arraycopy(this.annotations, 0, temp, 0, this.size);
			this.annotations = temp;
		}
	}

	public void addAll(AttributeList list)
	{
		this.ensureCapacity(this.size + list.size);
		System.arraycopy(list.annotations, 0, this.annotations, this.size, list.size);
	}

	@Override
	public void setAnnotation(IAnnotation annotation)
	{
		this.add(annotation);
	}

	public final void remoteAt(int index)
	{
		int numMoved = this.size - index - 1;
		if (numMoved > 0)
		{
			System.arraycopy(this.annotations, index + 1, this.annotations, index, numMoved);
		}
		this.annotations[--this.size] = null;
	}

	// Phases

	public void resolveTypes(MarkerList markers, IContext context, Attributable annotated)
	{
		for (int i = 0; i < this.size; i++)
		{
			final IAnnotation annotation = this.annotations[i];
			annotation.resolveTypes(markers, context);

			final String internalName = annotation.getType().getInternalName();
			if (internalName != null && !annotated.addRawAnnotation(internalName, annotation))
			{
				this.remoteAt(i--);
			}
		}
	}

	public void resolve(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.annotations[i].resolve(markers, context);
		}
	}

	public void checkTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.annotations[i].checkTypes(markers, context);
		}
	}

	public void check(MarkerList markers, IContext context, ElementType target)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.annotations[i].check(markers, context, target);
		}
	}

	public void foldConstants()
	{
		for (int i = 0; i < this.size; i++)
		{
			this.annotations[i].foldConstants();
		}
	}

	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.annotations[i].cleanup(compilableList, classCompilableList);
		}
	}

	// Compilation

	public void write(AnnotatableVisitor writer)
	{
		for (int i = 0, count = this.size; i < count; i++)
		{
			this.annotations[i].write(writer);
		}
	}

	public static void write(AttributeList list, DataOutput out) throws IOException
	{
		if (list == null)
		{
			out.writeShort(0);
			return;
		}

		int annotations = list.size;
		out.writeShort(annotations);
		for (int i = 0; i < annotations; i++)
		{
			list.annotations[i].write(out);
		}
	}

	public static AttributeList read(DataInput in) throws IOException
	{
		int annotations = in.readShort();

		if (annotations == 0)
		{
			return null;
		}

		AttributeList list = new AttributeList(annotations);
		list.size = annotations;
		for (int i = 0; i < annotations; i++)
		{
			Annotation a = new Annotation();
			list.annotations[i] = a;
			a.read(in);
		}

		return list;
	}

	public void toInlineString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.annotations[i].toString(indent, buffer);
			buffer.append(' ');
		}
	}

	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.annotations[i].toString(indent, buffer);
			buffer.append('\n').append(indent);
		}
	}
}

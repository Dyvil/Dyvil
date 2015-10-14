package dyvil.tools.compiler.ast.annotation;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.parsing.marker.MarkerList;

public class AnnotationList
{
	protected IAnnotation[]	annotations;
	protected int			annotationCount;
	
	public AnnotationList()
	{
		this.annotations = new IAnnotation[2];
	}
	
	public AnnotationList(int capacity)
	{
		this.annotations = new IAnnotation[capacity];
	}
	
	public int annotationCount()
	{
		return this.annotationCount;
	}
	
	public void setAnnotations(IAnnotation[] annotations, int count)
	{
		this.annotations = annotations;
		this.annotationCount = count;
	}
	
	public void setAnnotation(int index, IAnnotation annotation)
	{
		this.annotations[index] = annotation;
	}
	
	public final void addAnnotation(IAnnotation annotation)
	{
		if (this.annotations == null)
		{
			this.annotations = new IAnnotation[3];
			this.annotations[0] = annotation;
			this.annotationCount = 1;
			return;
		}
		
		int index = this.annotationCount++;
		if (this.annotationCount > this.annotations.length)
		{
			IAnnotation[] temp = new IAnnotation[this.annotationCount];
			System.arraycopy(this.annotations, 0, temp, 0, index);
			this.annotations = temp;
		}
		this.annotations[index] = annotation;
	}
	
	public final void removeAnnotation(int index)
	{
		int numMoved = this.annotationCount - index - 1;
		if (numMoved > 0)
		{
			System.arraycopy(this.annotations, index + 1, this.annotations, index, numMoved);
		}
		this.annotations[--this.annotationCount] = null;
	}
	
	public IAnnotation[] getAnnotations()
	{
		return this.annotations;
	}
	
	public IAnnotation getAnnotation(int index)
	{
		if (this.annotations == null)
		{
			return null;
		}
		return this.annotations[index];
	}
	
	public IAnnotation getAnnotation(IClass type)
	{
		if (this.annotations == null)
		{
			return null;
		}
		for (int i = 0; i < this.annotationCount; i++)
		{
			IAnnotation a = this.annotations[i];
			if (a.getType().getTheClass() == type)
			{
				return a;
			}
		}
		return null;
	}
	
	public void resolveTypes(MarkerList markers, IContext context, IAnnotated annotated)
	{
		for (int i = 0; i < this.annotationCount; i++)
		{
			IAnnotation a = this.annotations[i];
			String fullName = a.getType().getInternalName();
			if (fullName != null && !annotated.addRawAnnotation(fullName, a))
			{
				this.removeAnnotation(i--);
				continue;
			}
			
			this.annotations[i].resolveTypes(markers, context);
		}
	}
	
	public void resolve(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].resolve(markers, context);
		}
	}
	
	public void checkTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].checkTypes(markers, context);
		}
	}
	
	public void check(MarkerList markers, IContext context, ElementType target)
	{
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].check(markers, context, target);
		}
	}
	
	public void foldConstants()
	{
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].foldConstants();
		}
	}
	
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].cleanup(context, compilableList);
		}
	}
	
	public static void write(AnnotationList list, DataOutput out) throws IOException
	{
		if (list == null)
		{
			out.writeShort(0);
			return;
		}
		
		int annotations = list.annotationCount;
		out.writeShort(annotations);
		for (int i = 0; i < annotations; i++)
		{
			list.annotations[i].write(out);
		}
	}
	
	public static AnnotationList read(DataInput in) throws IOException
	{
		int annotations = in.readShort();
		
		if (annotations == 0)
		{
			return null;
		}
		
		AnnotationList list = new AnnotationList(annotations);
		list.annotationCount = annotations;
		for (int i = 0; i < annotations; i++)
		{
			Annotation a = new Annotation();
			list.annotations[i] = a;
			a.read(in);
		}
		
		return list;
	}
	
	public void toString(String prefix, StringBuilder buffer)
	{
		for (int i = 0; i < this.annotationCount; i++)
		{
			buffer.append(prefix);
			this.annotations[i].toString(prefix, buffer);
			buffer.append('\n');
		}
	}
}

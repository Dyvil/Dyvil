package dyvil.tools.compiler.ast.member;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public abstract class Member implements IMember
{
	protected ICodePosition position;
	
	protected Annotation[]	annotations;
	protected int			annotationCount;
	
	protected int modifiers;
	
	protected IType	type;
	protected Name	name;
	
	protected Member()
	{
	}
	
	protected Member(Name name)
	{
		this.name = name;
	}
	
	public Member(IType type)
	{
		this.type = type;
	}
	
	public Member(Name name, IType type)
	{
		this.name = name;
		this.type = type;
	}
	
	public Member(Name name, IType type, int modifiers)
	{
		this.name = name;
		this.type = type;
		this.modifiers = modifiers;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}
	
	@Override
	public int annotationCount()
	{
		return this.annotationCount;
	}
	
	@Override
	public void setAnnotations(Annotation[] annotations, int count)
	{
		this.annotations = annotations;
		this.annotationCount = count;
	}
	
	@Override
	public void setAnnotation(int index, Annotation annotation)
	{
		this.annotations[index] = annotation;
	}
	
	@Override
	public final void addAnnotation(Annotation annotation)
	{
		if (this.annotations == null)
		{
			this.annotations = new Annotation[3];
			this.annotations[0] = annotation;
			this.annotationCount = 1;
			return;
		}
		
		int index = this.annotationCount++;
		if (this.annotationCount > this.annotations.length)
		{
			Annotation[] temp = new Annotation[this.annotationCount];
			System.arraycopy(this.annotations, 0, temp, 0, index);
			this.annotations = temp;
		}
		this.annotations[index] = annotation;
	}
	
	@Override
	public final void removeAnnotation(int index)
	{
		int numMoved = this.annotationCount - index - 1;
		if (numMoved > 0)
		{
			System.arraycopy(this.annotations, index + 1, this.annotations, index, numMoved);
		}
		this.annotations[--this.annotationCount] = null;
	}
	
	@Override
	public Annotation[] getAnnotations()
	{
		return this.annotations;
	}
	
	@Override
	public Annotation getAnnotation(int index)
	{
		if (this.annotations == null)
		{
			return null;
		}
		return this.annotations[index];
	}
	
	@Override
	public Annotation getAnnotation(IClass type)
	{
		if (this.annotations == null)
		{
			return null;
		}
		for (int i = 0; i < this.annotationCount; i++)
		{
			Annotation a = this.annotations[i];
			if (a.getType().getTheClass() == type)
			{
				return a;
			}
		}
		return null;
	}
	
	@Override
	public void setModifiers(int modifiers)
	{
		this.modifiers = modifiers;
	}
	
	@Override
	public int getModifiers()
	{
		return this.modifiers;
	}
	
	@Override
	public boolean addModifier(int mod)
	{
		boolean flag = (this.modifiers & mod) != 0;
		this.modifiers |= mod;
		return flag;
	}
	
	@Override
	public void removeModifier(int mod)
	{
		this.modifiers &= ~mod;
	}
	
	@Override
	public int getAccessLevel()
	{
		return this.modifiers & Modifiers.ACCESS_MODIFIERS;
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
	public boolean hasModifier(int mod)
	{
		return (this.modifiers & mod) == mod;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.type != null)
		{
			this.type = this.type.resolve(markers, context, TypePosition.RETURN_TYPE);
		}
		
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].resolveTypes(markers, context);
		}
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.annotationCount; i++)
		{
			Annotation a = this.annotations[i];
			String fullName = a.getType().getInternalName();
			if (fullName != null && !this.addRawAnnotation(fullName))
			{
				this.removeAnnotation(i--);
				continue;
			}
			
			a.resolve(markers, context);
		}
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].checkTypes(markers, context);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		ElementType target = this.getAnnotationType();
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].check(markers, context, target);
		}
	}
	
	@Override
	public void foldConstants()
	{
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].foldConstants();
		}
	}
	
	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].cleanup(context, compilableList);
		}
	}
	
	public void write(DataOutput out) throws IOException
	{
		this.writeSignature(out);
		out.writeUTF(this.name.unqualified);
		this.writeAnnotations(out);
	}

	public void writeSignature(DataOutput out) throws IOException
	{
		IType.writeType(this.type, out);
	}
	
	protected void writeAnnotations(DataOutput out) throws IOException
	{
		out.writeInt(this.modifiers);
		
		int annotations = this.annotationCount;
		out.writeShort(annotations);
		for (int i = 0; i < annotations; i++)
		{
			this.annotations[i].write(out);
		}
	}
	
	public void read(DataInput in) throws IOException
	{
		this.readSignature(in);
		this.name = Name.get(in.readUTF());
		this.readAnnotations(in);
	}

	public void readSignature(DataInput in) throws IOException
	{
		this.type = IType.readType(in);
	}
	
	protected void readAnnotations(DataInput in) throws IOException
	{
		this.modifiers = in.readInt();
		
		int annotations = in.readShort();
		this.annotations = new Annotation[annotations];
		this.annotationCount = annotations;
		for (int i = 0; i < annotations; i++)
		{
			Annotation a = new Annotation();
			this.annotations[i] = a;
			a.read(in);
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		this.toString("", builder);
		return builder.toString();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		for (int i = 0; i < this.annotationCount; i++)
		{
			buffer.append(prefix);
			this.annotations[i].toString(prefix, buffer);
			buffer.append('\n');
		}
		
		buffer.append(prefix);
	}
}

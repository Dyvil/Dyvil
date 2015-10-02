package dyvil.tools.compiler.ast.member;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
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
	
	protected AnnotationList annotations;
	
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
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public AnnotationList getAnnotations()
	{
		return this.annotations;
	}
	
	@Override
	public IAnnotation getAnnotation(IClass type)
	{
		return this.annotations == null ? null : this.annotations.getAnnotation(type);
	}
	
	@Override
	public void setAnnotations(AnnotationList annotations)
	{
		this.annotations = annotations;
	}
	
	@Override
	public void addAnnotation(IAnnotation annotation)
	{
		if (this.annotations == null)
		{
			this.annotations = new AnnotationList();
		}
		this.annotations.addAnnotation(annotation);
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
			this.type = this.type.resolveType(markers, context);
		}
		if (this.annotations != null)
		{
			this.annotations.resolveTypes(markers, context, this);
		}
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		if (this.type != null)
		{
			this.type.resolve(markers, context);
		}
		if (this.annotations != null)
		{
			this.annotations.resolve(markers, context);
		}
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.type != null)
		{
			this.type.checkType(markers, context, TypePosition.RETURN_TYPE);
		}
		if (this.annotations != null)
		{
			this.annotations.checkTypes(markers, context);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.type != null)
		{
			this.type.check(markers, context);
		}
		if (this.annotations != null)
		{
			this.annotations.check(markers, context, this.getElementType());
		}
	}
	
	@Override
	public void foldConstants()
	{
		if (this.type != null)
		{
			this.type.foldConstants();
		}
		if (this.annotations != null)
		{
			this.annotations.foldConstants();
		}
	}
	
	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		if (this.annotations != null)
		{
			this.annotations.cleanup(context, compilableList);
		}
		if (this.type != null)
		{
			this.type.cleanup(context, compilableList);
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
		
		AnnotationList.write(this.annotations, out);
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
		this.annotations = AnnotationList.read(in);
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
		if (this.annotations != null)
		{
			this.annotations.toString(prefix, buffer);
		}
		buffer.append(prefix);
	}
}

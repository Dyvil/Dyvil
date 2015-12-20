package dyvil.tools.compiler.ast.member;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.modifiers.ModifierList;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class Member implements IMember
{
	protected ICodePosition position;
	
	protected ModifierSet    modifiers;
	protected AnnotationList annotations;

	protected IType type;
	protected Name  name;
	
	protected Member()
	{
	}
	
	protected Member(Name name)
	{
		this.name = name;
		this.modifiers = new ModifierList();
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
	
	public Member(Name name, IType type, ModifierSet modifiers)
	{
		this.name = name;
		this.type = type;
		this.modifiers = modifiers;
	}

	public Member(ICodePosition position, Name name, IType type, ModifierSet modifiers)
	{
		this.position = position;
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
	public void setModifiers(ModifierSet modifiers)
	{
		this.modifiers = modifiers;
	}
	
	@Override
	public ModifierSet getModifiers()
	{
		return this.modifiers;
	}
	
	@Override
	public int getAccessLevel()
	{
		return this.modifiers.toFlags() & Modifiers.ACCESS_MODIFIERS;
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
		ModifierSet.write(this.modifiers, out);
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
		this.modifiers = ModifierSet.read(in);
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

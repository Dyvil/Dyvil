package dyvil.tools.compiler.ast.structure;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;

import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.IAnnotated;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.member.IModified;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.backend.IObjectCompilable;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.ModifierTypes;

public class HeaderDeclaration implements IASTNode, INamed, IModified, IAnnotated, IObjectCompilable
{
	protected final IDyvilHeader header;
	
	protected ICodePosition position;
	
	protected AnnotationList	annotations;
	protected int				modifiers;
	
	protected Name name;
	
	public HeaderDeclaration(IDyvilHeader header)
	{
		this.header = header;
	}
	
	public HeaderDeclaration(IDyvilHeader header, ICodePosition position, Name name)
	{
		this.header = header;
		this.position = position;
		this.name = name;
	}
	
	public HeaderDeclaration(IDyvilHeader header, ICodePosition position, Name name, int modifiers, AnnotationList annotations)
	{
		this.header = header;
		this.position = position;
		this.name = name;
		this.modifiers = modifiers;
		this.annotations = annotations;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}
	
	@Override
	public AnnotationList getAnnotations()
	{
		return this.annotations;
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
	public IAnnotation getAnnotation(IClass type)
	{
		return this.annotations == null ? null : this.annotations.getAnnotation(type);
	}
	
	@Override
	public ElementType getElementType()
	{
		return ElementType.PACKAGE;
	}
	
	@Override
	public void setModifiers(int modifiers)
	{
		this.modifiers = modifiers;
	}
	
	@Override
	public boolean addModifier(int mod)
	{
		if ((this.modifiers & mod) == mod)
		{
			return false;
		}
		this.modifiers |= mod;
		return true;
	}
	
	@Override
	public void removeModifier(int mod)
	{
		this.modifiers &= ~mod;
	}
	
	@Override
	public int getModifiers()
	{
		return this.modifiers;
	}
	
	@Override
	public boolean hasModifier(int mod)
	{
		return (this.modifiers & mod) == mod;
	}
	
	@Override
	public Name getName()
	{
		return this.name;
	}
	
	@Override
	public void setName(Name name)
	{
		this.name = name;
	}
	
	public void check(MarkerList markers)
	{
		Name headerName = this.header.getName();
		if (headerName != this.name)
		{
			Marker m = markers.create(this.position, "header.name");
			m.addInfo("Header Name: " + headerName);
			m.addInfo("Header Declaration Name: " + this.name);
		}
	}
	
	@Override
	public void write(DataOutput out) throws IOException
	{
		out.writeUTF(this.name.unqualified);
		out.writeInt(this.modifiers);
		
		if (this.annotations == null)
		{
			out.writeShort(0);
			return;
		}
		
		int count = this.annotations.annotationCount();
		out.writeShort(count);
		for (int i = 0; i < count; i++)
		{
			this.annotations.getAnnotation(i).write(out);
		}
	}
	
	@Override
	public void read(DataInput in) throws IOException
	{
		this.name = Name.get(in.readUTF());
		this.modifiers = in.readInt();
		
		int count = in.readShort();
		if (count == 0)
		{
			this.annotations = null;
			return;
		}
		
		this.annotations = new AnnotationList(count);
		for (int i = 0; i < count; i++)
		{
			Annotation a = new Annotation();
			a.read(in);
			this.annotations.addAnnotation(a);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.annotations != null)
		{
			this.annotations.toString(prefix, buffer);
		}
		buffer.append(ModifierTypes.ACCESS.toString(this.modifiers));
		buffer.append("header ").append(this.name);
	}
}

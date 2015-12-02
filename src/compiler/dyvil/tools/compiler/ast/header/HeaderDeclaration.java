package dyvil.tools.compiler.ast.header;

import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.IAnnotated;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.modifiers.IModified;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.backend.IObjectCompilable;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;

public class HeaderDeclaration implements IASTNode, INamed, IModified, IAnnotated, IObjectCompilable
{
	protected final IDyvilHeader header;
	
	protected ICodePosition position;
	
	protected AnnotationList annotations;
	protected ModifierSet    modifiers;
	
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
	
	public HeaderDeclaration(IDyvilHeader header, ICodePosition position, Name name, ModifierSet modifiers, AnnotationList annotations)
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
			Marker m = I18n.createMarker(this.position, "header.name.mismatch");
			m.addInfo(I18n.getString("header.name", headerName));
			m.addInfo(I18n.getString("header.declaration.name", this.name));
			markers.add(m);
		}
	}
	
	@Override
	public void write(DataOutput out) throws IOException
	{
		out.writeUTF(this.name.unqualified);

		ModifierSet.write(this.modifiers, out);
		AnnotationList.write(this.annotations, out);
	}
	
	@Override
	public void read(DataInput in) throws IOException
	{
		this.name = Name.get(in.readUTF());

		this.modifiers = ModifierSet.read(in);
		this.annotations = AnnotationList.read(in);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.annotations != null)
		{
			this.annotations.toString(prefix, buffer);
		}

		this.modifiers.toString(buffer);
		buffer.append("header ").append(this.name);
	}
}

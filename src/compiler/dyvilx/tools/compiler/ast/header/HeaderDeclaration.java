package dyvilx.tools.compiler.ast.header;

import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.attribute.Attributable;
import dyvilx.tools.compiler.ast.annotation.IAnnotation;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.member.INamed;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.modifiers.IModified;
import dyvilx.tools.compiler.ast.modifiers.ModifierSet;
import dyvilx.tools.compiler.util.Markers;
import dyvil.lang.Name;
import dyvilx.tools.parsing.ASTNode;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;

public class HeaderDeclaration implements ASTNode, INamed, IModified, Attributable, IObjectCompilable
{
	protected final IHeaderUnit header;

	protected SourcePosition position;

	protected AttributeList annotations;
	protected ModifierSet   modifiers;

	protected Name name;

	public HeaderDeclaration(IHeaderUnit header)
	{
		this.header = header;
	}

	public HeaderDeclaration(IHeaderUnit header, SourcePosition position, Name name)
	{
		this.header = header;
		this.position = position;
		this.name = name;
	}

	public HeaderDeclaration(IHeaderUnit header, SourcePosition position, Name name, ModifierSet modifiers,
		                        AttributeList annotations)
	{
		this.header = header;
		this.position = position;
		this.name = name;
		this.modifiers = modifiers;
		this.annotations = annotations;
	}

	@Override
	public SourcePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
		this.position = position;
	}

	@Override
	public ElementType getElementType()
	{
		return ElementType.PACKAGE;
	}

	@Override
	public AttributeList getAttributes()
	{
		if (this.annotations != null)
		{
			return this.annotations;
		}
		return this.annotations = new AttributeList();
	}

	@Override
	public void setAttributes(AttributeList attributes)
	{
		this.annotations = attributes;
	}

	@Override
	public IAnnotation getAnnotation(IClass type)
	{
		return this.annotations == null ? null : this.annotations.get(type);
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
			Marker m = Markers.semantic(this.position, "header.name.mismatch");
			m.addInfo(Markers.getSemantic("header.name", headerName));
			m.addInfo(Markers.getSemantic("header.declaration.name", this.name));
			markers.add(m);
		}
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		out.writeUTF(this.name.unqualified);

		ModifierSet.write(this.modifiers, out);
		AttributeList.write(this.annotations, out);
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.name = Name.read(in);

		this.modifiers = ModifierSet.read(in);
		this.annotations = AttributeList.read(in);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.annotations != null)
		{
			this.annotations.toString(prefix, buffer);
		}
		this.modifiers.toString(MemberKind.HEADER, buffer);

		buffer.append("header ").append(this.name);
	}
}

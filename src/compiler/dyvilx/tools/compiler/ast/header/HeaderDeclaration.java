package dyvilx.tools.compiler.ast.header;

import dyvil.annotation.internal.NonNull;
import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.Attributable;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.member.Named;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.ASTNode;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;

public class HeaderDeclaration implements ASTNode, Named, Attributable, IObjectCompilable
{
	protected final IHeaderUnit header;

	protected SourcePosition position;

	protected @NonNull AttributeList attributes;

	protected Name name;

	public HeaderDeclaration(IHeaderUnit header)
	{
		this.header = header;
		this.attributes = new AttributeList();
	}

	public HeaderDeclaration(IHeaderUnit header, SourcePosition position, Name name)
	{
		this.header = header;
		this.position = position;
		this.name = name;
		this.attributes = new AttributeList();
	}

	public HeaderDeclaration(IHeaderUnit header, SourcePosition position, Name name, AttributeList attributes)
	{
		this.header = header;
		this.position = position;
		this.name = name;
		this.attributes = attributes;
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
		return this.attributes;
	}

	@Override
	public void setAttributes(AttributeList attributes)
	{
		this.attributes = attributes;
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
		AttributeList.write(this.attributes, out);
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.name = Name.read(in);
		this.attributes = AttributeList.read(in);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.attributes.toString(prefix, buffer);

		buffer.append("header ").append(this.name);
	}
}

package dyvilx.tools.compiler.ast.member;

import dyvil.annotation.internal.NonNull;
import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.IType.TypePosition;
import dyvilx.tools.compiler.check.ModifierChecks;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class AbstractMember implements Member
{
	// --------------- Instance Fields ---------------

	protected @NonNull AttributeList attributes;

	protected Name name;

	protected IType type;

	// Metadata

	protected SourcePosition position;

	// --------------- Constructors ---------------

	protected AbstractMember()
	{
		this.attributes = new AttributeList();
	}

	protected AbstractMember(Name name)
	{
		this.name = name;
		this.attributes = new AttributeList();
	}

	public AbstractMember(IType type)
	{
		this.type = type;
		this.attributes = new AttributeList();
	}

	public AbstractMember(Name name, IType type)
	{
		this.name = name;
		this.type = type;
		this.attributes = new AttributeList();
	}

	public AbstractMember(Name name, IType type, AttributeList attributes)
	{
		this.name = name;
		this.type = type;
		this.attributes = attributes;
	}

	public AbstractMember(SourcePosition position, Name name, IType type)
	{
		this.position = position;
		this.name = name;
		this.type = type;
		this.attributes = new AttributeList();
	}

	public AbstractMember(SourcePosition position, Name name, IType type, AttributeList attributes)
	{
		this.position = position;
		this.name = name;
		this.type = type;
		this.attributes = attributes;
	}

	// ------------------------------ Attributable Implementation ------------------------------

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
	public int getAccessLevel()
	{
		return this.getAttributes().flags() & Modifiers.ACCESS_MODIFIERS;
	}

	// ------------------------------ Named Implementation ------------------------------

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

	@Override
	public String getInternalName()
	{
		return this.name.qualified;
	}

	// ------------------------------ Typed Implementation ------------------------------

	@Override
	public IType getType()
	{
		return this.type;
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
	}

	// ------------------------------ Positioned Implementation ------------------------------

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

	// ------------------------------ Resolvable Implementation ------------------------------

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.type != null)
		{
			this.type = this.type.resolveType(this.hasModifier(Modifiers.GENERATED) ? MarkerList.BLACKHOLE : markers,
			                                  context);
		}

		this.attributes.resolveTypes(markers, context, this);
		ModifierChecks.checkModifiers(this, markers);
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		if (this.type != null)
		{
			this.type.resolve(markers, context);
		}
		this.attributes.resolve(markers, context);
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.type != null)
		{
			this.type.checkType(markers, context, TypePosition.RETURN_TYPE);
		}
		this.attributes.checkTypes(markers, context);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.type != null)
		{
			this.type.check(markers, context);
		}
		this.attributes.check(markers, context, this.getElementType());
	}

	@Override
	public void foldConstants()
	{
		if (this.type != null)
		{
			this.type.foldConstants();
		}
		this.attributes.foldConstants();
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.attributes.cleanup(compilableList, classCompilableList);
		if (this.type != null)
		{
			this.type.cleanup(compilableList, classCompilableList);
		}
	}

	// ------------------------------ AbstractMember Implementation ------------------------------

	// --------------- Serialization ---------------

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
		AttributeList.write(this.attributes, out);
	}

	public void read(DataInput in) throws IOException
	{
		this.readSignature(in);
		this.name = Name.read(in);
		this.readAnnotations(in);
	}

	public void readSignature(DataInput in) throws IOException
	{
		this.type = IType.readType(in);
	}

	protected void readAnnotations(DataInput in) throws IOException
	{
		this.attributes = AttributeList.read(in);
	}

	// --------------- Formatting ---------------

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		this.toString("", builder);
		return builder.toString();
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		this.attributes.toString(indent, buffer);
	}
}

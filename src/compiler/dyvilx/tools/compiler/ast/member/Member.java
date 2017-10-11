package dyvilx.tools.compiler.ast.member;

import dyvil.annotation.internal.NonNull;
import dyvil.reflect.Modifiers;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.annotation.IAnnotation;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.modifiers.ModifierList;
import dyvilx.tools.compiler.ast.modifiers.ModifierSet;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.IType.TypePosition;
import dyvil.lang.Name;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class Member implements IMember
{
	protected SourcePosition position;

	protected ModifierSet   modifiers;
	protected AttributeList annotations;

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

	public Member(SourcePosition position, Name name, IType type)
	{
		this.position = position;
		this.name = name;
		this.type = type;
	}

	public Member(SourcePosition position, Name name, IType type, ModifierSet modifiers, AttributeList annotations)
	{
		this.position = position;
		this.name = name;
		this.type = type;
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
	public final IAnnotation getAnnotation(IClass type)
	{
		return this.annotations == null ? null : this.getAttributes().get(type);
	}

	@Override
	public ModifierSet getModifiers()
	{
		return this.modifiers;
	}

	@Override
	public void setModifiers(ModifierSet modifiers)
	{
		this.modifiers = modifiers;
	}

	@Override
	public boolean hasModifier(int mod)
	{
		return this.modifiers != null && this.modifiers.hasIntModifier(mod);
	}

	@Override
	public int getAccessLevel()
	{
		return this.modifiers.toFlags() & Modifiers.ACCESS_MODIFIERS;
	}

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

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.type != null)
		{
			this.type = this.type.resolveType(this.hasModifier(Modifiers.GENERATED) ? MarkerList.BLACKHOLE : markers, context);
		}

		if (this.annotations != null)
		{
			this.annotations.resolveTypes(markers, context, this);
		}
		if (this.modifiers != null)
		{
			this.modifiers.resolveTypes(this, markers);
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
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		if (this.annotations != null)
		{
			this.annotations.cleanup(compilableList, classCompilableList);
		}
		if (this.type != null)
		{
			this.type.cleanup(compilableList, classCompilableList);
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
		AttributeList.write(this.annotations, out);
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
		this.modifiers = ModifierSet.read(in);
		this.annotations = AttributeList.read(in);
	}

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
		if (this.annotations != null)
		{
			this.annotations.toString(indent, buffer);
		}
		if (this.modifiers != null)
		{
			this.modifiers.toString(this.getKind(), buffer);
		}
	}
}

package dyvilx.tools.compiler.ast.pattern.object;

import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.pattern.Pattern;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.parsing.marker.MarkerList;

public class EnumPattern extends FieldPattern
{
	private final Name name;

	public EnumPattern(SourcePosition position, Name name)
	{
		super(position, null);

		this.name = name;
	}

	public Name getName()
	{
		return this.name;
	}

	@Override
	public boolean isType(IType type)
	{
		return this.dataMember != null ? super.isType(type) : type.resolveField(this.name) != null;
	}

	@Override
	public Pattern withType(IType type, MarkerList markers)
	{
		if (this.dataMember == null)
		{
			final IDataMember dataMember = type.resolveField(this.name);
			if (dataMember == null)
			{
				return null;
			}

			this.dataMember = dataMember;
		}

		return super.withType(type, markers);
	}

	@Override
	public Object constantValue()
	{
		return new EnumSurrogate(this.name.qualified);
	}

	@Override
	public Pattern resolve(MarkerList markers, IContext context)
	{
		return this;
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('.').append(this.name);
	}
}

class EnumSurrogate
{
	private final String name;

	public EnumSurrogate(String name)
	{
		this.name = name;
	}

	@Override
	public boolean equals(Object o)
	{
		return this == o || o != null && this.getClass() == o.getClass() && this.name.equals(((EnumSurrogate) o).name);
	}

	@Override
	public int hashCode()
	{
		return this.name.hashCode();
	}

	@Override
	public String toString()
	{
		return "." + this.name;
	}
}


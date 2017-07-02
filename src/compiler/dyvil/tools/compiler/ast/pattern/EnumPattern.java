package dyvil.tools.compiler.ast.pattern;

import dyvil.source.position.SourcePosition;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

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
	public IPattern withType(IType type, MarkerList markers)
	{
		if (this.dataMember != null)
		{
			return super.isType(type) ? this : null;
		}

		final IDataMember dataMember = type.resolveField(this.name);
		if (dataMember == null)
		{
			return null;
		}

		this.dataMember = dataMember;
		return this;
	}

	@Override
	public IPattern resolve(MarkerList markers, IContext context)
	{
		return this;
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('.').append(this.name);
	}
}

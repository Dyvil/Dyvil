package dyvil.tools.compiler.ast.expression.constant;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IImplicitContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.access.WildcardAccess;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class WildcardValue implements IConstantValue
{
	// Metadata

	private ICodePosition position;
	private IType type = Types.UNKNOWN;

	public WildcardValue(ICodePosition position)
	{
		this.position = position;
	}

	@Override
	public int valueTag()
	{
		return WILDCARD;
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
	public boolean isPartialWildcard()
	{
		return true;
	}

	@Override
	public IValue withLambdaParameter(IParameter parameter)
	{
		parameter.setPosition(this.position);
		return new WildcardAccess(parameter);
	}

	@Override
	public IType getType()
	{
		return this.type;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		this.type = type;
		return this;
	}

	@Override
	public boolean isType(IType type)
	{
		return this.getTypeMatch(type, null) != MISMATCH;
	}

	@Override
	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		return EXACT_MATCH;
	}

	@Override
	public int stringSize()
	{
		return this.type.getDefaultValue().stringSize();
	}

	@Override
	public boolean toStringBuilder(StringBuilder builder)
	{
		return this.type.getDefaultValue().toStringBuilder(builder);
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (!this.type.isResolved())
		{
			markers.add(Markers.semanticError(this.position, "wildcard.type.unresolved"));
		}
		else if (!this.type.hasDefaultValue())
		{
			final Marker marker = Markers.semanticError(this.position, "wildcard.type.no_default");
			marker.addInfo(Markers.getSemantic("wildcard.type", this.type));
			markers.add(marker);
		}
	}

	@Override
	public IValue foldConstants()
	{
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		if (Types.isVoid(type))
		{
			return;
		}

		if (type != null)
		{
			type.writeDefaultValue(writer);
			return;
		}
		this.type.writeDefaultValue(writer);
	}

	@Override
	public String toString()
	{
		return "_";
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('_');
	}
}

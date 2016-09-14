package dyvil.tools.compiler.ast.constant;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class WildcardValue implements IConstantValue
{
	// Metadata

	private ICodePosition position;
	private IType type = Types.UNKNOWN;

	private IParameter lambdaParameter;

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

	public IParameter getLambdaParameter()
	{
		return this.lambdaParameter;
	}

	@Override
	public void setLambdaParameter(IParameter lambdaParameter)
	{
		this.lambdaParameter = lambdaParameter;
	}

	@Override
	public boolean isPartialWildcard()
	{
		return this.lambdaParameter == null;
	}

	@Override
	public IType getType()
	{
		if (this.lambdaParameter != null)
		{
			return this.lambdaParameter.getType();
		}
		return this.type;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		this.type = type;

		if (this.lambdaParameter != null)
		{
			final IType paramType = this.lambdaParameter.getType();
			if (paramType == null || paramType.isUninferred())
			{
				this.lambdaParameter.setType(type);
			}
		}
		return this;
	}

	@Override
	public boolean isType(IType type)
	{
		return this.getTypeMatch(type) != MISMATCH;
	}

	@Override
	public int getTypeMatch(IType type)
	{
		if (this.lambdaParameter != null)
		{
			final IType paramType = this.lambdaParameter.getType();
			if (paramType != null && !paramType.isUninferred())
			{
				return Types.getTypeMatch(type, paramType);
			}
		}

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
		if (this.type == Types.UNKNOWN)
		{
			markers.add(Markers.semantic(this.position, "wildcard.type"));
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
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

		if (this.lambdaParameter != null)
		{
			final int lineNumber = this.getLineNumber();

			this.lambdaParameter.writeGet(writer, null, lineNumber);
			if (type != null)
			{
				this.lambdaParameter.getInternalType().writeCast(writer, type, lineNumber);
			}
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

package dyvilx.tools.compiler.ast.expression.access;

import dyvil.annotation.internal.NonNull;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IImplicitContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.parameter.IParameter;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public class WildcardAccess implements IValue
{
	protected IParameter parameter;

	public WildcardAccess(IParameter parameter)
	{
		this.parameter = parameter;
	}

	@Override
	public int valueTag()
	{
		return WILDCARD_PARAMETER;
	}

	@Override
	public SourcePosition getPosition()
	{
		return this.parameter.getPosition();
	}

	@Override
	public void setPosition(SourcePosition position)
	{
	}

	@Override
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public IType getType()
	{
		return this.parameter.getType();
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		final IType paramType = this.parameter.getType();
		if (paramType == null || paramType.isUninferred())
		{
			this.parameter.setType(type);
		}
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
		final IType paramType = this.parameter.getType();
		if (paramType != null && !paramType.isUninferred())
		{
			return Types.getTypeMatch(type, paramType);
		}
		return EXACT_MATCH;
	}

	// Phases

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
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
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		return this;
	}

	@Override
	public String toString()
	{
		return "_";
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		buffer.append('_');
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		final int lineNumber = this.lineNumber();

		this.parameter.writeGet(writer, null, lineNumber);
		if (type != null)
		{
			this.parameter.getType().writeCast(writer, type, lineNumber);
		}
	}
}

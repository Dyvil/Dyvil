package dyvilx.tools.compiler.ast.expression;

import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IImplicitContext;
import dyvilx.tools.compiler.ast.expression.constant.VoidValue;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.parameter.IParameter;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.parsing.marker.MarkerList;

public class TupleLikeExpr extends TupleExpr
{
	public TupleLikeExpr(SourcePosition position)
	{
		super(position);
	}

	public TupleLikeExpr(SourcePosition position, ArgumentList values)
	{
		super(position, values);
	}

	@Override
	public boolean isPartialWildcard()
	{
		return this.values.size() == 1 && this.values.getFirst().isPartialWildcard();
	}

	@Override
	public IValue withLambdaParameter(IParameter parameter)
	{
		return this.values.size() != 1 ? null : this.values.getFirst().withLambdaParameter(parameter);
	}

	@Override
	public IValue toAssignment(IValue rhs, SourcePosition position)
	{
		return this.values.size() != 1 ? null : this.values.getFirst().toAssignment(rhs, position);
	}

	@Override
	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		if (this.values.size() == 1)
		{
			return this.values.getFirst().getTypeMatch(type, implicitContext);
		}

		return super.getTypeMatch(type, implicitContext);
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		switch (this.values.size())
		{
		case 0:
			return new VoidValue(this.position);
		case 1:
			return this.values.getFirst().resolve(markers, context);
		}
		return super.resolve(markers, context);
	}
}

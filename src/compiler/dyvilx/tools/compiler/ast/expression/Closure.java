package dyvilx.tools.compiler.ast.expression;

import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IImplicitContext;
import dyvilx.tools.compiler.ast.expression.access.FieldAccess;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.parameter.CodeParameter;
import dyvilx.tools.compiler.ast.parameter.ParameterList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.compound.LambdaType;
import dyvilx.tools.parsing.marker.MarkerList;

public class Closure extends LambdaExpr
{
	// =============== Constructors ===============

	public Closure(SourcePosition position)
	{
		super(position);
	}

	// =============== Static Methods ===============

	public static boolean isTrailingClosure(IValue argument)
	{
		return argument instanceof Closure;
	}

	// =============== Properties ===============

	private boolean areParametersInferred()
	{
		return this.getMethod() != null || !this.parameters.isEmpty();
	}

	@Override
	public boolean isPolyExpression()
	{
		return true;
	}

	// =============== Methods ===============

	// --------------- Lambda Expression Types and Parameter Inference ---------------

	// Type checking for closures differs from the super implementation in that it does not allow assigning closures
	// to java.lang.Object

	@Override
	public boolean isType(IType type)
	{
		return this.areParametersInferred() ? super.isType(type) : type.getFunctionalMethod() != null;
	}

	@Override
	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		return this.isType(type) ? IValue.EXACT_MATCH : IValue.MISMATCH;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (!this.areParametersInferred())
		{
			final IMethod functionalMethod = type.getFunctionalMethod();
			if (functionalMethod == null)
			{
				return null;
			}

			final ParameterList parameterList = functionalMethod.getParameters();
			final int parameterCount = parameterList.size();

			for (int i = 0; i < parameterCount; i++)
			{
				this.parameters.add(new CodeParameter(null, this.position, Name.fromRaw("$" + i), Types.UNKNOWN));
			}
		}

		return super.withType(type, typeContext, markers, context);
	}

	// --------------- Resolution Phase ---------------

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (!this.areParametersInferred())
		{
			// as if we would "delayResolve" in the super method
			return this;
		}

		return super.resolve(markers, context);
	}

	// --------------- Implicit Value for Extension Function Types ---------------

	@Override
	public IValue resolveImplicit(IType type)
	{
		if (type != null)
		{
			return super.resolveImplicit(type);
		}

		final LambdaType lambdaType = this.getType().extract(LambdaType.class);
		if (lambdaType != null && lambdaType.isExtension())
		{
			return new FieldAccess(this.parameters.get(0));
		}

		return null;
	}
}

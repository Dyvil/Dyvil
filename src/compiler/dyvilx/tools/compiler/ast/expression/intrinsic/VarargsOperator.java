package dyvilx.tools.compiler.ast.expression.intrinsic;

import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IImplicitContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

public class VarargsOperator extends UnaryOperator
{
	protected boolean varargsPosition;

	public VarargsOperator(IValue value)
	{
		this.value = value;
	}

	public VarargsOperator(SourcePosition position, IValue value)
	{
		this.position = position;
		this.value = value;
	}

	@Override
	public int valueTag()
	{
		return VARARGS_EXPANSION;
	}

	@Override
	public IType getType()
	{
		return this.value.getType();
	}

	@Override
	public boolean checkVarargs(boolean typeCheck)
	{
		if (typeCheck)
		{
			this.varargsPosition = true;
		}
		return true;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		final IValue typed = this.value.withType(type, typeContext, markers, context);
		if (typed == null)
		{
			return null;
		}

		this.value = typed;
		return this;
	}

	@Override
	public boolean isType(IType type)
	{
		return this.value.isType(type);
	}

	@Override
	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		return this.value.getTypeMatch(type, implicitContext);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (!this.varargsPosition)
		{
			markers.add(Markers.semanticError(this.position, "varargs.invalid"));
		}

		this.value.check(markers, context);
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		this.value.writeExpression(writer, type);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.value.toString(prefix, buffer);
		buffer.append("...");
	}
}

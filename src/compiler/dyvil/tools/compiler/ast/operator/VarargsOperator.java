package dyvil.tools.compiler.ast.operator;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class VarargsOperator implements IValue
{
	protected IValue value;

	// Metadata
	protected ICodePosition position;
	protected boolean       varargsPosition;

	public VarargsOperator(IValue value)
	{
		this.value = value;
	}

	@Override
	public int valueTag()
	{
		return VARARGS_EXPANSION;
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
	public boolean isResolved()
	{
		return true;
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
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.value.resolveTypes(markers, context);
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.value = this.value.resolve(markers, context);
		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.value.checkTypes(markers, context);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (!this.varargsPosition)
		{
			markers.add(Markers.semantic(this.position, "varargs.invalid"));
		}

		this.value.check(markers, context);
	}

	@Override
	public IValue foldConstants()
	{
		this.value = this.value.foldConstants();
		return this;
	}

	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.value = this.value.cleanup(context, compilableList);
		return this;
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

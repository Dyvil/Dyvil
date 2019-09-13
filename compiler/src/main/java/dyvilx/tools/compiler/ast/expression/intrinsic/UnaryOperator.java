package dyvilx.tools.compiler.ast.expression.intrinsic;

import dyvil.lang.Formattable;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.ILabelContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.parsing.marker.MarkerList;

public abstract class UnaryOperator implements IValue
{
	protected IValue value;

	// Metadata
	protected SourcePosition position;

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
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public boolean hasSideEffects()
	{
		return this.value.hasSideEffects();
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
	public void resolveStatement(ILabelContext context, MarkerList markers)
	{
		this.value.resolveStatement(context, markers);
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.value.checkTypes(markers, context);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.value.check(markers, context);
	}

	@Override
	public IValue foldConstants()
	{
		this.value = this.value.foldConstants();
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.value = this.value.foldConstants();
		return this;
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}
}

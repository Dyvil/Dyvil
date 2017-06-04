package dyvil.tools.compiler.ast.reference;

import dyvil.annotation.internal.NonNull;
import dyvil.lang.Formattable;
import dyvil.source.position.SourcePosition;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;

public class ReferenceOperator implements IValue
{
	protected @NonNull IValue     value;
	protected @NonNull IReference reference;

	// Metadata
	private IType type;

	public ReferenceOperator(@NonNull IValue value, @NonNull IReference reference)
	{
		this.value = value;
		this.reference = reference;
	}

	@Override
	public SourcePosition getPosition()
	{
		return this.value.getPosition();
	}

	@Override
	public void setPosition(SourcePosition position)
	{
	}

	@Override
	public int valueTag()
	{
		return REFERENCE;
	}

	@NonNull
	public IValue getValue()
	{
		return this.value;
	}

	public void setValue(@NonNull IValue value)
	{
		this.value = value;
	}

	@NonNull
	public IReference getReference()
	{
		return this.reference;
	}

	public void setReference(@NonNull IReference reference)
	{
		this.reference = reference;
	}

	@Override
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public IType getType()
	{
		if (this.type == null)
		{
			final IType valueType = this.value.getType();
			this.type = new ReferenceType(valueType.getRefClass(), valueType);
		}
		return this.type;
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
		this.reference.checkTypes(this.value.getPosition(), markers, context);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.value.check(markers, context);
		this.reference.check(this.value.getPosition(), markers, context);
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
		this.value = this.value.cleanup(compilableList, classCompilableList);
		this.reference.cleanup(compilableList, classCompilableList);

		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		this.reference.writeReference(writer);
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('&');
		this.value.toString(prefix, buffer);
	}
}

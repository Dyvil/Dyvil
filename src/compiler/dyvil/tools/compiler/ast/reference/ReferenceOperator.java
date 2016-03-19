package dyvil.tools.compiler.ast.reference;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class ReferenceOperator implements IValue
{
	protected IValue     value;
	protected IReference reference;

	// Metadata
	private IType type;

	public ReferenceOperator(IValue value)
	{
		this.value = value;
	}

	public ReferenceOperator(IValue value, IReference reference)
	{
		this.value = value;
		this.reference = reference;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.value.getPosition();
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
	}
	
	@Override
	public int valueTag()
	{
		return REFERENCE;
	}
	
	public void setValue(IValue value)
	{
		this.value = value;
	}
	
	public IValue getValue()
	{
		return this.value;
	}
	
	@Override
	public boolean isResolved()
	{
		return this.reference != null;
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
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (type.isSuperTypeOf(this.getType()))
		{
			return this;
		}

		return null;
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
	public IValue resolveOperator(MarkerList markers, IContext context)
	{
		if (this.reference != null)
		{
			return this;
		}

		final IValue referenceValue = this.value.toReferenceValue(markers, context);
		if (referenceValue != null)
		{
			return referenceValue.resolveOperator(markers, context);
		}

		final IReference reference = this.value.toReference();

		if (reference == null)
		{
			if (this.value.isResolved())
			{
				markers.add(Markers.semanticError(this.value.getPosition(), "reference.expression.invalid"));
			}
			return this;
		}

		reference.resolve(this.value.getPosition(), markers, context);
		this.reference = reference;
		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.value.checkTypes(markers, context);

		if (this.reference != null)
		{
			this.reference.checkTypes(this.value.getPosition(), markers, context);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.value.check(markers, context);

		if (this.reference != null)
		{
			this.reference.check(this.value.getPosition(), markers, context);
		}
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

		if (this.reference != null)
		{
			this.reference.cleanup(context, compilableList);
		}
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
		return IASTNode.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('*');
		this.value.toString(prefix, buffer);
	}
}

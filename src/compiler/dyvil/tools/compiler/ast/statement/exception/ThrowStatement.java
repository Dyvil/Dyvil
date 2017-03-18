package dyvil.tools.compiler.ast.statement.exception;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IImplicitContext;
import dyvil.tools.compiler.ast.expression.AbstractValue;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class ThrowStatement extends AbstractValue implements IValueConsumer
{
	private static final TypeChecker.MarkerSupplier MARKER_SUPPLIER = TypeChecker.markerSupplier("throw.type");

	protected IValue value;

	public ThrowStatement(ICodePosition position)
	{
		this.position = position;
	}

	public ThrowStatement(ICodePosition position, IValue value)
	{
		this.position = position;
		this.value = value;
	}

	@Override
	public int valueTag()
	{
		return THROW;
	}

	@Override
	public boolean isUsableAsStatement()
	{
		return true;
	}

	@Override
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
		return true;
	}

	@Override
	public IType getType()
	{
		return Types.NONE;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return this;
	}

	@Override
	public boolean isType(IType type)
	{
		return true;
	}

	@Override
	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		return IValue.SECONDARY_SUBTYPE_MATCH;
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
		this.value = TypeChecker.convertValue(this.value, Types.THROWABLE, null, markers, context, MARKER_SUPPLIER);
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
		this.value.check(markers, context);

		IType exceptionType = this.value.getType();
		if (IContext.isUnhandled(context, exceptionType))
		{
			markers.add(Markers.semantic(this.value.getPosition(), "exception.unhandled", exceptionType.toString()));
		}
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
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		this.value.writeExpression(writer, null);
		writer.visitInsn(Opcodes.ATHROW);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("throw ");
		this.value.toString(prefix, buffer);
	}
}

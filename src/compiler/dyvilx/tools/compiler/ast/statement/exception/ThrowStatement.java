package dyvilx.tools.compiler.ast.statement.exception;

import dyvil.annotation.internal.NonNull;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.consumer.IValueConsumer;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IImplicitContext;
import dyvilx.tools.compiler.ast.expression.AbstractValue;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.transform.TypeChecker;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

public class ThrowStatement extends AbstractValue implements IValueConsumer
{
	// =============== Static Final Fields ===============

	private static final TypeChecker.MarkerSupplier MARKER_SUPPLIER = TypeChecker.markerSupplier("throw.type");

	// =============== Fields ===============

	protected IValue value;

	// =============== Constructors ===============

	public ThrowStatement(SourcePosition position)
	{
		this.position = position;
	}

	public ThrowStatement(SourcePosition position, IValue value)
	{
		this.position = position;
		this.value = value;
	}

	// =============== Properties ===============

	public IValue getValue()
	{
		return this.value;
	}

	@Override
	public void setValue(IValue value)
	{
		this.value = value;
	}

	// =============== Methods ===============

	// --------------- Misc. Value Properties ---------------

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
	public boolean isResolved()
	{
		return true;
	}

	// --------------- Type ---------------

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

	// --------------- Phases ---------------

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.value != null)
		{
			this.value.resolveTypes(markers, context);
		}
		else
		{
			markers.add(Markers.syntaxError(SourcePosition.after(this.position), "throw.expression"));
		}
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.value != null)
		{
			this.value = this.value.resolve(markers, context);
			this.value = TypeChecker.convertValue(this.value, Types.THROWABLE, null, markers, context, MARKER_SUPPLIER);
		}
		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.value != null)
		{
			this.value.checkTypes(markers, context);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.value != null)
		{
			this.value.check(markers, context);

			IType exceptionType = this.value.getType();
			if (IContext.isUnhandled(context, exceptionType))
			{
				markers
					.add(Markers.semantic(this.value.getPosition(), "exception.unhandled", exceptionType.toString()));
			}
		}
	}

	@Override
	public IValue foldConstants()
	{
		if (this.value != null)
		{
			this.value = this.value.foldConstants();
		}
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		if (this.value != null)
		{
			this.value = this.value.cleanup(compilableList, classCompilableList);
		}
		return this;
	}

	// --------------- Compilation ---------------

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		if (this.value != null)
		{
			this.value.writeExpression(writer, null);
		}
		writer.visitInsn(Opcodes.ATHROW);
	}

	// --------------- Formatting ---------------

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		buffer.append("throw");
		if (this.value != null)
		{
			buffer.append(' ');
			this.value.toString(indent, buffer);
		}
	}
}

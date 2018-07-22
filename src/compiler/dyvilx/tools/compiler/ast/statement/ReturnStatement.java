package dyvilx.tools.compiler.ast.statement;

import dyvil.reflect.Opcodes;
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
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.transform.TypeChecker;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public class ReturnStatement extends AbstractValue implements IValueConsumer
{
	private static final TypeChecker.MarkerSupplier MARKER_SUPPLIER = TypeChecker
		                                                                  .markerSupplier("return.type.incompatible",
		                                                                                  "return.type", "value.type");

	protected IValue value;

	public ReturnStatement(SourcePosition position)
	{
		this.position = position;
	}

	public ReturnStatement(SourcePosition position, IValue value)
	{
		this.position = position;
		this.value = value;
	}

	@Override
	public int valueTag()
	{
		return RETURN;
	}

	@Override
	public boolean isUsableAsStatement()
	{
		return true;
	}

	@Override
	public boolean isResolved()
	{
		return this.value == null || this.value.isResolved();
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
	public IType getType()
	{
		return this.value == null ? Types.VOID : this.value.getType();
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (Types.isVoid(type))
		{
			return this;
		}
		if (this.value == null)
		{
			return null;
		}
		return this.value.withType(type, typeContext, markers, context);
	}

	@Override
	public boolean isType(IType type)
	{
		return Types.isVoid(type) || this.value != null && this.value.isType(type);
	}

	@Override
	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		if (this.value == null)
		{
			return MISMATCH;
		}

		return this.value.getTypeMatch(type, implicitContext);
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.value != null)
		{
			this.value.resolveTypes(markers, context);
		}
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.value != null)
		{
			this.value = this.value.resolve(markers, context);
		}
		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		final IType returnType = context.getReturnType();

		if (this.value != null)
		{
			// return ... ;

			if (returnType != null && this.value.isResolved())
			{
				this.value = TypeChecker.convertValue(this.value, returnType, null, markers, context, MARKER_SUPPLIER);
			}

			this.value.checkTypes(markers, context);
			return;
		}

		// return;
		if (returnType != null && !Types.isSameClass(returnType, Types.VOID))
		{
			markers.add(Markers.semanticError(this.position, "return.void.invalid"));
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.value != null)
		{
			this.value.check(markers, context);
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

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		if (this.value == null)
		{
			if (type == null || Types.isVoid(type))
			{
				writer.visitInsn(Opcodes.RETURN);
				return;
			}

			type.writeDefaultValue(writer);
			return;
		}

		if (Types.isVoid(type))
		{
			this.value.writeExpression(writer, null);
			writer.visitInsn(this.value.getType().getReturnOpcode());
			return;
		}

		this.value.writeExpression(writer, type);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.value != null)
		{
			buffer.append("return ");
			this.value.toString("", buffer);
		}
		else
		{
			buffer.append("return");
		}
	}
}

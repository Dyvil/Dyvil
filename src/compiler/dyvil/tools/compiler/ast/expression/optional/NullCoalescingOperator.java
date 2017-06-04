package dyvil.tools.compiler.ast.expression.optional;

import dyvil.annotation.internal.NonNull;
import dyvil.lang.Formattable;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.constant.NullValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.compound.NullableType;
import dyvil.tools.compiler.ast.type.compound.UnionType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;

public class NullCoalescingOperator implements IValue
{
	protected IValue lhs;
	protected IValue rhs;

	// Metadata
	protected SourcePosition position;
	protected IType         type;

	protected Label elseLabel = new Label();

	public NullCoalescingOperator(IValue lhs, IValue rhs)
	{
		this.lhs = lhs;
		this.rhs = rhs;
	}

	public static NullCoalescingOperator apply(IValue lhs)
	{
		final NullCoalescingOperator op = new NullCoalescingOperator(lhs, NullValue.NULL);
		op.applyLabel();
		return op;
	}

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
	public int valueTag()
	{
		return NULL_COALESCING;
	}

	@Override
	public boolean isResolved()
	{
		return this.lhs.isResolved() && this.rhs.isResolved();
	}

	@Override
	public IType getType()
	{
		if (this.type != null)
		{
			return this.type;
		}
		final IType leftType = NullableType.unapply(this.lhs.getType());
		return this.type = UnionType.combine(leftType, this.rhs.getType(), null);
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		final IType lhsType = this.lhs.getType().isPrimitive() ? type : NullableType.apply(type);
		final IValue lhs = this.lhs.withType(lhsType, typeContext, markers, context);
		if (lhs == null)
		{
			return null;
		}

		final IValue rhs = this.rhs.withType(type, typeContext, markers, context);
		if (rhs == null)
		{
			return null;
		}

		this.lhs = lhs;
		this.rhs = rhs;
		this.type = null;
		return this;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.lhs.resolveTypes(markers, context);
		this.rhs.resolveTypes(markers, context);
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.lhs = this.lhs.resolve(markers, context);
		this.rhs = this.rhs.resolve(markers, context);
		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.applyLabel();

		this.lhs.checkTypes(markers, context);
		this.rhs.checkTypes(markers, context);
	}

	private void applyLabel()
	{
		this.lhs.setOptionalElseLabel(this.elseLabel, true);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.lhs.check(markers, context);
		this.rhs.check(markers, context);
	}

	@Override
	public IValue foldConstants()
	{
		this.lhs = this.lhs.foldConstants();
		this.rhs = this.rhs.foldConstants();
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.lhs = this.lhs.cleanup(compilableList, classCompilableList);
		this.rhs = this.rhs.cleanup(compilableList, classCompilableList);
		return this;
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		this.lhs.toString(indent, buffer);
		buffer.append(" ?? ");
		this.rhs.toString(indent, buffer);
	}

	@Override
	public void writeExpression(MethodWriter writer, IType atype) throws BytecodeException
	{
		final IType type = this.getType();

		final Object frameType = type.getFrameType();
		final int varIndex = writer.localCount();
		final Label endLabel = new Label();

		this.lhs.writeExpression(writer, type);
		writer.getFrame().set(frameType);

		if (this.lhs.valueTag() != IValue.OPTIONAL_CHAIN && !type.isPrimitive())
		{
			writer.visitInsn(Opcodes.DUP);
			writer.visitVarInsn(Opcodes.ASTORE, varIndex);
			writer.visitJumpInsn(Opcodes.IFNULL, this.elseLabel);
			writer.visitVarInsn(Opcodes.ALOAD, varIndex);
		}

		writer.visitJumpInsn(Opcodes.GOTO, endLabel);

		writer.visitTargetLabel(this.elseLabel);

		this.rhs.writeExpression(writer, type);
		writer.getFrame().set(frameType);

		writer.visitTargetLabel(endLabel);

		writer.resetLocals(varIndex);

		if (atype != null)
		{
			type.writeCast(writer, atype, this.lineNumber());
		}
	}
}

package dyvilx.tools.compiler.ast.expression.intrinsic;

import dyvil.lang.Formattable;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;

public abstract class BooleanOperator implements IValue
{
	public IValue left;
	public IValue right;

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
	public int valueTag()
	{
		return BOOLEAN_OR;
	}

	@Override
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public boolean hasSideEffects()
	{
		return this.left.hasSideEffects() || this.right.hasSideEffects();
	}

	@Override
	public IType getType()
	{
		return Types.BOOLEAN;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.left.resolveTypes(markers, context);
		this.right.resolveTypes(markers, context);
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.left = this.left.resolve(markers, context);
		this.right = this.right.resolve(markers, context);
		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.left.checkTypes(markers, context);
		this.right.checkTypes(markers, context);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.left.check(markers, context);
		this.right.check(markers, context);
	}

	@Override
	public IValue foldConstants()
	{
		this.left = this.left.foldConstants();
		this.right = this.right.foldConstants();
		return this.optimize();
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.left = this.left.cleanup(compilableList, classCompilableList);
		this.right = this.right.cleanup(compilableList, classCompilableList);
		return this.optimize();
	}

	protected abstract IValue optimize();

	protected static boolean hasValue(IValue expr, boolean value)
	{
		return expr.valueTag() == IValue.BOOLEAN && expr.booleanValue() == value;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		writeExpression(writer, this, type);
	}

	protected static void writeExpression(MethodWriter writer, IValue value, IType type)
	{
		final Label label = new Label();
		final Label end = new Label();

		value.writeInvJump(writer, label); // if (expr) {
		writer.visitLdcInsn(1); // true
		writer.visitJumpInsn(Opcodes.GOTO, end); // }
		writer.visitLabel(label); // else {
		writer.visitLdcInsn(0); // false
		writer.visitLabel(end); // }

		if (type != null)
		{
			Types.BOOLEAN.writeCast(writer, type, value.lineNumber());
		}
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}
}

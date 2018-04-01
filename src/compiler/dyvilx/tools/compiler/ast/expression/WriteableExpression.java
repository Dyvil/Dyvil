package dyvilx.tools.compiler.ast.expression;

import dyvil.reflect.Opcodes;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;

public interface WriteableExpression
{
	void writeExpression(MethodWriter writer, IType type) throws BytecodeException;

	default void writeNullCheckedExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		this.writeExpression(writer, type);
	}

	default void writeJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		this.writeExpression(writer, Types.BOOLEAN);
		writer.visitJumpInsn(Opcodes.IFNE, dest);
	}

	/**
	 * Writes this {@link IValue} to the given {@link MethodWriter} {@code writer} as a jump expression to the given
	 * {@link Label} {@code dest}. By default, this calls {@link #writeExpression(MethodWriter, IType)} and then writes
	 * an {@link Opcodes#IFEQ IFEQ} instruction pointing to {@code dest}. That means the JVM would jump to {@code dest}
	 * if the current value on the stack equals {@code 0}.
	 *
	 * @param writer
	 * @param dest
	 *
	 * @throws BytecodeException
	 */
	default void writeInvJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		this.writeExpression(writer, Types.BOOLEAN);
		writer.visitJumpInsn(Opcodes.IFEQ, dest);
	}
}

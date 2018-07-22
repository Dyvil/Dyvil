package dyvilx.tools.compiler.ast.statement.loop;

import dyvil.reflect.Opcodes;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvil.source.position.SourcePosition;

public class StringForStatement extends ForEachStatement
{
	public StringForStatement(SourcePosition position, IVariable var)
	{
		super(position, var);
	}

	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		dyvilx.tools.asm.Label startLabel = this.startLabel.getTarget();
		dyvilx.tools.asm.Label updateLabel = this.updateLabel.getTarget();
		dyvilx.tools.asm.Label endLabel = this.endLabel.getTarget();

		final IVariable var = this.variable;
		final int lineNumber = this.lineNumber();

		// Scope
		dyvilx.tools.asm.Label scopeLabel = new dyvilx.tools.asm.Label();
		writer.visitLabel(scopeLabel);

		final int localCount = writer.localCount();

		// Load the String
		var.getValue().writeExpression(writer, null);

		// Local Variables
		final int stringVarIndex = writer.localCount();
		final int lengthVarIndex = stringVarIndex + 1;
		final int indexVarIndex = stringVarIndex + 2;

		writer.visitInsn(Opcodes.DUP);
		writer.visitVarInsn(Opcodes.ASTORE, stringVarIndex);
		// Get the length
		writer.visitLineNumber(lineNumber);
		writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I", false);
		writer.visitInsn(Opcodes.DUP);
		writer.visitVarInsn(Opcodes.ISTORE, lengthVarIndex);

		// Initial Boundary Check - if the length is 0, skip the loop.
		writer.visitJumpInsn(Opcodes.IFEQ, endLabel);

		// Set index to 0
		writer.visitLdcInsn(0);
		writer.visitVarInsn(Opcodes.ISTORE, indexVarIndex);

		writer.visitTargetLabel(startLabel);

		// Get the char at the index
		writer.visitVarInsn(Opcodes.ALOAD, stringVarIndex);
		writer.visitVarInsn(Opcodes.ILOAD, indexVarIndex);
		writer.visitLineNumber(lineNumber);
		writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C", false);
		// Autocasting
		Types.CHAR.writeCast(writer, var.getType(), lineNumber);
		var.writeInit(writer, null);

		// Action
		if (this.action != null)
		{
			this.action.writeExpression(writer, Types.VOID);
		}

		writer.visitLabel(updateLabel);
		// Increment index
		writer.visitIincInsn(indexVarIndex, 1);
		// Boundary Check
		writer.visitVarInsn(Opcodes.ILOAD, indexVarIndex);
		writer.visitVarInsn(Opcodes.ILOAD, lengthVarIndex);
		writer.visitJumpInsn(Opcodes.IF_ICMPLT, startLabel);

		// Local Variables
		writer.resetLocals(localCount);
		writer.visitLabel(endLabel);

		var.writeLocal(writer, scopeLabel, endLabel);
	}
}

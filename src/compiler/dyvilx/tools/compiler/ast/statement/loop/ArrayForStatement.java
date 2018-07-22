package dyvilx.tools.compiler.ast.statement.loop;

import dyvil.reflect.Opcodes;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.compound.ArrayType;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvil.source.position.SourcePosition;

public class ArrayForStatement extends ForEachStatement
{
	protected ArrayType arrayType;

	public ArrayForStatement(SourcePosition position, IVariable var)
	{
		this(position, var, var.getValue().getType().extract(ArrayType.class));
	}

	public ArrayForStatement(SourcePosition position, IVariable var, ArrayType arrayType)
	{
		super(position, var);

		this.arrayType = arrayType;
	}

	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		dyvilx.tools.asm.Label startLabel = this.startLabel.getTarget();
		dyvilx.tools.asm.Label updateLabel = this.updateLabel.getTarget();
		dyvilx.tools.asm.Label endLabel = this.endLabel.getTarget();

		final IVariable var = this.variable;
		final IType elementType = this.arrayType.getElementType();
		final int lineNumber = this.lineNumber();

		// Scope
		dyvilx.tools.asm.Label scopeLabel = new dyvilx.tools.asm.Label();
		writer.visitLabel(scopeLabel);

		final int localCount = writer.localCount();

		// Load the array
		final int arrayVarIndex = var.getValue().writeStore(writer, null);

		// Local Variables
		final int lengthVarIndex = writer.localCount();
		final int indexVarIndex = lengthVarIndex + 1;

		writer.visitVarInsn(Opcodes.ALOAD, arrayVarIndex);
		// Load the length
		writer.visitLineNumber(lineNumber);
		writer.visitInsn(Opcodes.ARRAYLENGTH);
		writer.visitInsn(Opcodes.DUP);
		writer.visitVarInsn(Opcodes.ISTORE, lengthVarIndex);

		// Initial Boundary Check - if the length is less than or equal to 0, skip the loop
		writer.visitJumpInsn(Opcodes.IFLE, endLabel);

		// Set index to 0
		writer.visitLdcInsn(0);
		writer.visitVarInsn(Opcodes.ISTORE, indexVarIndex);

		writer.visitTargetLabel(startLabel);

		// Load the element
		writer.visitVarInsn(Opcodes.ALOAD, arrayVarIndex);
		writer.visitVarInsn(Opcodes.ILOAD, indexVarIndex);
		writer.visitLineNumber(lineNumber);
		writer.visitInsn(elementType.getArrayLoadOpcode());
		// Autocasting
		elementType.writeCast(writer, var.getType(), lineNumber);
		// Store variable
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
		writer.visitLabel(endLabel);
		writer.resetLocals(localCount);

		var.writeLocal(writer, scopeLabel, endLabel);
	}
}

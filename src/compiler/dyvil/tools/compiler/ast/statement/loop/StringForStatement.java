package dyvil.tools.compiler.ast.statement.loop;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.position.ICodePosition;

public class StringForStatement extends ForEachStatement
{
	public StringForStatement(ICodePosition position, IVariable var)
	{
		super(position, var);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		dyvil.tools.asm.Label startLabel = this.startLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label updateLabel = this.updateLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label endLabel = this.endLabel.target = new dyvil.tools.asm.Label();
		
		final IVariable var = this.variable;
		final int lineNumber = this.getLineNumber();

		// Scope
		dyvil.tools.asm.Label scopeLabel = new dyvil.tools.asm.Label();
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

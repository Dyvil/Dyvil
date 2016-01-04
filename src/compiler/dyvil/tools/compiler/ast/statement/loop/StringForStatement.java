package dyvil.tools.compiler.ast.statement.loop;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.position.ICodePosition;

public class StringForStatement extends ForEachStatement
{
	public StringForStatement(ICodePosition position, Variable var)
	{
		super(position, var);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		dyvil.tools.asm.Label startLabel = this.startLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label updateLabel = this.updateLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label endLabel = this.endLabel.target = new dyvil.tools.asm.Label();
		
		Variable var = this.variable;
		int lineNumber = this.getLineNumber();
		
		dyvil.tools.asm.Label scopeLabel = new dyvil.tools.asm.Label();
		writer.writeLabel(scopeLabel);

		int localCount = writer.localCount();
		
		// Load the String
		var.getValue().writeExpression(writer, null);
		
		// Local Variables
		final int stringVarIndex = writer.localCount();
		final int lengthVarIndex = stringVarIndex + 1;
		final int indexVarIndex = stringVarIndex + 2;

		writer.writeInsn(Opcodes.DUP);
		writer.writeVarInsn(Opcodes.ASTORE, stringVarIndex);
		// Get the length
		writer.writeLineNumber(lineNumber);
		writer.writeInvokeInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I", false);
		writer.writeInsn(Opcodes.DUP);
		writer.writeVarInsn(Opcodes.ISTORE, lengthVarIndex);
		
		// Initial Boundary Check - if the length is 0, skip the loop.
		writer.writeJumpInsn(Opcodes.IFEQ, endLabel);
		
		// Set index to 0
		writer.writeLDC(0);
		writer.writeVarInsn(Opcodes.ISTORE, indexVarIndex);
		
		writer.writeTargetLabel(startLabel);
		
		// Get the char at the index
		writer.writeVarInsn(Opcodes.ALOAD, stringVarIndex);
		writer.writeVarInsn(Opcodes.ILOAD, indexVarIndex);
		writer.writeLineNumber(lineNumber);
		writer.writeInvokeInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C", false);
		var.writeInit(writer, null);
		
		// Action
		if (this.action != null)
		{
			this.action.writeExpression(writer, Types.VOID);
		}
		
		writer.writeLabel(updateLabel);
		// Increment index
		writer.writeIINC(indexVarIndex, 1);
		// Boundary Check
		writer.writeVarInsn(Opcodes.ILOAD, indexVarIndex);
		writer.writeVarInsn(Opcodes.ILOAD, lengthVarIndex);
		writer.writeJumpInsn(Opcodes.IF_ICMPLT, startLabel);
		
		// Local Variables
		writer.resetLocals(localCount);
		writer.writeLabel(endLabel);
		
		var.writeLocal(writer, scopeLabel, endLabel);
	}
}

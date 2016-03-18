package dyvil.tools.compiler.ast.statement.loop;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.position.ICodePosition;

public class ArrayForStatement extends ForEachStatement
{
	protected IType arrayType;
	
	public ArrayForStatement(ICodePosition position, IVariable var)
	{
		this(position, var, var.getValue().getType());
	}
	
	public ArrayForStatement(ICodePosition position, IVariable var, IType arrayType)
	{
		super(position, var);

		this.arrayType = arrayType;
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		dyvil.tools.asm.Label startLabel = this.startLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label updateLabel = this.updateLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label endLabel = this.endLabel.target = new dyvil.tools.asm.Label();

		final IVariable var = this.variable;
		final IType elementType = this.arrayType.getElementType();
		final int lineNumber = this.getLineNumber();

		// Scope
		dyvil.tools.asm.Label scopeLabel = new dyvil.tools.asm.Label();
		writer.visitLabel(scopeLabel);

		final int localCount = writer.localCount();

		// Load the array
		var.getValue().writeExpression(writer, null);

		// Local Variables
		final int arrayVarIndex = writer.localCount();
		final int lengthVarIndex = arrayVarIndex + 1;
		final int indexVarIndex = arrayVarIndex + 2;

		writer.visitInsn(Opcodes.DUP);

		writer.visitVarInsn(Opcodes.ASTORE, arrayVarIndex);
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
		writer.resetLocals(localCount);
		writer.visitLabel(endLabel);
		
		var.writeLocal(writer, scopeLabel, endLabel);
	}
}

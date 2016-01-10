package dyvil.tools.compiler.ast.statement.loop;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.position.ICodePosition;

public class ArrayForStatement extends ForEachStatement
{
	protected IType arrayType;
	
	public ArrayForStatement(ICodePosition position, Variable var)
	{
		this(position, var, var.getValue().getType());
	}
	
	public ArrayForStatement(ICodePosition position, Variable var, IType arrayType)
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

		final Variable var = this.variable;
		final IType elementType = this.arrayType.getElementType();
		final int lineNumber = this.getLineNumber();

		// Scope
		dyvil.tools.asm.Label scopeLabel = new dyvil.tools.asm.Label();
		writer.writeLabel(scopeLabel);

		final int localCount = writer.localCount();

		// Load the array
		var.getValue().writeExpression(writer, null);

		// Local Variables
		final int arrayVarIndex = writer.localCount();
		final int lengthVarIndex = arrayVarIndex + 1;
		final int indexVarIndex = arrayVarIndex + 2;

		writer.writeInsn(Opcodes.DUP);

		writer.writeVarInsn(Opcodes.ASTORE, arrayVarIndex);
		// Load the length
		writer.writeLineNumber(lineNumber);
		writer.writeInsn(Opcodes.ARRAYLENGTH);
		writer.writeInsn(Opcodes.DUP);
		writer.writeVarInsn(Opcodes.ISTORE, lengthVarIndex);

		// Initial Boundary Check - if the length is less than or equal to 0, skip the loop
		writer.writeJumpInsn(Opcodes.IFLE, endLabel);

		// Set index to 0
		writer.writeLDC(0);
		writer.writeVarInsn(Opcodes.ISTORE, indexVarIndex);

		writer.writeTargetLabel(startLabel);

		// Load the element
		writer.writeVarInsn(Opcodes.ALOAD, arrayVarIndex);
		writer.writeVarInsn(Opcodes.ILOAD, indexVarIndex);
		writer.writeLineNumber(lineNumber);
		writer.writeInsn(elementType.getArrayLoadOpcode());
		// Autocasting
		elementType.writeCast(writer, var.getType(), lineNumber);
		// Store variable
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

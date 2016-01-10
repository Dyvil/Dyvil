package dyvil.tools.compiler.ast.statement.loop;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.position.ICodePosition;

public class IterableForStatement extends ForEachStatement
{
	public static final ITypeParameter ITERABLE_TYPE  = Types.ITERABLE.getTheClass().getTypeParameter(0);
	public static final IClass         ITERATOR_CLASS = Package.javaUtil.resolveClass("Iterator");
	
	protected IMethod  boxMethod;
	
	public IterableForStatement(ICodePosition position, Variable variable)
	{
		super(position, variable);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		dyvil.tools.asm.Label startLabel = this.startLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label updateLabel = this.updateLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label endLabel = this.endLabel.target = new dyvil.tools.asm.Label();
		
		final Variable var = this.variable;
		final IType varType = var.getType();
		final int lineNumber = this.getLineNumber();

		// Scope
		dyvil.tools.asm.Label scopeLabel = new dyvil.tools.asm.Label();
		writer.writeLabel(scopeLabel);
		int localCount = writer.localCount();
		
		// Get the iterator
		var.getValue().writeExpression(writer, null);
		writer.writeLineNumber(lineNumber);
		writer.writeInvokeInsn(Opcodes.INVOKEINTERFACE, "java/lang/Iterable", "iterator", "()Ljava/util/Iterator;",
		                       true);
		
		// Local Variables
		int iteratorVarIndex = writer.localCount();
		var.setLocalIndex(iteratorVarIndex + 1);
		
		// Store Iterator
		writer.writeVarInsn(Opcodes.ASTORE, iteratorVarIndex);
		
		// Jump to hasNext check
		writer.writeJumpInsn(Opcodes.GOTO, updateLabel);
		writer.writeTargetLabel(startLabel);
		
		// Invoke Iterator.next()
		writer.writeVarInsn(Opcodes.ALOAD, iteratorVarIndex);
		writer.writeLineNumber(lineNumber);
		writer.writeInvokeInsn(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true);
		// Auocasting
		Types.OBJECT.writeCast(writer, varType, lineNumber);
		
		// Store the next element
		writer.writeVarInsn(varType.getStoreOpcode(), iteratorVarIndex + 1);
		
		// Action
		if (this.action != null)
		{
			this.action.writeExpression(writer, Types.VOID);
		}
		
		writer.writeLabel(updateLabel);
		// Load Iterator
		writer.writeVarInsn(Opcodes.ALOAD, iteratorVarIndex);
		// Check hasNext
		writer.writeLineNumber(lineNumber);
		writer.writeInvokeInsn(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);
		// Go back to start if Iterator.hasNext() returned true
		writer.writeJumpInsn(Opcodes.IFNE, startLabel);
		
		// Local Variables
		writer.resetLocals(localCount);
		writer.writeLabel(endLabel);
		
		var.writeLocal(writer, scopeLabel, endLabel);
	}
}

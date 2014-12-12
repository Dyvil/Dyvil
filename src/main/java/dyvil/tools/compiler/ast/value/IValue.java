package dyvil.tools.compiler.ast.value;

import jdk.internal.org.objectweb.asm.Label;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.api.IASTNode;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.bytecode.MethodWriter;

public interface IValue extends IASTNode
{
	public boolean isConstant();
	
	public Type getType();
	
	@Override
	public IValue applyState(CompilerState state, IContext context);
	
	// Compilation
	
	/**
	 * Writes this {@link IValue} to the given {@link MethodWriter}
	 * {@code writer} as an expression. That means that this element remains as
	 * the first element of the stack.
	 * 
	 * @param visitor
	 */
	public void writeExpression(MethodWriter visitor);
	
	/**
	 * Writes this {@link IValue} to the given {@link MethodWriter}
	 * {@code writer} as a statement. That means that this element is removed
	 * from the stack.
	 * 
	 * @param writer
	 */
	public void writeStatement(MethodWriter writer);
	
	public void writeJump(MethodWriter writer, Label label);
}

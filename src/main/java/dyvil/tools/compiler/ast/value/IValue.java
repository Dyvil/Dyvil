package dyvil.tools.compiler.ast.value;

import jdk.internal.org.objectweb.asm.MethodVisitor;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.api.IASTObject;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;

public interface IValue extends IASTObject
{
	public boolean isConstant();
	
	public Type getType();
	
	@Override
	public IValue applyState(CompilerState state, IContext context);
	
	// Compilation
	
	public void write(MethodVisitor visitor);
}

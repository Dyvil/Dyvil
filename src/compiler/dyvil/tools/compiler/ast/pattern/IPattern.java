package dyvil.tools.compiler.ast.pattern;

import org.objectweb.asm.Label;

import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;

public interface IPattern extends IASTNode
{	
	public IType getType();
	
	public boolean isType(IType type);
	
	public void writeJump(MethodWriter writer, Label elseLabel);
}

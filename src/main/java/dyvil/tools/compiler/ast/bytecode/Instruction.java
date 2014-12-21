package dyvil.tools.compiler.ast.bytecode;

import dyvil.tools.compiler.ast.api.IASTNode;
import dyvil.tools.compiler.bytecode.MethodWriter;

public interface Instruction extends IASTNode
{
	public void write(MethodWriter writer);
}

package dyvil.tools.compiler.ast.bytecode;

import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public interface IInstruction extends IASTNode
{	
	public void resolve(MarkerList markers, Bytecode bytecode);
	
	// Compilation
	
	public void write(MethodWriter writer);
}

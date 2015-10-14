package dyvil.tools.compiler.ast.bytecode;

import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public interface IInstruction extends IASTNode
{
	@Override
	public default ICodePosition getPosition()
	{
		return null;
	}
	
	@Override
	public default void setPosition(ICodePosition position)
	{
	}
	
	public void resolve(MarkerList markers, Bytecode bytecode);
	
	// Compilation
	
	public void write(MethodWriter writer) throws BytecodeException;
}

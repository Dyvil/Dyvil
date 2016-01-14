package dyvil.tools.compiler.ast.bytecode;

import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public interface IInstruction extends IASTNode
{
	@Override
	default ICodePosition getPosition()
	{
		return null;
	}
	
	@Override
	default void setPosition(ICodePosition position)
	{
	}
	
	void resolve(MarkerList markers, InstructionList instructionList);
	
	// Compilation
	
	void write(MethodWriter writer) throws BytecodeException;
}

package dyvil.tools.compiler.ast.reference;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public interface IReference
{
	public void check(ICodePosition position, MarkerList markers);
	
	public void cleanup(IContext context, IClassCompilableList compilableList);
	
	public void writeReference(MethodWriter writer) throws BytecodeException;
}

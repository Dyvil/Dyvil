package dyvil.tools.compiler.ast.match;

import java.util.List;

import org.objectweb.asm.Label;

import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.pattern.IPatterned;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;

public interface ICase extends IASTNode, IValued, IPatterned
{
	// Condition
	
	public void setCondition(IValue condition);
	
	public IValue getCondition();
	
	// Phases
	
	public void resolveTypes(List<Marker> markers, IContext context);
	
	public ICase resolve(List<Marker> markers, IContext context);
	
	public void check(List<Marker> markers, IContext context);
	
	public ICase foldConstants();
	
	// Compilation
	
	public void writeExpression(MethodWriter writer, int varIndex, Label elseLabel);
	
	public void writeStatement(MethodWriter writer, int varIndex, Label elseLabel);
}

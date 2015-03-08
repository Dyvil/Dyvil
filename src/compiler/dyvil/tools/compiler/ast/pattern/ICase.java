package dyvil.tools.compiler.ast.pattern;

import java.util.List;

import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.lexer.marker.Marker;

public interface ICase extends IASTNode, IValued
{
	public void setPattern(IPattern pattern);
	
	public IPattern getPattern();
	
	public void setCondition(IValue condition);
	
	public IValue getCondition();
	
	// Phases
	
	public void resolveTypes(List<Marker> markers, IContext context);
	
	public ICase resolve(List<Marker> markers, IContext context);
	
	public void check(List<Marker> markers, IContext context);
	
	public ICase foldConstants();
}

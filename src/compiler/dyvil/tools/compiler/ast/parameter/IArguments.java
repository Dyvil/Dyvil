package dyvil.tools.compiler.ast.parameter;

import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public interface IArguments extends IASTNode, Iterable<IValue>
{
	public int size();
	
	public boolean isEmpty();
	
	// 'Variations'
	
	public IArguments dropFirstValue();
	
	public IArguments addLastValue(IValue value);
	
	// First Values
	
	public IValue getFirstValue();
	
	public void setFirstValue(IValue value);
	
	// Used by Methods
	
	public void setValue(int index, Parameter param, IValue value);
	
	public IValue getValue(int index, Parameter param);
	
	public IType getType(int index, Parameter param);
	
	public int getTypeMatch(int index, Parameter param);
	
	public int getVarargsTypeMatch(int index, Parameter param);
	
	public void checkValue(int index, Parameter param, MarkerList markers, ITypeContext context);
	
	public void checkVarargsValue(int index, Parameter param, MarkerList markers, ITypeContext context);
	
	public void writeValue(int index, String name, IValue defaultValue, MethodWriter writer);
	
	public void writeVarargsValue(int index, String name, IType type, MethodWriter writer);
	
	// Phase Methdos
	
	public void resolveTypes(MarkerList markers, IContext context);
	
	public void resolve(MarkerList markers, IContext context);
	
	public void check(MarkerList markers, IContext context);
	
	public void foldConstants();
}

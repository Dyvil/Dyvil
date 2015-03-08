package dyvil.tools.compiler.ast.parameter;

import java.util.List;

import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;

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
	
	public IValue getValue(int index, Parameter param);
	
	public IType getType(int index, Parameter param);
	
	public int getTypeMatch(int index, Parameter param);
	
	public int getVarargsTypeMatch(int index, Parameter param);
	
	public void checkValue(int index, Parameter param, List<Marker> markers, ITypeContext context);
	
	public void checkVarargsValue(int index, Parameter param, List<Marker> markers, ITypeContext context);
	
	public void writeValue(int index, String name, IValue defaultValue, MethodWriter writer);
	
	public void writeVarargsValue(int index, String name, IType type, MethodWriter writer);
	
	// Phase Methdos
	
	public void resolveTypes(List<Marker> markers, IContext context);
	
	public void resolve(List<Marker> markers, IContext context);
	
	public void check(List<Marker> markers, IContext context);
	
	public void foldConstants();
}

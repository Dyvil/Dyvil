package dyvil.tools.compiler.ast.parameter;

import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public interface IArguments extends IASTNode, Iterable<IValue>
{
	public static final float	DEFAULT_MATCH	= 1000;
	
	public int size();
	
	public boolean isEmpty();
	
	// 'Variations'
	
	public IArguments dropFirstValue();
	
	public IArguments addLastValue(IValue value);
	
	public default IArguments addLastValue(Name name, IValue value)
	{
		return this.addLastValue(value);
	}
	
	// First Values
	
	public IValue getFirstValue();
	
	public void setFirstValue(IValue value);
	
	// Last Values
	
	public IValue getLastValue();
	
	public void setLastValue(IValue value);
	
	// Used by Methods
	
	public void setValue(int index, IParameter param, IValue value);
	
	public IValue getValue(int index, IParameter param);
	
	public IType getType(int index, IParameter param);
	
	public float getTypeMatch(int index, IParameter param);
	
	public float getVarargsTypeMatch(int index, IParameter param);
	
	public void checkValue(int index, IParameter param, ITypeContext typeContext, MarkerList markers, IContext context);
	
	public void checkVarargsValue(int index, IParameter param, ITypeContext typeContext, MarkerList markers, IContext context);
	
	public void writeValue(int index, Name name, IValue defaultValue, MethodWriter writer) throws BytecodeException;
	
	public void writeVarargsValue(int index, Name name, IType type, MethodWriter writer) throws BytecodeException;
	
	// Phase Methods
	
	public void resolveTypes(MarkerList markers, IContext context);
	
	public void resolve(MarkerList markers, IContext context);
	
	public void checkTypes(MarkerList markers, IContext context);
	
	public void check(MarkerList markers, IContext context);
	
	public void foldConstants();
	
	public void cleanup(IContext context, IClassCompilableList compilableList);
	
	@Override
	public void toString(String prefix, StringBuilder buffer);
	
	public void typesToString(StringBuilder buffer);
}

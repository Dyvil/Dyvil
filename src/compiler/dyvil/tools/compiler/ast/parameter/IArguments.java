package dyvil.tools.compiler.ast.parameter;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public interface IArguments extends IASTNode, Iterable<IValue>
{
	static TypeChecker.MarkerSupplier argumentMarkerSupplier(IParameter parameter)
	{
		return TypeChecker.markerSupplier("method.access.argument_type", parameter.getName());
	}

	float DEFAULT_MATCH = 1000;
	float VARARGS_MATCH = 100;

	@Override
	default ICodePosition getPosition()
	{
		return null;
	}
	
	@Override
	default void setPosition(ICodePosition position)
	{
	}
	
	int size();
	
	boolean isEmpty();
	
	// 'Variations'
	
	IArguments dropFirstValue();
	
	IArguments withLastValue(IValue value);
	
	default IArguments withLastValue(Name name, IValue value)
	{
		return this.withLastValue(value);
	}
	
	// First Values
	
	IValue getFirstValue();
	
	void setFirstValue(IValue value);
	
	// Last Values
	
	IValue getLastValue();
	
	void setLastValue(IValue value);
	
	// Used by Methods
	
	void setValue(int index, IParameter param, IValue value);
	
	IValue getValue(int index, IParameter param);
	
	float getTypeMatch(int index, IParameter param);
	
	float getVarargsTypeMatch(int index, IParameter param);
	
	void checkValue(int index, IParameter param, ITypeContext typeContext, MarkerList markers, IContext context);
	
	void checkVarargsValue(int index, IParameter param, ITypeContext typeContext, MarkerList markers, IContext context);
	
	void inferType(int index, IParameter param, ITypeContext typeContext);
	
	void inferVarargsType(int index, IParameter param, ITypeContext typeContext);
	
	void writeValue(int index, IParameter param, MethodWriter writer) throws BytecodeException;
	
	// Phase Methods

	boolean isResolved();
	
	void resolveTypes(MarkerList markers, IContext context);
	
	void resolve(MarkerList markers, IContext context);
	
	void checkTypes(MarkerList markers, IContext context);
	
	void check(MarkerList markers, IContext context);
	
	void foldConstants();
	
	void cleanup(IContext context, IClassCompilableList compilableList);
	
	@Override
	void toString(String prefix, StringBuilder buffer);
	
	void typesToString(StringBuilder buffer);
}

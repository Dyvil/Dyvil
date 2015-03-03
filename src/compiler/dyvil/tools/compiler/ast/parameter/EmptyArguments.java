package dyvil.tools.compiler.ast.parameter;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;

public class EmptyArguments implements IArguments
{
	@Override
	public Iterator<IValue> iterator()
	{
		return Collections.emptyIterator();
	}
	
	@Override
	public int size()
	{
		return 0;
	}
	
	@Override
	public boolean isEmpty()
	{
		return true;
	}
	
	@Override
	public IArguments dropFirstValue()
	{
		return null;
	}
	
	@Override
	public IArguments addLastValue(IValue value)
	{
		return new SingleArgument(value);
	}
	
	@Override
	public IValue getFirstValue()
	{
		return null;
	}
	
	@Override
	public void setFirstValue(IValue value)
	{
	}
	
	@Override
	public IValue getValue(Parameter param)
	{
		return null;
	}
	
	@Override
	public IType getType(Parameter param)
	{
		return null;
	}
	
	@Override
	public void writeValue(Parameter param, MethodWriter writer)
	{
		if (param.varargs)
		{
			writer.visitLdcInsn(0);
			writer.visitTypeInsn(Opcodes.ANEWARRAY, param.type);
		}
	}
	
	@Override
	public int getTypeMatch(Parameter param)
	{
		return 0;
	}
	
	@Override
	public int getVarargsTypeMatch(Parameter param)
	{
		return 3;
	}
	
	@Override
	public void checkValue(List<Marker> markers, Parameter param, ITypeContext context)
	{
	}
	
	@Override
	public void checkVarargsValue(List<Marker> markers, Parameter param, ITypeContext context)
	{
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
	}
	
	@Override
	public void resolve(List<Marker> markers, IContext context)
	{
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
	}
	
	@Override
	public void foldConstants()
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
	}
}

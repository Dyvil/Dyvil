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
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;

public final class EmptyArguments implements IArguments
{
	public static final EmptyArguments	VISIBLE		= new EmptyArguments(true);
	public static final EmptyArguments	INSTANCE	= new EmptyArguments(false);
	
	private boolean						visible;
	
	private EmptyArguments()
	{
	}
	
	private EmptyArguments(boolean visible)
	{
		this.visible = visible;
	}
	
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
	public IValue getValue(int index, Parameter param)
	{
		return null;
	}
	
	@Override
	public IType getType(int index, Parameter param)
	{
		return null;
	}
	
	@Override
	public void writeValue(int index, String name, IValue defaultValue, MethodWriter writer)
	{
		defaultValue.writeExpression(writer);
	}
	
	@Override
	public void writeVarargsValue(int index, String name, IType type, MethodWriter writer)
	{
		writer.writeLDC(0);
		writer.writeTypeInsn(Opcodes.ANEWARRAY, type);
	}
	
	@Override
	public int getTypeMatch(int index, Parameter param)
	{
		return param.defaultValue != null ? 3 : 0;
	}
	
	@Override
	public int getVarargsTypeMatch(int index, Parameter param)
	{
		return 3;
	}
	
	@Override
	public void checkValue(int index, Parameter param, List<Marker> markers, ITypeContext context)
	{
	}
	
	@Override
	public void checkVarargsValue(int index, Parameter param, List<Marker> markers, ITypeContext context)
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
		if (this.visible)
		{
			buffer.append(Formatting.Method.emptyParameters);
		}
	}
}

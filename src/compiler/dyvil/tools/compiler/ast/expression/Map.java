package dyvil.tools.compiler.ast.expression;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.type.ClassGenericType;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class Map implements IValue
{
	private static final class Types
	{
		protected static final IClass MAP_CLASS = Package.dyvilCollection.resolveClass("Map");
	}
	
	protected ICodePosition position;
	
	protected IValue[]	keys;
	protected IValue[]	values;
	
	protected int count;
	
	// Metadata
	private IType	type;
	private IType	keyType;
	private IType	valueType;
	
	public Map(ICodePosition position)
	{
		this.position = position;
	}
	
	public Map(ICodePosition position, IValue[] keys, IValue[] values, int count)
	{
		this.position = position;
		this.keys = keys;
		this.values = values;
		this.count = count;
	}
	
	@Override
	public int valueTag()
	{
		return MAP;
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}
	
	public IType getKeyType()
	{
		if (this.keyType != null)
		{
			return this.keyType;
		}
		return this.keyType = Array.getCommonType(this.keys, this.count);
	}
	
	public IType getValueType()
	{
		if (this.valueType != null)
		{
			return this.valueType;
		}
		return this.valueType = Array.getCommonType(this.values, this.count);
	}
	
	@Override
	public IType getType()
	{
		if (this.type == null)
		{
			ClassGenericType gt = new ClassGenericType(Map.Types.MAP_CLASS);
			gt.addType(this.getKeyType());
			gt.addType(this.getValueType());
			return this.type = gt;
		}
		return this.type;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		
		return this;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return IValue.super.isType(type);
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		return IValue.super.getTypeMatch(type);
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.count; i++)
		{
			this.keys[i].resolveTypes(markers, context);
			this.values[i].resolveTypes(markers, context);
		}
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.count; i++)
		{
			this.keys[i] = this.keys[i].resolve(markers, context);
			this.values[i] = this.values[i].resolve(markers, context);
		}
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.count; i++)
		{
			this.keys[i].checkTypes(markers, context);
			this.values[i].checkTypes(markers, context);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.count; i++)
		{
			this.keys[i].check(markers, context);
			this.values[i].check(markers, context);
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		for (int i = 0; i < this.count; i++)
		{
			this.keys[i] = this.keys[i].foldConstants();
			this.values[i] = this.values[i].foldConstants();
		}
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		for (int i = 0; i < this.count; i++)
		{
			this.keys[i] = this.keys[i].cleanup(context, compilableList);
			this.values[i] = this.values[i].cleanup(context, compilableList);
		}
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.writeInsn(Opcodes.ACONST_NULL);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.count <= 0)
		{
			buffer.append(Formatting.Expression.emptyArray);
			return;
		}
		
		buffer.append(Formatting.Expression.arrayStart);
		
		this.keys[0].toString(prefix, buffer);
		buffer.append(Formatting.Expression.mapSeparator);
		this.values[0].toString(prefix, buffer);
		for (int i = 1; i < this.count; i++)
		{
			buffer.append(Formatting.Expression.arraySeperator);
			this.keys[i].toString(prefix, buffer);
			buffer.append(Formatting.Expression.mapSeparator);
			this.values[i].toString(prefix, buffer);
		}
	}
}

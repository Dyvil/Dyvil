package dyvil.tools.compiler.ast.access;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.ast.generic.GenericData;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.PrimitiveType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

import org.objectweb.asm.Label;

public abstract class AbstractCall implements ICall, IValued
{
	protected ICodePosition	position;
	public IValue			instance;
	public IArguments		arguments;
	
	public IMethod			method;
	protected IType			type;
	protected GenericData	genericData;
	
	public void setGenericData(GenericData data)
	{
		this.genericData = data;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}
	
	protected GenericData getGenericData()
	{
		if (this.method == null || this.genericData != null && this.genericData.computedGenerics >= 0)
		{
			return this.genericData;
		}
		return this.genericData = this.method.getGenericData(this.genericData, this.instance, this.arguments);
	}
	
	@Override
	public boolean isPrimitive()
	{
		return this.method != null && (this.method.isIntrinsic() || this.getType().isPrimitive());
	}
	
	@Override
	public IType getType()
	{
		if (this.method == null)
		{
			return Types.UNKNOWN;
		}
		if (this.type == null)
		{
			this.type = this.method.getType().getConcreteType(this.getGenericData()).getReturnType();
			
			if (this.method.isIntrinsic() && (this.instance == null || this.instance.getType().isPrimitive()))
			{
				this.type = PrimitiveType.getPrimitiveType(this.type);
			}
		}
		return this.type;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return type == Types.VOID ? this : IValue.autoBox(this, this.getType(), type);
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (type == Types.VOID)
		{
			return true;
		}
		if (this.method == null)
		{
			return false;
		}
		return type.isSuperTypeOf(this.getType());
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (this.method == null)
		{
			return 0;
		}
		
		IType type1 = this.getType();
		if (type.equals(type1))
		{
			return 3;
		}
		else if (type.isSuperTypeOf(type1))
		{
			return 2;
		}
		return 0;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.instance = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.instance;
	}
	
	@Override
	public void setArguments(IArguments arguments)
	{
		this.arguments = arguments;
	}
	
	@Override
	public IArguments getArguments()
	{
		return this.arguments;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.resolveTypes(markers, context);
		}
		if (this.arguments.isEmpty())
		{
			this.arguments = EmptyArguments.VISIBLE;
		}
		else
		{
			this.arguments.resolveTypes(markers, context);
		}
	}
	
	protected void checkArguments(MarkerList markers, IContext context)
	{
		if (this.method != null)
		{
			this.instance = this.method.checkArguments(markers, this.position, context, this.instance, this.arguments, this.getGenericData());
		}
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.checkTypes(markers, context);
		}
		
		this.arguments.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.check(markers, context);
		}
		
		if (this.method != null)
		{
			this.method.checkCall(markers, this.position, context, this.instance, this.arguments, this.getGenericData());
		}
		
		this.arguments.check(markers, context);
	}
	
	@Override
	public IValue foldConstants()
	{
		if (this.instance != null)
		{
			this.instance = this.instance.foldConstants();
		}
		this.arguments.foldConstants();
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		if (this.instance != null)
		{
			this.instance = this.instance.cleanup(context, compilableList);
		}
		this.arguments.cleanup(context, compilableList);
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		this.method.writeCall(writer, this.instance, this.arguments, this.type);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		this.method.writeCall(writer, this.instance, this.arguments, Types.VOID);
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		this.method.writeJump(writer, dest, this.instance, this.arguments);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		this.method.writeInvJump(writer, dest, this.instance, this.arguments);
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		this.toString("", builder);
		return builder.toString();
	}
}

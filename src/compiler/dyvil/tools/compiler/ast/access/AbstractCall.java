package dyvil.tools.compiler.ast.access;

import dyvil.tools.asm.Label;
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
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public abstract class AbstractCall implements ICall, IValued
{
	protected ICodePosition position;
	
	protected IValue		instance;
	protected IArguments	arguments	= EmptyArguments.INSTANCE;
	protected GenericData	genericData;
	
	// Metadata
	protected IMethod	method;
	protected IType		type;
	
	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
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
	
	public void setGenericData(GenericData data)
	{
		this.genericData = data;
	}
	
	protected GenericData getGenericData()
	{
		if (this.method == null || this.genericData != null && this.genericData.method != null)
		{
			return this.genericData;
		}
		return this.genericData = this.method.getGenericData(this.genericData, this.instance, this.arguments);
	}
	
	public IMethod getMethod()
	{
		return this.method;
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
		return type == Types.VOID || type.isSuperTypeOf(this.getType()) ? this : null;
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
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.resolveArguments(markers, context);
		return this.resolveCall(markers, context);
	}
	
	protected void resolveArguments(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance = this.instance.resolve(markers, context);
		}
		
		this.arguments.resolve(markers, context);
	}
	
	protected abstract IValue resolveCall(MarkerList markers, IContext context);
	
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
	
	// Inlined for performance
	@Override
	public int getLineNumber()
	{
		if (this.position == null)
		{
			return 0;
		}
		return this.position.startLine();
	}
	
	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		this.method.writeCall(writer, this.instance, this.arguments, this.type, this.getLineNumber());
		this.type.writeCast(writer, type, this.getLineNumber());
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		this.method.writeCall(writer, this.instance, this.arguments, this.type, this.getLineNumber());
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		this.method.writeCall(writer, this.instance, this.arguments, Types.VOID, this.getLineNumber());
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		this.method.writeJump(writer, dest, this.instance, this.arguments, this.getLineNumber());
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		this.method.writeInvJump(writer, dest, this.instance, this.arguments, this.getLineNumber());
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		this.toString("", builder);
		return builder.toString();
	}
}

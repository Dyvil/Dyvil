package dyvil.tools.compiler.ast.access;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.method.IConstructor;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class InitializerCall implements ICall
{
	protected ICodePosition position;
	
	protected boolean		isSuper;
	protected IArguments	arguments	= EmptyArguments.INSTANCE;
	
	// Metadata
	protected IConstructor constructor;
	
	public InitializerCall(ICodePosition position, boolean isSuper)
	{
		this.position = position;
		this.isSuper = isSuper;
	}
	
	public InitializerCall(ICodePosition position, IConstructor constructor, IArguments arguments, boolean isSuper)
	{
		this.position = position;
		this.constructor = constructor;
		this.arguments = arguments;
		this.isSuper = isSuper;
	}
	
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
	public int valueTag()
	{
		return INITIALIZER_CALL;
	}
	
	@Override
	public IArguments getArguments()
	{
		return this.arguments;
	}
	
	@Override
	public void setArguments(IArguments arguments)
	{
		this.arguments = arguments;
	}
	
	@Override
	public IType getType()
	{
		return Types.VOID;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return type == Types.VOID ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Types.VOID;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.arguments.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.arguments.resolve(markers, context);
		
		if (this.constructor != null)
		{
			return this;
		}
		
		IClass iclass = context.getThisClass();
		if (this.isSuper)
		{
			iclass = iclass.getSuperType().getTheClass();
		}
		
		IConstructor match = IContext.resolveConstructor(iclass, this.arguments);
		if (match == null)
		{
			Marker marker = markers.create(this.position, "resolve.constructor", iclass.getName().qualified);
			if (!this.arguments.isEmpty())
			{
				StringBuilder builder = new StringBuilder("Argument Types: ");
				this.arguments.typesToString(builder);
				marker.addInfo(builder.toString());
			}
		}
		
		this.constructor = match;
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.arguments.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.arguments.check(markers, context);
	}
	
	@Override
	public IValue foldConstants()
	{
		this.arguments.foldConstants();
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.arguments.foldConstants();
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		throw new BytecodeException();
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		writer.writeVarInsn(Opcodes.ALOAD, 0);
		this.constructor.writeArguments(writer, this.arguments);
		this.constructor.writeInvoke(writer, this.getLineNumber());
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.isSuper ? "super.new" : "this.new");
		this.arguments.toString(prefix, buffer);
	}
}

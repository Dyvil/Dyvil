package dyvil.tools.compiler.ast.access;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.method.IConstructor;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class InitializerCall extends ASTNode implements IValue
{
	private IConstructor	constructor;
	private IArguments		arguments;
	private boolean			isSuper;
	
	public InitializerCall(ICodePosition position, IConstructor constructor, IArguments arguments, boolean isSuper)
	{
		this.position = position;
		this.constructor = constructor;
		this.arguments = arguments;
		this.isSuper = isSuper;
	}
	
	public InitializerCall(ICodePosition position, IConstructor constructor, IArguments arguments)
	{
		this.position = position;
		this.constructor = constructor;
		this.arguments = arguments;
	}
	
	@Override
	public int getValueType()
	{
		return INITIALIZER_CALL;
	}
	
	@Override
	public IType getType()
	{
		return Type.VOID;
	}
	
	@Override
	public IValue withType(IType type)
	{
		return type == Type.VOID ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Type.VOID;
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		return 0;
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
	public void writeExpression(MethodWriter writer)
	{
		// Should never happen
		this.writeStatement(writer);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		writer.writeVarInsn(Opcodes.ALOAD, 0);
		this.constructor.writeInvoke(writer, this.arguments);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.isSuper ? "super" : "this");
		if (this.arguments == EmptyArguments.INSTANCE)
		{
			buffer.append(Formatting.Method.emptyParameters);
			return;
		}
		this.arguments.toString(prefix, buffer);
	}
}

package dyvil.tools.compiler.ast.expression;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.field.IAccessible;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class ThisValue implements IValue
{
	protected ICodePosition	position;
	protected IType			type	= Types.UNKNOWN;
	
	// Metadata
	protected IAccessible getter;
	
	public ThisValue(IType type)
	{
		this.type = type;
	}
	
	public ThisValue(IType type, IAccessible getter)
	{
		this.type = type;
		this.getter = getter;
	}
	
	public ThisValue(ICodePosition position)
	{
		this.position = position;
	}
	
	public ThisValue(ICodePosition position, IType type, IContext context, MarkerList markers)
	{
		this.position = position;
		this.type = type;
		this.checkTypes(markers, context);
	}
	
	public ThisValue(ICodePosition position, IType type, IAccessible getter)
	{
		this.position = position;
		this.type = type;
		this.getter = getter;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}
	
	@Override
	public int valueTag()
	{
		return THIS;
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return type.isSuperTypeOf(this.type) ? this : null;
	}
	
	@Override
	public Object toObject()
	{
		return null;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (context.isStatic())
		{
			markers.add(this.position, "this.access.static");
		}
		
		if (this.type == Types.UNKNOWN)
		{
			this.type = context.getThisClass().getType();
		}
		else
		{
			this.type = this.type.resolve(markers, context, TypePosition.CLASS);
		}
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		IClass iclass = this.type.getTheClass();
		
		this.getter = context.getAccessibleThis(iclass);
		if (this.getter == null)
		{
			markers.add(this.position, "this.instance", iclass.getFullName());
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public IValue foldConstants()
	{
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("this");
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		this.getter.writeGet(writer);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		this.writeExpression(writer);
		writer.writeInsn(Opcodes.ARETURN);
	}
}

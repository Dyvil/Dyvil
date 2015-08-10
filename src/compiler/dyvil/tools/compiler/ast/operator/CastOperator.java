package dyvil.tools.compiler.ast.operator;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.Value;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class CastOperator extends Value
{
	protected IValue	value;
	protected IType		type;
	
	// Metadata
	private boolean typeHint;
	
	public CastOperator(ICodePosition position, IValue value)
	{
		this.position = position;
		this.value = value;
	}
	
	public CastOperator(IValue value, IType type)
	{
		this.value = value;
		this.type = type;
	}
	
	@Override
	public int valueTag()
	{
		return CAST_OPERATOR;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return this.type.isPrimitive();
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return IValue.autoBox(this, this.type, type);
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.type = this.type.resolve(markers, context, TypePosition.TYPE);
		this.value.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.value = this.value.resolve(markers, context);
		if (this.type == Types.VOID)
		{
			markers.add(this.position, "cast.void");
			return this;
		}
		
		if (!this.type.isResolved())
		{
			return this;
		}
		
		IType prevType = this.value.getType();
		
		IValue value1 = this.value.withType(this.type, this.type, markers, context);
		if (value1 != null && value1 != this.value)
		{
			this.value = value1;
			this.typeHint = true;
			this.type = value1.getType();
			return this;
		}
		
		boolean primitiveType = this.type.isPrimitive();
		boolean primitiveValue = this.value.isPrimitive();
		if (primitiveType)
		{
			if (!primitiveValue)
			{
				markers.add(this.position, "cast.reference");
			}
		}
		else if (primitiveValue)
		{
			markers.add(this.position, "cast.primitive");
		}
		
		if (value1 == null && !(primitiveType && primitiveValue) && !prevType.isSuperClassOf(this.type))
		{
			markers.add(this.position, "cast.incompatible", prevType, this.type);
			return this;
		}
		
		if (!this.typeHint && this.type.equals(prevType))
		{
			markers.add(this.position, "cast.unnecessary");
			this.typeHint = true;
		}
		
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.value.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.value.check(markers, context);
		
	}
	
	@Override
	public IValue foldConstants()
	{
		this.value = this.value.foldConstants();
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		if (this.typeHint)
		{
			return this.value.cleanup(context, compilableList);
		}
		
		this.value = this.value.cleanup(context, compilableList);
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		this.value.writeExpression(writer);
		if (this.typeHint)
		{
			return;
		}
		
		this.value.getType().writeCast(writer, this.type, this.getLineNumber());
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		this.writeExpression(writer);
		writer.writeInsn(this.type.getReturnOpcode());
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.value.toString(prefix, buffer);
		buffer.append(" as ");
		this.type.toString(prefix, buffer);
	}
}

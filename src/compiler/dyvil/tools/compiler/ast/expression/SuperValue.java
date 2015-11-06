package dyvil.tools.compiler.ast.expression;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class SuperValue implements IValue
{
	protected ICodePosition	position;
	protected IType			type	= Types.UNKNOWN;
	
	public SuperValue(ICodePosition position)
	{
		this.position = position;
	}
	
	public SuperValue(ICodePosition position, IType type)
	{
		this.position = position;
		this.type = type;
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
		return SUPER;
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
			markers.add(I18n.createMarker(this.position, "super.access.static"));
			return;
		}
		
		IType thisType = context.getThisClass().getType();
		if (this.type == Types.UNKNOWN)
		{
			this.type = thisType.getSuperType();
			if (this.type == null)
			{
				Marker marker = I18n.createMarker(this.position, "super.access.type");
				marker.addInfo("Enclosing Type: " + thisType);
				markers.add(marker);
			}
			return;
		}
		
		this.type = this.type.resolveType(markers, context);
		if (!this.type.isResolved())
		{
			return;
		}
		
		int distance = this.type.getSubClassDistance(thisType);
		if (distance == 1)
		{
			return;
		}
		
		Marker marker = I18n.createMarker(this.position, distance == 0 ? "super.type.invalid" : "super.type.indirect");
		marker.addInfo("Enclosing Type: " + thisType);
		marker.addInfo("Requested Super Type: " + this.type);
		markers.add(marker);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.type.resolve(markers, context);
		
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.type.checkType(markers, context, TypePosition.CLASS);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.type.check(markers, context);
	}
	
	@Override
	public IValue foldConstants()
	{
		this.type.foldConstants();
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.type.cleanup(context, compilableList);
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.writeVarInsn(Opcodes.ALOAD, 0);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		writer.writeVarInsn(Opcodes.ALOAD, 0);
		writer.writeInsn(Opcodes.ARETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("super");
		
		if (this.type != Types.UNKNOWN)
		{
			buffer.append('[');
			this.type.toString(prefix, buffer);
			buffer.append(']');
		}
	}
}

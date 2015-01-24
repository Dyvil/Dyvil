package dyvil.tools.compiler.ast.operator;

import java.util.List;

import jdk.internal.org.objectweb.asm.Label;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.PrimitiveType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.util.OpcodeUtil;

public class CastOperator extends ASTNode implements IValue
{
	public IValue	value;
	public IType	type;
	
	public CastOperator(IValue value, IType type)
	{
		this.value = value;
		this.type = type;
	}
	
	@Override
	public int getValueType()
	{
		return CAST_OPERATOR;
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		this.value.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
		this.value = this.value.resolve(markers, context);
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		this.value.check(markers, context);
		
		if (this.type == Type.VOID)
		{
			markers.add(Markers.create(this.position, "cast.void"));
		}
		
		boolean primitiveType = this.type.isPrimitive();
		IType type = this.value.getType();
		if (primitiveType)
		{
			if (!type.isPrimitive())
			{
				markers.add(Markers.create(this.position, "cast.reference"));
			}
		}
		else if (type.isPrimitive())
		{
			markers.add(Markers.create(this.position, "cast.primitive"));
		}
		else if (this.value.isType(this.type))
		{
			markers.add(Markers.create(this.position, "cast.unnecessary"));
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		this.value.writeExpression(writer);
		if (this.type.isPrimitive())
		{
			OpcodeUtil.writePrimitiveCast((PrimitiveType) this.value.getType(), (PrimitiveType) this.type, writer);
		}
		else
		{
			writer.visitTypeInsn(Opcodes.CHECKCAST, this.type);
		}
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label dest)
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.value.toString(prefix, buffer);
		buffer.append(" :> ");
		this.type.toString(prefix, buffer);
	}
}

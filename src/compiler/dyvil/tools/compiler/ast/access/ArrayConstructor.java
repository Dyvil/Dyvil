package dyvil.tools.compiler.ast.access;

import java.util.ArrayList;
import java.util.List;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class ArrayConstructor extends ASTNode implements IValue, ITyped
{
	public IType		type;
	public List<IValue>	lengths	= new ArrayList();
	
	public ArrayConstructor(ICodePosition position)
	{
		this.position = position;
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
	public IValue withType(IType type)
	{
		return Type.isSuperType(type, this.type) ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return Type.isSuperType(type, this.type);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (this.type.equals(type))
		{
			return 3;
		}
		else if (type.isSuperTypeOf(this.type))
		{
			return 2;
		}
		return 0;
	}
	
	@Override
	public int getValueType()
	{
		return ARRAY_CONSTRUCTOR;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		this.type = this.type.resolve(context);
		if (!this.type.isResolved())
		{
			markers.add(Markers.create(this.type.getPosition(), "resolve.type", this.type.toString()));
		}
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
	}
	
	@Override
	public IValue foldConstants()
	{
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		int len = this.lengths.size();
		
		if (len == 1)
		{
			this.lengths.get(0).writeExpression(writer);
			this.type.setArrayDimensions(0);
			writer.visitTypeInsn(Opcodes.ANEWARRAY, this.type);
			this.type.setArrayDimensions(1);
			return;
		}
		
		for (int i = 0; i < len; i++)
		{
			this.lengths.get(i).writeExpression(writer);
		}
		
		writer.visitMultiANewArrayInsn(this.type, len);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		this.writeExpression(writer);
		writer.visitInsn(Opcodes.ARETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("new ");
		int dims = this.type.getArrayDimensions();
		this.type.setArrayDimensions(0);
		this.type.toString(prefix, buffer);
		
		for (IValue v : this.lengths)
		{
			buffer.append('[');
			v.toString(prefix, buffer);
			buffer.append(']');
		}
		
		this.type.setArrayDimensions(dims);
	}
}

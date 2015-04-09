package dyvil.tools.compiler.ast.operator;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.GenericType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class ClassOperator extends ASTNode implements IValue
{
	private IType	type;
	private IType	genericType;
	public boolean	dotless;
	
	public ClassOperator(ICodePosition position)
	{
		this.position = position;
	}
	
	public ClassOperator(IType type)
	{
		this.setType(type);
	}
	
	@Override
	public int getValueType()
	{
		return CLASS_OPERATOR;
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
	
	@Override
	public IType getType()
	{
		if (this.genericType == null)
		{
			GenericType generic = new GenericType(Types.CLASS_CLASS);
			generic.addType(type);
			return this.genericType = generic;
		}
		return this.genericType;
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (this.genericType == null)
		{
			GenericType generic = new GenericType(Types.CLASS_CLASS);
			generic.addType(type);
			this.genericType = generic;
		}
		return type.isSuperTypeOf(this.genericType);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (type.equals(this.genericType))
		{
			return 3;
		}
		if (type.isSuperTypeOf(this.genericType))
		{
			return 2;
		}
		return 0;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.type = this.type.resolve(markers, context);
		GenericType generic = new GenericType(Types.CLASS_CLASS);
		generic.addType(type);
		this.genericType = generic;
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
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
	public void writeExpression(MethodWriter writer)
	{
		org.objectweb.asm.Type t = org.objectweb.asm.Type.getType(this.type.getExtendedName());
		writer.writeLDC(t);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		this.writeExpression(writer);
		writer.writeInsn(Opcodes.ARETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("class[");
		this.type.toString(prefix, buffer);
		buffer.append(']');
	}
}

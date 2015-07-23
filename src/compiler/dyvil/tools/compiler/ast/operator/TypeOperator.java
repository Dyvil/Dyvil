package dyvil.tools.compiler.ast.operator;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.LiteralExpression;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.type.ClassGenericType;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class TypeOperator extends ASTNode implements IValue
{
	private IType	type;
	private IType	genericType;
	public boolean	dotless;
	
	public TypeOperator(ICodePosition position)
	{
		this.position = position;
	}
	
	public TypeOperator(IType type)
	{
		this.setType(type);
	}
	
	@Override
	public int valueTag()
	{
		return TYPE_OPERATOR;
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
			ClassGenericType generic = new ClassGenericType(Types.TYPE_CLASS);
			generic.addType(this.type);
			return this.genericType = generic;
		}
		return this.genericType;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (type.getTheClass().getAnnotation(Types.TYPE_CONVERTIBLE) != null)
		{
			return new LiteralExpression(this).withType(type, typeContext, markers, context);
		}
		
		return type.isSuperTypeOf(this.getType()) ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (type.getTheClass().getAnnotation(Types.TYPE_CONVERTIBLE) != null)
		{
			return true;
		}
		
		return type.isSuperTypeOf(this.getType());
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		if (type.getTheClass().getAnnotation(Types.TYPE_CONVERTIBLE) != null)
		{
			return CONVERSION_MATCH;
		}
		
		return type.getSubTypeDistance(this.getType());
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.type == null)
		{
			this.type = Types.UNKNOWN;
			markers.add(this.position, "typeoperator.invalid");
			return;
		}
		
		this.type = this.type.resolve(markers, context, TypePosition.TYPE);
		ClassGenericType generic = new ClassGenericType(Types.TYPE_CLASS);
		generic.addType(this.type);
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
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		this.type.writeTypeExpression(writer);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		this.writeExpression(writer);
		writer.writeInsn(Opcodes.ARETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("type[");
		this.type.toString(prefix, buffer);
		buffer.append(']');
	}
}

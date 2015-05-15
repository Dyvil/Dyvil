package dyvil.tools.compiler.ast.operator;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.LiteralExpression;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.GenericType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class TypeOperator extends ASTNode implements IValue
{
	public static final IClass	TYPE_CONVERTIBLE	= Package.dyvilLangLiteral.resolveClass("TypeConvertible");
	
	private IType				type;
	private IType				genericType;
	public boolean				dotless;
	
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
			GenericType generic = new GenericType(Types.TYPE_CLASS);
			generic.addType(this.type);
			return this.genericType = generic;
		}
		return this.genericType;
	}
	
	@Override
	public IValue withType(IType type)
	{
		if (type.getTheClass().getAnnotation(TYPE_CONVERTIBLE) != null)
		{
			return new LiteralExpression(type, this);
		}
		
		return type.isSuperTypeOf(this.getType()) ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (type.getTheClass().getAnnotation(TYPE_CONVERTIBLE) != null)
		{
			return true;
		}
		
		return type.isSuperTypeOf(this.getType());
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (type.getTheClass().getAnnotation(TYPE_CONVERTIBLE) != null)
		{
			return 2;
		}
		
		IType thisType = this.getType();
		if (type.equals(thisType))
		{
			return 3;
		}
		if (type.isSuperTypeOf(thisType))
		{
			return 2;
		}
		return 0;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.type = this.type.resolve(markers, context);
		GenericType generic = new GenericType(Types.TYPE_CLASS);
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

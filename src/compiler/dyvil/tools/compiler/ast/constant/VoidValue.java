package dyvil.tools.compiler.ast.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class VoidValue implements IConstantValue
{
	protected ICodePosition position;
	
	public VoidValue(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int valueTag()
	{
		return VOID;
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
	public IType getType()
	{
		return Types.VOID;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return type == Types.VOID || type.isSuperTypeOf(Types.VOID) ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Types.VOID || type.isSuperTypeOf(Types.VOID);
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		return type.getSubTypeDistance(Types.VOID);
	}
	
	@Override
	public int stringSize()
	{
		return 0;
	}
	
	@Override
	public boolean toStringBuilder(StringBuilder builder)
	{
		return false;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.writeFieldInsn(Opcodes.GETSTATIC, "dyvil/lang/Void", "instance", "Ldyvil/lang/Void;");
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("()");
	}
}

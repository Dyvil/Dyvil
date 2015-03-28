package dyvil.tools.compiler.ast.access;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.parameter.Parameter;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public final class ClassParameterSetter implements IValue
{
	private Parameter	parameter;
	private String		owner;
	private String		desc;
	
	public ClassParameterSetter(Parameter param, String owner, String desc)
	{
		this.parameter = param;
		this.owner = owner;
		this.desc = desc;
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		writer.writeVarInsn(Opcodes.ALOAD, 0);
		writer.writeVarInsn(this.parameter.type.getLoadOpcode(), this.parameter.index);
		writer.writePutField(this.owner, this.parameter.qualifiedName, this.desc);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("this.").append(this.parameter.name).append(" = ").append(this.parameter.name);
	}
	
	// ----- Ignore -----
	
	@Override
	public int getValueType()
	{
		return 0;
	}
	
	@Override
	public IType getType()
	{
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return false;
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		return 0;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
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
		this.writeStatement(writer);
	}
}

package dyvil.tools.compiler.ast.access;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public final class ClassParameterSetter implements IValue
{
	private IClass theClass;
	private IParameter	parameter;
	
	public ClassParameterSetter(IClass theClass, IParameter param)
	{
		this.theClass = theClass;
		this.parameter = param;
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		writer.writeVarInsn(Opcodes.ALOAD, 0);
		writer.writeVarInsn(this.parameter.getType().getLoadOpcode(), this.parameter.getIndex());
		writer.writeFieldInsn(Opcodes.PUTFIELD, this.theClass.getInternalName(), this.parameter.getName().qualified, this.parameter.getDescription());
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		Name name = this.parameter.getName();
		buffer.append("this.").append(name).append(" = ").append(name);
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

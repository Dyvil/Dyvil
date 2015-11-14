package dyvil.tools.compiler.ast.access;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class ClassParameterSetter implements IValue
{
	private IClass		theClass;
	private IParameter	parameter;
	
	public ClassParameterSetter(IClass theClass, IParameter param)
	{
		this.theClass = theClass;
		this.parameter = param;
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		writer.writeVarInsn(Opcodes.ALOAD, 0);
		writer.writeVarInsn(this.parameter.getType().getLoadOpcode(), this.parameter.getLocalIndex());
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
	public int valueTag()
	{
		return 0;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return null;
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
	}
	
	@Override
	public boolean isResolved()
	{
		return true;
	}
	
	@Override
	public IType getType()
	{
		return null;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return false;
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
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		this.writeStatement(writer);
	}
}

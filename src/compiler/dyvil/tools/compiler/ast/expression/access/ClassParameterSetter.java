package dyvil.tools.compiler.ast.expression.access;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.lang.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public final class ClassParameterSetter implements IValue
{
	private IClass     theClass;
	private IParameter parameter;

	public ClassParameterSetter(IClass theClass, IParameter param)
	{
		this.theClass = theClass;
		this.parameter = param;
	}

	@Override
	public SourcePosition getPosition()
	{
		return this.parameter.getPosition();
	}

	@Override
	public void setPosition(SourcePosition position)
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
		return Types.VOID;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		writer.visitVarInsn(Opcodes.ALOAD, 0);
		writer.visitVarInsn(this.parameter.getType().getLoadOpcode(), this.parameter.getLocalIndex());
		writer.visitFieldInsn(Opcodes.PUTFIELD, this.theClass.getInternalName(), this.parameter.getName().qualified,
		                      this.parameter.getDescriptor());
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
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		return this;
	}
}

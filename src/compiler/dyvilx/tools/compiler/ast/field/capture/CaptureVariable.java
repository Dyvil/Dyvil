package dyvilx.tools.compiler.ast.field.capture;

import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;

import java.lang.annotation.ElementType;
import java.util.function.Function;

public class CaptureVariable extends CaptureDataMember implements IVariable
{
	public static Function<? super IVariable, CaptureVariable> FACTORY = CaptureVariable::new;

	public CaptureVariable()
	{
	}

	public CaptureVariable(IVariable variable)
	{
		this.variable = variable;
	}

	@Override
	public ElementType getElementType()
	{
		return ElementType.LOCAL_VARIABLE;
	}

	@Override
	public boolean isAssigned()
	{
		return this.variable.isAssigned();
	}

	@Override
	public IType getReferenceType()
	{
		return this.variable.getReferenceType();
	}

	@Override
	public void setReferenceType()
	{
		this.variable.setReferenceType();
	}

	@Override
	public void writeGet_Get(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		writer.visitVarInsn(this.variable.getInternalType().getLoadOpcode(), this.localIndex);
	}

	@Override
	public void writeSet_Set(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		if (this.variable.getReferenceType() == null)
		{
			writer.visitVarInsn(this.variable.getInternalType().getStoreOpcode(), this.localIndex);
		}
	}

	@Override
	public void writeLocal(MethodWriter writer, Label start, Label end)
	{
	}

	@Override
	public void writeInit(MethodWriter writer, IValue value) throws BytecodeException
	{
	}
}

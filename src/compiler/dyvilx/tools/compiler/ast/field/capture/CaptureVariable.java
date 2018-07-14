package dyvilx.tools.compiler.ast.field.capture;

import dyvil.annotation.internal.NonNull;
import dyvil.reflect.Opcodes;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.WriteableExpression;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.transform.Names;

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
	public boolean setAssigned()
	{
		return this.variable.setAssigned() && this.variable.setReferenceType();
	}

	@Override
	public IType getReferenceType()
	{
		return this.variable.getReferenceType();
	}

	@Override
	public boolean setReferenceType()
	{
		return this.variable.setReferenceType();
	}

	private WriteableExpression asWriteableExpression()
	{
		return (writer, type) -> writer.visitVarInsn(Opcodes.ALOAD, this.localIndex);
	}

	@Override
	public void writeGetRaw(@NonNull MethodWriter writer, WriteableExpression receiver, int lineNumber)
	{
		writer.visitVarInsn(Opcodes.AUTO_LOAD, this.localIndex);
	}

	@Override
	public void writeGet(@NonNull MethodWriter writer, WriteableExpression receiver, int lineNumber)
		throws BytecodeException
	{
		final IType referenceType = this.variable.getReferenceType();
		if (referenceType != null)
		{
			referenceType.resolveField(Names.value).writeGet(writer, this.asWriteableExpression(), lineNumber);
			return;
		}

		writer.visitVarInsn(Opcodes.AUTO_LOAD, this.localIndex);
	}

	@Override
	public void writeSet(@NonNull MethodWriter writer, WriteableExpression receiver, @NonNull WriteableExpression value,
		int lineNumber) throws BytecodeException
	{
		final IType referenceType = this.variable.getReferenceType();
		assert referenceType != null;

		referenceType.resolveField(Names.value).writeSet(writer, this.asWriteableExpression(), value, lineNumber);
	}

	@Override
	public void writeSetCopy(@NonNull MethodWriter writer, WriteableExpression receiver,
		@NonNull WriteableExpression value, int lineNumber) throws BytecodeException
	{
		final IType referenceType = this.variable.getReferenceType();
		assert referenceType != null;

		referenceType.resolveField(Names.value).writeSetCopy(writer, this.asWriteableExpression(), value, lineNumber);
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

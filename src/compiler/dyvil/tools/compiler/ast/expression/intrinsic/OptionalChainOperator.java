package dyvil.tools.compiler.ast.expression.intrinsic;

import dyvil.annotation.internal.NonNull;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.compound.NullableType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class OptionalChainOperator implements IValue, OptionalChainAware
{
	protected IValue receiver;

	// Metadata
	protected ICodePosition position;
	protected Label         elseLabel;

	public OptionalChainOperator(IValue receiver)
	{
		this.receiver = receiver;
	}

	@Override
	public int valueTag()
	{
		return OPTIONAL_CHAIN;
	}

	@Override
	public boolean needsOptionalElseLabel()
	{
		return this.elseLabel == null;
	}

	@Override
	public Label getOptionalElseLabel()
	{
		return this.elseLabel;
	}

	@Override
	public boolean setOptionalElseLabel(Label label)
	{
		this.receiver.setOptionalElseLabel(label);
		this.elseLabel = label;
		return true;
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
	public boolean isResolved()
	{
		return this.receiver.isResolved();
	}

	@Override
	public IType getType()
	{
		return NullableType.unapply(this.receiver.getType());
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		this.receiver = TypeChecker.convertValue(this.receiver, NullableType.apply(type), typeContext, markers, context,
		                                         TypeChecker.markerSupplier("optional.chain.type.incompatible"));
		return this;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.receiver.resolveTypes(markers, context);
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.receiver = this.receiver.resolve(markers, context);
		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.receiver.checkTypes(markers, context);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.receiver.check(markers, context);

		if (this.elseLabel == null)
		{
			markers.add(Markers.semanticError(this.position, "optional.chain.invalid"));
		}
	}

	@Override
	public IValue foldConstants()
	{
		this.receiver = this.receiver.foldConstants();
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.receiver = this.receiver.cleanup(compilableList, classCompilableList);
		return this;
	}

	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		this.receiver.toString(indent, buffer);
		buffer.append('?');
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		this.receiver.writeExpression(writer, type);

		if (this.elseLabel == null)
		{
			return;
		}

		final int varIndex = writer.localCount();

		writer.visitInsn(Opcodes.DUP);
		writer.visitVarInsn(Opcodes.ASTORE, varIndex);
		writer.visitJumpInsn(Opcodes.IFNULL, this.elseLabel);
		writer.visitVarInsn(Opcodes.ALOAD, varIndex);
	}
}

package dyvilx.tools.compiler.ast.expression.optional;

import dyvil.annotation.internal.NonNull;
import dyvil.lang.Formattable;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.AnnotationVisitor;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.compound.NullableType;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.transform.TypeChecker;
import dyvilx.tools.parsing.marker.MarkerList;

public class OptionalUnwrapOperator implements IValue
{
	protected IValue receiver;

	// Metadata
	protected boolean        force;
	protected SourcePosition position;

	public OptionalUnwrapOperator(IValue receiver)
	{
		this.receiver = receiver;
	}

	public OptionalUnwrapOperator(IValue receiver, boolean force)
	{
		this.receiver = receiver;
		this.force = force;
	}

	@Override
	public int valueTag()
	{
		return OPTIONAL_UNWRAP;
	}

	@Override
	public SourcePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
		this.position = position;
	}

	public IValue getReceiver()
	{
		return this.receiver;
	}

	public void setReceiver(IValue receiver)
	{
		this.receiver = receiver;
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
	public void setType(IType type)
	{
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		this.receiver = TypeChecker.convertValue(this.receiver, NullableType.apply(type), typeContext, markers, context,
		                                         TypeChecker.markerSupplier(this.getTypeError()));
		return this;
	}

	protected String getTypeError()
	{
		return "optional.unwrap.type.incompatible";
	}

	@Override
	public boolean isAnnotationConstant()
	{
		return this.receiver.isAnnotationConstant();
	}

	@Override
	public IValue toAnnotationConstant(MarkerList markers, IContext context, int depth)
	{
		return this.receiver.toAnnotationConstant(markers, context, depth);
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
		return Formattable.toString(this);
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		this.receiver.toString(indent, buffer);
		buffer.append('!');
		if (this.force)
		{
			buffer.append('!');
		}
	}

	@Override
	public void writeNullCheckedExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		this.receiver.writeExpression(writer, type == null ? null : NullableType.unapply(type));
	}

	@Override
	public void writeAnnotationValue(AnnotationVisitor visitor, String key)
	{
		this.receiver.writeAnnotationValue(visitor, key);
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		this.receiver.writeExpression(writer, type == null ? null : NullableType.unapply(type));
		if (this.force || this.getType().isPrimitive())
		{
			return;
		}

		writer.visitInsn(Opcodes.DUP);
		writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
		writer.visitInsn(Opcodes.POP);
	}
}

package dyvilx.tools.gensrc.ast.directive;

import dyvil.annotation.internal.NonNull;
import dyvil.lang.Name;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.access.FieldAccess;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.gensrc.ast.GenSrcValue;
import dyvilx.tools.parsing.marker.MarkerList;

public class WriteCall implements GenSrcValue
{
	protected IValue writerField;
	protected IValue value;

	public WriteCall()
	{
	}

	public WriteCall(IValue value)
	{
		this.value = value;
	}

	@Override
	public int valueTag()
	{
		return WRITE_CALL;
	}

	@Override
	public SourcePosition getPosition()
	{
		return this.value.getPosition();
	}

	@Override
	public void setPosition(SourcePosition position)
	{
	}

	public IValue getValue()
	{
		return this.value;
	}

	public void setValue(IValue value)
	{
		this.value = value;
	}

	@Override
	public IType getType()
	{
		return Types.VOID;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.value.resolveTypes(markers, context);
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.value = this.value.resolve(markers, context);
		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.writerField = new FieldAccess(this.getPosition(), null, Name.fromRaw("writer")).resolve(markers, context);
		this.writerField.checkTypes(markers, context);
		this.value.checkTypes(markers, context);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.writerField.check(markers, context);
		this.value.check(markers, context);
	}

	@Override
	public IValue foldConstants()
	{
		this.writerField = this.writerField.foldConstants();
		this.value = this.value.foldConstants();
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.writerField = this.writerField.cleanup(compilableList, classCompilableList);
		this.value = this.value.cleanup(compilableList, classCompilableList);
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		this.writerField.writeExpression(writer, null);

		this.value.writeExpression(writer, Types.STRING);

		writer.visitLineNumber(this.lineNumber());
		writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/Writer", "write", "(Ljava/lang/String;)V", false);
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		buffer.append("#(");
		this.value.toString(indent, buffer);
		buffer.append(')');
	}
}

package dyvil.tools.compiler.ast.expression.intrinsic;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.ILabelContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class PopExpr implements IValue
{
	private IValue value;

	public PopExpr(IValue value)
	{
		this.value = value;
	}

	@Override
	public int valueTag()
	{
		return POP_EXPR;
	}

	@Override
	public ICodePosition getPosition()
	{
		return this.value.getPosition();
	}

	@Override
	public void setPosition(ICodePosition position)
	{
	}

	@Override
	public boolean isIgnoredClassAccess()
	{
		return true;
	}

	@Override
	public IValue asIgnoredClassAccess()
	{
		return this;
	}

	@Override
	public boolean isResolved()
	{
		return this.value.isResolved();
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
	public void resolveStatement(ILabelContext context, MarkerList markers)
	{
		this.value.resolveStatement(context, markers);
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.value.checkTypes(markers, context);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.value.check(markers, context);
	}

	@Override
	public IValue foldConstants()
	{
		this.value = this.value.foldConstants();
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.value = this.value.foldConstants();
		return this;
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("__discard__ ");
		this.value.toString(prefix, buffer);
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		final IType valueType = this.value.getType();
		this.value.writeExpression(writer, null);

		if (!Types.isVoid(valueType))
		{
			writer.visitInsn(Opcodes.AUTO_POP);
		}
	}
}

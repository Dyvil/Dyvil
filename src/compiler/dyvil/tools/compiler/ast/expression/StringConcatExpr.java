package dyvil.tools.compiler.ast.expression;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.constant.StringValue;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.CaseClasses;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class StringConcatExpr implements IValue
{
	private IValue[] values = new IValue[3];
	private int valueCount;

	public StringConcatExpr()
	{
	}

	@Override
	public int valueTag()
	{
		return STRINGBUILDER;
	}

	@Override
	public ICodePosition getPosition()
	{
		return this.values[0].getPosition();
	}

	@Override
	public void setPosition(ICodePosition position)
	{
	}

	public void addFirstValue(IValue value)
	{
		final int index = this.valueCount++;
		if (index >= this.values.length)
		{
			final IValue[] temp = new IValue[index + 1];
			System.arraycopy(this.values, 0, temp, 1, index);
			temp[0] = value;
			this.values = temp;
			return;
		}

		System.arraycopy(this.values, 0, this.values, 1, index);
		this.values[0] = value;
	}

	public void addValue(IValue value)
	{
		final int index = this.valueCount++;
		if (index >= this.values.length)
		{
			final IValue[] temp = new IValue[index + 1];
			System.arraycopy(this.values, 0, temp, 0, index);
			this.values = temp;
		}

		this.values[index] = value;
	}

	@Override
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public IType getType()
	{
		return Types.STRING;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i].resolveTypes(markers, context);
		}
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i] = this.values[i].resolve(markers, context);
		}
		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i].checkTypes(markers, context);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			IValue value = this.values[i];
			value.check(markers, context);

			if (value.getType() == Types.VOID)
			{
				markers.add(Markers.semantic(value.getPosition(), "string.concat.void"));
			}
		}
	}

	@Override
	public IValue foldConstants()
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i] = this.values[i].foldConstants();
		}

		final StringBuilder buffer = new StringBuilder();
		int start = -1;
		for (int i = 0; i <= this.valueCount; i++)
		{
			final String stringValue = i == this.valueCount ? null : this.values[i].stringValue();
			if (stringValue != null)
			{
				if (start < 0)
				{
					start = i;
				}
				buffer.append(stringValue);
			}
			else if (start >= 0)
			{
				this.values[start] = new StringValue(buffer.toString());
				System.arraycopy(this.values, i, this.values, start + 1, this.valueCount - i);
				this.valueCount -= i - start - 1;
				i = start + 1;
				start = -1;
				buffer.delete(0, buffer.length());
			}
		}

		final IValue firstValue = this.values[0];
		if (this.valueCount == 1 && firstValue.isType(Types.STRING))
		{
			return firstValue;
		}

		return this;
	}

	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i] = this.values[i].cleanup(context, compilableList);
		}

		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		if (this.valueCount == 2 && this.values[0].isType(Types.STRING) && this.values[1].isType(Types.STRING))
		{
			this.values[0].writeExpression(writer, Types.STRING);
			this.values[1].writeExpression(writer, Types.STRING);
			writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "concat",
			                       "(Ljava/lang/String;)Ljava/lang/String;", false);
			return;
		}

		int estSize = 0;
		for (int i = 0; i < this.valueCount; i++)
		{
			estSize += this.values[i].stringSize();
		}

		writer.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
		writer.visitInsn(Opcodes.DUP);
		writer.visitLdcInsn(estSize);
		writer.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(I)V", false);

		for (int i = 0; i < this.valueCount; i++)
		{
			IValue value = this.values[i];
			if (value.valueTag() == IValue.STRING || value.valueTag() == IValue.CHAR)
			{
				String string = value.stringValue();
				CaseClasses.writeStringAppend(writer, string);
				continue;
			}

			value.writeExpression(writer, null);
			CaseClasses.writeStringAppend(writer, value.getType());
		}

		writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;",
		                       false);

		if (type != null)
		{
			Types.STRING.writeCast(writer, type, this.getLineNumber());
		}
	}

	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.valueCount <= 0)
		{
			return;
		}

		this.values[0].toString(prefix, buffer);
		for (int i = 1; i < this.valueCount; i++)
		{
			buffer.append(" + ");
			this.values[i].toString(prefix, buffer);
		}
	}
}

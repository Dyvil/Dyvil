package dyvilx.tools.compiler.ast.expression.constant;

import dyvil.lang.Formattable;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;

public class VoidValue implements IConstantValue
{
	protected SourcePosition position;

	public VoidValue()
	{
	}

	public VoidValue(SourcePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int valueTag()
	{
		return VOID;
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

	@Override
	public boolean isStatement()
	{
		return true;
	}

	@Override
	public boolean isUsableAsStatement()
	{
		return true;
	}

	@Override
	public IType getType()
	{
		return Types.VOID;
	}

	@Override
	public int stringSize()
	{
		return 0;
	}
	
	@Override
	public boolean toStringBuilder(StringBuilder builder)
	{
		return false;
	}
	
	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		if (type == null || Types.isVoid(type))
		{
			return;
		}

		// writer.visitFieldInsn(Opcodes.GETSTATIC, "dyvil/lang/Void", "instance", "Ldyvil/lang/Void;");
		writer.visitInsn(Opcodes.ACONST_NULL);
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("()");
	}
}

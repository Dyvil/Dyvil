package dyvil.tools.compiler.ast.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.position.ICodePosition;

public class VoidValue implements IConstantValue
{
	protected ICodePosition position;

	public VoidValue()
	{
	}

	public VoidValue(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int valueTag()
	{
		return VOID;
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
		return IASTNode.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("()");
	}
}

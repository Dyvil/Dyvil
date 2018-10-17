package dyvilx.tools.compiler.ast.pattern.constant;

import dyvil.lang.internal.Null;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.pattern.Pattern;
import dyvilx.tools.compiler.ast.pattern.AbstractPattern;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;

public final class NullPattern extends AbstractPattern
{
	public NullPattern(SourcePosition position)
	{
		this.position = position;
	}

	@Override
	public int getPatternType()
	{
		return NULL;
	}

	@Override
	public IType getType()
	{
		return Types.NULL;
	}

	@Override
	public Object getConstantValue()
	{
		return Null.instance;
	}

	@Override
	public void writeJumpOnMismatch(MethodWriter writer, int varIndex, Label target) throws BytecodeException
	{
		Pattern.loadVar(writer, varIndex);
		writer.visitJumpInsn(Opcodes.IFNONNULL, target);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append((String) null);
	}
}

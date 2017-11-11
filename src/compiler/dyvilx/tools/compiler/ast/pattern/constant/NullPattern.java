package dyvilx.tools.compiler.ast.pattern.constant;

import dyvil.lang.internal.Null;
import dyvil.reflect.Opcodes;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.pattern.IPattern;
import dyvilx.tools.compiler.ast.pattern.Pattern;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvil.source.position.SourcePosition;

public final class NullPattern extends Pattern
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
	public Object constantValue()
	{
		return Null.instance;
	}

	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, IType matchedType, Label elseLabel)
		throws BytecodeException
	{
		IPattern.loadVar(writer, varIndex, matchedType);
		writer.visitJumpInsn(Opcodes.IFNONNULL, elseLabel);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append((String) null);
	}
}

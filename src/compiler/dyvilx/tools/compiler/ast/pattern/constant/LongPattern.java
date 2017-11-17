package dyvilx.tools.compiler.ast.pattern.constant;

import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.pattern.IPattern;
import dyvilx.tools.compiler.ast.pattern.Pattern;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;

public final class LongPattern extends Pattern
{
	private long value;

	public LongPattern(SourcePosition position, long value)
	{
		this.position = position;
		this.value = value;
	}

	@Override
	public int getPatternType()
	{
		return LONG;
	}

	@Override
	public IType getType()
	{
		return Types.LONG;
	}

	@Override
	public IPattern withType(IType type, MarkerList markers)
	{
		return IPattern.primitiveWithType(this, type, Types.LONG);
	}

	@Override
	public Object constantValue()
	{
		return this.value;
	}

	@Override
	public void writeJumpOnMismatch(MethodWriter writer, int varIndex, Label target) throws BytecodeException
	{
		IPattern.loadVar(writer, varIndex);
		writer.visitLdcInsn(this.value);
		writer.visitJumpInsn(Opcodes.IF_LCMPNE, target);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value).append('L');
	}
}

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

public final class DoublePattern extends Pattern
{
	private double value;

	public DoublePattern(SourcePosition position, double value)
	{
		this.position = position;
		this.value = value;
	}

	@Override
	public int getPatternType()
	{
		return DOUBLE;
	}

	@Override
	public IType getType()
	{
		return Types.DOUBLE;
	}

	@Override
	public IPattern withType(IType type, MarkerList markers)
	{
		return IPattern.primitiveWithType(this, type, Types.DOUBLE);
	}

	@Override
	public Object constantValue()
	{
		return this.value;
	}

	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, IType matchedType, Label elseLabel)
		throws BytecodeException
	{
		IPattern.loadVar(writer, varIndex, matchedType);
		matchedType.writeCast(writer, Types.DOUBLE, this.lineNumber());
		writer.visitLdcInsn(this.value);
		writer.visitJumpInsn(Opcodes.IF_DCMPNE, elseLabel);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value).append('D');
	}
}

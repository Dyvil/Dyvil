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

public final class FloatPattern extends Pattern
{
	private float value;

	public FloatPattern(SourcePosition position, float value)
	{
		this.position = position;
		this.value = value;
	}

	@Override
	public int getPatternType()
	{
		return FLOAT;
	}

	@Override
	public IType getType()
	{
		return Types.FLOAT;
	}

	@Override
	public IPattern withType(IType type, MarkerList markers)
	{
		return IPattern.primitiveWithType(this, type, Types.FLOAT);
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
		matchedType.writeCast(writer, Types.FLOAT, this.lineNumber());
		writer.visitLdcInsn(this.value);
		writer.visitJumpInsn(Opcodes.IF_FCMPNE, elseLabel);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value).append('F');
	}
}

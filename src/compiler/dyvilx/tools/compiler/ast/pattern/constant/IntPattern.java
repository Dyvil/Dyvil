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

public final class IntPattern extends Pattern
{
	private int value;

	public IntPattern(SourcePosition position, int value)
	{
		this.position = position;
		this.value = value;
	}

	@Override
	public int getPatternType()
	{
		return INT;
	}

	@Override
	public IType getType()
	{
		return Types.INT;
	}

	@Override
	public IPattern withType(IType type, MarkerList markers)
	{
		return IPattern.primitiveWithType(this, type, Types.INT);
	}

	@Override
	public Object constantValue()
	{
		return this.value;
	}

	// Switch Resolution

	@Override
	public boolean isSwitchable()
	{
		return true;
	}

	@Override
	public int switchValue()
	{
		return this.value;
	}

	@Override
	public int minValue()
	{
		return this.value;
	}

	@Override
	public int maxValue()
	{
		return this.value;
	}

	// Compilation

	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, IType matchedType, Label elseLabel)
		throws BytecodeException
	{
		IPattern.loadVar(writer, varIndex, matchedType);
		matchedType.writeCast(writer, Types.INT, this.lineNumber());
		writer.visitLdcInsn(this.value);
		writer.visitJumpInsn(Opcodes.IF_ICMPNE, elseLabel);
	}

	@Override
	public String toString()
	{
		return java.lang.Integer.toString(this.value);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value);
	}
}

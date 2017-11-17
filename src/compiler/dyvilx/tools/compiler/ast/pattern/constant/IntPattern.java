package dyvilx.tools.compiler.ast.pattern.constant;

import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.pattern.Pattern;
import dyvilx.tools.compiler.ast.pattern.AbstractPattern;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;

public final class IntPattern extends AbstractPattern
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
	public Pattern withType(IType type, MarkerList markers)
	{
		return Pattern.primitiveWithType(this, type, Types.INT);
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

	// Compilation

	@Override
	public void writeJumpOnMismatch(MethodWriter writer, int varIndex, Label target) throws BytecodeException
	{
		Pattern.loadVar(writer, varIndex);
		writer.visitLdcInsn(this.value);
		writer.visitJumpInsn(Opcodes.IF_ICMPNE, target);
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

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

public final class BooleanPattern extends Pattern
{
	private boolean value;

	public BooleanPattern(SourcePosition position, boolean value)
	{
		this.position = position;
		this.value = value;
	}

	@Override
	public int getPatternType()
	{
		return BOOLEAN;
	}

	@Override
	public IType getType()
	{
		return Types.BOOLEAN;
	}

	@Override
	public IPattern withType(IType type, MarkerList markers)
	{
		return IPattern.primitiveWithType(this, type, Types.BOOLEAN);
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
		return this.value ? 1 : 0;
	}

	@Override
	public int minValue()
	{
		return this.value ? 1 : 0;
	}

	@Override
	public int maxValue()
	{
		return this.value ? 1 : 0;
	}

	// Compilation

	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, IType matchedType, Label elseLabel)
		throws BytecodeException
	{
		IPattern.loadVar(writer, varIndex, matchedType);
		matchedType.writeCast(writer, Types.BOOLEAN, this.lineNumber());
		writer.visitJumpInsn(this.value ? Opcodes.IFEQ : Opcodes.IFNE, elseLabel);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value);
	}
}

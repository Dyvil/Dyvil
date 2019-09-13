package dyvilx.tools.compiler.ast.pattern.constant;

import dyvil.annotation.internal.NonNull;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.pattern.Pattern;
import dyvilx.tools.compiler.ast.pattern.AbstractPattern;
import dyvilx.tools.compiler.ast.pattern.TypeCheckPattern;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.PrimitiveType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.method.MethodWriter;
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
		switch (type.getTypecode())
		{
		case PrimitiveType.BYTE_CODE:
		case PrimitiveType.SHORT_CODE:
		case PrimitiveType.CHAR_CODE:
		case PrimitiveType.INT_CODE:
			return this;
		case PrimitiveType.LONG_CODE:
			return new LongPattern(this.position, this.value);
		case PrimitiveType.FLOAT_CODE:
			return new FloatPattern(this.position, this.value);
		case PrimitiveType.DOUBLE_CODE:
			return new DoublePattern(this.position, this.value);
		}
		if (Types.isSuperType(type, Types.INT.getObjectType()))
		{
			return new TypeCheckPattern(this, type, Types.INT);
		}
		return null;
	}

	@Override
	public Object getConstantValue()
	{
		return this.value;
	}

	// Switch Resolution

	@Override
	public boolean hasSwitchHash()
	{
		return true;
	}

	@Override
	public int getSwitchHashValue()
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

	// Formatting

	@Override
	public String toString()
	{
		return java.lang.Integer.toString(this.value);
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		buffer.append(this.value);
	}
}

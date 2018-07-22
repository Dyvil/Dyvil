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

public final class LongPattern extends AbstractPattern
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
	public Pattern withType(IType type, MarkerList markers)
	{
		switch (type.getTypecode())
		{
		case PrimitiveType.LONG_CODE:
			return new LongPattern(this.position, this.value);
		case PrimitiveType.FLOAT_CODE:
			return new FloatPattern(this.position, this.value);
		case PrimitiveType.DOUBLE_CODE:
			return new DoublePattern(this.position, this.value);
		}
		if (Types.isSuperType(type, Types.LONG.getObjectType()))
		{
			return new TypeCheckPattern(this, type, Types.LONG);
		}
		return null;
	}

	@Override
	public Object constantValue()
	{
		return this.value;
	}

	@Override
	public void writeJumpOnMismatch(MethodWriter writer, int varIndex, Label target) throws BytecodeException
	{
		Pattern.loadVar(writer, varIndex);
		writer.visitLdcInsn(this.value);
		writer.visitJumpInsn(Opcodes.IF_LCMPNE, target);
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		buffer.append(this.value).append('L');
	}
}

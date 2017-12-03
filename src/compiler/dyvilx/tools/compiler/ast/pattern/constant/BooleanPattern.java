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
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;

public final class BooleanPattern extends AbstractPattern
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
	public Pattern withType(IType type, MarkerList markers)
	{
		if (type.getTypecode() == PrimitiveType.BOOLEAN_CODE)
		{
			return this;
		}
		if (Types.isSuperType(type, Types.BOOLEAN.getObjectType()))
		{
			return new TypeCheckPattern(this, type, Types.BOOLEAN);
		}
		return null;
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

	// Compilation

	@Override
	public void writeJumpOnMismatch(MethodWriter writer, int varIndex, Label target) throws BytecodeException
	{
		Pattern.loadVar(writer, varIndex);
		writer.visitJumpInsn(this.value ? Opcodes.IFEQ : Opcodes.IFNE, target);
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		buffer.append(this.value);
	}
}

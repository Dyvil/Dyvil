package dyvilx.tools.compiler.ast.pattern.constant;

import dyvil.annotation.internal.NonNull;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.pattern.AbstractPattern;
import dyvilx.tools.compiler.ast.pattern.Pattern;
import dyvilx.tools.compiler.ast.pattern.TypeCheckPattern;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.PrimitiveType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.lexer.StringLiterals;
import dyvilx.tools.parsing.marker.MarkerList;

public final class CharPattern extends AbstractPattern
{
	private static final byte TYPE_CHAR   = 1;
	private static final byte TYPE_STRING = 2;

	private String value;

	private byte type;

	public CharPattern(SourcePosition position, String value)
	{
		this.position = position;
		this.value = value;
	}

	public CharPattern(SourcePosition position, String value, boolean forceChar)
	{
		this.position = position;
		this.value = value;
		this.type = forceChar ? TYPE_CHAR : TYPE_STRING;
	}

	@Override
	public int getPatternType()
	{
		return Pattern.CHAR;
	}

	@Override
	public IType getType()
	{
		return Types.CHAR;
	}

	@Override
	public boolean isType(IType type)
	{
		if (this.value.length() == 1 && this.type != TYPE_STRING)
		{
			if (Types.isSuperType(type, Types.CHAR))
			{
				return true;
			}
		}
		return this.type != TYPE_CHAR && Types.isSuperType(type, Types.STRING);
	}

	@Override
	public Pattern withType(IType type, MarkerList markers)
	{
		if (this.value.length() == 1 && this.type != TYPE_STRING)
		{
			switch (type.getTypecode())
			{
			case PrimitiveType.BYTE_CODE:
			case PrimitiveType.SHORT_CODE:
			case PrimitiveType.INT_CODE:
			case PrimitiveType.CHAR_CODE:
				this.type = TYPE_CHAR;
				return this;
			}

			if (Types.isSuperType(type, Types.CHAR.getObjectType()))
			{
				this.type = TYPE_CHAR;
				return new TypeCheckPattern(this, type, Types.CHAR);
			}
		}
		if (this.type == TYPE_CHAR)
		{
			return null;
		}

		if (Types.isSuperType(type, Types.STRING))
		{
			this.type = TYPE_STRING;
			// See StringPattern.withType for implementation note
			return this;
		}
		return null;
	}

	@Override
	public Object constantValue()
	{
		if (this.type == TYPE_CHAR)
		{
			return this.value.charAt(0);
		}
		return this.value;
	}

	// Switch Resolution

	@Override
	public boolean isSwitchable()
	{
		return true;
	}

	@Override
	public boolean switchCheck()
	{
		return this.type != TYPE_CHAR;
	}

	@Override
	public int switchValue()
	{
		if (this.type == TYPE_CHAR)
		{
			return this.value.charAt(0);
		}
		return this.value.hashCode();
	}

	// Compilation

	@Override
	public void writeJumpOnMismatch(MethodWriter writer, int varIndex, Label target) throws BytecodeException
	{
		if (this.type == TYPE_STRING)
		{
			StringPattern.writeJumpOnMismatch(writer, varIndex, target, this.value);
			return;
		}

		Pattern.loadVar(writer, varIndex);
		writer.visitLdcInsn(this.value.charAt(0));
		writer.visitJumpInsn(Opcodes.IF_ICMPNE, target);
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		StringLiterals.appendCharLiteral(this.value, buffer);
	}
}

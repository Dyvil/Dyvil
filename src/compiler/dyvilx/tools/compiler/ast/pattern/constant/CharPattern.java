package dyvilx.tools.compiler.ast.pattern.constant;

import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.pattern.IPattern;
import dyvilx.tools.compiler.ast.pattern.Pattern;
import dyvilx.tools.compiler.ast.pattern.TypeCheckPattern;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.lexer.StringLiterals;
import dyvilx.tools.parsing.marker.MarkerList;

public final class CharPattern extends Pattern
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
		return IPattern.CHAR;
	}

	@Override
	public IType getType()
	{
		return Types.CHAR;
	}

	@Override
	public IPattern withType(IType type, MarkerList markers)
	{
		if (this.value.length() == 1 && this.type != TYPE_STRING)
		{
			final IPattern pattern = IPattern.primitiveWithType(this, type, Types.CHAR);
			if (this.type == TYPE_CHAR || pattern != null)
			{
				this.type = TYPE_CHAR;
				return pattern;
			}
		}
		if (this.type == TYPE_CHAR)
		{
			return null;
		}

		if (type.getTheClass() == Types.STRING_CLASS)
		{
			// also accepts String! or String?
			this.type = TYPE_STRING;
			return this;
		}
		if (Types.isSuperType(type, Types.STRING))
		{
			this.type = TYPE_STRING;
			return new TypeCheckPattern(this, type, Types.STRING);
		}
		return null;
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
	public boolean isSwitchable()
	{
		return true;
	}

	@Override
	public int subPatterns()
	{
		return 1;
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

	@Override
	public boolean switchCheck()
	{
		return this.type != TYPE_CHAR;
	}

	@Override
	public int minValue()
	{
		return this.switchValue();
	}

	@Override
	public int maxValue()
	{
		return this.switchValue();
	}

	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, IType matchedType, Label elseLabel)
		throws BytecodeException
	{
		if (this.type == TYPE_STRING)
		{
			StringPattern.writeStringInvJump(writer, varIndex, elseLabel, this.value);
			return;
		}

		IPattern.loadVar(writer, varIndex, matchedType);
		matchedType.writeCast(writer, Types.CHAR, this.lineNumber());
		writer.visitLdcInsn(this.value.charAt(0));
		writer.visitJumpInsn(Opcodes.IF_ICMPNE, elseLabel);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		StringLiterals.appendCharLiteral(this.value, buffer);
	}
}

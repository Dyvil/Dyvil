package dyvil.tools.compiler.ast.pattern.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.pattern.IPattern;
import dyvil.tools.compiler.ast.pattern.Pattern;
import dyvil.tools.compiler.ast.pattern.TypeCheckPattern;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.lexer.LexerUtil;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class CharPattern extends Pattern
{
	private String value;
	
	private byte type;
	
	public CharPattern(ICodePosition position, String value)
	{
		this.position = position;
		this.value = value;
	}
	
	@Override
	public int getPatternType()
	{
		return CHAR;
	}
	
	@Override
	public IType getType()
	{
		return Types.CHAR;
	}
	
	@Override
	public IPattern withType(IType type, MarkerList markers)
	{
		if (this.value.length() == 1 && this.type != STRING)
		{
			final IPattern pattern = IPattern.primitiveWithType(this, type, Types.CHAR);
			if (this.type == CHAR || pattern != null)
			{
				this.type = CHAR;
				return pattern;
			}
		}
		if (this.type == CHAR)
		{
			return null;
		}
		
		if (Types.isSameType(type, Types.STRING))
		{
			this.type = STRING;
			return this;
		}
		if (Types.isSuperType(type, Types.STRING))
		{
			this.type = STRING;
			return new TypeCheckPattern(this, type, Types.STRING);
		}
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (this.value.length() == 1 && this.type != STRING)
		{
			if (Types.isSuperType(type, Types.CHAR))
			{
				return true;
			}
		}
		return this.type != CHAR && Types.isSuperType(type, Types.STRING);
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
		if (this.type == CHAR)
		{
			return this.value.charAt(0);
		}
		return this.value.hashCode();
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
		if (this.type == STRING)
		{
			StringPattern.writeStringInvJump(writer, varIndex, elseLabel, this.value);
			return;
		}

		IPattern.loadVar(writer, varIndex, matchedType);
		matchedType.writeCast(writer, Types.CHAR, this.getLineNumber());
		writer.visitLdcInsn(this.value.charAt(0));
		writer.visitJumpInsn(Opcodes.IF_ICMPNE, elseLabel);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		LexerUtil.appendCharLiteral(this.value, buffer);
	}
}

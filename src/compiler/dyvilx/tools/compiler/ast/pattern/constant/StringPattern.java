package dyvilx.tools.compiler.ast.pattern.constant;

import dyvil.lang.Formattable;
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

public final class StringPattern extends Pattern
{
	private String value;
	
	public StringPattern(SourcePosition position, String value)
	{
		this.position = position;
		this.value = value;
	}
	
	@Override
	public int getPatternType()
	{
		return STRING;
	}
	
	@Override
	public IType getType()
	{
		return Types.STRING;
	}
	
	@Override
	public IPattern withType(IType type, MarkerList markers)
	{
		if (Types.isExactType(type, Types.STRING))
		{
			return this;
		}
		if (Types.isSuperType(type, Types.STRING))
		{
			return new TypeCheckPattern(this, type, Types.STRING);
		}
		return null;
	}
	
	@Override
	public boolean isSwitchable()
	{
		return true;
	}
	
	@Override
	public boolean switchCheck()
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
		return this.value.hashCode();
	}
	
	@Override
	public int minValue()
	{
		return this.value.hashCode();
	}
	
	@Override
	public int maxValue()
	{
		return this.value.hashCode();
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, IType matchedType, Label elseLabel)
			throws BytecodeException
	{
		writeStringInvJump(writer, varIndex, elseLabel, this.value);
	}
	
	protected static void writeStringInvJump(MethodWriter writer, int varIndex, Label elseLabel, String value)
			throws BytecodeException
	{
		writer.visitLdcInsn(value);
		if (varIndex >= 0)
		{
			writer.visitVarInsn(Opcodes.ALOAD, varIndex);
		}
		else
		{
			writer.visitInsn(Opcodes.SWAP);
		}
		writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
		writer.visitJumpInsn(Opcodes.IFEQ, elseLabel);
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		StringLiterals.appendStringLiteral(this.value, buffer);
	}
}

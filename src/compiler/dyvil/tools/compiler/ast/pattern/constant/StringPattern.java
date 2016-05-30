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
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.lexer.LexerUtil;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class StringPattern extends Pattern
{
	private String value;
	
	public StringPattern(ICodePosition position, String value)
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
		return IASTNode.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		LexerUtil.appendStringLiteral(this.value, buffer);
	}
}

package dyvil.tools.compiler.ast.pattern;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.lexer.LexerUtil;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class CharPattern extends Pattern
{
	private char value;
	
	public CharPattern(ICodePosition position, char value)
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
		return IPattern.primitiveWithType(this, type, Types.CHAR);
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Types.CHAR || type.isSuperTypeOf(Types.CHAR);
	}
	
	@Override
	public boolean isSwitchable()
	{
		return true;
	}
	
	@Override
	public int switchCases()
	{
		return 1;
	}
	
	@Override
	public int switchValue(int index)
	{
		return this.value;
	}
	
	@Override
	public int minValue()
	{
		return this.value;
	}
	
	@Override
	public int maxValue()
	{
		return this.value;
	}
	
	@Override
	{
		{
		}
	
	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException
	{
		if (varIndex >= 0)
		{
			writer.writeVarInsn(Opcodes.ILOAD, varIndex);
		}
		writer.writeLDC(this.value);
		writer.writeJumpInsn(Opcodes.IF_ICMPNE, elseLabel);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		LexerUtil.appendCharLiteral(this.value, buffer);
	}
}

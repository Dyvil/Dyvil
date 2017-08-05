package dyvilx.tools.compiler.ast.pattern.constant;

import dyvil.reflect.Opcodes;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.pattern.IPattern;
import dyvilx.tools.compiler.ast.pattern.Pattern;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public final class WildcardPattern extends Pattern
{
	public WildcardPattern(SourcePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int getPatternType()
	{
		return WILDCARD;
	}
	
	@Override
	public boolean isExhaustive()
	{
		return true;
	}

	@Override
	public boolean isWildcard()
	{
		return true;
	}

	@Override
	public IType getType()
	{
		return Types.ANY;
	}
	
	@Override
	public IPattern withType(IType type, MarkerList markers)
	{
		return this;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return true;
	}
	
	@Override
	public boolean isSwitchable()
	{
		return true;
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, IType matchedType, Label elseLabel)
			throws BytecodeException
	{
		if (varIndex < 0)
		{
			writer.visitInsn(Opcodes.AUTO_POP);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('_');
	}
}

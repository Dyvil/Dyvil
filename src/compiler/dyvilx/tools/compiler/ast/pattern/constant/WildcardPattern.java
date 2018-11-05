package dyvilx.tools.compiler.ast.pattern.constant;

import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.pattern.Pattern;
import dyvilx.tools.compiler.ast.pattern.AbstractPattern;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;

public final class WildcardPattern extends AbstractPattern
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
	public Pattern withType(IType type, MarkerList markers)
	{
		return this;
	}

	@Override
	public boolean isType(IType type)
	{
		return true;
	}

	@Override
	public boolean hasSwitchHash()
	{
		return true;
	}

	@Override
	public void writeJumpOnMismatch(MethodWriter writer, int varIndex, Label target) throws BytecodeException
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

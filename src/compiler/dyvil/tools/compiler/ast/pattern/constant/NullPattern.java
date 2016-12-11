package dyvil.tools.compiler.ast.pattern.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.pattern.IPattern;
import dyvil.tools.compiler.ast.pattern.Pattern;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class NullPattern extends Pattern
{
	public NullPattern(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int getPatternType()
	{
		return NULL;
	}
	
	@Override
	public IType getType()
	{
		return Types.NULL;
	}
	
	@Override
	public IPattern withType(IType type, MarkerList markers)
	{
		return type.isPrimitive() ? null : this;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return !type.isPrimitive();
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, IType matchedType, Label elseLabel)
			throws BytecodeException
	{
		IPattern.loadVar(writer, varIndex, matchedType);
		writer.visitJumpInsn(Opcodes.IFNONNULL, elseLabel);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append((String) null);
	}
}

package dyvil.tools.compiler.ast.pattern;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

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
		if (type == Types.STRING || type.equals(Types.STRING))
		{
			return this;
		}
		if (type.isSuperTypeOf(Types.STRING))
		{
			return new TypeCheckPattern(this, Types.STRING);
		}
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Types.STRING || type.isSuperTypeOf(Types.STRING);
	}
	
	@Override
	public void writeJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException
	{
		writer.writeLDC(this.value);
		if (varIndex >= 0)
		{
			writer.writeVarInsn(Opcodes.ALOAD, varIndex);
		}
		else
		{
			writer.writeInsn(Opcodes.SWAP);
		}
		writer.writeInvokeInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "compareTo", "(Ljava/lang/String;)I", false);
		writer.writeJumpInsn(Opcodes.IFEQ, elseLabel);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException
	{
		writer.writeLDC(this.value);
		if (varIndex >= 0)
		{
			writer.writeVarInsn(Opcodes.ALOAD, varIndex);
		}
		else
		{
			writer.writeInsn(Opcodes.SWAP);
		}
		writer.writeInvokeInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "compareTo", "(Ljava/lang/String;)I", false);
		writer.writeJumpInsn(Opcodes.IFNE, elseLabel);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('"').append(this.value).append('"');
	}
}

package dyvil.tools.compiler.ast.pattern;

import org.objectweb.asm.Label;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class BooleanPattern extends ASTNode implements IPattern
{
	private boolean	value;
	
	public BooleanPattern(ICodePosition position, boolean value)
	{
		this.position = position;
		this.value = value;
	}
	
	@Override
	public IType getType()
	{
		return Type.BOOLEAN;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Type.BOOLEAN || type.isSuperTypeOf(Type.BOOLEAN);
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label elseLabel)
	{
		writer.writeFrameJump(this.value ? Opcodes.IFEQ : Opcodes.IFNE, elseLabel);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value);
	}
}

package dyvil.tools.compiler.ast.pattern;

import org.objectweb.asm.Label;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class StringPattern extends ASTNode implements IPattern
{
	private String	value;
	
	public StringPattern(ICodePosition position, String value)
	{
		this.position = position;
		this.value = value;
	}
	
	@Override
	public IType getType()
	{
		return Type.STRING;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Type.STRING || type.isSuperTypeOf(Type.STRING);
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label elseLabel)
	{
		writer.writeLDC(this.value);
		writer.writeInvokeInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false, 2, MethodWriter.INT);
		writer.writeFrameJump(Opcodes.IFEQ, elseLabel);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('"').append(this.value).append('"');
	}
}

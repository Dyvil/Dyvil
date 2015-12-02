package dyvil.tools.compiler.ast.pattern;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class UnboxPattern implements IPattern
{
	private IPattern pattern;
	private IMethod  unboxMethod;
	
	public UnboxPattern(IPattern pattern, IMethod boxingMethod)
	{
		this.pattern = pattern;
		this.unboxMethod = boxingMethod;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.pattern.getPosition();
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
		this.pattern.setPosition(position);
	}
	
	@Override
	public int getPatternType()
	{
		return UNBOX;
	}
	
	@Override
	public IType getType()
	{
		return this.unboxMethod.getType();
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
	public IPattern resolve(MarkerList markers, IContext context)
	{
		this.pattern = this.pattern.resolve(markers, context);
		return this;
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException
	{
		if (varIndex >= 0)
		{
			writer.writeVarInsn(Opcodes.ALOAD, varIndex);
		}
		this.unboxMethod.writeInvoke(writer, null, null, this.pattern.getLineNumber());
		this.pattern.writeInvJump(writer, -1, elseLabel);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.pattern.toString(prefix, buffer);
	}
}

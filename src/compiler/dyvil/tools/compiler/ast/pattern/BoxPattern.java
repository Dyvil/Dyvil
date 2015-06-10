package dyvil.tools.compiler.ast.pattern;

import org.objectweb.asm.Label;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class BoxPattern implements IPattern
{
	private IPattern	pattern;
	private IMethod		boxingMethod;
	
	public BoxPattern(IPattern pattern, IMethod boxingMethod)
	{
		this.pattern = pattern;
		this.boxingMethod = boxingMethod;
	}
	
	@Override
	public int getPatternType()
	{
		return BOXED;
	}
	
	@Override
	public IType getType()
	{
		return this.boxingMethod.getType();
	}
	
	@Override
	public IPattern withType(IType type)
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
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.pattern = this.pattern.resolve(markers, context);
	}
	
	@Override
	public void writeJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException
	{
		if (varIndex >= 0)
		{
			writer.writeVarInsn(Opcodes.ALOAD, varIndex);
		}
		writer.writeInvokeInsn(Opcodes.INVOKEVIRTUAL, this.boxingMethod.getTheClass().getInternalName(), this.boxingMethod.getName().qualified,
				this.boxingMethod.getDescriptor(), false);
		this.pattern.writeJump(writer, -1, elseLabel);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException
	{
		if (varIndex >= 0)
		{
			writer.writeVarInsn(Opcodes.ALOAD, varIndex);
		}
		writer.writeInvokeInsn(Opcodes.INVOKEVIRTUAL, this.boxingMethod.getTheClass().getInternalName(), this.boxingMethod.getName().qualified,
				this.boxingMethod.getDescriptor(), false);
		this.pattern.writeInvJump(writer, -1, elseLabel);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.pattern.toString(prefix, buffer);
	}
}

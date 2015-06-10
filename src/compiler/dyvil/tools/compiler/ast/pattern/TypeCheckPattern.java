package dyvil.tools.compiler.ast.pattern;

import org.objectweb.asm.Label;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class TypeCheckPattern implements IPattern
{
	private IPattern	pattern;
	private IType		type;
	
	public TypeCheckPattern(IPattern pattern, IType type)
	{
		this.pattern = pattern;
		this.type = type;
	}
	
	@Override
	public int getPatternType()
	{
		return TYPECHECK;
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public IPattern withType(IType type)
	{
		if (type.isPrimitive())
		{
			return null;
		}
		
		this.type = type;
		return this;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return !type.isPrimitive();
	}
	
	@Override
	public IField resolveField(Name name)
	{
		return this.pattern.resolveField(name);
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		this.type = this.type.resolve(markers, context);
		this.pattern.resolve(markers, context);
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.pattern.checkTypes(markers, context);
	}
	
	@Override
	public void writeJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException
	{
		// TODO
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException
	{
		if (varIndex < 0)
		{
			varIndex = writer.localCount();
			writer.writeVarInsn(Opcodes.ASTORE, varIndex);
		}
		
		writer.writeVarInsn(Opcodes.ALOAD, varIndex);
		writer.writeTypeInsn(Opcodes.INSTANCEOF, this.type.getInternalName());
		writer.writeJumpInsn(Opcodes.IFEQ, elseLabel);
		writer.writeVarInsn(Opcodes.ALOAD, varIndex);
		writer.writeTypeInsn(Opcodes.CHECKCAST, this.type.getInternalName());
		this.pattern.writeInvJump(writer, -1, elseLabel);
		writer.resetLocals(varIndex);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.pattern.toString(prefix, buffer);
		buffer.append(" as ");
		this.type.toString(prefix, buffer);
	}
}

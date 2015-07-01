package dyvil.tools.compiler.ast.pattern;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

import org.objectweb.asm.Label;

public class TypeCheckPattern implements IPattern
{
	private ICodePosition	position;
	private IPattern		pattern;
	private IType			type;
	
	public TypeCheckPattern(ICodePosition position, IPattern pattern)
	{
		this.position = position;
		this.pattern = pattern;
	}
	
	public TypeCheckPattern(IPattern pattern, IType type)
	{
		this.pattern = pattern;
		this.type = type;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}
	
	@Override
	public int getPatternType()
	{
		return TYPECHECK;
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
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
		
		return type.isSuperTypeOf(this.type) ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return !type.isPrimitive();
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		return this.pattern.resolveField(name);
	}
	
	@Override
	public IPattern resolve(MarkerList markers, IContext context)
	{
		if (this.pattern != null)
		{
			this.pattern = this.pattern.resolve(markers, context);
		}
		
		if (this.type != null)
		{
			this.type = this.type.resolve(markers, context, TypePosition.CLASS);
			
			if (this.pattern != null && this.pattern.getPatternType() != WILDCARD)
			{
				this.pattern = this.pattern.withType(this.type);
			}
			
			if (this.type.isPrimitive())
			{
				markers.add(this.type.getPosition(), "pattern.typecheck.primitive");
			}
		}
		else
		{
			markers.add(this.position, "pattern.typecheck.invalid");
		}
		
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.pattern != null)
		{
			this.pattern.checkTypes(markers, context);
		}
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
		
		if (this.pattern.getPatternType() != WILDCARD)
		{
			writer.writeVarInsn(Opcodes.ALOAD, varIndex);
			writer.writeTypeInsn(Opcodes.CHECKCAST, this.type.getInternalName());
			this.pattern.writeInvJump(writer, -1, elseLabel);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.pattern != null)
		{
			this.pattern.toString(prefix, buffer);
		}
		int patternType = this.pattern.getPatternType();
		if (patternType == BINDING || patternType != CASE_CLASS && !this.pattern.isType(this.type))
		{
			buffer.append(" as ");
			this.type.toString(prefix, buffer);
		}
	}
}

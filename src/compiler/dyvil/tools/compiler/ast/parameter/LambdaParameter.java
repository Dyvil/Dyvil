package dyvil.tools.compiler.ast.parameter;

import java.lang.annotation.ElementType;

import org.objectweb.asm.ClassWriter;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class LambdaParameter extends Member implements IVariable
{
	public int		index;
	public IType	baseType;
	
	public LambdaParameter()
	{
		super(null);
	}
	
	public LambdaParameter(int index, String name, IType type)
	{
		super(null, name, type);
		this.index = index;
	}
	
	@Override
	public void setValue(IValue value)
	{
	}
	
	@Override
	public IValue getValue()
	{
		return null;
	}
	
	@Override
	public void setIndex(int index)
	{
		this.index = index;
	}
	
	@Override
	public int getIndex()
	{
		return this.index;
	}
	
	@Override
	public String getDescription()
	{
		return this.type.getExtendedName();
	}
	
	@Override
	public String getSignature()
	{
		return this.type.getSignature();
	}
	
	@Override
	public boolean processAnnotation(Annotation annotation)
	{
		return false;
	}
	
	@Override
	public ElementType getAnnotationType()
	{
		return null;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.type = this.type.resolve(markers, context);
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public void foldConstants()
	{
	}
	
	@Override
	public void write(ClassWriter writer)
	{
	}
	
	public void write(MethodWriter writer)
	{
		this.index = writer.registerParameter(this.name, this.type);
	}
	
	@Override
	public void writeGet(MethodWriter writer, IValue instance)
	{
		writer.writeVarInsn(this.type.getLoadOpcode(), this.index);
		
		if (this.baseType != this.type)
		{
			writer.writeTypeInsn(Opcodes.CHECKCAST, this.type);
		}
	}
	
	@Override
	public void writeSet(MethodWriter writer, IValue instance, IValue value)
	{
		value.writeExpression(writer);
		
		writer.writeVarInsn(this.type.getStoreOpcode(), this.index);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.type.toString(prefix, buffer);
		buffer.append(' ');
		buffer.append(this.name);
	}
}

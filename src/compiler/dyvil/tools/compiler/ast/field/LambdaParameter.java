package dyvil.tools.compiler.ast.field;

import java.lang.annotation.ElementType;
import java.util.List;

import jdk.internal.org.objectweb.asm.ClassWriter;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;

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
	public void addAnnotation(Annotation annotation)
	{
		if (!this.processAnnotation(annotation))
		{
			annotation.target = ElementType.PARAMETER;
			this.annotations.add(annotation);
		}
	}
	
	private boolean processAnnotation(Annotation annotation)
	{
		String name = annotation.type.fullName;
		if ("dyvil.lang.annotation.byref".equals(name))
		{
			this.modifiers |= Modifiers.BYREF;
			return true;
		}
		return false;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		this.type = this.type.resolve(markers, context);
	}
	
	@Override
	public void resolve(List<Marker> markers, IContext context)
	{
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
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
		writer.visitParameter(this.name, this.type, this.index);
	}
	
	@Override
	public void writeGet(MethodWriter writer, IValue instance)
	{
		writer.visitVarInsn(this.type.getLoadOpcode(), this.index, this.type);
		
		if (this.baseType != this.type)
		{
			writer.visitTypeInsn(Opcodes.CHECKCAST, this.type);
		}
	}
	
	@Override
	public void writeSet(MethodWriter writer, IValue instance, IValue value)
	{
		value.writeExpression(writer);
		
		writer.visitVarInsn(this.type.getStoreOpcode(), this.index, null);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.type.toString(prefix, buffer);
		buffer.append(' ');
		buffer.append(this.name);
	}
}

package dyvil.tools.compiler.ast.field;

import java.lang.annotation.ElementType;
import java.util.Iterator;
import java.util.List;

import jdk.internal.org.objectweb.asm.ClassWriter;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Modifiers;

public class Variable extends Member implements IField
{
	public int		index;
	public IValue	value;
	
	public Variable(ICodePosition position)
	{
		super(null);
		this.position = position;
	}
	
	public Variable(ICodePosition position, String name, IType type)
	{
		super(null, name, type);
		this.position = position;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.value = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.value;
	}
	
	@Override
	public void addAnnotation(Annotation annotation)
	{
		if (!this.processAnnotation(annotation))
		{
			annotation.target = ElementType.LOCAL_VARIABLE;
			this.annotations.add(annotation);
		}
	}
	
	private boolean processAnnotation(Annotation annotation)
	{
		String name = annotation.type.fullName;
		if ("dyvil.lang.annotation.lazy".equals(name))
		{
			this.modifiers |= Modifiers.LAZY;
			return true;
		}
		return false;
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
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		this.type = this.type.resolve(context);
		if (!this.type.isResolved())
		{
			markers.add(Markers.create(this.type.getPosition(), "resolve.type", this.type.toString()));
		}
		
		for (Annotation a : this.annotations)
		{
			a.resolveTypes(markers, context);
		}
		
		if (this.value != null)
		{
			this.value.resolveTypes(markers, context);
		}
	}
	
	@Override
	public void resolve(List<Marker> markers, IContext context)
	{
		for (Iterator<Annotation> iterator = this.annotations.iterator(); iterator.hasNext();)
		{
			Annotation a = iterator.next();
			if (this.processAnnotation(a))
			{
				iterator.remove();
				continue;
			}
			
			a.resolve(markers, context);
		}
		
		if (this.value != null)
		{
			this.value = this.value.resolve(markers, context);
		}
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		for (Annotation a : this.annotations)
		{
			a.check(markers, context);
		}
		
		IValue value1 = this.value.withType(this.type);
		if (value1 == null)
		{
			Marker marker = Markers.create(this.value.getPosition(), "access.variable.type", this.name);
			marker.addInfo("Field Type: " + this.type);
			marker.addInfo("Value Type: " + this.value.getType());
			markers.add(marker);
		}
		else
		{
			this.value = value1;
		}
		
		this.value.check(markers, context);
	}
	
	@Override
	public void foldConstants()
	{
		for (Annotation a : this.annotations)
		{
			a.foldConstants();
		}
		
		this.value = this.value.foldConstants();
	}
	
	@Override
	public void write(ClassWriter writer)
	{
	}
	
	@Override
	public void writeGet(MethodWriter writer)
	{
		if (this.value != null && (this.modifiers & Modifiers.LAZY) == Modifiers.LAZY)
		{
			this.value.writeExpression(writer);
		}
		writer.visitVarInsn(this.type.getLoadOpcode(), this.index);
	}
	
	@Override
	public void writeSet(MethodWriter writer)
	{
		writer.visitVarInsn(this.type.getStoreOpcode(), this.index, null);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.type.toString("", buffer);
		buffer.append(' ').append(this.name);
		
		if (this.value != null)
		{
			buffer.append(Formatting.Field.keyValueSeperator);
			Formatting.appendValue(this.value, prefix, buffer);
		}
	}
}

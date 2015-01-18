package dyvil.tools.compiler.ast.field;

import java.lang.annotation.ElementType;
import java.util.Iterator;
import java.util.List;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Label;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.api.IContext;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.api.IType;
import dyvil.tools.compiler.ast.api.IValue;
import dyvil.tools.compiler.ast.method.Member;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Modifiers;

public class Variable extends Member implements IField
{
	public int		index;
	
	public Label	start;
	public Label	end;
	
	public IValue	value;
	
	public Variable()
	{
		super(null);
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
			markers.add(new SemanticError(this.type.getPosition(), "'" + this.type + "' could not be resolved to a type"));
		}
		
		for (Annotation a : this.annotations)
		{
			a.resolveTypes(markers, context);
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
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		for (Annotation a : this.annotations)
		{
			a.check(markers, context);
		}
	}
	
	@Override
	public void foldConstants()
	{
		for (Annotation a : this.annotations)
		{
			a.foldConstants();
		}
	}
	
	@Override
	public void write(ClassWriter writer)
	{
	}
	
	@Override
	public void writeGet(MethodWriter writer)
	{
		if ((this.modifiers & Modifiers.LAZY) == Modifiers.LAZY)
		{
			this.value.writeExpression(writer);
		}
		writer.visitVarInsn(this.type.getLoadOpcode(), this.index, this.type);
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
	}
}

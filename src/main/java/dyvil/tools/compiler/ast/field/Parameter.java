package dyvil.tools.compiler.ast.field;

import java.lang.annotation.ElementType;
import java.util.Iterator;

import jdk.internal.org.objectweb.asm.ClassWriter;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.method.Member;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.util.Modifiers;
import dyvil.tools.compiler.util.Util;

public class Parameter extends Member implements IField
{
	public int		index;
	private char	seperator;
	
	public Parameter()
	{
		super(null);
	}
	
	public Parameter(int index, String name, Type type, int modifiers)
	{
		this(index, name, type, modifiers, ',');
	}
	
	public Parameter(int index, String name, Type type, int modifiers, char seperator)
	{
		super(null, name, type, modifiers);
		this.index = index;
		this.seperator = seperator;
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
	
	public void setSeperator(char seperator)
	{
		this.seperator = seperator;
	}
	
	public char getSeperator()
	{
		return this.seperator;
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
		String name = annotation.type.qualifiedName;
		if ("dyvil.lang.annotation.byref".equals(name))
		{
			this.modifiers |= Modifiers.BYREF;
			return true;
		}
		return false;
	}
	
	@Override
	public Parameter applyState(CompilerState state, IContext context)
	{
		if (state == CompilerState.RESOLVE_TYPES)
		{
			this.type = this.type.resolve(context);
		}
		else if (state == CompilerState.RESOLVE)
		{
			for (Iterator<Annotation> iterator = this.annotations.iterator(); iterator.hasNext();)
			{
				Annotation a = iterator.next();
				if (this.processAnnotation(a))
				{
					iterator.remove();
					continue;
				}
				
				a.applyState(state, context);
			}
			return this;
		}
		
		Util.applyState(this.annotations, state, context);
		return this;
	}
	
	@Override
	public void write(ClassWriter writer)
	{
	}
	
	public void write(MethodWriter writer)
	{
		writer.visitParameter(this.name, this.type, this.index);
		
		if ((this.modifiers & Modifiers.BYREF) != 0)
		{
			writer.visitParameterAnnotation(this.index, "Ldyvil/lang/annotation/byref;", true);
		}
		
		for (Annotation a : this.annotations)
		{
			a.write(writer, this.index);
		}
	}
	
	@Override
	public void writeGet(MethodWriter writer)
	{
		writer.visitVarInsn(this.type.getLoadOpcode(), this.index, this.type);
	}
	
	@Override
	public void writeSet(MethodWriter writer)
	{
		writer.visitVarInsn(this.type.getStoreOpcode(), this.index, this.type);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		for (Annotation a : this.annotations)
		{
			a.toString(prefix, buffer);
			buffer.append(' ');
		}
		
		this.type.toString("", buffer);
		buffer.append(' ').append(this.name);
	}
}

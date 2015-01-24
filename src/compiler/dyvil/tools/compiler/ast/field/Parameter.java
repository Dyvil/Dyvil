package dyvil.tools.compiler.ast.field;

import java.lang.annotation.ElementType;
import java.util.Iterator;
import java.util.List;

import jdk.internal.org.objectweb.asm.ClassWriter;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.util.Modifiers;

public class Parameter extends Member implements IField
{
	public int		index;
	public char		seperator;
	private boolean	varargs;
	
	public Parameter()
	{
		super(null);
	}
	
	public Parameter(int index, String name, IType type)
	{
		super(null, name, type);
		this.index = index;
	}
	
	public Parameter(int index, String name, IType type, int modifiers, List<Annotation> annotations)
	{
		super(null, name, type, modifiers, annotations);
		this.index = index;
		this.seperator = ',';
	}
	
	public Parameter(int index, String name, IType type, int modifiers, List<Annotation> annotations, char seperator)
	{
		super(null, name, type, modifiers, annotations);
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
	
	public void setVarargs()
	{
		this.type = this.type.clone();
		this.type.addArrayDimension();
		this.varargs = true;
	}
	
	public boolean isVarargs()
	{
		return this.varargs;
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
		this.type = this.type.resolve(context);
		if (!this.type.isResolved())
		{
			markers.add(Markers.create(this.type.getPosition(), "resolve.type", this.type.toString()));
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
		writer.visitVarInsn(this.type.getStoreOpcode(), this.index, null);
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
		if (this.isVarargs())
		{
			int len = buffer.length();
			buffer.replace(len - 2, len, "...");
		}
		buffer.append(' ').append(this.name);
	}
}

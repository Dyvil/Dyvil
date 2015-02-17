package dyvil.tools.compiler.ast.field;

import java.lang.annotation.ElementType;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import dyvil.tools.compiler.util.Modifiers;

public class Parameter extends Member implements IVariable
{
	public int		index;
	public char		seperator;
	private boolean	varargs;
	
	public IValue	defaultValue;
	
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
		this.defaultValue = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.defaultValue;
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
	
	public void setVarargs2()
	{
		this.varargs = true;
	}
	
	public boolean isVarargs()
	{
		return this.varargs;
	}
	
	public IType getType(Map<String, IType> types)
	{
		if (types == null)
		{
			return this.type;
		}
		return this.type.getConcreteType(types);
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
		
		if (this.annotations != null)
		{
			for (Annotation a : this.annotations)
			{
				a.resolveTypes(markers, context);
			}
		}
		
		if (this.defaultValue != null)
		{
			this.defaultValue.resolveTypes(markers, context);
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
		
		if (this.defaultValue != null)
		{
			this.defaultValue = this.defaultValue.resolve(markers, context);
		}
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		for (Annotation a : this.annotations)
		{
			a.check(markers, context);
		}
		
		if (this.defaultValue != null)
		{
			IValue value1 = this.defaultValue.withType(this.type);
			if (value1 == null)
			{
				Marker marker = Markers.create(this.defaultValue.getPosition(), "parameter.type", this.name);
				marker.addInfo("Parameter Type: " + this.type);
				marker.addInfo("Value Type: " + this.defaultValue.getType());
				markers.add(marker);
			}
			else
			{
				this.defaultValue = value1;
			}
			
			this.defaultValue.check(markers, context);
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
	public void writeGet(MethodWriter writer, IValue instance)
	{
		writer.visitVarInsn(this.type.getLoadOpcode(), this.index, this.type);
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
		if (this.annotations != null)
		{
			for (Annotation a : this.annotations)
			{
				a.toString(prefix, buffer);
				buffer.append(' ');
			}
		}
		
		if (this.isVarargs())
		{
			this.type.getElementType().toString(prefix, buffer);
			buffer.append("...");
		}
		else
		{
			this.type.toString(prefix, buffer);
		}
		buffer.append(' ').append(this.name);
		
		if (this.defaultValue != null)
		{
			buffer.append(Formatting.Field.keyValueSeperator).append(' ');
			this.defaultValue.toString(prefix, buffer);
		}
	}
}

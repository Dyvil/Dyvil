package dyvil.tools.compiler.ast.parameter;

import java.lang.annotation.ElementType;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IBaseMethod;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public final class MethodParameter extends Member implements IParameter
{
	public IBaseMethod	method;
	
	public int			index;
	public boolean		varargs;
	
	public IValue		defaultValue;
	
	public MethodParameter()
	{
	}
	
	public MethodParameter(Name name)
	{
		this.name = name;
	}
	
	public MethodParameter(Name name, IType type)
	{
		this.name = name;
		this.type = type;
	}
	
	@Override
	public void setMethod(IBaseMethod method)
	{
		this.method = method;
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
	
	@Override
	public void setVarargs(boolean varargs)
	{
		this.varargs = varargs;
	}
	
	@Override
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
	public boolean addRawAnnotation(String type)
	{
		switch (type)
		{
		case "dyvil/annotation/var":
			this.modifiers |= Modifiers.VAR;
			return false;
		case "dyvil/annotation/lazy":
			this.modifiers |= Modifiers.LAZY;
			return false;
		}
		return true;
	}
	
	@Override
	public ElementType getAnnotationType()
	{
		return ElementType.PARAMETER;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		super.resolveTypes(markers, context);
		
		if (this.defaultValue != null)
		{
			this.defaultValue.resolveTypes(markers, context);
		}
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, context);
		
		if (this.defaultValue != null)
		{
			context.getThisClass().getTheClass().addCompilable(this);
			
			this.defaultValue = this.defaultValue.resolve(markers, context);
			
			if (this.type == Types.UNKNOWN)
			{
				this.type = this.defaultValue.getType();
				if (this.type == Types.UNKNOWN)
				{
					markers.add(this.position, "parameter.type.infer", this.name.unqualified);
				}
				return;
			}
			
			IValue value1 = this.defaultValue.withType(this.type);
			if (value1 == null)
			{
				Marker marker = markers.create(this.defaultValue.getPosition(), "parameter.type", this.name.unqualified);
				marker.addInfo("Parameter Type: " + this.type);
				marker.addInfo("Value Type: " + this.defaultValue.getType());
			}
			else
			{
				this.defaultValue = value1;
			}
			return;
		}
		if (this.type == Types.UNKNOWN)
		{
			markers.add(this.position, "parameter.type.nodefault", this.name.unqualified);
		}
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		super.checkTypes(markers, context);
		
		if (this.defaultValue != null)
		{
			this.defaultValue.checkTypes(markers, context);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		super.check(markers, context);
		
		if (this.defaultValue != null)
		{
			this.defaultValue.check(markers, context);
		}
	}
	
	@Override
	public void write(ClassWriter writer)
	{
		if (this.defaultValue == null)
		{
			return;
		}
		
		// Copy the access modifiers and add the STATIC modifier
		int modifiers = this.method.getModifiers() & Modifiers.ACCESS_MODIFIERS | Modifiers.STATIC;
		String name = "parDefault$" + this.method.getName().qualified + "$" + this.index;
		String desc = "()" + this.type.getExtendedName();
		MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(modifiers, name, desc, null, null));
		mw.begin();
		this.defaultValue.writeExpression(mw);
		mw.end(this.type);
	}
	
	@Override
	public void write(MethodWriter writer)
	{
		writer.registerParameter(this.index, this.name.qualified, this.type, 0);
		
		if ((this.modifiers & Modifiers.VAR) != 0)
		{
			writer.addParameterAnnotation(this.index, "Ldyvil/annotation/var;", true);
		}
		
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].write(writer, this.index);
		}
	}
	
	@Override
	public void writeGet(MethodWriter writer, IValue instance)
	{
		writer.writeVarInsn(this.type.getLoadOpcode(), this.index + writer.inlineOffset());
	}
	
	@Override
	public void writeSet(MethodWriter writer, IValue instance, IValue value)
	{
		if (value != null)
		{
			value.writeExpression(writer);
		}
		writer.writeVarInsn(this.type.getStoreOpcode(), this.index + writer.inlineOffset());
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].toString(prefix, buffer);
			buffer.append(' ');
		}
		
		if (this.varargs)
		{
			this.type.getElementType().toString(prefix, buffer);
			buffer.append("... ");
		}
		else
		{
			this.type.toString(prefix, buffer);
			buffer.append(' ');
		}
		buffer.append(this.name);
		
		if (this.defaultValue != null)
		{
			buffer.append(Formatting.Field.keyValueSeperator).append(' ');
			this.defaultValue.toString(prefix, buffer);
		}
	}
}

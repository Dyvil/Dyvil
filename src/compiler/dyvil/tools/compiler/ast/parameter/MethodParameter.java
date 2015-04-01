package dyvil.tools.compiler.ast.parameter;

import java.lang.annotation.ElementType;

import org.objectweb.asm.ClassWriter;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IBaseMethod;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class MethodParameter extends Member implements IParameter
{
	public IBaseMethod	method;
	
	public int			index;
	public boolean		varargs;
	
	public IValue		defaultValue;
	
	public MethodParameter()
	{
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
		if ("dyvil.lang.annotation.var".equals(this.name))
		{
			this.modifiers |= Modifiers.VAR;
			return false;
		}
		if ("dyvil.lang.annotation.lazy".equals(this.name))
		{
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
			IClass iclass = context.getThisType().getTheClass();
			this.defaultValue = this.defaultValue.resolve(markers, context);
			iclass.addCompilable(this);
		}
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		super.checkTypes(markers, context);
		
		if (this.defaultValue != null)
		{
			IValue value1 = this.defaultValue.withType(this.type);
			if (value1 == null)
			{
				Marker marker = markers.create(this.defaultValue.getPosition(), "parameter.type");
				marker.addInfo("Parameter Type: " + this.type);
				marker.addInfo("Value Type: " + this.defaultValue.getType());
				
			}
			else
			{
				this.defaultValue = value1;
			}
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
		this.index = writer.registerParameter(this.name.qualified, this.type);
		
		if ((this.modifiers & Modifiers.VAR) != 0)
		{
			writer.addParameterAnnotation(this.index, "Ldyvil/lang/annotation/var;", true);
		}
		
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].write(writer, this.index);
		}
	}
	
	@Override
	public void writeGet(MethodWriter writer, IValue instance)
	{
		writer.writeVarInsn(this.type.getLoadOpcode(), this.index);
	}
	
	@Override
	public void writeSet(MethodWriter writer, IValue instance, IValue value)
	{
		if (value != null)
		{
			value.writeExpression(writer);
		}
		writer.writeVarInsn(this.type.getStoreOpcode(), this.index);
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

package dyvil.tools.compiler.ast.parameter;

import java.lang.annotation.ElementType;

import org.objectweb.asm.ClassWriter;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.member.IModified;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class Parameter extends Member implements IVariable
{
	public IParameterized	parameterized;
	
	public int				index;
	public char				seperator;
	public boolean			varargs;
	
	public IValue			defaultValue;
	
	public Parameter()
	{
	}
	
	public Parameter(int index, String name, IType type)
	{
		super(name, type);
		this.index = index;
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
		if ("dyvil.lang.annotation.var".equals(name))
		{
			this.modifiers |= Modifiers.VAR;
			return false;
		}
		if ("dyvil.lang.annotation.lazy".equals(name))
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
	public void check(MarkerList markers, IContext context)
	{
		super.check(markers, context);
		
		if (this.defaultValue != null)
		{
			IValue value1 = this.defaultValue.withType(this.type);
			if (value1 == null)
			{
				Marker marker = markers.create(this.defaultValue.getPosition(), "parameter.type", this.name);
				marker.addInfo("Parameter Type: " + this.type);
				marker.addInfo("Value Type: " + this.defaultValue.getType());
				
			}
			else
			{
				this.defaultValue = value1;
			}
			
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
		int modifiers = ((IModified) this.parameterized).getModifiers() & Modifiers.ACCESS_MODIFIERS | Modifiers.STATIC;
		String name = "parDefault$" + ((INamed) this.parameterized).getQualifiedName() + "$" + this.index;
		String desc = "()" + this.type.getExtendedName();
		MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(modifiers, name, desc, null, null));
		mw.begin();
		this.defaultValue.writeExpression(mw);
		mw.end(this.type);
	}
	
	public void write(MethodWriter writer)
	{
		this.index = writer.registerParameter(this.name, this.type);
		
		if ((this.modifiers & Modifiers.VAR) != 0)
		{
			writer.addParameterAnnotation(this.index, "Ldyvil/lang/annotation/byref;", true);
		}
		
		for (Annotation a : this.annotations)
		{
			a.write(writer, this.index);
		}
	}
	
	@Override
	public void writeGet(MethodWriter writer, IValue instance)
	{
		if (this.parameterized.isClass())
		{
			writer.writeGetField(((IClass) this.parameterized).getInternalName(), qualifiedName, this.getDescription(), type);
			return;
		}
		
		writer.writeVarInsn(this.type.getLoadOpcode(), this.index);
	}
	
	@Override
	public void writeSet(MethodWriter writer, IValue instance, IValue value)
	{
		value.writeExpression(writer);
		
		if (this.parameterized.isClass())
		{
			writer.writePutField(((IClass) this.parameterized).getInternalName(), qualifiedName, this.getDescription());
			return;
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

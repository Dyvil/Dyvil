package dyvil.tools.compiler.ast.parameter;

import java.lang.annotation.ElementType;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public final class ClassParameter extends Member implements IParameter
{
	public IClass	theClass;
	
	public int		index;
	public boolean	varargs;
	
	public IValue	defaultValue;
	
	public ClassParameter()
	{
	}
	
	public ClassParameter(Name name, IType type)
	{
		this.name = name;
		this.type = type;
	}
	
	@Override
	public boolean isField()
	{
		return true;
	}
	
	@Override
	public boolean isVariable()
	{
		return false;
	}
	
	@Override
	public void setTheClass(IClass iclass)
	{
		this.theClass = iclass;
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
		case "dyvil/lang/annotation/var":
			this.modifiers |= Modifiers.VAR;
			return false;
		case "dyvil/lang/annotation/lazy":
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
					markers.add(this.position, "classparameter.type.infer", this.name.unqualified);
				}
				return;
			}
			
			IValue value1 = this.defaultValue.withType(this.type);
			if (value1 == null)
			{
				Marker marker = markers.create(this.defaultValue.getPosition(), "classparameter.type", this.name.unqualified);
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
			markers.add(this.position, "classparameter.type.nodefault", this.name.unqualified);
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
		String desc = this.getDescription();
		writer.visitField(this.modifiers & 0xFFFF, this.name.qualified, desc, this.getSignature(), null);
		
		if (this.defaultValue == null)
		{
			return;
		}
		
		// Copy the access modifiers and add the STATIC modifier
		int modifiers = this.theClass.getModifiers() & Modifiers.ACCESS_MODIFIERS | Modifiers.STATIC;
		String name = "parDefault$class$" + this.index;
		MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(modifiers, name, "()" + desc, null, null));
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
		if (instance != null)
		{
			instance.writeExpression(writer);
		}
		
		writer.writeFieldInsn(Opcodes.GETFIELD, this.theClass.getInternalName(), this.name.qualified, this.getDescription());
	}
	
	@Override
	public void writeSet(MethodWriter writer, IValue instance, IValue value)
	{
		if (instance != null)
		{
			instance.writeExpression(writer);
		}
		
		if (value != null)
		{
			value.writeExpression(writer);
		}
		
		writer.writeFieldInsn(Opcodes.PUTFIELD, this.theClass.getInternalName(), this.name.qualified, this.getDescription());
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
			buffer.append(Formatting.Field.keyValueSeperator);
			this.defaultValue.toString(prefix, buffer);
		}
	}
}

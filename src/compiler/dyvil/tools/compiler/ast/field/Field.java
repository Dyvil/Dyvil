package dyvil.tools.compiler.ast.field;

import java.lang.annotation.ElementType;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.util.ModifierTypes;

public class Field extends Member implements IField
{
	protected IClass	theClass;
	private IValue		value;
	
	public Field(IClass iclass)
	{
		this.theClass = iclass;
	}
	
	public Field(IClass iclass, String name)
	{
		super(name);
		this.theClass = iclass;
	}
	
	public Field(IClass iclass, String name, IType type)
	{
		super(name, type);
		this.theClass = iclass;
	}
	
	@Override
	public boolean isField()
	{
		return true;
	}
	
	@Override
	public IClass getTheClass()
	{
		return this.theClass;
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
		if ("dyvil.lang.annotation.lazy".equals(type))
		{
			this.modifiers |= Modifiers.LAZY;
			return false;
		}
		if ("dyvil.lang.annotation.sealed".equals(type))
		{
			this.modifiers |= Modifiers.SEALED;
			return false;
		}
		if ("java.lang.Deprecated".equals(type))
		{
			this.modifiers |= Modifiers.DEPRECATED;
			return false;
		}
		return true;
	}
	
	@Override
	public ElementType getAnnotationType()
	{
		return ElementType.FIELD;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		super.resolveTypes(markers, context);
		
		if (this.value != null)
		{
			this.value.resolveTypes(markers, context);
		}
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, context);
		
		if (this.value != null)
		{
			this.value = this.value.resolve(markers, context);
		}
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		super.checkTypes(markers, context);
		
		if (this.value != null)
		{
			IValue value1 = this.value.withType(this.type);
			if (value1 == null)
			{
				Marker marker = markers.create(this.value.getPosition(), "field.type", this.name);
				marker.addInfo("Field Type: " + this.type);
				marker.addInfo("Value Type: " + this.value.getType());
				
			}
			else
			{
				this.value = value1;
			}
			
			this.value.checkTypes(markers, context);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		super.check(markers, context);
		
		if (this.value != null)
		{
			this.value.check(markers, context);
		}
	}
	
	@Override
	public void foldConstants()
	{
		super.foldConstants();
		
		if (this.value != null)
		{
			this.value = this.value.foldConstants();
		}
	}
	
	@Override
	public void write(ClassWriter writer)
	{
		if ((this.modifiers & Modifiers.LAZY) == Modifiers.LAZY)
		{
			String desc = "()" + this.getDescription();
			String signature = this.type.getSignature();
			if (signature != null)
			{
				signature = "()" + signature;
			}
			MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(this.modifiers & Modifiers.METHOD_MODIFIERS, this.name, desc, signature, null));
			
			for (int i = 0; i < this.annotationCount; i++)
			{
				this.annotations[i].write(mw);
			}
			
			mw.addAnnotation("Ldyvil/lang/annotation/lazy;", false);
			
			mw.begin();
			this.value.writeExpression(mw);
			mw.end(this.type);
			
			return;
		}
		
		FieldVisitor fv = writer.visitField(this.modifiers & 0xFFFF, this.name, this.getDescription(), this.type.getSignature(), null);
		if ((this.modifiers & Modifiers.SEALED) != 0)
		{
			fv.visitAnnotation("Ldyvil/lang/annotation/sealed", false);
		}
		if ((this.modifiers & Modifiers.DEPRECATED) != 0)
		{
			fv.visitAnnotation("Ljava/lang/Deprecated;", true);
		}
		
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].write(fv);
		}
	}
	
	@Override
	public void writeGet(MethodWriter writer, IValue instance)
	{
		if (instance != null && ((this.modifiers & Modifiers.STATIC) == 0 || instance.getValueType() != IValue.CLASS_ACCESS))
		{
			instance.writeExpression(writer);
		}
		
		String owner = this.theClass.getInternalName();
		String name = this.name;
		String desc = this.type.getExtendedName();
		if ((this.modifiers & Modifiers.STATIC) == Modifiers.STATIC)
		{
			writer.writeGetStatic(owner, name, desc, this.type);
		}
		else
		{
			writer.writeGetField(owner, name, desc, this.type);
		}
	}
	
	@Override
	public void writeSet(MethodWriter writer, IValue instance, IValue value)
	{
		if (instance != null && ((this.modifiers & Modifiers.STATIC) == 0 || instance.getValueType() != IValue.CLASS_ACCESS))
		{
			instance.writeExpression(writer);
		}
		
		value.writeExpression(writer);
		
		String owner = this.theClass.getInternalName();
		String name = this.name;
		String desc = this.type.getExtendedName();
		if ((this.modifiers & Modifiers.STATIC) == Modifiers.STATIC)
		{
			writer.writePutStatic(owner, name, desc);
		}
		else
		{
			writer.writePutField(owner, name, desc);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		super.toString(prefix, buffer);
		
		buffer.append(ModifierTypes.FIELD.toString(this.modifiers));
		this.type.toString("", buffer);
		buffer.append(' ');
		
		if (Formatting.Field.convertQualifiedNames)
		{
			buffer.append(this.qualifiedName);
		}
		else
		{
			buffer.append(this.name);
		}
		
		IValue value = this.value;
		if (value != null)
		{
			buffer.append(Formatting.Field.keyValueSeperator);
			Formatting.appendValue(value, prefix, buffer);
		}
		buffer.append(';');
	}
}

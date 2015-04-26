package dyvil.tools.compiler.ast.field;

import java.lang.annotation.ElementType;

import org.objectweb.asm.FieldVisitor;

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
import dyvil.tools.compiler.backend.exception.BytecodeException;
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
	
	public Field(IClass iclass, Name name)
	{
		super(name);
		this.theClass = iclass;
	}
	
	public Field(IClass iclass, Name name, IType type)
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
		switch (type)
		{
		case "dyvil/annotation/lazy":
			this.modifiers |= Modifiers.LAZY;
			return false;
		case "dyvil/annotation/sealed":
			this.modifiers |= Modifiers.SEALED;
			return false;
		case "dyvil/annotation/Transient":
			this.modifiers |= Modifiers.TRANSIENT;
			return false;
		case "dyvil/annotation/Volatile":
			this.modifiers |= Modifiers.VOLATILE;
			return false;
		case "java/lang/Deprecated":
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
			
			if (this.type == Types.UNKNOWN)
			{
				this.type = this.value.getType();
				if (this.type == Types.UNKNOWN)
				{
					markers.add(this.position, "field.type.infer", this.name.unqualified);
				}
				return;
			}
			
			IValue value1 = this.value.withType(this.type);
			if (value1 == null)
			{
				Marker marker = markers.create(this.value.getPosition(), "field.type", this.name.unqualified);
				marker.addInfo("Field Type: " + this.type);
				marker.addInfo("Value Type: " + this.value.getType());
			}
			else
			{
				this.value = value1;
			}
			return;
		}
		if (this.type == Types.UNKNOWN)
		{
			markers.add(this.position, "field.type.novalue", this.name.unqualified);
		}
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		super.checkTypes(markers, context);
		
		if (this.value != null)
		{
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
	public void write(ClassWriter writer) throws BytecodeException
	{
		if ((this.modifiers & Modifiers.LAZY) == Modifiers.LAZY)
		{
			String desc = "()" + this.getDescription();
			String signature = this.type.getSignature();
			if (signature != null)
			{
				signature = "()" + signature;
			}
			MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(this.modifiers & Modifiers.METHOD_MODIFIERS, this.name.qualified, desc,
					signature, null));
			
			for (int i = 0; i < this.annotationCount; i++)
			{
				this.annotations[i].write(mw);
			}
			
			mw.addAnnotation("Ldyvil/annotation/lazy;", false);
			
			mw.begin();
			this.value.writeExpression(mw);
			mw.end(this.type);
			
			return;
		}
		
		FieldVisitor fv = writer.visitField(this.modifiers & 0xFFFF, this.name.qualified, this.type.getExtendedName(), this.type.getSignature(), null);
		if ((this.modifiers & Modifiers.SEALED) != 0)
		{
			fv.visitAnnotation("Ldyvil/annotation/sealed", false);
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
	public void writeGet(MethodWriter writer, IValue instance) throws BytecodeException
	{
		if (instance != null && ((this.modifiers & Modifiers.STATIC) == 0 || instance.valueTag() != IValue.CLASS_ACCESS))
		{
			instance.writeExpression(writer);
		}
		
		String owner = this.theClass.getInternalName();
		String name = this.name.qualified;
		String desc = this.type.getExtendedName();
		if ((this.modifiers & Modifiers.STATIC) != 0)
		{
			writer.writeFieldInsn(Opcodes.GETSTATIC, owner, name, desc);
		}
		else
		{
			writer.writeFieldInsn(Opcodes.GETFIELD, owner, name, desc);
		}
	}
	
	@Override
	public void writeSet(MethodWriter writer, IValue instance, IValue value) throws BytecodeException
	{
		if (instance != null && ((this.modifiers & Modifiers.STATIC) == 0 || instance.valueTag() != IValue.CLASS_ACCESS))
		{
			instance.writeExpression(writer);
		}
		
		value.writeExpression(writer);
		
		String owner = this.theClass.getInternalName();
		String name = this.name.qualified;
		String desc = this.type.getExtendedName();
		if ((this.modifiers & Modifiers.STATIC) != 0)
		{
			writer.writeFieldInsn(Opcodes.PUTSTATIC, owner, name, desc);
		}
		else
		{
			writer.writeFieldInsn(Opcodes.PUTFIELD, owner, name, desc);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		super.toString(prefix, buffer);
		
		buffer.append(ModifierTypes.FIELD.toString(this.modifiers));
		this.type.toString("", buffer);
		buffer.append(' ');
		buffer.append(this.name);
		
		if (this.value != null)
		{
			buffer.append(Formatting.Field.keyValueSeperator);
			this.value.toString(prefix, buffer);
		}
		buffer.append(';');
	}
}

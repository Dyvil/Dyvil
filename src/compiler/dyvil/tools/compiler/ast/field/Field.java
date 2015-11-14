package dyvil.tools.compiler.ast.field;

import java.lang.annotation.ElementType;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.FieldVisitor;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.ThisValue;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.Deprecation;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.compiler.util.ModifierTypes;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class Field extends Member implements IField
{
	protected IClass	theClass;
	protected IValue	value;
	
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
	
	public Field(IClass iclass, Name name, IType type, int modifiers)
	{
		super(name, type);
		this.theClass = iclass;
		this.modifiers = modifiers;
	}
	
	public Field(ICodePosition position, IClass iclass, Name name, IType type, int modifiers)
	{
		super(name, type);
		this.position = position;
		this.theClass = iclass;
		this.modifiers = modifiers;
	}
	
	@Override
	public boolean isField()
	{
		return true;
	}
	
	@Override
	public void setTheClass(IClass iclass)
	{
		this.theClass = iclass;
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
	public boolean addRawAnnotation(String type, IAnnotation annotation)
	{
		switch (type)
		{
		case "dyvil/annotation/_internal/lazy":
			this.modifiers |= Modifiers.LAZY;
			return false;
		case "dyvil/annotation/_internal/internal":
			this.modifiers |= Modifiers.INTERNAL;
			return false;
		case "dyvil/annotation/Transient":
			this.modifiers |= Modifiers.TRANSIENT;
			return false;
		case "dyvil/annotation/Volatile":
			this.modifiers |= Modifiers.VOLATILE;
			return false;
		case Deprecation.JAVA_INTERNAL:
		case Deprecation.DYVIL_INTERNAL:
			this.modifiers |= Modifiers.DEPRECATED;
			return true;
		}
		return true;
	}
	
	@Override
	public ElementType getElementType()
	{
		return ElementType.FIELD;
	}
	
	@Override
	public IValue checkAccess(MarkerList markers, ICodePosition position, IValue instance, IContext context)
	{
		if (instance != null)
		{
			if ((this.modifiers & Modifiers.STATIC) != 0)
			{
				if (instance.valueTag() != IValue.CLASS_ACCESS)
				{
					markers.add(I18n.createMarker(position, "field.access.static", this.name));
				}
				else if (instance.getType().getTheClass() != this.theClass)
				{
					markers.add(I18n.createMarker(position, "field.access.static.type", this.name, this.theClass.getFullName()));
				}
				instance = null;
			}
			else if (instance.valueTag() == IValue.CLASS_ACCESS)
			{
				if (!instance.getType().getTheClass().isObject())
				{
					markers.add(I18n.createMarker(position, "field.access.instance", this.name));
				}
			}
			else
			{
				IType type = this.theClass.getClassType();
				IValue instance1 = IType.convertValue(instance, type, type, markers, context);
				
				if (instance1 == null)
				{
					Util.createTypeError(markers, instance, type, type, "field.access.receiver_type", this.name);
				}
				else
				{
					instance = instance1;
				}
			}
		}
		else if ((this.modifiers & Modifiers.STATIC) == 0)
		{
			if (context.isStatic())
			{
				markers.add(I18n.createMarker(position, "field.access.instance", this.name));
			}
			else
			{
				markers.add(I18n.createMarker(position, "field.access.unqualified", this.name.unqualified));
				instance = new ThisValue(position, this.theClass.getType(), context, markers);
			}
		}
		
		if (this.hasModifier(Modifiers.DEPRECATED))
		{
			Deprecation.checkDeprecation(markers, position, this, "field");
		}
		
		switch (IContext.getVisibility(context, this))
		{
		case IContext.INTERNAL:
			markers.add(I18n.createMarker(position, "field.access.internal", this.name));
			break;
		case IContext.INVISIBLE:
			markers.add(I18n.createMarker(position, "field.access.invisible", this.name));
			break;
		}
		
		return instance;
	}
	
	@Override
	public IValue checkAssign(MarkerList markers, IContext context, ICodePosition position, IValue instance, IValue newValue)
	{
		if ((this.modifiers & Modifiers.FINAL) != 0)
		{
			markers.add(I18n.createMarker(position, "field.assign.final", this.name.unqualified));
		}
		
		IValue value1 = IType.convertValue(newValue, this.type, this.type, markers, context);
		if (value1 == null)
		{
			Marker marker = I18n.createMarker(newValue.getPosition(), "field.assign.type", this.name.unqualified);
			marker.addInfo(I18n.getString("field.type", this.type));
			marker.addInfo(I18n.getString("field.type", newValue.getType()));
			markers.add(marker);
		}
		else
		{
			newValue = value1;
		}
		
		return newValue;
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
			
			boolean inferType = false;
			if (this.type == Types.UNKNOWN)
			{
				inferType = true;
				this.type = this.value.getType();
				if (this.type == Types.UNKNOWN)
				{
					markers.add(I18n.createMarker(this.position, "field.type.infer", this.name.unqualified));
					this.type = Types.ANY;
				}
			}
			
			IValue value1 = IType.convertValue(this.value, this.type, this.type, markers, context);
			if (value1 == null)
			{
				Marker marker = I18n.createMarker(this.value.getPosition(), "field.type.incompatible", this.name.unqualified);
				marker.addInfo(I18n.getString("field.type", this.type));
				marker.addInfo(I18n.getString("value.type", this.value.getType()));
				markers.add(marker);
			}
			else
			{
				this.value = value1;
				if (inferType)
				{
					this.type = value1.getType();
				}
			}
			
			return;
		}
		if (this.type == Types.UNKNOWN)
		{
			markers.add(I18n.createMarker(this.position, "field.type.novalue", this.name.unqualified));
			this.type = Types.ANY;
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
		
		if (this.type == Types.VOID)
		{
			markers.add(I18n.createMarker(this.position, "field.type.void"));
		}
		
		int illegalModifiers = this.modifiers & ~Modifiers.FIELD_MODIFIERS;
		if (illegalModifiers != 0)
		{
			markers.add(
					I18n.createError(this.position, "modifiers.illegal", I18n.getString("field", this.name), ModifierTypes.METHOD.toString(illegalModifiers)));
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
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		super.cleanup(context, compilableList);
		
		if (this.value != null)
		{
			this.value = this.value.cleanup(context, compilableList);
		}
	}
	
	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		if ((this.modifiers & Modifiers.LAZY) == Modifiers.LAZY)
		{
			String desc = "()" + this.getDescription();
			String signature = this.getSignature();
			if (signature != null)
			{
				signature = "()" + signature;
			}
			MethodWriter mw = new MethodWriterImpl(writer,
					writer.visitMethod(this.modifiers & Modifiers.METHOD_MODIFIERS, this.name.qualified, desc, signature, null));
					
			mw.visitAnnotation("Ldyvil/annotation/_internal/lazy;", false);
			
			mw.begin();
			this.value.writeExpression(mw, this.type);
			mw.end(this.type);
			
			return;
		}
		
		FieldVisitor fv = writer.visitField(this.modifiers & 0xFFFF, this.name.qualified, this.type.getExtendedName(), this.type.getSignature(), null);
		
		IField.writeAnnotations(fv, this.annotations, this.type);
	}
	
	@Override
	public void writeStaticInit(MethodWriter writer) throws BytecodeException
	{
		if (this.value != null && (this.modifiers & Modifiers.STATIC) != 0)
		{
			this.value.writeExpression(writer, this.type);
			writer.writeFieldInsn(Opcodes.PUTSTATIC, this.theClass.getInternalName(), this.name.qualified, this.getDescription());
		}
	}
	
	@Override
	public void writeGet(MethodWriter writer, IValue instance, int lineNumber) throws BytecodeException
	{
		if (instance != null)
		{
			instance.writeExpression(writer, this.theClass.getType());
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
			writer.writeLineNumber(lineNumber);
			writer.writeFieldInsn(Opcodes.GETFIELD, owner, name, desc);
		}
	}
	
	@Override
	public void writeSet(MethodWriter writer, IValue instance, IValue value, int lineNumber) throws BytecodeException
	{
		if (instance != null)
		{
			instance.writeExpression(writer, this.theClass.getType());
		}
		if (value != null)
		{
			value.writeExpression(writer, this.type);
		}
		
		String owner = this.theClass.getInternalName();
		String name = this.name.qualified;
		String desc = this.type.getExtendedName();
		if ((this.modifiers & Modifiers.STATIC) != 0)
		{
			writer.writeFieldInsn(Opcodes.PUTSTATIC, owner, name, desc);
		}
		else
		{
			writer.writeLineNumber(lineNumber);
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
	}
}

package dyvil.tools.compiler.ast.parameter;

import java.lang.annotation.ElementType;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.FieldVisitor;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.ThisValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class ClassParameter extends Parameter implements IField
{
	public IClass theClass;
	
	public ClassParameter()
	{
	}
	
	public ClassParameter(IClass theClass)
	{
		this.theClass = theClass;
	}
	
	public ClassParameter(Name name, IType type)
	{
		this.name = name;
		this.type = type;
	}
	
	public ClassParameter(IClass theClass, Name name, IType type, int modifiers)
	{
		this.theClass = theClass;
		this.name = name;
		this.type = type;
		this.modifiers = modifiers;
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
	public IClass getTheClass()
	{
		return this.theClass;
	}
	
	@Override
	public boolean addRawAnnotation(String type, IAnnotation annotation)
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
					markers.add(I18n.createMarker(position, "classparameter.access.static", this.name.unqualified));
					return null;
				}
			}
			else if (instance.valueTag() == IValue.CLASS_ACCESS)
			{
				markers.add(I18n.createMarker(position, "classparameter.access.instance", this.name.unqualified));
			}
		}
		else if ((this.modifiers & Modifiers.STATIC) == 0)
		{
			markers.add(I18n.createMarker(position, "classparameter.access.unqualified", this.name.unqualified));
			return new ThisValue(position, this.theClass.getType(), context, markers);
		}
		
		return instance;
	}
	
	@Override
	public IValue checkAssign(MarkerList markers, IContext context, ICodePosition position, IValue instance, IValue newValue)
	{
		if (this.theClass.hasModifier(Modifiers.ANNOTATION))
		{
			markers.add(I18n.createError(position, "classparameter.assign.annotation", this.name.unqualified));
		}
		else if (newValue != null && (this.modifiers & Modifiers.FINAL) != 0)
		{
			markers.add(I18n.createMarker(position, "classparameter.assign.final", this.name.unqualified));
		}
		
		IValue value1 = newValue.withType(this.type, null, markers, context);
		if (value1 == null)
		{
			Marker marker = I18n.createMarker(newValue.getPosition(), "classparameter.assign.type", this.name.unqualified);
			marker.addInfo("Class Parameter Type: " + this.type);
			marker.addInfo("Value Type: " + newValue.getType());
			markers.add(marker);
		}
		else
		{
			newValue = value1;
		}
		
		return newValue;
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, context);
		
		if (this.defaultValue != null)
		{
			this.defaultValue = this.defaultValue.resolve(markers, context);
			
			IValue value1 = this.defaultValue.withType(this.type, null, markers, context);
			if (value1 == null)
			{
				Marker marker = I18n.createMarker(this.defaultValue.getPosition(), "classparameter.type", this.name.unqualified);
				marker.addInfo("Parameter Type: " + this.type);
				marker.addInfo("Value Type: " + this.defaultValue.getType());
				markers.add(marker);
			}
			else
			{
				this.defaultValue = value1;
			}
			
			this.defaultValue = Util.constant(this.defaultValue, markers);
			return;
		}
		if (this.type == Types.UNKNOWN)
		{
			markers.add(I18n.createMarker(this.position, "classparameter.type.nodefault", this.name.unqualified));
			this.type = Types.ANY;
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
		
		if (this.type == Types.VOID)
		{
			markers.add(I18n.createMarker(this.position, "classparameter.type.void"));
		}
	}
	
	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		String desc = this.getDescription();
		FieldVisitor fv = writer.visitField(this.modifiers & 0xFFFF, this.name.qualified, desc, this.getSignature(), null);
		
		IField.writeAnnotations(fv, this.annotations, this.type);
		
		if (this.defaultValue == null)
		{
			return;
		}
		
		// Copy the access modifiers and add the STATIC modifier
		int modifiers = this.theClass.getModifiers() & Modifiers.ACCESS_MODIFIERS | Modifiers.STATIC;
		String name = "parDefault$class$" + this.index;
		MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(modifiers, name, "()" + desc, null, null));
		mw.begin();
		this.defaultValue.writeExpression(mw, this.type);
		mw.end(this.type);
	}
	
	@Override
	public void write(MethodWriter writer)
	{
		writer.registerParameter(this.index, this.name.qualified, this.type, 0);
		this.writeAnnotations(writer);
	}
	
	@Override
	public void writeGet(MethodWriter writer, IValue instance, int lineNumber) throws BytecodeException
	{
		if (instance != null)
		{
			instance.writeExpression(writer, this.theClass.getType());
		}
		
		if (this.theClass.hasModifier(Modifiers.ANNOTATION))
		{
			StringBuilder desc = new StringBuilder("()");
			this.type.appendExtendedName(desc);
			writer.writeInvokeInsn(Opcodes.INVOKEINTERFACE, this.theClass.getInternalName(), this.name.qualified, desc.toString(), true);
		}
		else
		{
			writer.writeFieldInsn(Opcodes.GETFIELD, this.theClass.getInternalName(), this.name.qualified, this.getDescription());
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
		
		writer.writeFieldInsn(Opcodes.PUTFIELD, this.theClass.getInternalName(), this.name.qualified, this.getDescription());
	}
}

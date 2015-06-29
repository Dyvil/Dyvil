package dyvil.tools.compiler.ast.parameter;

import java.lang.annotation.ElementType;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.ThisValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class ClassParameter extends Parameter implements IField
{
	public IClass	theClass;
	
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
	public IClass getTheClass()
	{
		return this.theClass;
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
	public IValue checkAccess(MarkerList markers, ICodePosition position, IValue instance, IContext context)
	{
		if (instance != null)
		{
			if ((this.modifiers & Modifiers.STATIC) != 0)
			{
				if (instance.valueTag() != IValue.CLASS_ACCESS)
				{
					markers.add(position, "classparameter.access.static", this.name.unqualified);
					return null;
				}
			}
			else if (instance.valueTag() == IValue.CLASS_ACCESS)
			{
				markers.add(position, "classparameter.access.instance", this.name.unqualified);
			}
		}
		else if ((this.modifiers & Modifiers.STATIC) == 0)
		{
			markers.add(position, "classparameter.access.unqualified", this.name.unqualified);
			return new ThisValue(position, this.theClass.getType());
		}
		
		return instance;
	}
	
	@Override
	public IValue checkAssign(MarkerList markers, IContext context, ICodePosition position, IValue instance, IValue newValue)
	{
		if (newValue != null && (this.modifiers & Modifiers.FINAL) != 0)
		{
			markers.add(position, "classparameter.assign.final", this.name.unqualified);
		}
		
		IValue value1 = newValue.withType(this.type, null, markers, context);
		if (value1 == null)
		{
			Marker marker = markers.create(newValue.getPosition(), "classparameter.assign.type", this.name.unqualified);
			marker.addInfo("Class Parameter Type: " + this.type);
			marker.addInfo("Value Type: " + newValue.getType());
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
			
			boolean inferType = false;
			if (this.type == Types.UNKNOWN)
			{
				inferType = true;
				this.type = this.defaultValue.getType();
				if (this.type == Types.UNKNOWN)
				{
					markers.add(this.position, "classparameter.type.infer", this.name.unqualified);
					this.type = Types.ANY;
				}
			}
			
			IValue value1 = this.defaultValue.withType(this.type, null, markers, context);
			if (value1 == null)
			{
				Marker marker = markers.create(this.defaultValue.getPosition(), "classparameter.type", this.name.unqualified);
				marker.addInfo("Parameter Type: " + this.type);
				marker.addInfo("Value Type: " + this.defaultValue.getType());
			}
			else
			{
				this.defaultValue = value1;
				if (inferType)
				{
					this.type = value1.getType();
				}
			}
			return;
		}
		if (this.type == Types.UNKNOWN)
		{
			markers.add(this.position, "classparameter.type.nodefault", this.name.unqualified);
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
			markers.add(this.position, "classparameter.type.void");
		}
	}
	
	@Override
	public void write(ClassWriter writer) throws BytecodeException
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
			writer.addParameterAnnotation(this.index, "Ldyvil/annotation/var;", true);
		}
		
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].write(writer, this.index);
		}
	}
	
	@Override
	public void writeGet(MethodWriter writer, IValue instance) throws BytecodeException
	{
		if (instance != null)
		{
			instance.writeExpression(writer);
		}
		
		writer.writeFieldInsn(Opcodes.GETFIELD, this.theClass.getInternalName(), this.name.qualified, this.getDescription());
	}
	
	@Override
	public void writeSet(MethodWriter writer, IValue instance, IValue value) throws BytecodeException
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
}

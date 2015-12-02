package dyvil.tools.compiler.ast.field;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.ThisExpr;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;

public final class CaptureField implements IField
{
	public IClass		theClass;
	public String		name;
	public IDataMember	field;
	private IType		type;
	
	public CaptureField(IClass iclass)
	{
		this.theClass = iclass;
	}
	
	public CaptureField(IClass iclass, IDataMember field)
	{
		this.theClass = iclass;
		this.field = field;
		this.type = field.getType();
		
		this.name = "this$" + field.getName().qualified;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.field.getPosition();
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
	}
	
	@Override
	public boolean isField()
	{
		return this.field.isField();
	}
	
	@Override
	public boolean isVariable()
	{
		return this.field.isVariable();
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
	public int getAccessLevel()
	{
		return this.field.getAccessLevel();
	}
	
	@Override
	public void setName(Name name)
	{
		this.field.setName(name);
	}
	
	@Override
	public Name getName()
	{
		return this.field.getName();
	}
	
	@Override
	public void setType(IType type)
	{
		this.field.setType(type);
		this.type = type;
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public void setModifiers(ModifierSet modifiers)
	{
		this.field.setModifiers(modifiers);
	}
	
	@Override
	public ModifierSet getModifiers()
	{
		return this.field.getModifiers();
	}
	
	@Override
	public boolean hasModifier(int mod)
	{
		return this.field.hasModifier(mod);
	}
	
	@Override
	public AnnotationList getAnnotations()
	{
		return this.field.getAnnotations();
	}
	
	@Override
	public void setAnnotations(AnnotationList annotations)
	{
	}
	
	@Override
	public IAnnotation getAnnotation(IClass type)
	{
		return this.field.getAnnotation(type);
	}
	
	@Override
	public void addAnnotation(IAnnotation annotation)
	{
	}
	
	@Override
	public ElementType getElementType()
	{
		return ElementType.FIELD;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.field.setValue(value);
	}
	
	@Override
	public IValue getValue()
	{
		return this.field.getValue();
	}
	
	@Override
	public IValue checkAccess(MarkerList markers, ICodePosition position, IValue instance, IContext context)
	{
		if (instance == null)
		{
			markers.add(I18n.createMarker(position, "field.access.unqualified", this.name));
			return new ThisExpr(position, context.getThisClass().getType(), context, markers);
		}
		
		return instance;
	}
	
	@Override
	public IValue checkAssign(MarkerList markers, IContext context, ICodePosition position, IValue instance, IValue newValue)
	{
		return this.field.checkAssign(markers, context, position, instance, newValue);
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public void foldConstants()
	{
	}
	
	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
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
	public void write(ClassWriter writer) throws BytecodeException
	{
		writer.visitField(Modifiers.PRIVATE | Modifiers.MANDATED, this.name, this.type.getExtendedName(), this.type.getSignature(), null);
	}
	
	@Override
	public void writeGet(MethodWriter writer, IValue instance, int lineNumber) throws BytecodeException
	{
		writer.writeVarInsn(Opcodes.ALOAD, 0);
		String owner = this.theClass.getInternalName();
		String name = this.name;
		String desc = this.type.getExtendedName();
		writer.writeFieldInsn(Opcodes.GETFIELD, owner, name, desc);
	}
	
	@Override
	public void writeSet(MethodWriter writer, IValue instance, IValue value, int lineNumber) throws BytecodeException
	{
		writer.writeVarInsn(Opcodes.ALOAD, 0);
		if (value != null)
		{
			value.writeExpression(writer, this.type);
		}
		String owner = this.theClass.getInternalName();
		String name = this.name;
		String desc = this.type.getExtendedName();
		writer.writeFieldInsn(Opcodes.PUTFIELD, owner, name, desc);
	}
	
	@Override
	public void writeSignature(DataOutput out) throws IOException
	{
	}
	
	@Override
	public void readSignature(DataInput in) throws IOException
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.field.toString(prefix, buffer);
	}
}

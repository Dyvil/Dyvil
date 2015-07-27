package dyvil.tools.compiler.ast.field;

import java.lang.annotation.ElementType;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.ThisValue;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

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
	public void setModifiers(int modifiers)
	{
		this.field.setModifiers(modifiers);
	}
	
	@Override
	public boolean addModifier(int mod)
	{
		return this.field.addModifier(mod);
	}
	
	@Override
	public void removeModifier(int mod)
	{
		this.field.removeModifier(mod);
	}
	
	@Override
	public int getModifiers()
	{
		return this.field.getModifiers();
	}
	
	@Override
	public boolean hasModifier(int mod)
	{
		return this.field.hasModifier(mod);
	}
	
	@Override
	public int annotationCount()
	{
		return this.field.annotationCount();
	}
	
	@Override
	public void setAnnotations(Annotation[] annotations, int count)
	{
		this.field.setAnnotations(annotations, count);
	}
	
	@Override
	public void setAnnotation(int index, Annotation annotation)
	{
		this.field.setAnnotation(index, annotation);
	}
	
	@Override
	public void addAnnotation(Annotation annotation)
	{
		this.field.addAnnotation(annotation);
	}
	
	@Override
	public boolean addRawAnnotation(String type)
	{
		return this.field.addRawAnnotation(type);
	}
	
	@Override
	public void removeAnnotation(int index)
	{
		this.field.removeAnnotation(index);
	}
	
	@Override
	public Annotation[] getAnnotations()
	{
		return this.field.getAnnotations();
	}
	
	@Override
	public Annotation getAnnotation(int index)
	{
		return this.field.getAnnotation(index);
	}
	
	@Override
	public Annotation getAnnotation(IClass type)
	{
		return this.field.getAnnotation(type);
	}
	
	@Override
	public ElementType getAnnotationType()
	{
		return this.field.getAnnotationType();
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
			markers.add(position, "field.access.unqualified", this.name);
			return new ThisValue(position, context.getThisClass().getType(), context, markers);
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
			value.writeExpression(writer);
		}
		String owner = this.theClass.getInternalName();
		String name = this.name;
		String desc = this.type.getExtendedName();
		writer.writeFieldInsn(Opcodes.PUTFIELD, owner, name, desc);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.field.toString(prefix, buffer);
	}
}

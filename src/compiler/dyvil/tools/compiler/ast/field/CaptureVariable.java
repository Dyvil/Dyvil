package dyvil.tools.compiler.ast.field;

import java.lang.annotation.ElementType;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class CaptureVariable implements IVariable
{
	public int			index;
	public IVariable	variable;
	private IType		refType;
	
	public CaptureVariable()
	{
	}
	
	public CaptureVariable(IVariable variable)
	{
		this.variable = variable;
		this.refType = variable.getType();
	}
	
	@Override
	public int getAccessLevel()
	{
		return this.variable.getAccessLevel();
	}
	
	@Override
	public void setName(Name name)
	{
		this.variable.setName(name);
	}
	
	@Override
	public Name getName()
	{
		return this.variable.getName();
	}
	
	@Override
	public void setType(IType type)
	{
		this.variable.setType(type);
	}
	
	@Override
	public IType getType()
	{
		return this.variable.getType();
	}
	
	public IType getCaptureType()
	{
		return this.refType != null ? this.refType : this.variable.getType();
	}
	
	@Override
	public void setModifiers(int modifiers)
	{
		this.variable.setModifiers(modifiers);
	}
	
	@Override
	public boolean addModifier(int mod)
	{
		return this.variable.addModifier(mod);
	}
	
	@Override
	public void removeModifier(int mod)
	{
		this.variable.removeModifier(mod);
	}
	
	@Override
	public int getModifiers()
	{
		return this.variable.getModifiers();
	}
	
	@Override
	public boolean hasModifier(int mod)
	{
		return this.variable.hasModifier(mod);
	}
	
	@Override
	public void setAnnotations(Annotation[] annotations, int count)
	{
		this.variable.setAnnotations(annotations, count);
	}
	
	@Override
	public void setAnnotation(int index, Annotation annotation)
	{
		this.variable.setAnnotation(index, annotation);
	}
	
	@Override
	public void addAnnotation(Annotation annotation)
	{
		this.variable.addAnnotation(annotation);
	}
	
	@Override
	public boolean addRawAnnotation(String type)
	{
		return this.variable.addRawAnnotation(type);
	}
	
	@Override
	public void removeAnnotation(int index)
	{
		this.variable.removeAnnotation(index);
	}
	
	@Override
	public Annotation getAnnotation(int index)
	{
		return this.variable.getAnnotation(index);
	}
	
	@Override
	public Annotation getAnnotation(IClass type)
	{
		return this.variable.getAnnotation(type);
	}
	
	@Override
	public ElementType getAnnotationType()
	{
		return this.variable.getAnnotationType();
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.variable.setValue(value);
	}
	
	@Override
	public IValue getValue()
	{
		return this.variable.getValue();
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
	public boolean isCaptureType()
	{
		return this.refType != null;
	}
	
	@Override
	public IType getCaptureType(boolean init)
	{
		return this.refType != null ? this.refType : this.variable.getCaptureType(init);
	}
	
	@Override
	public IValue checkAccess(MarkerList markers, ICodePosition position, IValue instance, IContext context)
	{
		return this.variable.checkAccess(markers, position, instance, context);
	}
	
	@Override
	public IValue checkAssign(MarkerList markers, ICodePosition position, IValue instance, IValue newValue)
	{
		this.refType = this.variable.getCaptureType(true);
		return this.variable.checkAssign(markers, position, instance, newValue);
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
	public String getDescription()
	{
		return this.refType.getExtendedName();
	}
	
	@Override
	public String getSignature()
	{
		return this.refType.getSignature();
	}
	
	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
	}
	
	@Override
	public void writeGet(MethodWriter writer, IValue instance) throws BytecodeException
	{
		int index = this.variable.getIndex();
		this.variable.setIndex(this.index);
		this.variable.writeGet(writer, instance);
		this.variable.setIndex(index);
	}
	
	@Override
	public void writeSet(MethodWriter writer, IValue instance, IValue value) throws BytecodeException
	{
		int index = this.variable.getIndex();
		this.variable.setIndex(this.index);
		this.variable.writeSet(writer, instance, value);
		this.variable.setIndex(index);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.variable.toString(prefix, buffer);
	}
}

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
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class CaptureVariable implements IVariable
{
	public int		index;
	public IField	variable;
	private IType	type;
	
	public CaptureVariable()
	{
	}
	
	public CaptureVariable(IField variable)
	{
		this.variable = variable;
		this.type = variable.getType();
	}
	
	@Override
	public int getAccessLevel()
	{
		return this.variable.getAccessLevel();
	}
	
	@Override
	public byte getAccessibility()
	{
		return this.variable.getAccessibility();
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
		return this.type.getExtendedName();
	}
	
	@Override
	public String getSignature()
	{
		return this.type.getSignature();
	}
	
	@Override
	public void write(ClassWriter writer)
	{
		
	}
	
	@Override
	public void writeGet(MethodWriter writer, IValue instance)
	{
		writer.writeVarInsn(this.type.getLoadOpcode(), this.index);
	}
	
	@Override
	public void writeSet(MethodWriter writer, IValue instance, IValue value)
	{
		if (value != null)
		{
			value.writeExpression(writer);
		}
		writer.writeVarInsn(this.type.getStoreOpcode(), this.index);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.variable.toString(prefix, buffer);
	}
}

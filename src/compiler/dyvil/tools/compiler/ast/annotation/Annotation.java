package dyvil.tools.compiler.ast.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.EnumValue;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.Parameter;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.AnnotationType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValueList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public class Annotation extends ASTNode implements ITyped
{
	public String			name;
	public AnnotationType	type;
	public IArguments		arguments;
	public ElementType		target;
	
	public Annotation(AnnotationType type)
	{
		this.type = type;
		this.name = type.name;
	}
	
	public Annotation(ICodePosition position)
	{
		this.position = position;
	}
	
	public Annotation(ICodePosition position, AnnotationType type)
	{
		this.position = position;
		this.name = type.name;
		this.type = type;
	}
	
	public Annotation(ICodePosition position, String name)
	{
		this.position = position;
		this.name = name;
		this.type = new AnnotationType(position, name);
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = (AnnotationType) type;
	}
	
	@Override
	public Type getType()
	{
		return this.type;
	}
	
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.type = this.type.resolve(markers, context);
		this.arguments.resolveTypes(markers, context);
	}
	
	public void resolve(MarkerList markers, IContext context)
	{
		this.type.readMetaAnnotations();
		this.arguments.resolve(markers, context);
	}
	
	public void checkTypes(MarkerList markers, IContext context)
	{
		IClass theClass = this.type.theClass;
		if (theClass == null)
		{
			return;
		}
		
		int count = theClass.parameterCount();
		for (int i = 0; i < count; i++)
		{
			Parameter param = theClass.getParameter(i);
			IType type = param.getType();
			IValue value = this.arguments.getValue(i, param);
			IValue value1 = value.withType(type);
			
			if (value1 == null)
			{
				Marker marker = markers.create(value.getPosition(), "annotation.type", param.qualifiedName);
				marker.addInfo("Required Type: " + type);
				marker.addInfo("Value Type: " + value.getType());
				continue;
			}
			
			value1 = Util.constant(value1, markers);
			if (value1 != value)
			{
				this.arguments.setValue(i, param, value1);
			}
		}
	}
	
	public void check(MarkerList markers, IContext context)
	{
		IClass theClass = this.type.theClass;
		if (theClass == null)
		{
			return;
		}
		
		if (!theClass.hasModifier(Modifiers.ANNOTATION))
		{
			markers.add(this.position, "annotation.type", this.name);
			return;
		}
		
		if (!this.type.isTarget(this.target))
		{
			Marker error = markers.create(this.position, "annotation.target", this.name);
			error.addInfo("Element Target: " + this.target);
			error.addInfo("Allowed Targets: " + this.type.getTargets());
			markers.add(error);
		}
	}
	
	public void foldConstants()
	{
		this.arguments.foldConstants();
	}
	
	public void write(ClassWriter writer)
	{
		if (this.type.retention != RetentionPolicy.SOURCE)
		{
			this.write(writer.visitAnnotation(this.type.getExtendedName(), this.type.retention == RetentionPolicy.RUNTIME));
		}
	}
	
	public void write(MethodWriter writer)
	{
		if (this.type.retention != RetentionPolicy.SOURCE)
		{
			this.write(writer.addAnnotation(this.type.getExtendedName(), this.type.retention == RetentionPolicy.RUNTIME));
		}
	}
	
	public void write(FieldVisitor writer)
	{
		if (this.type.retention != RetentionPolicy.SOURCE)
		{
			this.write(writer.visitAnnotation(this.type.getExtendedName(), this.type.retention == RetentionPolicy.RUNTIME));
		}
	}
	
	public void write(MethodWriter writer, int index)
	{
		if (this.type.retention != RetentionPolicy.SOURCE)
		{
			this.write(writer.addParameterAnnotation(index, this.type.getExtendedName(), this.type.retention == RetentionPolicy.RUNTIME));
		}
	}
	
	private void write(AnnotationVisitor visitor)
	{
		IClass iclass = this.type.getTheClass();
		int count = iclass.parameterCount();
		for (int i = 0; i < count; i++)
		{
			Parameter param = iclass.getParameter(i);
			visitValue(visitor, param.qualifiedName, this.arguments.getValue(i, param));
		}
	}
	
	private static void visitValue(AnnotationVisitor visitor, String key, IValue value)
	{
		int valueType = value.getValueType();
		if (valueType == IValue.VALUE_LIST)
		{
			AnnotationVisitor arrayVisitor = visitor.visitArray(key);
			for (IValue v : (IValueList) value)
			{
				visitValue(arrayVisitor, null, v);
			}
		}
		else if (valueType == IValue.ENUM)
		{
			EnumValue enumValue = (EnumValue) value;
			visitor.visitEnum(key, enumValue.type.getExtendedName(), enumValue.name);
		}
		else if (value.isConstant())
		{
			visitor.visit(key, value.toObject());
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('@').append(this.name);
		this.arguments.toString(prefix, buffer);
	}
}

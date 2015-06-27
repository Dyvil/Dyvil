package dyvil.tools.compiler.ast.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassMetadata;
import dyvil.tools.compiler.ast.constant.EnumValue;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValueList;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.MethodParameter;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.NamedType;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;

public final class Annotation extends ASTNode implements ITyped
{
	public static final MethodParameter	VALUE		= new MethodParameter();
	
	static
	{
		VALUE.name = Name.getQualified("value");
	}
	
	public Name							name;
	public IType						type;
	public IArguments					arguments	= EmptyArguments.INSTANCE;
	
	public Annotation(IType type)
	{
		this.type = type;
		this.name = type.getName();
	}
	
	public Annotation(ICodePosition position)
	{
		this.position = position;
	}
	
	public Annotation(ICodePosition position, IType type)
	{
		this.position = position;
		this.name = type.getName();
		this.type = type;
	}
	
	public Annotation(ICodePosition position, Name name)
	{
		this.position = position;
		this.name = name;
		this.type = new NamedType(position, name);
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
	
	@Override
	public IType getType()
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
		this.arguments.resolve(markers, context);
	}
	
	public void checkTypes(MarkerList markers, IContext context)
	{
		IClass theClass = this.type.getTheClass();
		if (theClass == null)
		{
			return;
		}
		
		int count = theClass.parameterCount();
		for (int i = 0; i < count; i++)
		{
			IParameter param = theClass.getParameter(i);
			IType type = param.getType();
			IValue value = this.arguments.getValue(i, param);
			IValue value1 = value.withType(type);
			
			if (value1 == null)
			{
				Marker marker = markers.create(value.getPosition(), "annotation.type", param.getName().qualified);
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
	
	public void check(MarkerList markers, IContext context, ElementType target)
	{
		IClass theClass = this.type.getTheClass();
		if (theClass == null)
		{
			return;
		}
		
		if (!theClass.hasModifier(Modifiers.ANNOTATION))
		{
			markers.add(this.position, "annotation.type", this.name);
			return;
		}
		
		IClassMetadata metadata = theClass.getMetadata();
		if (!metadata.isTarget(target))
		{
			Marker error = markers.create(this.position, "annotation.target", this.name);
			error.addInfo("Element Target: " + target);
			error.addInfo("Allowed Targets: " + metadata.getTargets());
			markers.add(error);
		}
	}
	
	public void foldConstants()
	{
		this.arguments.foldConstants();
	}
	
	private RetentionPolicy getRetention()
	{
		return this.type.getTheClass().getMetadata().getRetention();
	}
	
	public void write(ClassWriter writer)
	{
		RetentionPolicy retention = this.getRetention();
		if (retention != RetentionPolicy.SOURCE)
		{
			this.write(writer.visitAnnotation(this.type.getExtendedName(), retention == RetentionPolicy.RUNTIME));
		}
	}
	
	public void write(MethodWriter writer)
	{
		RetentionPolicy retention = this.getRetention();
		if (retention != RetentionPolicy.SOURCE)
		{
			this.write(writer.addAnnotation(this.type.getExtendedName(), retention == RetentionPolicy.RUNTIME));
		}
	}
	
	public void write(FieldVisitor writer)
	{
		RetentionPolicy retention = this.getRetention();
		if (retention != RetentionPolicy.SOURCE)
		{
			this.write(writer.visitAnnotation(this.type.getExtendedName(), retention == RetentionPolicy.RUNTIME));
		}
	}
	
	public void write(MethodWriter writer, int index)
	{
		RetentionPolicy retention = this.getRetention();
		if (retention != RetentionPolicy.SOURCE)
		{
			this.write(writer.addParameterAnnotation(index, this.type.getExtendedName(), retention == RetentionPolicy.RUNTIME));
		}
	}
	
	private void write(AnnotationVisitor visitor)
	{
		IClass iclass = this.type.getTheClass();
		int count = iclass.parameterCount();
		for (int i = 0; i < count; i++)
		{
			IParameter param = iclass.getParameter(i);
			visitValue(visitor, param.getName().qualified, this.arguments.getValue(i, param));
		}
	}
	
	private static void visitValue(AnnotationVisitor visitor, String key, IValue value)
	{
		int valueType = value.valueTag();
		if (valueType == IValue.ARRAY)
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
			visitor.visitEnum(key, enumValue.type.getExtendedName(), enumValue.name.qualified);
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

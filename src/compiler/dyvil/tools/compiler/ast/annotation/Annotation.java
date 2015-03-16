package dyvil.tools.compiler.ast.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.util.Iterator;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.EnumValue;
import dyvil.tools.compiler.ast.constant.IConstantValue;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.ArgumentMap;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.AnnotationType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValueList;
import dyvil.tools.compiler.ast.value.IValueMap.KeyValuePair;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class Annotation extends ASTNode implements ITyped
{
	public String			name;
	public AnnotationType	type;
	// TODO Use IArguments and clean up the annotation system
	public ArgumentMap		arguments	= new ArgumentMap();
	public ElementType		target;
	
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
		this.arguments.resolveTypes(markers, context);
	}
	
	public void check(MarkerList markers, IContext context)
	{
		IClass theClass = this.type.theClass;
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
		
		for (Iterator<KeyValuePair> iterator = this.arguments.entryIterator(); iterator.hasNext();)
		{
			KeyValuePair entry = iterator.next();
			IMethod m = theClass.getBody().getMethod(entry.key);
			if (m == null)
			{
				markers.add(this.position, "annotation.method", this.name, entry.key);
				continue;
			}
			
			entry.value.check(markers, context);
			
			if (!entry.value.isConstant())
			{
				markers.add(entry.value.getPosition(), "annotation.constant", entry.key);
				continue;
			}
			
			IType type = m.getType();
			if (!entry.value.isType(type))
			{
				Marker marker = markers.create(entry.value.getPosition(), "annotation.type", entry.key);
				marker.addInfo("Required Type: " + type);
				marker.addInfo("Value Type: " + entry.value.getType());
				
			}
		}
	}
	
	public void foldConstants()
	{
		this.arguments.foldConstants();
	}
	
	public void write(ClassWriter writer)
	{
		RetentionPolicy retention = this.type.retention;
		if (retention != RetentionPolicy.SOURCE)
		{
			boolean visible = retention == RetentionPolicy.RUNTIME;
			
			AnnotationVisitor visitor = writer.visitAnnotation(this.type.getExtendedName(), visible);
			for (Iterator<KeyValuePair> iterator = this.arguments.entryIterator(); iterator.hasNext();)
			{
				KeyValuePair entry = iterator.next();
				visitValue(visitor, entry.key, entry.value);
			}
		}
	}
	
	public void write(MethodWriter writer)
	{
		RetentionPolicy retention = this.type.retention;
		if (retention != RetentionPolicy.SOURCE)
		{
			boolean visible = retention == RetentionPolicy.RUNTIME;
			
			AnnotationVisitor visitor = writer.addAnnotation(this.type.getExtendedName(), visible);
			for (Iterator<KeyValuePair> iterator = this.arguments.entryIterator(); iterator.hasNext();)
			{
				KeyValuePair entry = iterator.next();
				visitValue(visitor, entry.key, entry.value);
			}
		}
	}
	
	public void write(FieldVisitor writer)
	{
		RetentionPolicy retention = this.type.retention;
		if (retention != RetentionPolicy.SOURCE)
		{
			boolean visible = retention == RetentionPolicy.RUNTIME;
			
			AnnotationVisitor visitor = writer.visitAnnotation(this.type.getExtendedName(), visible);
			for (Iterator<KeyValuePair> iterator = this.arguments.entryIterator(); iterator.hasNext();)
			{
				KeyValuePair entry = iterator.next();
				visitValue(visitor, entry.key, entry.value);
			}
		}
	}
	
	public void write(MethodWriter writer, int index)
	{
		RetentionPolicy retention = this.type.retention;
		if (retention != RetentionPolicy.SOURCE)
		{
			boolean visible = retention == RetentionPolicy.RUNTIME;
			
			AnnotationVisitor visitor = writer.addParameterAnnotation(index, this.type.getExtendedName(), visible);
			for (Iterator<KeyValuePair> iterator = this.arguments.entryIterator(); iterator.hasNext();)
			{
				KeyValuePair entry = iterator.next();
				visitValue(visitor, entry.key, entry.value);
			}
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
			visitor.visit(key, ((IConstantValue) value).toObject());
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('@').append(this.name);
		this.arguments.toString(prefix, buffer);
	}
}

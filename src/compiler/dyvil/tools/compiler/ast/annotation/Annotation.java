package dyvil.tools.compiler.ast.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.util.*;
import java.util.Map.Entry;

import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.FieldVisitor;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.EnumValue;
import dyvil.tools.compiler.ast.constant.IConstantValue;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.AnnotationType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValueList;
import dyvil.tools.compiler.ast.value.IValueMap;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class Annotation extends ASTNode implements ITyped, IValueMap<String>
{
	public String				name;
	public AnnotationType		type;
	public Map<String, IValue>	parameters	= new HashMap();
	public ElementType			target;
	
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
	
	@Override
	public void setValues(Map<String, IValue> map)
	{
		this.parameters = map;
	}
	
	@Override
	public Map<String, IValue> getValues()
	{
		return this.parameters;
	}
	
	@Override
	public void addValue(String key, IValue value)
	{
		this.parameters.put(key, value);
	}
	
	@Override
	public IValue getValue(String key)
	{
		return this.parameters.get(key);
	}
	
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		this.type = this.type.resolve(markers, context);
		
		for (Entry<String, IValue> entry : this.parameters.entrySet())
		{
			entry.getValue().resolveTypes(markers, context);
		}
	}
	
	public void resolve(List<Marker> markers, IContext context)
	{
		this.type.readMetaAnnotations();
		
		for (Entry<String, IValue> entry : this.parameters.entrySet())
		{
			entry.getValue().resolve(markers, context);
		}
	}
	
	public void check(List<Marker> markers, IContext context)
	{
		IClass theClass = this.type.theClass;
		if (!theClass.hasModifier(Modifiers.ANNOTATION))
		{
			markers.add(Markers.create(this.position, "annotation.type", this.name));
			return;
		}
		
		if (!this.type.isTarget(this.target))
		{
			Marker error = Markers.create(this.position, "annotation.target", this.name);
			error.addInfo("Element Target: " + this.target);
			error.addInfo("Allowed Targets: " + this.type.getTargets());
			markers.add(error);
		}
		
		for (Entry<String, IValue> entry : this.parameters.entrySet())
		{
			String key = entry.getKey();
			IMethod m = theClass.getBody().getMethod(key);
			if (m == null)
			{
				markers.add(Markers.create(this.position, "annotation.method", this.name, key));
				continue;
			}
			
			IValue value = entry.getValue();
			value.check(markers, context);
			
			if (!value.isConstant())
			{
				markers.add(Markers.create(value.getPosition(), "annotation.constant", key));
				continue;
			}
			
			IType type = m.getType();
			if (!value.isType(type))
			{
				Marker marker = Markers.create(value.getPosition(), "annotation.type", key);
				marker.addInfo("Required Type: " + type);
				marker.addInfo("Value Type: " + value.getType());
				markers.add(marker);
			}
		}
	}
	
	public void foldConstants()
	{
		for (Entry<String, IValue> entry : this.parameters.entrySet())
		{
			entry.getValue().foldConstants();
		}
	}
	
	public void write(ClassWriter writer)
	{
		RetentionPolicy retention = this.type.retention;
		if (retention != RetentionPolicy.SOURCE)
		{
			boolean visible = retention == RetentionPolicy.RUNTIME;
			
			AnnotationVisitor visitor = writer.visitAnnotation(this.type.getExtendedName(), visible);
			for (Entry<String, IValue> entry : this.parameters.entrySet())
			{
				visitValue(visitor, entry.getKey(), entry.getValue());
			}
		}
	}
	
	public void write(MethodWriter writer)
	{
		RetentionPolicy retention = this.type.retention;
		if (retention != RetentionPolicy.SOURCE)
		{
			boolean visible = retention == RetentionPolicy.RUNTIME;
			
			AnnotationVisitor visitor = writer.visitAnnotation(this.type.getExtendedName(), visible);
			for (Entry<String, IValue> entry : this.parameters.entrySet())
			{
				visitValue(visitor, entry.getKey(), entry.getValue());
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
			for (Entry<String, IValue> entry : this.parameters.entrySet())
			{
				visitValue(visitor, entry.getKey(), entry.getValue());
			}
		}
	}
	
	public void write(MethodWriter writer, int index)
	{
		RetentionPolicy retention = this.type.retention;
		if (retention != RetentionPolicy.SOURCE)
		{
			boolean visible = retention == RetentionPolicy.RUNTIME;
			
			AnnotationVisitor visitor = writer.visitParameterAnnotation(index, this.type.getExtendedName(), visible);
			for (Entry<String, IValue> entry : this.parameters.entrySet())
			{
				visitValue(visitor, entry.getKey(), entry.getValue());
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
		else if (value instanceof IConstantValue)
		{
			visitor.visit(key, ((IConstantValue) value).toObject());
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('@').append(this.name);
		if (!this.parameters.isEmpty())
		{
			buffer.append(Formatting.Method.parametersStart);
			
			Iterator<Entry<String, IValue>> iterator = this.parameters.entrySet().iterator();
			while (true)
			{
				Entry<String, IValue> e = iterator.next();
				buffer.append(e.getKey()).append(Formatting.Field.keyValueSeperator);
				e.getValue().toString("", buffer);
				if (iterator.hasNext())
				{
					buffer.append(Formatting.Method.parameterSeperator);
				}
				else
				{
					break;
				}
			}
			
			buffer.append(Formatting.Method.parametersEnd);
		}
	}
}

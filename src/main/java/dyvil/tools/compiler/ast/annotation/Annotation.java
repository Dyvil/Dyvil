package dyvil.tools.compiler.ast.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.util.*;
import java.util.Map.Entry;

import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.api.*;
import dyvil.tools.compiler.ast.type.AnnotationType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Modifiers;

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
		this.type = this.type.resolve(context);
		if (!this.type.isResolved())
		{
			markers.add(new SemanticError(this.position, "'" + this.name + "' could not be resolved to a type"));
		}
		
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
	
	public void check(List<Marker> markers)
	{
		IClass theClass = this.type.theClass;
		if (!theClass.hasModifier(Modifiers.ANNOTATION))
		{
			markers.add(new SemanticError(this.position, "The type '" + this.name + "' is not an annotation type"));
		}
		
		if (!this.type.isTarget(this.target))
		{
			markers.add(new SemanticError(this.position, "The annotation type '" + this.name + "' is not applicable for the annotated element type " + this.target));
		}
		
		for (Entry<String, IValue> entry : this.parameters.entrySet())
		{
			String key = entry.getKey();
			IMethod m = theClass.getBody().getMethod(key);
			if (m == null)
			{
				markers.add(new SemanticError(this.position, "Invalid annotation method '" + key + "' for annotation type " + this.name));
				continue;
			}
			
			IValue value = entry.getValue();
			value.check(markers);
			
			if (!value.isConstant())
			{
				markers.add(new SemanticError(value.getPosition(), "The annotation value '" + key + "' has to be a constant expression"));
				continue;
			}
			
			IType type = m.getType();
			if (!Type.isSuperType(type, value.getType()))
			{
				markers.add(new SemanticError(value.getPosition(), "The annotation value '" + key + "' does not match the required type " + type));
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
		if (value instanceof IValueList)
		{
			AnnotationVisitor arrayVisitor = visitor.visitArray(key);
			for (IValue v : ((IValueList) value).getValues())
			{
				visitValue(arrayVisitor, null, v);
			}
		}
		else
		{
			visitor.visit(key, value.toObject());
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

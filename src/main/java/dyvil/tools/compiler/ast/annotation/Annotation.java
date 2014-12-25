package dyvil.tools.compiler.ast.annotation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.api.IMethod;
import dyvil.tools.compiler.ast.api.ITyped;
import dyvil.tools.compiler.ast.api.IValueList;
import dyvil.tools.compiler.ast.api.IValueMap;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.EnumValue;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Modifiers;

public class Annotation extends ASTNode implements ITyped, IValueMap<String>
{
	public Type					type;
	public Map<String, IValue>	parameters	= new HashMap();
	
	private String				name;
	
	public Annotation(ICodePosition position, Type type)
	{
		this.position = position;
		this.name = type.name;
		this.type = type;
	}
	
	public Annotation(ICodePosition position, String name)
	{
		this.position = position;
		this.name = name;
		this.type = new Type(name, position);
	}
	
	@Override
	public void setType(Type type)
	{
		this.type = type;
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
	
	@Override
	public Annotation applyState(CompilerState state, IContext context)
	{
		if (state == CompilerState.RESOLVE_TYPES)
		{
			this.type = this.type.resolve(context);
			if (!this.type.isResolved())
			{
				state.addMarker(new SemanticError(this.position, "'" + this.name + "' could not be resolved to a type"));
			}
		}
		else if (state == CompilerState.CHECK)
		{
			IClass theClass = this.type.theClass;
			if (!theClass.hasModifier(Modifiers.ANNOTATION))
			{
				state.addMarker(new SemanticError(this.position, "The type '" + this.name + "' is not an annotation type"));
			}
			
			for (Entry<String, IValue> entry : this.parameters.entrySet())
			{
				String key = entry.getKey();
				IMethod m = theClass.getBody().getMethod(key);
				if (m == null)
				{
					state.addMarker(new SemanticError(this.position, "Invalid annotation method '" + key + "' for annotation type " + this.name));
					continue;
				}
				
				IValue value = entry.getValue();
				Type type = m.getType();
				if (!Type.isSuperType(type, value.getType()))
				{
					state.addMarker(new SemanticError(value.getPosition(), "The annotation value '" + key + "' does not match the required type " + type));
				}
			}
		}
		return this;
	}
	
	public void write(MethodWriter writer)
	{
		boolean visible = false;
		Annotation retention = this.type.theClass.getAnnotation(Type.ARetention);
		if (retention != null)
		{
			EnumValue value = (EnumValue) retention.getValue("value");
			switch (value.name)
			{
			case "SOURCE":
				return;
			case "CLASS":
				break;
			case "RUNTIME":
				visible = true;
				break;
			}
		}
		
		AnnotationVisitor visitor = writer.visitAnnotation(this.type.getExtendedName(), visible);
		for (Entry<String, IValue> entry : this.parameters.entrySet())
		{
			visitValue(visitor, entry.getKey(), entry.getValue());
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

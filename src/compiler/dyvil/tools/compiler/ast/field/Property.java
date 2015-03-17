package dyvil.tools.compiler.ast.field;

import java.lang.annotation.ElementType;
import java.util.List;

import org.objectweb.asm.ClassWriter;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.Method;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.Parameter;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.util.ModifierTypes;

public class Property extends Member implements IProperty, IContext
{
	private IClass theClass;
	
	public IValue		get;
	public IValue		set;
	
	protected IMethod	getterMethod;
	protected Parameter	setterParameter;
	protected IMethod	setterMethod;
	
	public Property(IClass iclass)
	{
		this.theClass = iclass;
	}
	
	public Property(IClass iclass, String name)
	{
		super(name);
		this.theClass = iclass;
	}
	
	public Property(IClass iclass, String name, IType type)
	{
		super(name, type);
		this.theClass = iclass;
	}
	
	@Override
	public ElementType getAnnotationType()
	{
		return ElementType.FIELD;
	}
	
	@Override
	public void setValue(IValue value)
	{
	}
	
	@Override
	public IValue getValue()
	{
		return null;
	}
	
	@Override
	public void setGetter(IValue get)
	{
		this.get = get;
	}
	
	@Override
	public IValue getGetter()
	{
		return this.get;
	}
	
	@Override
	public void setSetter(IValue set)
	{
		this.set = set;
	}
	
	@Override
	public IValue getSetter()
	{
		return this.set;
	}
	
	@Override
	public void setGetterMethod(IMethod method)
	{
		this.get = method.getValue();
		this.getterMethod = method;
	}
	
	@Override
	public void setSetterMethod(IMethod method)
	{
		this.set = method.getValue();
		this.setterMethod = method;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		super.resolveTypes(markers, context);
		
		if (this.get != null)
		{
			this.get.resolveTypes(markers, this);
		}
		if (this.set != null)
		{
			this.set.resolveTypes(markers, this);
		}
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, context);
		
		if (this.get != null)
		{
			Method getter = new Method(this.theClass, "get$" + this.qualifiedName, this.type);
			getter.modifiers = this.modifiers | Modifiers.SYNTHETIC;
			getter.value = this.get;
			getter.setAnnotations(this.annotations, this.annotationCount);
			this.getterMethod = getter;
			this.get = this.get.resolve(markers, this);
		}
		if (this.set != null)
		{
			this.setterParameter = new Parameter(0, this.qualifiedName, this.type);
			Method setter = new Method(this.theClass, "set$" + this.qualifiedName, Type.VOID);
			setter.modifiers = this.modifiers | Modifiers.SYNTHETIC;
			setter.value = this.set;
			setter.addParameter(this.setterParameter);
			setter.setAnnotations(this.annotations, this.annotationCount);
			this.set = this.set.resolve(markers, this);
		}
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		super.checkTypes(markers, context);
		
		if (this.get != null)
		{
			IValue get1 = this.get.withType(this.type);
			if (get1 == null)
			{
				Marker marker = markers.create(this.get.getPosition(), "property.getter.type", this.name);
				marker.addInfo("Property Type: " + this.type);
				marker.addInfo("Getter Value Type: " + this.get.getType());
				
			}
			else
			{
				this.get = get1;
			}
		}
		if (this.set != null && !this.set.isType(Type.VOID))
		{
			markers.add(this.set.getPosition(), "property.setter.type", this.name);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		super.check(markers, context);
		
		if (this.get == null && this.set == null)
		{
			markers.add(this.position, "property.empty", this.name);
		}
	}
	
	@Override
	public void foldConstants()
	{
		super.foldConstants();
		
		if (this.get != null)
		{
			this.get = this.get.foldConstants();
		}
		if (this.set != null)
		{
			this.set = this.set.foldConstants();
		}
	}
	
	@Override
	public IType getThisType()
	{
		return this.theClass.getType();
	}
	
	@Override
	public Package resolvePackage(String name)
	{
		return this.theClass.resolvePackage(name);
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		return this.theClass.resolveClass(name);
	}
	
	@Override
	public FieldMatch resolveField(String name)
	{
		if (name.equals(this.name))
		{
			return new FieldMatch(this.setterParameter, 1);
		}
		return this.theClass.resolveField(name);
	}
	
	@Override
	public MethodMatch resolveMethod(IValue instance, String name, IArguments arguments)
	{
		return this.theClass.resolveMethod(instance, name, arguments);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, String name, IArguments arguments)
	{
		this.theClass.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public MethodMatch resolveConstructor(IArguments arguments)
	{
		return this.theClass.resolveConstructor(arguments);
	}
	
	@Override
	public void getConstructorMatches(List<MethodMatch> list, IArguments arguments)
	{
		this.theClass.getConstructorMatches(list, arguments);
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		return this.theClass.getAccessibility(member);
	}
	
	@Override
	public byte getAccessibility()
	{
		byte b = 0;
		if (this.get != null)
		{
			b |= IContext.READ_ACCESS;
		}
		if (this.set != null)
		{
			b |= IContext.WRITE_ACCESS;
		}
		return b;
	}
	
	// Compilation
	
	@Override
	public String getDescription()
	{
		return null;
	}

	@Override
	public String getSignature()
	{
		return null;
	}
	
	@Override
	public void write(ClassWriter writer)
	{
		if (this.getterMethod != null)
		{
			this.getterMethod.write(writer);
		}
		if (this.setterMethod != null)
		{
			this.setterMethod.write(writer);
		}
	}
	
	@Override
	public void writeGet(MethodWriter writer, IValue instance)
	{
		if (instance != null && ((this.modifiers & Modifiers.STATIC) == 0 || instance.getValueType() != IValue.CLASS_ACCESS))
		{
			instance.writeExpression(writer);
		}
		
		int opcode;
		int args;
		if ((this.modifiers & Modifiers.STATIC) == Modifiers.STATIC)
		{
			opcode = Opcodes.INVOKESTATIC;
			args = 0;
		}
		else
		{
			opcode = Opcodes.INVOKEVIRTUAL;
			args = 1;
		}
		
		String owner = this.theClass.getInternalName();
		String name = "get$" + this.qualifiedName;
		String desc = "()" + this.type.getExtendedName();
		writer.writeInvokeInsn(opcode, owner, name, desc, false, args, this.type);
	}
	
	@Override
	public void writeSet(MethodWriter writer, IValue instance, IValue value)
	{
		if (instance != null && ((this.modifiers & Modifiers.STATIC) == 0 || instance.getValueType() != IValue.CLASS_ACCESS))
		{
			instance.writeExpression(writer);
		}
		
		value.writeExpression(writer);
		
		int opcode;
		int args;
		if ((this.modifiers & Modifiers.STATIC) == Modifiers.STATIC)
		{
			opcode = Opcodes.INVOKESTATIC;
			args = 1;
		}
		else
		{
			opcode = Opcodes.INVOKEVIRTUAL;
			args = 2;
		}
		
		String owner = this.theClass.getInternalName();
		String name = "set$" + this.qualifiedName;
		String desc = "(" + this.type.getExtendedName() + ")V";
		writer.writeInvokeInsn(opcode, owner, name, desc, false, args, (String) null);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		for (Annotation annotation : this.annotations)
		{
			buffer.append(prefix);
			annotation.toString(prefix, buffer);
			buffer.append('\n');
		}
		
		buffer.append(prefix);
		buffer.append(ModifierTypes.FIELD.toString(this.modifiers));
		this.type.toString("", buffer);
		buffer.append(' ');
		
		if (Formatting.Field.convertQualifiedNames)
		{
			buffer.append(this.qualifiedName);
		}
		else
		{
			buffer.append(this.name);
		}
		
		buffer.append('\n').append(prefix).append('{');
		if (this.get != null)
		{
			buffer.append('\n').append(prefix).append(Formatting.Method.indent).append(Formatting.Field.propertyGet);
			this.get.toString(prefix + Formatting.Method.indent, buffer);
			buffer.append(';');
		}
		if (this.set != null)
		{
			buffer.append('\n').append(prefix).append(Formatting.Method.indent).append(Formatting.Field.propertySet);
			this.set.toString(prefix + Formatting.Method.indent, buffer);
			buffer.append(';');
		}
		buffer.append('\n').append(prefix).append('}');
	}
}

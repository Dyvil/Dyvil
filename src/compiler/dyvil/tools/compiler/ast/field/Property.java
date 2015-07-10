package dyvil.tools.compiler.ast.field;

import java.lang.annotation.ElementType;

import dyvil.lang.List;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.ThisValue;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.MethodParameter;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.ModifierTypes;

public class Property extends Member implements IProperty, IContext
{
	protected IClass			theClass;
	
	public IValue				get;
	public IValue				set;
	
	protected MethodParameter	setterParameter;
	private IProperty			overrideProperty;
	
	public Property(IClass iclass)
	{
		this.theClass = iclass;
	}
	
	public Property(IClass iclass, Name name)
	{
		super(name);
		this.theClass = iclass;
	}
	
	public Property(IClass iclass, Name name, IType type)
	{
		super(name, type);
		this.theClass = iclass;
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
	public boolean isField()
	{
		return true;
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
	public IValue checkAccess(MarkerList markers, ICodePosition position, IValue instance, IContext context)
	{
		if (instance != null)
		{
			if ((this.modifiers & Modifiers.STATIC) != 0)
			{
				if (instance.valueTag() != IValue.CLASS_ACCESS)
				{
					markers.add(position, "property.access.static", this.name.unqualified);
				}
				instance = null;
			}
			else if (instance.valueTag() == IValue.CLASS_ACCESS)
			{
				markers.add(position, "property.access.instance", this.name.unqualified);
			}
		}
		else if ((this.modifiers & Modifiers.STATIC) == 0)
		{
			if (context.isStatic())
			{
				markers.add(position, "property.access.instance", this.name.unqualified);
			}
			else
			{
				markers.add(position, "property.access.unqualified", this.name.unqualified);
				instance = new ThisValue(position, this.theClass.getType());
			}
		}
		
		if (this.hasModifier(Modifiers.DEPRECATED))
		{
			markers.add(position, "property.access.deprecated", this.name);
		}
		
		switch (context.getVisibility(this))
		{
		case IContext.SEALED:
			markers.add(position, "property.access.sealed", this.name);
			break;
		case IContext.INVISIBLE:
			markers.add(position, "property.access.invisible", this.name);
			break;
		}
		
		return instance;
	}
	
	@Override
	public IValue checkAssign(MarkerList markers, IContext context, ICodePosition position, IValue instance, IValue newValue)
	{
		if (this.set == null)
		{
			markers.add(position, "property.assign.readonly", this.name.unqualified);
		}
		
		IValue value1 = newValue.withType(this.type, null, markers, context);
		if (value1 == null)
		{
			Marker marker = markers.create(newValue.getPosition(), "property.assign.type", this.name.unqualified);
			marker.addInfo("Property Type: " + this.type);
			marker.addInfo("Value Type: " + newValue.getType());
		}
		else
		{
			newValue = value1;
		}
		
		return newValue;
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
		
		if (this.set != null)
		{
			this.setterParameter = new MethodParameter(this.name, this.type);
			this.setterParameter.setIndex(1);
			this.set = this.set.resolve(markers, this);
			
			IValue set1 = this.set.withType(Types.VOID, Types.VOID, markers, context);
			if (set1 == null)
			{
				markers.add(this.set.getPosition(), "property.setter.type", this.name);
			}
			else
			{
				this.set = set1;
			}
		}
		if (this.get != null)
		{
			this.get = this.get.resolve(markers, this);
			
			boolean inferType = false;
			if (this.type == Types.UNKNOWN)
			{
				inferType = true;
				this.type = this.get.getType();
				if (this.type == Types.UNKNOWN)
				{
					markers.add(this.position, "property.type.infer", this.name.unqualified);
					this.type = Types.ANY;
				}
			}
			
			IValue get1 = this.get.withType(this.type, this.type, markers, context);
			if (get1 == null)
			{
				Marker marker = markers.create(this.get.getPosition(), "property.getter.type", this.name.unqualified);
				marker.addInfo("Property Type: " + this.type);
				marker.addInfo("Getter Value Type: " + this.get.getType());
			}
			else
			{
				this.get = get1;
				if (inferType)
				{
					this.type = get1.getType();
				}
			}
			
			return;
		}
		if (this.type == Types.UNKNOWN)
		{
			markers.add(this.position, "property.type.infer.writeonly", this.name.unqualified);
			this.type = Types.ANY;
		}
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		super.checkTypes(markers, context);
		
		if (this.get != null)
		{
			this.get.checkTypes(markers, context);
		}
		if (this.set != null)
		{
			this.set.checkTypes(markers, context);
		}
		
		if ((this.modifiers & Modifiers.STATIC) == 0)
		{
			this.checkOverride(markers);
		}
	}
	
	private void checkOverride(MarkerList markers)
	{
		IDataMember f = this.theClass.getSuperType().resolveField(this.name);
		if (f == null)
		{
			if ((this.modifiers & Modifiers.OVERRIDE) != 0)
			{
				markers.add(this.position, "property.override", this.name);
			}
			return;
		}
		
		if (!(f instanceof IProperty))
		{
			return;
		}
		
		this.overrideProperty = (IProperty) f;
		
		if ((this.modifiers & Modifiers.OVERRIDE) == 0)
		{
			markers.add(this.position, "property.overrides", this.name);
		}
		else if (this.overrideProperty.hasModifier(Modifiers.FINAL))
		{
			markers.add(this.position, "property.override.final", this.name);
		}
		else
		{
			IType type = this.overrideProperty.getType();
			if (type != this.type && !type.equals(this.type))
			{
				Marker marker = markers.create(this.position, "property.override.type", this.name);
				marker.addInfo("Property Type: " + this.type);
				marker.addInfo("Overriden Property Type: " + type);
			}
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		super.check(markers, context);
		
		if (this.get != null)
		{
			this.get.check(markers, context);
		}
		if (this.set != null)
		{
			this.set.check(markers, context);
			
			if (this.type == Types.VOID)
			{
				markers.add(this.position, "property.type.void");
			}
		}
		// If both are null
		else if (this.get == null)
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
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		super.cleanup(context, compilableList);
		
		if (this.get != null)
		{
			this.get = this.get.cleanup(this, compilableList);
		}
		if (this.set != null)
		{
			this.set = this.set.cleanup(this, compilableList);
		}
	}
	
	@Override
	public boolean isStatic()
	{
		return (this.modifiers & Modifiers.STATIC) != 0;
	}
	
	@Override
	public IDyvilHeader getHeader()
	{
		return this.theClass.getHeader();
	}
	
	@Override
	public IClass getThisClass()
	{
		return this.theClass;
	}
	
	@Override
	public Package resolvePackage(Name name)
	{
		return this.theClass.resolvePackage(name);
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		return this.theClass.resolveClass(name);
	}
	
	@Override
	public IType resolveType(Name name)
	{
		return this.theClass.resolveType(name);
	}
	
	@Override
	public ITypeVariable resolveTypeVariable(Name name)
	{
		return this.theClass.resolveTypeVariable(name);
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		if (name == this.name)
		{
			return this.setterParameter;
		}
		return this.theClass.resolveField(name);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		this.theClass.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
		this.theClass.getConstructorMatches(list, arguments);
	}
	
	@Override
	public boolean handleException(IType type)
	{
		return false;
	}
	
	@Override
	public byte getVisibility(IClassMember member)
	{
		return this.theClass.getVisibility(member);
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
	public void write(ClassWriter writer) throws BytecodeException
	{
		String extended = this.type.getExtendedName();
		String signature = this.type.getSignature();
		if (this.get != null)
		{
			MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(this.modifiers, this.name.qualified, "()" + extended, signature == null ? null
					: "()" + signature, null));
			
			if ((this.modifiers & Modifiers.STATIC) == 0)
			{
				mw.setThisType(this.theClass.getInternalName());
			}
			
			for (int i = 0; i < this.annotationCount; i++)
			{
				this.annotations[i].write(writer);
			}
			
			if ((this.modifiers & Modifiers.DEPRECATED) == Modifiers.DEPRECATED)
			{
				mw.addAnnotation("Ljava/lang/Deprecated;", true);
			}
			if ((this.modifiers & Modifiers.SEALED) == Modifiers.SEALED)
			{
				mw.addAnnotation("Ldyvil/annotation/sealed;", false);
			}
			
			mw.begin();
			this.get.writeExpression(mw);
			mw.end(this.type);
		}
		if (this.set != null)
		{
			MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(this.modifiers, this.name.qualified + "_$eq", "(" + extended + ")V",
					signature == null ? null : "(" + signature + ")V", null));
			
			if ((this.modifiers & Modifiers.STATIC) == 0)
			{
				mw.setThisType(this.theClass.getInternalName());
			}
			
			for (int i = 0; i < this.annotationCount; i++)
			{
				this.annotations[i].write(writer);
			}
			
			if ((this.modifiers & Modifiers.DEPRECATED) == Modifiers.DEPRECATED)
			{
				mw.addAnnotation("Ljava/lang/Deprecated;", true);
			}
			if ((this.modifiers & Modifiers.SEALED) == Modifiers.SEALED)
			{
				mw.addAnnotation("Ldyvil/annotation/sealed;", false);
			}
			
			this.setterParameter.write(mw);
			
			mw.begin();
			this.set.writeStatement(mw);
			mw.end(Types.VOID);
		}
	}
	
	@Override
	public void writeGet(MethodWriter writer, IValue instance) throws BytecodeException
	{
		if (instance != null)
		{
			instance.writeExpression(writer);
		}
		
		int opcode;
		if ((this.modifiers & Modifiers.STATIC) != 0)
		{
			opcode = Opcodes.INVOKESTATIC;
		}
		else
		{
			opcode = Opcodes.INVOKEVIRTUAL;
		}
		
		String owner = this.theClass.getInternalName();
		String name = this.name.qualified;
		String desc = "()" + this.type.getExtendedName();
		writer.writeInvokeInsn(opcode, owner, name, desc, false);
	}
	
	@Override
	public void writeSet(MethodWriter writer, IValue instance, IValue value) throws BytecodeException
	{
		if (instance != null)
		{
			instance.writeExpression(writer);
		}
		if (value != null)
		{
			value.writeExpression(writer);
		}
		
		int opcode;
		if ((this.modifiers & Modifiers.STATIC) != 0)
		{
			opcode = Opcodes.INVOKESTATIC;
		}
		else
		{
			opcode = Opcodes.INVOKEVIRTUAL;
		}
		
		String owner = this.theClass.getInternalName();
		String name = this.name.qualified + "_$eq";
		String desc = "(" + this.type.getExtendedName() + ")V";
		writer.writeInvokeInsn(opcode, owner, name, desc, false);
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
		buffer.append(this.name);
		
		buffer.append('\n').append(prefix).append('{');
		if (this.get != null)
		{
			buffer.append('\n').append(prefix).append(Formatting.Method.indent);
			if (this.set == null)
			{
				this.get.toString(prefix + Formatting.Method.indent, buffer);
				buffer.append('\n').append(prefix).append('}');
				return;
			}
			
			buffer.append(Formatting.Field.propertyGet);
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

package dyvil.tools.compiler.ast.field;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;

import dyvil.collection.List;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.ThisValue;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
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
	private static final byte	GETTER	= 1;
	private static final byte	SETTER	= 2;
	
	protected IClass theClass;
	
	protected IValue	getter;
	protected IValue	setter;
	protected int		getterModifiers;
	protected int		setterModifiers;
	
	protected MethodParameter	setterParameter;
	protected IProperty			overrideProperty;
	
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
	
	public Property(IClass iclass, Name name, IType type, int modifiers)
	{
		super(name, type);
		this.theClass = iclass;
		this.modifiers = modifiers;
	}
	
	public Property(ICodePosition position, IClass iclass, Name name, IType type, int modifiers)
	{
		super(name, type);
		this.position = position;
		this.theClass = iclass;
		this.modifiers = modifiers;
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
	public boolean hasGetter()
	{
		return this.getter != null || this.getterModifiers != 0;
	}
	
	@Override
	public void setGetterModifiers(int modifiers)
	{
		this.getterModifiers = modifiers;
	}
	
	@Override
	public void addGetterModifier(int modifier)
	{
		this.getterModifiers |= modifier;
	}
	
	@Override
	public int getGetterModifiers()
	{
		return this.getterModifiers;
	}
	
	@Override
	public void setGetter(IValue get)
	{
		this.getter = get;
	}
	
	@Override
	public IValue getGetter()
	{
		return this.getter;
	}
	
	@Override
	public boolean hasSetter()
	{
		return this.setter != null || this.setterModifiers != 0;
	}
	
	@Override
	public void setSetterModifiers(int modifiers)
	{
		this.setterModifiers = modifiers;
	}
	
	@Override
	public void addSetterModifier(int modifier)
	{
		this.setterModifiers |= modifier;
	}
	
	@Override
	public int getSetterModifiers()
	{
		return this.setterModifiers;
	}
	
	@Override
	public void setSetter(IValue set)
	{
		this.setter = set;
	}
	
	@Override
	public IValue getSetter()
	{
		return this.setter;
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
				if (!instance.getType().getTheClass().isObject())
				{
					markers.add(position, "property.access.instance", this.name.unqualified);
				}
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
				instance = new ThisValue(position, this.theClass.getType(), context, markers);
			}
		}
		
		if (this.hasModifier(Modifiers.DEPRECATED))
		{
			markers.add(position, "property.access.deprecated", this.name);
		}
		
		switch (context.getThisClass().getVisibility(this))
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
		if (this.setter == null)
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
		
		if (this.getter != null)
		{
			this.getter.resolveTypes(markers, this);
		}
		else if (this.getterModifiers != 0)
		{
			this.getterModifiers |= Modifiers.ABSTRACT;
		}
		if (this.setter != null)
		{
			this.setter.resolveTypes(markers, this);
		}
		else if (this.setterModifiers != 0)
		{
			this.setterModifiers |= Modifiers.ABSTRACT;
		}
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, context);
		
		if (this.setter != null)
		{
			this.setterParameter = new MethodParameter(this.name, this.type);
			this.setterParameter.setIndex(1);
		}
		
		if (this.setter != null)
		{
			this.setter = this.setter.resolve(markers, this);
			
			IValue set1 = this.setter.withType(Types.VOID, Types.VOID, markers, context);
			if (set1 == null)
			{
				markers.add(this.setter.getPosition(), "property.setter.type", this.name);
			}
			else
			{
				this.setter = set1;
			}
		}
		if (this.getter != null)
		{
			this.getter = this.getter.resolve(markers, this);
			
			boolean inferType = false;
			if (this.type == Types.UNKNOWN)
			{
				inferType = true;
				this.type = this.getter.getType();
				if (this.type == Types.UNKNOWN)
				{
					markers.add(this.position, "property.type.infer", this.name.unqualified);
					this.type = Types.ANY;
				}
			}
			
			IValue get1 = this.getter.withType(this.type, this.type, markers, context);
			if (get1 == null)
			{
				Marker marker = markers.create(this.getter.getPosition(), "property.getter.type", this.name.unqualified);
				marker.addInfo("Property Type: " + this.type);
				marker.addInfo("Getter Value Type: " + this.getter.getType());
			}
			else
			{
				this.getter = get1;
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
		
		boolean getter = this.getter != null;
		boolean setter = this.setter != null;
		if (getter)
		{
			this.getter.checkTypes(markers, context);
		}
		if (setter)
		{
			this.setter.checkTypes(markers, context);
		}
		
		if ((this.modifiers & Modifiers.STATIC) == 0)
		{
			this.checkOverride(markers);
		}
		
		if (this.theClass.hasModifier(Modifiers.INTERFACE_CLASS))
		{
			if (!setter && !getter)
			{
				this.modifiers |= Modifiers.ABSTRACT;
			}
		}
	}
	
	private void checkOverride(MarkerList markers)
	{
		IDataMember f = this.theClass.getSuperField(this.name);
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
		
		if (!this.theClass.isAbstract())
		{
			if ((this.overrideProperty.getGetterModifiers() & Modifiers.ABSTRACT) != 0 && this.getter == null)
			{
				markers.add(this.position, "property.getter.abstract", this.name);
			}
			
			if ((this.overrideProperty.getSetterModifiers() & Modifiers.ABSTRACT) != 0 && this.setter == null)
			{
				markers.add(this.position, "property.setter.abstract", this.name);
			}
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		super.check(markers, context);
		
		if (this.getter != null)
		{
			this.getter.check(markers, context);
		}
		if (this.setter != null)
		{
			this.setter.check(markers, context);
			
			if (this.type == Types.VOID)
			{
				markers.add(this.position, "property.type.void");
			}
		}
		
		// No setter and no getter
		if (this.getter == null && this.setter == null && this.getterModifiers == 0 && this.setterModifiers == 0)
		{
			markers.add(this.position, "property.empty", this.name);
		}
	}
	
	@Override
	public void foldConstants()
	{
		super.foldConstants();
		
		if (this.getter != null)
		{
			this.getter = this.getter.foldConstants();
		}
		if (this.setter != null)
		{
			this.setter = this.setter.foldConstants();
		}
	}
	
	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		super.cleanup(context, compilableList);
		
		if (this.getter != null)
		{
			this.getter = this.getter.cleanup(this, compilableList);
		}
		if (this.setter != null)
		{
			this.setter = this.setter.cleanup(this, compilableList);
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
	public IAccessible getAccessibleThis(IClass type)
	{
		return this.theClass.getAccessibleThis(type);
	}
	
	@Override
	public IVariable capture(IVariable variable)
	{
		return null;
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
		if (this.getter != null || this.getterModifiers != 0)
		{
			int modifiers = this.modifiers | this.getterModifiers;
			
			MethodWriter mw = new MethodWriterImpl(writer,
					writer.visitMethod(modifiers, this.name.qualified, "()" + extended, signature == null ? null : "()" + signature, null));
					
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
			
			if (this.getter != null)
			{
				mw.begin();
				this.getter.writeExpression(mw);
				mw.end(this.type);
			}
		}
		if (this.setter != null || this.setterModifiers != 0)
		{
			int modifiers = this.modifiers | this.setterModifiers;
			
			MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(modifiers, this.name.qualified + "_$eq", "(" + extended + ")V",
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
			
			if (this.setterParameter != null)
			{
				this.setterParameter.write(mw);
			}
			
			if (this.setter != null)
			{
				mw.begin();
				this.setter.writeStatement(mw);
				mw.end(Types.VOID);
			}
		}
	}
	
	@Override
	public void writeGet(MethodWriter writer, IValue instance, int lineNumber) throws BytecodeException
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
			writer.writeLineNumber(lineNumber);
			opcode = Opcodes.INVOKEVIRTUAL;
		}
		
		String owner = this.theClass.getInternalName();
		String name = this.name.qualified;
		String desc = "()" + this.type.getExtendedName();
		writer.writeInvokeInsn(opcode, owner, name, desc, false);
	}
	
	@Override
	public void writeSet(MethodWriter writer, IValue instance, IValue value, int lineNumber) throws BytecodeException
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
			writer.writeLineNumber(lineNumber);
			opcode = Opcodes.INVOKEVIRTUAL;
		}
		
		String owner = this.theClass.getInternalName();
		String name = this.name.qualified + "_$eq";
		String desc = "(" + this.type.getExtendedName() + ")V";
		writer.writeInvokeInsn(opcode, owner, name, desc, false);
	}
	
	@Override
	public void writeSignature(DataOutput out) throws IOException
	{
	}
	
	@Override
	public void readSignature(DataInput in) throws IOException
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		super.toString(prefix, buffer);
		
		buffer.append(ModifierTypes.FIELD.toString(this.modifiers));
		this.type.toString("", buffer);
		buffer.append(' ');
		buffer.append(this.name);
		
		buffer.append('\n').append(prefix).append('{');
		if (this.getter != null || this.getterModifiers != 0)
		{
			buffer.append('\n').append(prefix).append(Formatting.Method.indent);
			buffer.append(ModifierTypes.FIELD.toString(this.getterModifiers));
			buffer.append(Formatting.Field.propertyGet);
			this.getter.toString(prefix + Formatting.Method.indent, buffer);
			
			if (this.setter == null && this.setterModifiers == 0)
			{
				buffer.append('\n').append(prefix).append('}');
				return;
			}
			
			buffer.append(';');
		}
		if (this.setter != null || this.setterModifiers != 0)
		{
			buffer.append('\n').append(prefix).append(Formatting.Method.indent);
			buffer.append(ModifierTypes.FIELD.toString(this.setterModifiers));
			buffer.append(Formatting.Field.propertySet);
			if (this.setter != null)
			{
				this.setter.toString(prefix + Formatting.Method.indent, buffer);
			}
			buffer.append(';');
		}
		buffer.append('\n').append(prefix).append('}');
	}
}

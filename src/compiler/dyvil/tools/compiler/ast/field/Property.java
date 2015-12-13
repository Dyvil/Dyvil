package dyvil.tools.compiler.ast.field;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.ThisExpr;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.method.ConstructorMatchList;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.modifiers.EmptyModifiers;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
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
import dyvil.tools.compiler.transform.Deprecation;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;

public class Property extends Member implements IProperty, IContext
{
	protected IClass theClass;
	
	protected IValue      getter;
	protected IValue      setter;
	protected ModifierSet getterModifiers;
	protected ModifierSet setterModifiers;
	
	protected MethodParameter setterParameter;
	protected IProperty       overrideProperty;
	
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
	
	public Property(IClass iclass, Name name, IType type, ModifierSet modifiers)
	{
		super(name, type, modifiers);
		this.theClass = iclass;
	}
	
	public Property(ICodePosition position, IClass iclass, Name name, IType type, ModifierSet modifiers)
	{
		super(name, type, modifiers);
		this.position = position;
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
		return false;
	}
	
	@Override
	public ElementType getElementType()
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
		return this.getter != null || this.getterModifiers != null;
	}
	
	@Override
	public void setGetterModifiers(ModifierSet modifiers)
	{
		this.getterModifiers = modifiers;
	}
	
	@Override
	public ModifierSet getGetterModifiers()
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
		return this.setter != null || this.setterModifiers != null;
	}
	
	@Override
	public void setSetterModifiers(ModifierSet modifiers)
	{
		this.setterModifiers = modifiers;
	}
	
	@Override
	public ModifierSet getSetterModifiers()
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
			if (this.modifiers.hasIntModifier(Modifiers.STATIC))
			{
				if (instance.valueTag() != IValue.CLASS_ACCESS)
				{
					markers.add(I18n.createMarker(position, "property.access.static", this.name.unqualified));
				}
				else if (instance.getType().getTheClass() != this.theClass)
				{
					markers.add(I18n.createMarker(position, "property.access.static.type", this.name.unqualified,
					                              this.theClass.getFullName()));
				}
				instance = null;
			}
			else if (instance.valueTag() == IValue.CLASS_ACCESS)
			{
				if (!instance.getType().getTheClass().isObject())
				{
					markers.add(I18n.createMarker(position, "property.access.instance", this.name.unqualified));
				}
			}
			else
			{
				IType type = this.theClass.getType();
				instance = type.convertValue(instance, type, markers, context);
			}
		}
		else if (!this.modifiers.hasIntModifier(Modifiers.STATIC))
		{
			if (context.isStatic())
			{
				markers.add(I18n.createMarker(position, "property.access.instance", this.name.unqualified));
			}
			else
			{
				markers.add(I18n.createMarker(position, "property.access.unqualified", this.name.unqualified));
				instance = new ThisExpr(position, this.theClass.getType(), context, markers);
			}
		}
		
		Deprecation.checkAnnotations(markers, position, this, "property");

		switch (IContext.getVisibility(context, this))
		{
		case IContext.INTERNAL:
			markers.add(I18n.createMarker(position, "property.access.sealed", this.name));
			break;
		case IContext.INVISIBLE:
			markers.add(I18n.createMarker(position, "property.access.invisible", this.name));
			break;
		}
		
		return instance;
	}
	
	@Override
	public IValue checkAssign(MarkerList markers, IContext context, ICodePosition position, IValue instance, IValue newValue)
	{
		if (this.setter == null)
		{
			markers.add(I18n.createMarker(position, "property.assign.readonly", this.name.unqualified));
		}
		
		IValue value1 = newValue.withType(this.type, null, markers, context);
		if (value1 == null)
		{
			Marker marker = I18n.createMarker(newValue.getPosition(), "property.assign.type", this.name.unqualified);
			marker.addInfo(I18n.getString("property.type", this.type));
			marker.addInfo(I18n.getString("value.type", newValue.getType()));
			markers.add(marker);
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
		else if (this.getterModifiers != null)
		{
			this.getterModifiers.addIntModifier(Modifiers.ABSTRACT);
		}
		if (this.setter != null)
		{
			this.setter.resolveTypes(markers, this);
		}
		else if (this.setterModifiers != null)
		{
			this.setterModifiers.addIntModifier(Modifiers.ABSTRACT);
		}
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, context);
		
		if (this.setter != null || this.setterModifiers != null)
		{
			this.setterParameter = new MethodParameter(this.name, this.type);
			this.setterParameter.setIndex(0);
			this.setterParameter.setModifiers(EmptyModifiers.INSTANCE);
		}
		
		if (this.setter != null)
		{
			this.setter = this.setter.resolve(markers, this);
			
			IValue set1 = this.setter.withType(Types.VOID, Types.VOID, markers, context);
			if (set1 == null)
			{
				markers.add(I18n.createMarker(this.setter.getPosition(), "property.setter.type", this.name));
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
					markers.add(I18n.createMarker(this.position, "property.type.infer", this.name.unqualified));
					this.type = Types.ANY;
				}
			}
			
			IValue get1 = this.getter.withType(this.type, this.type, markers, context);
			if (get1 == null)
			{
				Marker marker = I18n.createMarker(this.getter.getPosition(), "property.getter.type.incompatible",
				                                  this.name.unqualified);
				marker.addInfo(I18n.getString("property.type", this.type));
				marker.addInfo(I18n.getString("property.getter.type", this.getter.getType()));
				markers.add(marker);
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
			markers.add(I18n.createMarker(this.position, "property.type.infer.writeonly", this.name.unqualified));
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
		
		if (!this.modifiers.hasIntModifier(Modifiers.STATIC))
		{
			this.checkOverride(markers);
		}
		
		if (this.theClass.hasModifier(Modifiers.INTERFACE_CLASS))
		{
			if (!setter && !getter)
			{
				this.modifiers.addIntModifier(Modifiers.ABSTRACT);
			}
		}
	}
	
	private void checkOverride(MarkerList markers)
	{
		IDataMember f = this.theClass.getSuperField(this.name);
		if (f == null)
		{
			if (this.modifiers.hasIntModifier(Modifiers.OVERRIDE))
			{
				markers.add(I18n.createMarker(this.position, "property.override", this.name));
			}
			return;
		}
		
		if (!(f instanceof IProperty))
		{
			return;
		}
		
		this.overrideProperty = (IProperty) f;
		
		if (!this.modifiers.hasIntModifier(Modifiers.OVERRIDE))
		{
			markers.add(I18n.createMarker(this.position, "property.overrides", this.name));
		}
		else if (this.overrideProperty.hasModifier(Modifiers.FINAL))
		{
			markers.add(I18n.createMarker(this.position, "property.override.final", this.name));
		}
		else
		{
			IType type = this.overrideProperty.getType().getConcreteType(this.theClass.getType());
			if (type != this.type && !type.isSameType(this.type))
			{
				Marker marker = I18n.createMarker(this.position, "property.override.type.incompatible", this.name);
				marker.addInfo(I18n.getString("property.type", this.type));
				marker.addInfo(I18n.getString("property.override.type", type));
				markers.add(marker);
			}
		}
		
		if (!this.theClass.isAbstract())
		{
			if (this.overrideProperty.getGetterModifiers().hasIntModifier(Modifiers.ABSTRACT) && this.getter == null)
			{
				markers.add(I18n.createMarker(this.position, "property.getter.abstract", this.name));
			}
			
			if (this.overrideProperty.getSetterModifiers().hasIntModifier(Modifiers.ABSTRACT) && this.setter == null)
			{
				markers.add(I18n.createMarker(this.position, "property.setter.abstract", this.name));
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
		if (this.getterModifiers != null)
		{
			ModifierUtil.checkMethodModifiers(markers, this, this.getterModifiers.toFlags() | this.modifiers.toFlags(),
			                                  this.getter != null, "property.getter");
		}
		if (this.setter != null)
		{
			this.setter.check(markers, context);
			
			if (this.type == Types.VOID)
			{
				markers.add(I18n.createMarker(this.position, "property.type.void"));
			}
		}
		if (this.setterModifiers != null)
		{
			ModifierUtil.checkMethodModifiers(markers, this, this.setterModifiers.toFlags() | this.modifiers.toFlags(),
			                                  this.setter != null, "property.setter");
		}
		
		// No setter and no getter
		if (this.getter == null && this.setter == null && this.getterModifiers == null && this.setterModifiers == null)
		{
			markers.add(I18n.createMarker(this.position, "property.empty", this.name));
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
		return this.modifiers.hasIntModifier(Modifiers.STATIC);
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
	public void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments)
	{
		this.theClass.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public void getConstructorMatches(ConstructorMatchList list, IArguments arguments)
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
	public IAccessible getAccessibleImplicit()
	{
		return null;
	}
	
	@Override
	public boolean isMember(IVariable variable)
	{
		return variable == this.setterParameter;
	}
	
	@Override
	public IDataMember capture(IVariable variable)
	{
		if (this.isMember(variable))
		{
			return variable;
		}
		return this.theClass.capture(variable);
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
	
	protected void writeAnnotations(MethodWriter mw, int modifiers)
	{
		if (this.annotations != null)
		{
			int count = this.annotations.annotationCount();
			for (int i = 0; i < count; i++)
			{
				this.annotations.getAnnotation(i).write(mw);
			}
		}
		
		if ((modifiers & Modifiers.DEPRECATED) != 0 && this.getAnnotation(Deprecation.DEPRECATED_CLASS) == null)
		{
			mw.visitAnnotation(Deprecation.DYVIL_EXTENDED, true);
		}
		if ((modifiers & Modifiers.INTERNAL) == Modifiers.INTERNAL)
		{
			mw.visitAnnotation("Ldyvil/annotation/_internal/internal;", false);
		}
	}
	
	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		String extended = this.type.getExtendedName();
		String signature = this.type.getSignature();
		if (this.getter != null || this.getterModifiers != null)
		{
			int modifiers = this.modifiers.toFlags();

			if (this.getterModifiers != null)
			{
				modifiers |= this.getterModifiers.toFlags();
			}
			
			MethodWriter mw = new MethodWriterImpl(writer,
			                                       writer.visitMethod(modifiers, this.name.qualified, "()" + extended,
			                                                          signature == null ? null : "()" + signature,
			                                                          null));

			if ((modifiers & Modifiers.STATIC) == 0)
			{
				mw.setThisType(this.theClass.getInternalName());
			}
			
			this.writeAnnotations(mw, modifiers);
			
			if (this.getter != null)
			{
				mw.begin();
				this.getter.writeExpression(mw, this.type);
				mw.end(this.type);
			}
		}
		if (this.setter != null || this.setterModifiers != null)
		{
			int modifiers = this.modifiers.toFlags();

			if (this.setterModifiers != null)
			{
				modifiers |= this.setterModifiers.toFlags();
			}
			
			MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(modifiers, this.name.qualified + "_$eq",
			                                                                  "(" + extended + ")V", signature == null ?
					                                                                  null :
					                                                                  "(" + signature + ")V", null));

			if ((modifiers & Modifiers.STATIC) == 0)
			{
				mw.setThisType(this.theClass.getInternalName());
			}
			
			this.writeAnnotations(mw, modifiers);
			this.setterParameter.write(mw);
			
			if (this.setter != null)
			{
				mw.begin();
				this.setter.writeExpression(mw, Types.VOID);
				mw.end(Types.VOID);
			}
		}
	}
	
	@Override
	public void writeGet(MethodWriter writer, IValue instance, int lineNumber) throws BytecodeException
	{
		if (instance != null)
		{
			instance.writeExpression(writer, this.theClass.getType());
		}
		
		int opcode;
		if (this.modifiers.hasIntModifier(Modifiers.STATIC))
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
			instance.writeExpression(writer, this.theClass.getType());
		}
		if (value != null)
		{
			value.writeExpression(writer, this.type);
		}
		
		int opcode;
		if (this.modifiers.hasIntModifier(Modifiers.STATIC))
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

		this.modifiers.toString(buffer);
		this.type.toString("", buffer);
		buffer.append(' ');
		buffer.append(this.name);

		// Block Start
		if (Formatting.getBoolean("property.block.newline"))
		{
			buffer.append('\n').append(prefix);
		}
		else
		{
			buffer.append(' ');
		}
		buffer.append('{');

		// Getters
		if (this.getter != null || this.getterModifiers != null)
		{
			String getterPrefix = Formatting.getIndent("property.getter.indent", prefix);

			buffer.append('\n').append(prefix);
			if (this.getterModifiers != null)
			{
				this.getterModifiers.toString(buffer);
			}
			buffer.append("get");

			// Separator
			if (Formatting.getBoolean("property.getter.separator.space_before"))
			{
				buffer.append(' ');
			}
			buffer.append(':');
			if (Formatting.getBoolean("property.getter.newline"))
			{
				buffer.append('\n').append(getterPrefix);
			}
			else if (Formatting.getBoolean("property.getter.separator.space_after"))
			{
				buffer.append(' ');
			}

			if (this.getter != null)
			{
				this.getter.toString(getterPrefix, buffer);
			}

			if (Formatting.getBoolean("property.getter.semicolon"))
			{
				buffer.append(';');
			}
		}

		// Setters
		if (this.setter != null || this.setterModifiers != null)
		{
			String setterPrefix = Formatting.getIndent("property.setter.indent", prefix);

			buffer.append('\n').append(prefix);
			if (this.setterModifiers != null)
			{
				this.setterModifiers.toString(buffer);
			}
			buffer.append("set");

			// Separator
			if (Formatting.getBoolean("property.setter.separator.space_before"))
			{
				buffer.append(' ');
			}
			buffer.append(':');
			if (Formatting.getBoolean("property.setter.newline"))
			{
				buffer.append('\n').append(setterPrefix);
			}
			else if (Formatting.getBoolean("property.setter.separator.space_after"))
			{
				buffer.append(' ');
			}

			if (this.setter != null)
			{
				this.setter.toString(setterPrefix, buffer);
			}

			if (Formatting.getBoolean("property.setter.semicolon"))
			{
				buffer.append(';');
			}
		}

		// Block End
		buffer.append('\n').append(prefix).append('}');
	}
}

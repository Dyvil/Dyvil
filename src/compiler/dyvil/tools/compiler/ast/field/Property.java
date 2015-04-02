package dyvil.tools.compiler.ast.field;

import java.lang.annotation.ElementType;
import java.util.List;

import org.objectweb.asm.ClassWriter;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.MethodParameter;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.util.ModifierTypes;

public class Property extends Member implements IProperty, IContext
{
	private IClass				theClass;
	
	public IValue				get;
	public IValue				set;
	
	protected MethodParameter	setterParameter;
	
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
	public ElementType getAnnotationType()
	{
		return ElementType.FIELD;
	}
	
	@Override
	public boolean isField()
	{
		return true;
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
			this.get = this.get.resolve(markers, this);
		}
		if (this.set != null)
		{
			this.setterParameter = new MethodParameter(this.name, this.type);
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
			
			this.get.checkTypes(markers, context);
		}
		if (this.set != null)
		{
			IValue set1 = this.set.withType(Types.VOID);
			if (set1 == null)
			{
				markers.add(this.set.getPosition(), "property.setter.type", this.name);
			}
			else
			{
				this.set = set1;
			}
			
			this.set.checkTypes(markers, context);
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
	public boolean isStatic()
	{
		return (this.modifiers & Modifiers.STATIC) != 0;
	}
	
	@Override
	public IType getThisType()
	{
		return this.theClass.getType();
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
	public IField resolveField(Name name)
	{
		if (name == this.name)
		{
			return this.setterParameter;
		}
		return this.theClass.resolveField(name);
	}
	
	@Override
	public ITypeVariable resolveTypeVariable(Name name)
	{
		return this.theClass.resolveTypeVariable(name);
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
		String extended = this.type.getExtendedName();
		String signature = this.type.getSignature();
		if (this.get != null)
		{
			MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(this.modifiers | Modifiers.SYNTHETIC, "get$" + this.name.qualified, "()"
					+ extended, signature == null ? null : "()" + signature, null));
			
			if ((this.modifiers & Modifiers.STATIC) == 0)
			{
				mw.setInstanceMethod();
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
				mw.addAnnotation("Ldyvil/lang/annotation/sealed;", false);
			}
			
			mw.begin();
			this.get.writeExpression(mw);
			mw.end(this.type);
		}
		if (this.set != null)
		{
			MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(this.modifiers | Modifiers.SYNTHETIC, "set$" + this.name.qualified, "("
					+ extended + ")V", signature == null ? null : "(" + signature + ")V", null));
			
			if ((this.modifiers & Modifiers.STATIC) == 0)
			{
				mw.setInstanceMethod();
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
				mw.addAnnotation("Ldyvil/lang/annotation/sealed;", false);
			}
			
			this.setterParameter.write(mw);
			
			mw.begin();
			this.set.writeStatement(mw);
			mw.end(Types.VOID);
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
		if ((this.modifiers & Modifiers.STATIC) == Modifiers.STATIC)
		{
			opcode = Opcodes.INVOKESTATIC;
		}
		else
		{
			opcode = Opcodes.INVOKEVIRTUAL;
		}
		
		String owner = this.theClass.getInternalName();
		String name = "get$" + this.name.qualified;
		String desc = "()" + this.type.getExtendedName();
		writer.writeInvokeInsn(opcode, owner, name, desc, false);
	}
	
	@Override
	public void writeSet(MethodWriter writer, IValue instance, IValue value)
	{
		if (instance != null && ((this.modifiers & Modifiers.STATIC) == 0 || instance.getValueType() != IValue.CLASS_ACCESS))
		{
			instance.writeExpression(writer);
		}
		
		if (value != null)
		{
			value.writeExpression(writer);
		}
		
		int opcode;
		if ((this.modifiers & Modifiers.STATIC) == Modifiers.STATIC)
		{
			opcode = Opcodes.INVOKESTATIC;
		}
		else
		{
			opcode = Opcodes.INVOKEVIRTUAL;
		}
		
		String owner = this.theClass.getInternalName();
		String name = "set$" + this.name.qualified;
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

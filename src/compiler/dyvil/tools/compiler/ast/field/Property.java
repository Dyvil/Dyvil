package dyvil.tools.compiler.ast.field;

import java.util.List;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.Method;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.util.Modifiers;

public class Property extends Field implements IProperty
{
	public IValue		get;
	public IValue		set;
	
	protected IMethod	getterMethod;
	protected Parameter	setterParameter;
	protected IMethod	setterMethod;
	
	public Property(IClass iclass)
	{
		super(iclass);
	}
	
	public Property(IClass iclass, String name)
	{
		super(iclass, name);
	}
	
	public Property(IClass iclass, String name, IType type)
	{
		super(iclass, name, type);
	}
	
	public Property(IClass iclass, String name, IType type, int modifiers, List<Annotation> annotations)
	{
		super(iclass, name, type, modifiers, annotations);
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
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		this.type = this.type.resolve(context);
		if (!this.type.isResolved())
		{
			markers.add(new SemanticError(this.type.getPosition(), "'" + this.type + "' could not be resolved to a type"));
		}
		
		if (this.get != null)
		{
			this.get.resolveTypes(markers, this);
		}
		if (this.set != null)
		{
			this.set.resolveTypes(markers, this);
		}
		
		for (Annotation a : this.annotations)
		{
			a.resolveTypes(markers, context);
		}
	}
	
	@Override
	public void resolve(List<Marker> markers, IContext context)
	{
		if (this.get != null)
		{
			this.getterMethod = new Method(this.theClass, "get$" + this.qualifiedName, this.type, this.modifiers | Modifiers.SYNTHETIC, this.annotations);
			this.getterMethod.setValue(this.get);
			this.get = this.get.resolve(markers, this);
		}
		if (this.set != null)
		{
			this.setterParameter = new Parameter(0, this.qualifiedName, this.type);
			this.setterMethod = new Method(this.theClass, "set$" + this.qualifiedName, Type.VOID, this.modifiers | Modifiers.SYNTHETIC, this.annotations);
			this.setterMethod.addParameter(this.setterParameter);
			this.setterMethod.setValue(this.set);
			this.set = this.set.resolve(markers, this);
		}
		
		for (Annotation a : this.annotations)
		{
			a.resolve(markers, context);
		}
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		if (this.get == null && this.set == null)
		{
			markers.add(new SemanticError(this.position, "The property '" + this.name + "' does not have a getter nor a setter"));
		}
		else
		{
			if (this.get != null && !this.get.requireType(this.type))
			{
				SemanticError error = new SemanticError(this.get.getPosition(), "The getter value of the property '" + this.name + "' is incompatible with the property type");
				error.addInfo("Property Type: " + this.type);
				error.addInfo("Getter Value Type: " + this.get.getType());
				markers.add(error);
			}
			if (this.set != null && !this.set.requireType(Type.VOID))
			{
				markers.add(new SemanticError(this.set.getPosition(), "The setter value of the property '" + this.name + "' has to be of type void"));
			}
		}
		
		for (Annotation a : this.annotations)
		{
			a.check(markers, context);
		}
	}
	
	@Override
	public void foldConstants()
	{
		if (this.get != null)
		{
			this.get = this.get.foldConstants();
		}
		if (this.set != null)
		{
			this.set = this.set.foldConstants();
		}
		
		for (Annotation a : this.annotations)
		{
			a.foldConstants();
		}
	}
	
	@Override
	public FieldMatch resolveField(String name)
	{
		if (name.equals(this.name))
		{
			return new FieldMatch(this.setterParameter, 1);
		}
		return super.resolveField(name);
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
	public void writeGet(MethodWriter writer)
	{
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
		writer.visitMethodInsn(opcode, owner, name, desc, false, args, this.type);
	}
	
	@Override
	public byte getAccessibility()
	{
		byte b = 0;
		if (this.get != null)
		{
			b |= READ_ACCESS;
		}
		if (this.set != null)
		{
			b |= WRITE_ACCESS;
		}
		return b;
	}
	
	@Override
	public void writeSet(MethodWriter writer)
	{
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
		writer.visitMethodInsn(opcode, owner, name, desc, false, args, null);
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
		buffer.append(Modifiers.FIELD.toString(this.modifiers));
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

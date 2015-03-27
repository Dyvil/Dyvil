package dyvil.tools.compiler.ast.classes;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.access.MethodCall;
import dyvil.tools.compiler.ast.field.*;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.generic.WildcardType;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.Method;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.Parameter;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.GenericType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.visitor.AnnotationClassVisitor;
import dyvil.tools.compiler.backend.visitor.BytecodeVisitor;
import dyvil.tools.compiler.backend.visitor.SimpleFieldVisitor;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.transform.Symbols;

public class BytecodeClass extends CodeClass
{
	public Package		thePackage;
	public boolean		typesResolved;
	
	private IType		outerType;
	private List<IType>	innerTypes;
	
	public BytecodeClass(String name)
	{
		this.name = name;
		this.qualifiedName = name;
	}
	
	@Override
	public boolean isSubTypeOf(IType type)
	{
		if (!this.typesResolved)
		{
			this.resolveTypes(null, Package.rootPackage);
		}
		return super.isSubTypeOf(type);
	}
	
	@Override
	public IClassBody getBody()
	{
		if (!this.typesResolved)
		{
			this.resolveTypes(null, Package.rootPackage);
		}
		return this.body;
	}
	
	@Override
	public ITypeVariable getTypeVariable(int index)
	{
		if (!this.typesResolved)
		{
			this.resolveTypes(null, Package.rootPackage);
		}
		return super.getTypeVariable(index);
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.typesResolved = true;
		
		if (this.genericCount > 0)
		{
			GenericType type = new GenericType(this);
			
			for (int i = 0; i < this.genericCount; i++)
			{
				ITypeVariable var = this.generics[i];
				var.resolveTypes(markers, context);
				type.addType(new WildcardType(null, 0, var.getCaptureClass()));
			}
			
			this.type = type;
		}
		
		if (this.superType != null)
		{
			if (this.superType.isName("void"))
			{
				this.superType = null;
			}
			else
			{
				this.superType = this.superType.resolve(markers, context);
			}
		}
		
		for (int i = 0; i < this.interfaceCount; i++)
		{
			this.interfaces[i] = this.interfaces[i].resolve(markers, context);
		}
		
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].resolveTypes(markers, context);
		}
		
		this.body.resolveTypes(markers, this);
		
		if (this.outerType != null)
		{
			this.outerClass = this.outerType.resolve(markers, context).getTheClass();
		}
		
		if (this.innerTypes != null)
		{
			for (IType t : this.innerTypes)
			{
				IClass iclass = t.resolve(markers, context).getTheClass();
				this.body.addClass(iclass);
			}
			this.innerTypes = null;
		}
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public void foldConstants()
	{
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		if (!this.typesResolved)
		{
			this.resolveTypes(null, Package.rootPackage);
		}
		
		for (int i = 0; i < this.genericCount; i++)
		{
			ITypeVariable var = this.generics[i];
			if (var.isName(name))
			{
				return var.getCaptureClass();
			}
		}
		
		IClass clazz = this.body.getClass(name);
		if (clazz != null)
		{
			return clazz;
		}
		
		return null;
	}
	
	@Override
	public FieldMatch resolveField(String name)
	{
		if (!this.typesResolved)
		{
			this.resolveTypes(null, Package.rootPackage);
		}
		
		// Own properties
		IField field = this.body.getProperty(name);
		if (field != null)
		{
			return new FieldMatch(field, 1);
		}
		
		// Own fields
		field = this.body.getField(name);
		if (field != null)
		{
			return new FieldMatch(field, 1);
		}
		
		if (this.instanceField != null && "instance".equals(name))
		{
			return new FieldMatch(this.instanceField, 1);
		}
		
		FieldMatch match;
		
		// Inherited Fields
		if (this.superType != null)
		{
			match = this.superType.resolveField(name);
			if (match != null)
			{
				return match;
			}
		}
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, String name, IArguments arguments)
	{
		if (!this.typesResolved)
		{
			this.resolveTypes(null, Package.rootPackage);
		}
		
		this.body.getMethodMatches(list, instance, name, arguments);
		
		if (!list.isEmpty())
		{
			return;
		}
		
		if (this.superType != null)
		{
			this.superType.getMethodMatches(list, instance, name, arguments);
		}
		for (int i = 0; i < this.interfaceCount; i++)
		{
			this.interfaces[i].getMethodMatches(list, instance, name, arguments);
		}
	}
	
	@Override
	public void getConstructorMatches(List<MethodMatch> list, IArguments arguments)
	{
		if (!this.typesResolved)
		{
			this.resolveTypes(null, Package.rootPackage);
		}
		
		this.body.getMethodMatches(list, null, "<init>", arguments);
	}
	
	public boolean addSpecialMethod(String specialType, String name, IMethod method)
	{
		if ("get".equals(specialType))
		{
			IProperty property = this.getProperty(name, method);
			property.setGetterMethod(method);
			return false;
		}
		if ("set".equals(specialType))
		{
			IProperty property = this.getProperty(name, method);
			property.setSetterMethod(method);
			return false;
		}
		if ("parDefault".equals(specialType))
		{
			int i = name.indexOf('$');
			IMethod method1 = this.body.getMethod(name.substring(0, i));
			int parIndex = Integer.parseInt(name.substring(i + 1));
			
			MethodCall call = new MethodCall(null);
			call.method = method;
			method1.getParameter(parIndex).defaultValue = call;
			return false;
		}
		return true;
	}
	
	private IProperty getProperty(String name, IMethod method)
	{
		IProperty property = this.body.getProperty(name);
		if (property == null)
		{
			Property prop = new Property(this, name, method.getType());
			prop.modifiers = method.getModifiers() & ~Modifiers.SYNTHETIC;
			this.body.addProperty(prop);
			return prop;
		}
		return property;
	}
	
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
	{
		this.modifiers = access;
		this.internalName = name;
		
		this.body = new ClassBody(this);
		if (interfaces != null)
		{
			this.interfaces = new IType[interfaces.length];
		}
		
		int index = name.lastIndexOf('$');
		if (index == -1)
		{
			index = name.lastIndexOf('/');
		}
		if (index == -1)
		{
			this.name = name;
			this.qualifiedName = Symbols.qualify(name);
			this.thePackage = Package.rootPackage;
			this.fullName = name;
		}
		else
		{
			this.name = name.substring(index + 1);
			this.qualifiedName = Symbols.qualify(this.name);
			this.fullName = name.replace('/', '.');
			this.thePackage = Package.rootPackage.resolvePackage(this.fullName.substring(0, index));
		}
		
		if (signature != null)
		{
			this.generics = new ITypeVariable[2];
			ClassFormat.readClassSignature(signature, this);
		}
		else
		{
			if (superName != null)
			{
				this.superType = ClassFormat.internalToType(superName);
			}
			else
			{
				this.superType = null;
			}
			
			if (interfaces != null)
			{
				this.interfaceCount = interfaces.length;
				this.interfaces = new IType[this.interfaceCount];
				for (int i = 0; i < this.interfaceCount; i++)
				{
					this.interfaces[i] = ClassFormat.internalToType(interfaces[i]);
				}
			}
		}
		
		this.type = new dyvil.tools.compiler.ast.type.Type(this);
	}
	
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value)
	{
		Field field = new Field(this);
		field.setName(Symbols.unqualify(name), name);
		field.setModifiers(access);
		field.setType(ClassFormat.internalToType(desc));
		
		if (value != null)
		{
			field.setValue(IValue.fromObject(value));
		}
		
		if ((this.modifiers & Modifiers.OBJECT_CLASS) == 0 || (access & Modifiers.SYNTHETIC) == 0)
		{
			this.body.addField(field);
		}
		else
		{
			// This is the instance field of a singleton object class, ignore
			// annotations as it shouldn't have any
			this.instanceField = field;
			return null;
		}
		
		return new SimpleFieldVisitor(field);
	}
	
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
	{
		if ((this.modifiers & Modifiers.ANNOTATION) != 0)
		{
			Parameter param = new Parameter();
			param.modifiers = access;
			param.name = Symbols.unqualify(name);
			param.qualifiedName = name;
			param.type = ClassFormat.internalToType(desc.substring(desc.lastIndexOf(')') + 1));
			this.addParameter(param);
			return new AnnotationClassVisitor(param);
		}
		
		Method method = new Method(this);
		method.setName(Symbols.unqualify(name), name);
		method.setModifiers(access);
		
		if (signature != null)
		{
			method.setGeneric();
			ClassFormat.readMethodType(signature, method);
		}
		else
		{
			ClassFormat.readMethodType(desc, method);
		}
		
		int parCount = method.parameterCount();
		if ((access & Modifiers.VARARGS) != 0)
		{
			Parameter param = method.getParameter(parCount - 1);
			param.setVarargs2();
		}
		
		boolean flag = true;
		if ((access & Modifiers.SYNTHETIC) != 0)
		{
			int index = name.indexOf('$');
			if (index != -1)
			{
				flag = this.addSpecialMethod(name.substring(0, index), name.substring(index + 1), method);
			}
		}
		
		if (flag)
		{
			this.body.addMethod(method);
		}
		
		return new BytecodeVisitor(method);
	}
	
	public void visitOuterClass(String owner, String name, String desc)
	{
		this.outerType = ClassFormat.internalToType(owner);
	}
	
	public void visitInnerClass(String name, String outerName, String innerName, int access)
	{
		if (this.innerTypes == null)
		{
			this.innerTypes = new ArrayList(1);
		}
		
		IType type = ClassFormat.internalToType(name);
		this.innerTypes.add(type);
	}
}

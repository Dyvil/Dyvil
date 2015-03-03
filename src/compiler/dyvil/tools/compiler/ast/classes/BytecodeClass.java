package dyvil.tools.compiler.ast.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import jdk.internal.org.objectweb.asm.*;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.annotation.Annotation;
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
import dyvil.tools.compiler.ast.type.AnnotationType;
import dyvil.tools.compiler.ast.type.GenericType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.AnnotationVisitorImpl;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.lexer.marker.Marker;
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
		this.body = new ClassBody(null, this);
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
	public ClassBody getBody()
	{
		if (!this.typesResolved)
		{
			this.resolveTypes(null, Package.rootPackage);
		}
		return this.body;
	}
	
	@Override
	public List<ITypeVariable> getTypeVariables()
	{
		if (!this.typesResolved)
		{
			this.resolveTypes(null, Package.rootPackage);
		}
		return super.getTypeVariables();
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		this.typesResolved = true;
		
		if (this.generics != null)
		{
			GenericType type = new GenericType(this);
			
			for (ITypeVariable v : this.generics)
			{
				v.resolveTypes(markers, context);
				type.addType(new WildcardType(null, 0, v.getCaptureClass()));
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
		
		for (ListIterator<IType> iterator = this.interfaces.listIterator(); iterator.hasNext();)
		{
			IType i1 = iterator.next();
			IType i2 = i1.resolve(markers, context);
			if (i1 != i2)
			{
				iterator.set(i2);
			}
		}
		
		if (this.annotations != null)
		{
			for (Annotation a : this.annotations)
			{
				a.resolveTypes(markers, context);
			}
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
	public void resolve(List<Marker> markers, IContext context)
	{
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
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
		
		if (this.generics != null)
		{
			for (ITypeVariable var : this.generics)
			{
				if (var.isName(name))
				{
					return var.getCaptureClass();
				}
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
		for (IType i : this.interfaces)
		{
			i.getMethodMatches(list, instance, name, arguments);
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
		return true;
	}
	
	private IProperty getProperty(String name, IMethod method)
	{
		IProperty property = this.body.getProperty(name);
		if (property == null)
		{
			property = new Property(this, name, method.getType(), method.getModifiers() & ~Modifiers.SYNTHETIC, method.getAnnotations());
			this.body.addProperty(property);
		}
		return property;
	}
	
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
	{
		this.modifiers = access;
		this.internalName = name;
		
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
			this.generics = new ArrayList(3);
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
				for (String s : interfaces)
				{
					this.interfaces.add(ClassFormat.internalToType(s));
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
		
		return new FieldVisitor(Opcodes.ASM5)
		{
			@Override
			public AnnotationVisitor visitAnnotation(String name, boolean visible)
			{
				AnnotationType type = new AnnotationType();
				ClassFormat.internalToType(name, type);
				Annotation annotation = new Annotation(null, type);
				
				return new AnnotationVisitorImpl(Opcodes.ASM5, field, annotation);
			}
		};
	}
	
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
	{
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
		
		List<Parameter> parameters = method.getParameters();
		
		if ((access & Modifiers.VARARGS) != 0)
		{
			Parameter param = parameters.get(parameters.size() - 1);
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
		
		return new MethodVisitor(Opcodes.ASM5)
		{
			@Override
			public void visitParameter(String name, int index)
			{
				parameters.get(index).setName(name);
			}
			
			@Override
			public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index)
			{
				if ((access & Modifiers.STATIC) == 0)
				{
					if (index != 0 && index <= parameters.size())
					{
						parameters.get(index - 1).setName(name);
					}
					return;
				}
				
				if (index < parameters.size())
				{
					parameters.get(index).setName(name);
				}
			}
			
			@Override
			public AnnotationVisitor visitAnnotation(String name, boolean visible)
			{
				AnnotationType type = new AnnotationType();
				ClassFormat.internalToType(name, type);
				Annotation annotation = new Annotation(null, type);
				
				return new AnnotationVisitorImpl(Opcodes.ASM5, method, annotation);
			}
		};
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

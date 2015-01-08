package dyvil.tools.compiler.ast.classes;

import java.util.List;
import java.util.ListIterator;

import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import jdk.internal.org.objectweb.asm.FieldVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.api.*;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.field.Parameter;
import dyvil.tools.compiler.ast.field.Property;
import dyvil.tools.compiler.ast.method.Method;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.AnnotationType;
import dyvil.tools.compiler.bytecode.AnnotationVisitorImpl;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.util.ClassFormat;
import dyvil.tools.compiler.util.Modifiers;
import dyvil.tools.compiler.util.Symbols;

public class BytecodeClass extends CodeClass
{
	public Package	thePackage;
	public boolean	typesResolved;
	
	public BytecodeClass()
	{
		this.body = new ClassBody(null, this);
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		if (this.superType != null)
		{
			if (this.superType.isName("void"))
			{
				this.superType = null;
			}
			else
			{
				this.superType = this.superType.resolve(context);
			}
		}
		
		for (ListIterator<IType> iterator = this.interfaces.listIterator(); iterator.hasNext();)
		{
			IType i1 = iterator.next();
			IType i2 = i1.resolve(context);
			if (i1 != i2)
			{
				iterator.set(i2);
			}
		}
		
		for (Annotation a : this.annotations)
		{
			a.resolveTypes(markers, Package.rootPackage);
		}
		
		this.body.resolveTypes(markers, Package.rootPackage);
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
		if (this.name.equals(name))
		{
			return this;
		}
		IClass iclass = this.thePackage.resolveClass(name);
		if (iclass != null)
		{
			return iclass;
		}
		return Package.rootPackage.resolveClass(name);
	}
	
	@Override
	public FieldMatch resolveField(IContext context, String name)
	{
		if (!this.typesResolved)
		{
			this.resolveTypes(null, Package.rootPackage);
			this.typesResolved = true;
		}
		return super.resolveField(context, name);
	}
	
	@Override
	public MethodMatch resolveMethod(IContext returnType, String name, IType... argumentTypes)
	{
		if (!this.typesResolved)
		{
			this.resolveTypes(null, Package.rootPackage);
			this.typesResolved = true;
		}
		return super.resolveMethod(returnType, name, argumentTypes);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IType type, String name, IType... argumentTypes)
	{
		if (!this.typesResolved)
		{
			this.resolveTypes(null, Package.rootPackage);
			this.typesResolved = true;
		}
		super.getMethodMatches(list, type, name, argumentTypes);
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
		
		int index = name.lastIndexOf('/');
		if (index == -1)
		{
			this.name = name;
			this.thePackage = Package.rootPackage;
		}
		else
		{
			this.name = name.substring(index + 1);
			this.thePackage = Package.rootPackage.resolvePackage(name.substring(0, index));
		}
		
		this.qualifiedName = name.replace('/', '.');
		
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
	
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value)
	{
		Field field = new Field(this);
		field.setName(Symbols.contract(name), name);
		field.setModifiers(access);
		field.setType(ClassFormat.internalToType(desc));
		
		this.body.addField(field);
		
		return new FieldVisitor(Opcodes.ASM5)
		{
			@Override
			public AnnotationVisitor visitAnnotation(String name, boolean visible)
			{
				AnnotationType type = new AnnotationType();
				ClassFormat.internalToType(name, type);
				Annotation annotation = new Annotation(null, type);
				field.addAnnotation(annotation);
				
				return new AnnotationVisitorImpl(Opcodes.ASM5, annotation);
			}
		};
	}
	
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
	{
		Method method = new Method(this);
		method.setName(Symbols.contract(name), name);
		method.setModifiers(access);
		ClassFormat.readMethodType(desc, method);
		
		List<Parameter> parameters = method.getParameters();
		
		if ((access & Modifiers.VARARGS) != 0)
		{
			Parameter param = parameters.get(parameters.size() - 1);
			param.setVarargs();
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
			public AnnotationVisitor visitAnnotation(String name, boolean visible)
			{
				AnnotationType type = new AnnotationType();
				ClassFormat.internalToType(name, type);
				Annotation annotation = new Annotation(null, type);
				method.addAnnotation(annotation);
				
				return new AnnotationVisitorImpl(Opcodes.ASM5, annotation);
			}
		};
	}
}

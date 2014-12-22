package dyvil.tools.compiler.ast.classes;

import java.util.List;

import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import jdk.internal.org.objectweb.asm.FieldVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.method.Method;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.util.ClassFormat;

public class BytecodeClass extends CodeClass
{
	public boolean	typesResolved;
	
	public BytecodeClass()
	{
		this.body = new ClassBody(null, this);
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		if (this.name.equals(name))
		{
			return this;
		}
		return Package.rootPackage.resolveClass(name);
	}
	
	@Override
	public FieldMatch resolveField(IContext context, String name)
	{
		if (!this.typesResolved)
		{
			this.applyState(CompilerState.RESOLVE_TYPES, Package.rootPackage);
			this.typesResolved = true;
		}
		return super.resolveField(context, name);
	}
	
	@Override
	public MethodMatch resolveMethod(IContext returnType, String name, Type... argumentTypes)
	{
		if (!this.typesResolved)
		{
			this.applyState(CompilerState.RESOLVE_TYPES, Package.rootPackage);
			this.typesResolved = true;
		}
		return super.resolveMethod(returnType, name, argumentTypes);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, Type type, String name, Type... argumentTypes)
	{
		if (!this.typesResolved)
		{
			this.applyState(CompilerState.RESOLVE_TYPES, Package.rootPackage);
			this.typesResolved = true;
		}
		super.getMethodMatches(list, type, name, argumentTypes);
	}
	
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
	{
		this.modifiers = access;
		this.internalName = name;
		
		int index = name.lastIndexOf('/');
		if (index == -1)
		{
			this.name = name;
		}
		else
		{
			this.name = name.substring(index + 1);
		}
		
		this.qualifiedName = name.replace('/', '.');
		
		if (superName != null)
		{
			this.superClass = ClassFormat.internalToType(superName);
		}
		else
		{
			this.superClass = null;
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
		field.setQualifiedName(name);
		field.setModifiers(access);
		field.setType(ClassFormat.internalToType(desc));
		
		this.body.addField(field);
		
		return new FieldVisitor(Opcodes.ASM5)
		{
			@Override
			public AnnotationVisitor visitAnnotation(String name, boolean visible)
			{
				Type type = ClassFormat.internalToType(name);
				Annotation annotation = new Annotation(null, type);
				field.addAnnotation(annotation);
				
				return new AnnotationVisitorImpl(Opcodes.ASM5, annotation);
			}
		};
	}
	
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
	{
		Method method = new Method(this);
		method.setQualifiedName(name);
		method.setModifiers(access);
		ClassFormat.readMethodType(desc, method);
		
		this.body.addMethod(method);
		
		return new MethodVisitor(Opcodes.ASM5)
		{
			@Override
			public AnnotationVisitor visitAnnotation(String name, boolean visible)
			{
				Type type = ClassFormat.internalToType(name);
				Annotation annotation = new Annotation(null, type);
				method.addAnnotation(annotation);
				
				return new AnnotationVisitorImpl(Opcodes.ASM5, annotation);
			}
		};
	}
}

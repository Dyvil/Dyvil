package dyvil.tools.compiler.ast.classes;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.Method;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.util.ClassFormat;
import dyvil.tools.compiler.ast.structure.Package;

public class BytecodeClass extends CodeClass
{
	public boolean	fieldsResolved;
	public boolean	methodsResolved;
	
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
	public IField resolveField(String name)
	{
		if (!this.fieldsResolved)
		{
			for (IField field : this.body.fields)
			{
				field.applyState(CompilerState.RESOLVE_TYPES, this);
			}
			this.fieldsResolved = true;
		}
		return super.resolveField(name);
	}
	
	@Override
	public IMethod resolveMethod(String name, Type... args)
	{
		if (!this.methodsResolved)
		{
			for (IMethod method : this.body.methods)
			{
				method.applyState(CompilerState.RESOLVE_TYPES, this);
			}
			this.methodsResolved = true;
		}
		return super.resolveMethod(name, args);
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
	
	public void visitField(int access, String name, String desc, String signature, Object value)
	{
		Field field = new Field(this);
		field.setQualifiedName(name);
		field.setModifiers(access);
		field.setType(ClassFormat.internalToType(desc));
		
		this.body.addField(field);
	}
	
	public void visitMethod(int access, String name, String desc, String signature, String[] exceptions)
	{
		Method method = new Method(this);
		method.setQualifiedName(name);
		method.setModifiers(access);
		ClassFormat.readMethodType(desc, method);
		
		this.body.addMethod(method);
	}
}

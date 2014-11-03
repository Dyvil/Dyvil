package dyvil.tools.compiler.ast.classes;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.method.Method;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.util.ClassFormat;

public class BytecodeClass extends CodeClass
{
	public BytecodeClass()
	{
		this.body = new ClassBody(null, this);
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
			this.superClasses.add(ClassFormat.internalToType(superName));
		}
		
		if (interfaces != null)
		{
			for (String s : interfaces)
			{
				this.superClasses.add(ClassFormat.internalToType(s));
			}
		}
		
		this.applyState(CompilerState.RESOLVE_TYPES, Package.rootPackage);
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

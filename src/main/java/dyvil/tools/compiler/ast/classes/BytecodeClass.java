package dyvil.tools.compiler.ast.classes;

import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.method.Method;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.bytecode.ClassReader;

public class BytecodeClass extends AbstractClass
{
	public BytecodeClass()
	{
		this.body = new ClassBody(null, this);
	}
	
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
	{
		this.modifiers = access;
		this.name = ClassReader.internalToPackage(name);
		this.superClasses.add(Type.fromInternal(superName));
		for (String s : interfaces)
		{
			this.superClasses.add(Type.fromInternal(s));
		}
	}
	
	public void visitField(int access, String name, String desc, String signature, Object value)
	{
		Field field = new Field(this);
		field.setName(name);
		field.setModifiers(access);
		field.setType(Type.fromInternal(signature == null ? desc : signature));
		this.body.addField(field);
	}
	
	public void visitMethod(int access, String name, String desc, String signature, String[] exceptions)
	{
		Method method = new Method(this);
		method.setName(name);
		method.setModifiers(access);
		
		this.body.addMethod(method);
	}
}

package dyvil.tools.compiler.bytecode;

import java.io.File;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.objectweb.asm.*;

import dyvil.tools.compiler.ast.classes.BytecodeClass;
import dyvil.tools.compiler.ast.classes.IClass;

public class ClassReader extends ClassVisitor
{
	protected BytecodeClass	bclass;
	
	public ClassReader(BytecodeClass bclass)
	{
		super(Opcodes.ASM5);
		this.bclass = bclass;
	}
	
	public static File getRTFile()
	{
		String s = System.getProperty("sun.boot.class.path");
		int index = s.indexOf("rt.jar");
		if (index != -1)
		{
			int index1 = s.lastIndexOf(':', index);
			int index2 = s.indexOf(':', index + 1);
			String s1 = s.substring(index1 + 1, index2);
			return new File(s1);
		}
		return null;
	}
	
	public static String packageToInternal(String name)
	{
		return name.replace('.', '/');
	}
	
	public static String internalToPackage(String name)
	{
		return name.replace('/', '.');
	}
	
	public static IClass loadClass(File file, String name, boolean decompile)
	{
		try (JarFile jarFile = new JarFile(file))
		{
			BytecodeClass bclass = new BytecodeClass();
			name = packageToInternal(name) + ".class";
			JarEntry entry = jarFile.getJarEntry(name);
			InputStream is = jarFile.getInputStream(entry);
			
			org.objectweb.asm.ClassReader reader = new org.objectweb.asm.ClassReader(is);
			ClassReader visitor = new ClassReader(bclass);
			reader.accept(visitor, 0);
			
			return bclass;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}
	
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
	{
		this.bclass.visit(version, access, name, signature, superName, interfaces);
	}
	
	@Override
	public void visitSource(String source, String debug)
	{}
	
	@Override
	public void visitOuterClass(String owner, String name, String desc)
	{}
	
	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible)
	{
		return null;
	}
	
	@Override
	public void visitAttribute(Attribute attr)
	{}
	
	@Override
	public void visitInnerClass(String name, String outerName, String innerName, int access)
	{}
	
	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value)
	{
		this.bclass.visitField(access, name, desc, signature, value);
		return null;
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
	{
		this.bclass.visitMethod(access, name, desc, signature, exceptions);
		return null;
	}
	
	@Override
	public void visitEnd()
	{
	}
}

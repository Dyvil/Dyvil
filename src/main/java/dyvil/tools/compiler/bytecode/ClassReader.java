package dyvil.tools.compiler.bytecode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

import jdk.internal.org.objectweb.asm.*;
import dyvil.tools.compiler.ast.classes.BytecodeClass;
import dyvil.tools.compiler.ast.classes.IClass;

public class ClassReader extends ClassVisitor
{
	protected BytecodeClass	bclass;
	
	public static File		javaRTJar;
	public static File		dyvilRTJar;
	
	public ClassReader(BytecodeClass bclass)
	{
		super(Opcodes.ASM5);
		this.bclass = bclass;
	}
	
	static
	{
		String s = System.getProperty("sun.boot.class.path");
		int index = s.indexOf("rt.jar");
		if (index != -1)
		{
			int index1 = s.lastIndexOf(':', index);
			int index2 = s.indexOf(':', index + 1);
			String s1 = s.substring(index1 + 1, index2);
			javaRTJar = new File(s1);
		}
		
		// TODO Actually use the installed Dyvil Runtime Library
		dyvilRTJar = new File("bin");
	}
	
	public static String classFile(String name)
	{
		return name.replace('.', '/') + ".class";
	}
	
	public static String packageToInternal(String name)
	{
		return name.replace('.', '/');
	}
	
	public static String internalToPackage(String name)
	{
		return name.replace('/', '.');
	}
	
	public static Object getChildren(File parent, String child)
	{
		if (parent.isDirectory())
		{
			return new File(parent, child);
		}
		else if (parent.getPath().endsWith(".jar"))
		{
			try (JarFile jarFile = new JarFile(parent, false, ZipFile.OPEN_READ))
			{
				return jarFile.getJarEntry(child);
			}
			catch (IOException ex)
			{}
		}
		return null;
	}
	
	public static InputStream getInputStream(File parent, String child)
	{
		if (parent.isDirectory())
		{
			try
			{
				return new FileInputStream(new File(parent, child));
			}
			catch (IOException ex)
			{
				return null;
			}
		}
		else if (parent.getPath().endsWith(".jar"))
		{
			try (JarFile jarFile = new JarFile(parent, false, ZipFile.OPEN_READ))
			{
				JarEntry entry = jarFile.getJarEntry(child);
				return jarFile.getInputStream(entry);
			}
			catch (IOException ex)
			{
				return null;
			}
		}
		return null;
	}
	
	public static IClass loadClass(InputStream is, boolean decompile)
	{
		try
		{
			BytecodeClass bclass = new BytecodeClass();
			jdk.internal.org.objectweb.asm.ClassReader reader = new jdk.internal.org.objectweb.asm.ClassReader(is);
			ClassReader visitor = new ClassReader(bclass);
			reader.accept(visitor, 0);
			
			return bclass;
		}
		catch (IOException ex)
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
	{}
}

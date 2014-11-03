package dyvil.tools.compiler.bytecode;

import java.io.*;

import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;

public class ClassWriter
{
	public static void saveClass(File file, IClass iclass)
	{
		try
		{
			if (!file.exists())
			{
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		
		try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file)))
		{
			jdk.internal.org.objectweb.asm.ClassWriter writer = new jdk.internal.org.objectweb.asm.ClassWriter(Opcodes.ASM5);
			iclass.write(writer);
			writer.visitEnd();
			byte[] bytes = writer.toByteArray();
			os.write(bytes, 0, bytes.length);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
}

package dyvil.tools.compiler.backend;

import java.io.*;

import dyvil.collection.Entry;
import dyvil.collection.Map;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.external.ExternalHeader;
import dyvil.tools.compiler.ast.imports.ImportDeclaration;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.operator.Operator;
import dyvil.tools.compiler.ast.structure.DyvilHeader;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.tools.compiler.ast.type.alias.TypeAlias;

public class HeaderFile
{
	private static final int	FILE_VERSION	= 2;
	
	public static void write(File file, IDyvilHeader header)
	{
		byte[] bytes;
		
		// First try to compile the file to a byte array
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); DataOutputStream dos = new DataOutputStream(bos))
		{
			write(dos, header);
			bytes = bos.toByteArray();
		}
		catch (Throwable ex)
		{
			// If the compilation fails, skip creating and writing the file.
			DyvilCompiler.logger.warning("Error during compilation of '" + file + "': " + ex);
			DyvilCompiler.logger.throwing("ClassWriter", "compile", ex);
			return;
		}
		
		// If the compilation was successful, we can try to write the newly
		// created byte array to a newly created, empty file.
		ClassWriter.save(file, bytes);
	}
	
	public static DyvilHeader read(InputStream is)
	{
		try (DataInputStream dis = new DataInputStream(is))
		{
			return read(dis);
		}
		catch (Throwable ex)
		{
			DyvilCompiler.logger.throwing("HeaderFile", "read", ex);
		}
		return null;
	}
	
	private static void write(DataOutputStream dos, IDyvilHeader header) throws Throwable
	{
		dos.writeShort(FILE_VERSION);
		
		// Header Name
		dos.writeUTF(header.getName());
		
		// Include Declarations
		/*
		 * int includes = header.includeCount(); dos.writeShort(includes); for
		 * (int i = 0; i < includes; i++) { header.getInclude(i).write(dos); }
		 */
		
		// Import Declarations
		int imports = header.importCount();
		dos.writeShort(imports);
		for (int i = 0; i < imports; i++)
		{
			header.getImport(i).write(dos);
		}
		
		// Using Declarations
		int staticImports = header.usingCount();
		dos.writeShort(staticImports);
		for (int i = 0; i < staticImports; i++)
		{
			header.getUsing(i).write(dos);
		}
		
		// Operators Definitions
		Map<Name, Operator> operators = header.getOperators();
		dos.writeShort(operators.size());
		for (Entry<Name, Operator> entry : operators)
		{
			entry.getValue().write(dos);
		}
		
		// Type Aliases
		Map<Name, ITypeAlias> typeAliases = header.getTypeAliases();
		dos.writeShort(typeAliases.size());
		for (Entry<Name, ITypeAlias> entry : typeAliases)
		{
			entry.getValue().write(dos);
		}
	}
	
	private static DyvilHeader read(DataInputStream dis) throws Throwable
	{
		int fileVersion = dis.readShort();
		if (fileVersion > FILE_VERSION)
		{
			throw new IllegalStateException("Unknown Dyvil Header File Version: " + fileVersion);
		}
		
		String name = dis.readUTF();
		DyvilHeader header = new ExternalHeader(name);
		
		// Include Declarations
		/*
		 * int includes = dis.readShort(); for (int i = 0; i < includes; i++) {
		 * IncludeDeclaration id = new IncludeDeclaration(null); id.read(dis);
		 * header.addInclude(id); }
		 */
		
		// Import Declarations
		int imports = dis.readShort();
		for (int i = 0; i < imports; i++)
		{
			ImportDeclaration id = new ImportDeclaration(null);
			id.read(dis);
			header.addImport(id);
		}
		
		int staticImports = dis.readShort();
		for (int i = 0; i < staticImports; i++)
		{
			ImportDeclaration id = new ImportDeclaration(null, true);
			id.read(dis);
			header.addUsing(id);
		}
		
		int operators = dis.readShort();
		for (int i = 0; i < operators; i++)
		{
			Operator op = Operator.read(dis);
			header.addOperator(op);
		}
		
		if (fileVersion >= 2)
		{
			int typeAliases = dis.readShort();
			for (int i = 0; i < typeAliases; i++)
			{
				TypeAlias ta = new TypeAlias();
				ta.read(dis);
				header.addTypeAlias(ta);
			}
		}
		
		return header;
	}
}

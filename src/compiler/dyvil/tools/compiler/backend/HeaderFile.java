package dyvil.tools.compiler.backend;

import java.io.*;

import dyvil.collection.Entry;
import dyvil.collection.Map;
import dyvil.io.FileUtils;
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
	private static final int FILE_VERSION = 1;
	
	public static void write(File file, IDyvilHeader header)
	{
		if (!FileUtils.createFile(file))
		{
			DyvilCompiler.error("Error during compilation of '" + file + "': could not create file");
			return;
		}
		
		try (ObjectWriter writer = new ObjectWriter(); OutputStream fo = new BufferedOutputStream(new FileOutputStream(file)))
		{
			
			writer.writeShort(FILE_VERSION);
			write(writer, header);
			
			// Directly write the DyO byte code to the file output to save some
			// unnecessary byte array allocations
			writer.writeTo(fo);
		}
		catch (Throwable ex)
		{
			// If the compilation fails, skip creating and writing the file.
			DyvilCompiler.warn("Error during compilation of '" + file + "': " + ex.getLocalizedMessage());
			DyvilCompiler.error("ClassWriter", "compile", ex);
			return;
		}
	}
	
	public static DyvilHeader read(InputStream is)
	{
		try (ObjectReader reader = new ObjectReader(new DataInputStream(is)))
		{
			int fileVersion = reader.readShort();
			if (fileVersion > FILE_VERSION)
			{
				throw new IllegalStateException("Unknown Dyvil Header File Version: " + fileVersion);
			}
			return read(reader);
		}
		catch (Throwable ex)
		{
			DyvilCompiler.error("HeaderFile", "read", ex);
		}
		return null;
	}
	
	private static void write(DataOutput writer, IDyvilHeader header) throws Throwable
	{
		// Header Name
		writer.writeUTF(header.getName());
		
		// Include Declarations
		writer.writeShort(0);
		
		// Import Declarations
		int imports = header.importCount();
		writer.writeShort(imports);
		for (int i = 0; i < imports; i++)
		{
			header.getImport(i).write(writer);
		}
		
		// Using Declarations
		int staticImports = header.usingCount();
		writer.writeShort(staticImports);
		for (int i = 0; i < staticImports; i++)
		{
			header.getUsing(i).write(writer);
		}
		
		// Operators Definitions
		Map<Name, Operator> operators = header.getOperators();
		writer.writeShort(operators.size());
		for (Entry<Name, Operator> entry : operators)
		{
			entry.getValue().write(writer);
		}
		
		// Type Aliases
		Map<Name, ITypeAlias> typeAliases = header.getTypeAliases();
		writer.writeShort(typeAliases.size());
		for (Entry<Name, ITypeAlias> entry : typeAliases)
		{
			entry.getValue().write(writer);
		}
		
		// Classes
		writer.writeShort(0);
	}
	
	private static DyvilHeader read(ObjectReader reader) throws Throwable
	{
		String name = reader.readUTF();
		DyvilHeader header = new ExternalHeader(name);
		
		// Include Declarations
		reader.readShort();
		
		// Import Declarations
		int imports = reader.readShort();
		for (int i = 0; i < imports; i++)
		{
			ImportDeclaration id = new ImportDeclaration(null);
			id.read(reader);
			header.addImport(id);
		}
		
		int staticImports = reader.readShort();
		for (int i = 0; i < staticImports; i++)
		{
			ImportDeclaration id = new ImportDeclaration(null, true);
			id.read(reader);
			header.addUsing(id);
		}
		
		int operators = reader.readShort();
		for (int i = 0; i < operators; i++)
		{
			Operator op = Operator.read(reader);
			header.addOperator(op);
		}
		
		int typeAliases = reader.readShort();
		for (int i = 0; i < typeAliases; i++)
		{
			TypeAlias ta = new TypeAlias();
			ta.read(reader);
			header.addTypeAlias(ta);
		}
		
		// int classes = reader.readShort();
		
		return header;
	}
}

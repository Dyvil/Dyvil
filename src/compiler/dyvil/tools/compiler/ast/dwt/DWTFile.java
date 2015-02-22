package dyvil.tools.compiler.ast.dwt;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.structure.ICompilationUnit;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.lexer.Dlex;
import dyvil.tools.compiler.lexer.Dlex.TokenIterator;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.library.Library;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.dwt.DWTParser;
import dyvil.tools.compiler.util.Modifiers;

public class DWTFile extends ASTNode implements ICompilationUnit
{
	public static final Package			javaxSwing	= Library.javaLibrary.resolvePackage("javax.swing");
	
	public final CodeFile				inputFile;
	public final File					outputDirectory;
	public final File					outputFile;
	
	public final String					name;
	public final Package				pack;
	protected transient TokenIterator	tokens;
	protected List<Marker>				markers;
	
	protected DWTNode					rootNode;
	
	protected Map<String, IType>		fields		= new TreeMap();
	
	public DWTFile(Package pack, CodeFile input, File output)
	{
		this.position = input;
		this.pack = pack;
		this.inputFile = input;
		this.markers = input.markers;
		
		String name = input.getAbsolutePath();
		int start = name.lastIndexOf('/');
		int end = name.lastIndexOf('.');
		this.name = name.substring(start + 1, end);
		
		name = output.getPath();
		start = name.lastIndexOf('/');
		end = name.lastIndexOf('.');
		this.outputDirectory = new File(name.substring(0, start));
		this.outputFile = new File(name.substring(0, end) + ".class");
		
		this.rootNode = new DWTNode();
	}
	
	@Override
	public CodeFile getInputFile()
	{
		return this.inputFile;
	}
	
	@Override
	public File getOutputFile()
	{
		return this.outputFile;
	}
	
	@Override
	public void tokenize()
	{
		Dlex lexer = new Dlex(this.inputFile);
		lexer.tokenize();
		this.tokens = lexer.iterator();
	}
	
	@Override
	public void parse()
	{
		ParserManager manager = new ParserManager(new DWTParser(this.rootNode));
		manager.parse(this.inputFile, this.tokens);
		this.tokens = null;
	}
	
	@Override
	public void resolveTypes()
	{
		this.rootNode.resolveTypes(this.markers, javaxSwing);
		this.rootNode.addFields(this.fields);
	}
	
	@Override
	public void resolve()
	{
		this.rootNode.resolve(this.markers, javaxSwing);
	}
	
	@Override
	public void check()
	{
	}
	
	@Override
	public void foldConstants()
	{
	}
	
	@Override
	public void compile()
	{
		int size = this.markers.size();
		if (size > 0)
		{
			StringBuilder buffer = new StringBuilder("Markers in DWT File '");
			buffer.append(this.inputFile).append(": ").append(size).append("\n\n");
			
			boolean error = false;
			for (Marker marker : this.markers)
			{
				if (!error && marker.isError())
				{
					error = true;
				}
				marker.log(buffer);
			}
			DyvilCompiler.logger.info(buffer.toString());
			if (error)
			{
				DyvilCompiler.logger.warning(this.name + " was not compiled due to errors in the DWT File");
				return;
			}
		}
		
		ClassWriter.createFile(this.outputFile);
		
		try (OutputStream os = new BufferedOutputStream(new FileOutputStream(this.outputFile)))
		{
			jdk.internal.org.objectweb.asm.ClassWriter writer = new jdk.internal.org.objectweb.asm.ClassWriter(Opcodes.ASM5);
			this.write(writer);
			writer.visitEnd();
			byte[] bytes = writer.toByteArray();
			os.write(bytes, 0, bytes.length);
		}
		catch (Exception ex)
		{
			DyvilCompiler.logger.throwing("DWTFile", "compile", ex);
		}
		
	}
	
	public void write(jdk.internal.org.objectweb.asm.ClassWriter writer)
	{
		writer.visit(Opcodes.V1_8, Modifiers.PUBLIC, this.pack.internalName + this.name, null, "java/lang/Object", null);
		
		for (Entry<String, IType> entry : this.fields.entrySet())
		{
			String name = entry.getKey();
			IType type = entry.getValue();
			writer.visitField(Modifiers.PUBLIC | Modifiers.STATIC, name, type.getExtendedName(), null, null);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.rootNode.toString(prefix, buffer);
	}
}

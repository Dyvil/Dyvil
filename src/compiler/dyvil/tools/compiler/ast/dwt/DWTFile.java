package dyvil.tools.compiler.ast.dwt;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.method.IConstructor;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.structure.ICompilationUnit;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.lexer.Dlex;
import dyvil.tools.compiler.lexer.TokenIterator;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.library.Library;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.dwt.DWTParser;

public class DWTFile extends ASTNode implements ICompilationUnit
{
	public static final Package		javaxSwing	= Library.javaLibrary.resolvePackage("javax.swing");
	
	public final CodeFile			inputFile;
	public final File				outputDirectory;
	public final File				outputFile;
	
	public final String				name;
	public final String				internalName;
	public final Package			pack;
	protected TokenIterator			tokens;
	protected MarkerList			markers		= new MarkerList();
	
	protected DWTNode				rootNode;
	
	protected Map<String, IType>	fields		= new TreeMap();
	
	public DWTFile(Package pack, CodeFile input, File output)
	{
		this.pack = pack;
		this.inputFile = input;
		
		String name = input.getAbsolutePath();
		int start = name.lastIndexOf('/');
		int end = name.lastIndexOf('.');
		this.name = name.substring(start + 1, end);
		this.internalName = pack.internalName + this.name;
		
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
		this.tokens = Dlex.tokenIterator(this.inputFile.getCode());
	}
	
	@Override
	public void parse()
	{
		ParserManager manager = new ParserManager(new DWTParser(this.rootNode));
		manager.parse(this.markers, this.tokens);
		this.tokens = null;
		
		int size = this.markers.size();
		if (size > 0)
		{
			StringBuilder buf = new StringBuilder("Syntax Errors in DWT File '");
			String code = this.inputFile.getCode();
			buf.append(this.inputFile).append(": ").append(size).append("\n\n");
			
			for (Marker marker : this.markers)
			{
				marker.log(code, buf);
			}
			DyvilCompiler.logger.info(buf.toString());
			DyvilCompiler.logger.warning(this.name + " contains Syntax Errors. Skipping.");
		}
	}
	
	@Override
	public void resolveTypes()
	{
		this.rootNode.resolveTypes(this.markers, Package.rootPackage);
		this.rootNode.addFields(this.fields);
	}
	
	@Override
	public void resolve()
	{
		IConstructor match = IContext.resolveConstructor(this.markers, this.rootNode.theClass, EmptyArguments.INSTANCE);
		if (match == null)
		{
			this.markers.add(this.position, "dwt.component.constructor");
		}
		
		this.rootNode.resolve(this.markers, Package.rootPackage);
	}
	
	@Override
	public void checkTypes()
	{
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
			StringBuilder buf = new StringBuilder("Problems in DWT File ").append(this.inputFile).append(":\n\n");
			String code = this.inputFile.getCode();
			
			int warnings = 0;
			int errors = 0;
			for (Marker marker : this.markers)
			{
				if (marker.isError())
				{
					errors++;
				}
				else
				{
					warnings++;
				}
				marker.log(code, buf);
			}
			buf.append(errors).append(errors == 1 ? " Error, " : " Errors, ").append(warnings).append(warnings == 1 ? " Warning" : " Warnings");
			DyvilCompiler.logger.info(buf.toString());
			if (errors > 0)
			{
				DyvilCompiler.logger.warning(this.name + " was not compiled due to errors in the DWT File");
				return;
			}
		}
		
		ClassWriter.createFile(this.outputFile);
		
		try (OutputStream os = new BufferedOutputStream(new FileOutputStream(this.outputFile)))
		{
			org.objectweb.asm.ClassWriter writer = new org.objectweb.asm.ClassWriter(DyvilCompiler.asmVersion);
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
	
	public void write(org.objectweb.asm.ClassWriter writer)
	{
		writer.visit(DyvilCompiler.classVersion, Modifiers.PUBLIC, this.internalName, null, "java/lang/Object", null);
		
		// Write Fields
		
		for (Entry<String, IType> entry : this.fields.entrySet())
		{
			String name = entry.getKey();
			IType type = entry.getValue();
			writer.visitField(Modifiers.PUBLIC | Modifiers.STATIC, name, type.getExtendedName(), null, null);
		}
		
		// Write init Method
		
		MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.PUBLIC | Modifier.STATIC, "init", "()V", null,
				new String[] { "java/lang/Exception" }));
		
		mw.begin();
		this.rootNode.write(this.internalName, mw);
		mw.end(Types.VOID);
		
		// Write public static void main(String[] args)
		
		mw = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.PUBLIC | Modifier.STATIC, "main", "([Ljava/lang/String;)V", null,
				new String[] { "java/lang/Exception" }));
		mw.resetLocals(1);
		mw.begin();
		mw.writeInvokeInsn(Opcodes.INVOKESTATIC, this.internalName, "init", "()V", false);
		mw.end(Types.VOID);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.rootNode.toString(prefix, buffer);
	}
}

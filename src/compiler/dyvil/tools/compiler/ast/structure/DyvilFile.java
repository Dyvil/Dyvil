package dyvil.tools.compiler.ast.structure;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.ClassBody;
import dyvil.tools.compiler.ast.classes.CodeClass;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.imports.Import;
import dyvil.tools.compiler.ast.imports.PackageDecl;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.lexer.Dlex;
import dyvil.tools.compiler.lexer.TokenIterator;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.classes.CompilationUnitParser;
import dyvil.tools.compiler.phase.CompilerPhase;

public class DyvilFile extends ASTNode implements ICompilationUnit, IContext
{
	public final CodeFile		inputFile;
	public final File			outputDirectory;
	public final File			outputFile;
	
	public final String			name;
	public final Package		pack;
	protected TokenIterator		tokens;
	protected List<Marker>		markers			= new ArrayList();
	
	protected PackageDecl		packageDeclaration;
	protected List<Import>		imports			= new ArrayList();
	protected List<Import>		staticImports	= new ArrayList();
	protected List<CodeClass>	classes			= new ArrayList();
	
	public DyvilFile(Package pack, CodeFile input, File output)
	{
		this.position = input;
		this.pack = pack;
		this.inputFile = input;
		
		String name = input.getAbsolutePath();
		int start = name.lastIndexOf('/');
		int end = name.lastIndexOf('.');
		this.name = name.substring(start + 1, end);
		
		name = output.getPath();
		start = name.lastIndexOf('/');
		end = name.lastIndexOf('.');
		this.outputDirectory = new File(name.substring(0, start));
		this.outputFile = new File(name.substring(0, end) + ".class");
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
	
	public void setPackageDeclaration(PackageDecl packageDecl)
	{
		this.packageDeclaration = packageDecl;
	}
	
	public PackageDecl getPackageDeclaration()
	{
		return this.packageDeclaration;
	}
	
	public List<Import> getImports()
	{
		return this.imports;
	}
	
	public void addImport(Import iimport)
	{
		this.imports.add(iimport);
	}
	
	public void addStaticImport(Import iimport)
	{
		this.staticImports.add(iimport);
	}
	
	public boolean hasStaticImports()
	{
		return !this.staticImports.isEmpty();
	}
	
	public List<CodeClass> getClasses()
	{
		return this.classes;
	}
	
	public void addClass(CodeClass iclass)
	{
		this.classes.add(iclass);
		this.pack.addClass(iclass);
	}
	
	public String getFullName(String name)
	{
		if (!name.equals(this.name))
		{
			name = this.name + '.' + name;
		}
		return this.pack.fullName + '.' + name;
	}
	
	public String getInternalName(String name)
	{
		if (!name.equals(this.name))
		{
			name = this.name + '$' + name;
		}
		return this.pack.internalName + name;
	}
	
	@Override
	public void tokenize()
	{
		this.tokens = Dlex.tokenIterator(this.inputFile.getCode(), this.inputFile);
	}
	
	@Override
	public void parse()
	{
		ParserManager manager = new ParserManager(new CompilationUnitParser(this));
		manager.semicolonInference = true;
		manager.parse(this.markers, this.tokens);
		this.tokens = null;
		
		int size = this.markers.size();
		if (size > 0)
		{
			StringBuilder buffer = new StringBuilder("Syntax Errors in Compilation Unit '");
			buffer.append(this.inputFile).append(": ").append(size).append("\n\n");
			
			boolean error = false;
			for (Marker marker : this.markers)
			{
				marker.log(buffer);
			}
			DyvilCompiler.logger.info(buffer.toString());
			DyvilCompiler.logger.warning(this.name + " contains Syntax Errors. Skipping.");
			
			if (DyvilCompiler.states.contains(CompilerPhase.PRINT))
			{
				DyvilCompiler.logger.info("Code:\n" + this.toString());
			}
		}
	}
	
	@Override
	public void resolveTypes()
	{
		for (Import i : this.imports)
		{
			i.resolveTypes(this.markers);
		}
		
		for (Import i : this.staticImports)
		{
			i.resolveTypes(this.markers);
		}
		
		for (IClass i : this.classes)
		{
			i.resolveTypes(this.markers, Package.rootPackage);
		}
	}
	
	@Override
	public void resolve()
	{
		for (IClass i : this.classes)
		{
			i.resolve(this.markers, this);
		}
	}
	
	@Override
	public void check()
	{
		this.pack.check(this.packageDeclaration, this.inputFile, this.markers);
		
		for (IClass i : this.classes)
		{
			i.check(this.markers, this);
		}
	}
	
	@Override
	public void foldConstants()
	{
		for (IClass i : this.classes)
		{
			i.foldConstants();
		}
	}
	
	@Override
	public void compile()
	{
		int size = this.markers.size();
		if (size > 0)
		{
			StringBuilder builder = new StringBuilder("Problems in Dyvil File ").append(this.inputFile).append(":\n\n");
			
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
				marker.log(builder);
			}
			builder.append(errors).append(errors == 1 ? " Error, " : " Errors, ").append(warnings).append(warnings == 1 ? " Warning" : " Warnings");
			DyvilCompiler.logger.info(builder.toString());
			if (errors > 0)
			{
				DyvilCompiler.logger.warning(this.name + " was not compiled due to errors in the Compilation Unit\n");
				return;
			}
		}
		
		for (IClass iclass : this.classes)
		{
			String name = iclass.getQualifiedName();
			if (!name.equals(this.name))
			{
				name = this.name + "$" + name;
			}
			File file = new File(this.outputDirectory, name + ".class");
			ClassWriter.saveClass(file, iclass);
			
			ClassBody body = iclass.getBody();
			if (body != null)
			{
				for (IClass iclass1 : body.classes)
				{
					name = this.name + "$" + iclass1.getQualifiedName() + ".class";
					file = new File(this.outputDirectory, name);
					ClassWriter.saveClass(file, iclass1);
				}
			}
		}
	}
	
	@Override
	public boolean isStatic()
	{
		return false;
	}
	
	@Override
	public Type getThisType()
	{
		return null;
	}
	
	@Override
	public Package resolvePackage(String name)
	{
		return null;
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		// Own classes
		for (IClass aclass : this.classes)
		{
			if (aclass.isName(name))
			{
				return aclass;
			}
		}
		
		IClass iclass;
		
		// Imported Classes
		for (Import i : this.imports)
		{
			iclass = i.resolveClass(name);
			if (iclass != null)
			{
				return iclass;
			}
		}
		
		// Package Classes
		iclass = this.pack.resolveClass(name);
		if (iclass != null)
		{
			return iclass;
		}
		
		// Standart Dyvil Classes
		iclass = Package.dyvilLang.resolveClass(name);
		if (iclass != null)
		{
			return iclass;
		}
		
		// Standart Java Classes
		return Package.javaLang.resolveClass(name);
	}
	
	@Override
	public FieldMatch resolveField(String name)
	{
		for (Import i : this.staticImports)
		{
			FieldMatch field = i.resolveField(name);
			if (field != null)
			{
				return field;
			}
		}
		return null;
	}
	
	@Override
	public MethodMatch resolveMethod(IValue instance, String name, IArguments arguments)
	{
		for (Import i : this.staticImports)
		{
			MethodMatch method = i.resolveMethod(instance, name, arguments);
			if (method != null)
			{
				return method;
			}
		}
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, String name, IArguments arguments)
	{
		for (Import i : this.staticImports)
		{
			i.getMethodMatches(list, instance, name, arguments);
		}
	}
	
	@Override
	public MethodMatch resolveConstructor(IArguments arguments)
	{
		return null;
	}
	
	@Override
	public void getConstructorMatches(List<MethodMatch> list, IArguments arguments)
	{
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.packageDeclaration != null)
		{
			buffer.append(prefix);
			this.packageDeclaration.toString(prefix, buffer);
			buffer.append(";\n");
			if (Formatting.Package.newLine)
			{
				buffer.append('\n');
			}
		}
		
		if (!this.imports.isEmpty())
		{
			for (Import iimport : this.imports)
			{
				buffer.append(prefix);
				iimport.toString(prefix, buffer);
				buffer.append(";\n");
			}
			if (Formatting.Import.newLine)
			{
				buffer.append('\n');
			}
		}
		
		if (!this.staticImports.isEmpty())
		{
			for (Import iimport : this.staticImports)
			{
				buffer.append(prefix);
				iimport.toString(prefix, buffer);
				buffer.append(";\n");
			}
			if (Formatting.Import.newLine)
			{
				buffer.append('\n');
			}
		}
		
		for (IClass iclass : this.classes)
		{
			iclass.toString(prefix, buffer);
			
			if (Formatting.Class.newLine)
			{
				buffer.append('\n');
			}
		}
	}
}

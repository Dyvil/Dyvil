package dyvil.tools.compiler.ast.structure;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassBody;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.imports.Import;
import dyvil.tools.compiler.ast.imports.PackageDecl;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.lexer.Dlex;
import dyvil.tools.compiler.lexer.TokenIterator;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.classes.CompilationUnitParser;
import dyvil.tools.compiler.phase.ICompilerPhase;

public class DyvilFile extends ASTNode implements ICompilationUnit, IDyvilUnit
{
	public final CodeFile	inputFile;
	public final File		outputDirectory;
	public final File		outputFile;
	
	public final String		name;
	public Package			pack;
	
	protected TokenIterator	tokens;
	protected MarkerList	markers			= new MarkerList();
	
	protected PackageDecl	packageDeclaration;
	protected List<Import>	imports			= new ArrayList();
	protected List<Import>	staticImports	= new ArrayList();
	protected List<IClass>	classes			= new ArrayList();
	
	public DyvilFile(Package pack, CodeFile input, File output)
	{
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
	
	@Override
	public void setPackage(Package pack)
	{
		this.pack = pack;
	}
	
	@Override
	public Package getPackage()
	{
		return this.pack;
	}
	
	@Override
	public void setPackageDeclaration(PackageDecl packageDecl)
	{
		this.packageDeclaration = packageDecl;
	}
	
	@Override
	public PackageDecl getPackageDeclaration()
	{
		return this.packageDeclaration;
	}
	
	public List<Import> getImports()
	{
		return this.imports;
	}
	
	@Override
	public void addImport(Import iimport)
	{
		this.imports.add(iimport);
	}
	
	@Override
	public void addStaticImport(Import iimport)
	{
		this.staticImports.add(iimport);
	}
	
	@Override
	public boolean hasStaticImports()
	{
		return !this.staticImports.isEmpty();
	}
	
	public List<IClass> getClasses()
	{
		return this.classes;
	}
	
	@Override
	public void addClass(IClass iclass)
	{
		this.classes.add(iclass);
		this.pack.addClass(iclass);
	}
	
	@Override
	public String getFullName(String name)
	{
		if (!name.equals(this.name))
		{
			name = this.name + '.' + name;
		}
		return this.pack.fullName + '.' + name;
	}
	
	@Override
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
		this.tokens = Dlex.tokenIterator(this.inputFile.getCode());
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
			StringBuilder buf = new StringBuilder("Syntax Errors in Compilation Unit '");
			String code = this.inputFile.getCode();
			buf.append(this.inputFile).append(": ").append(size).append("\n\n");
			
			for (Marker marker : this.markers)
			{
				marker.log(code, buf);
			}
			DyvilCompiler.logger.info(buf.toString());
			DyvilCompiler.logger.warning(this.name + " contains Syntax Errors. Skipping.");
			
			if (DyvilCompiler.phases.contains(ICompilerPhase.PRINT))
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
			i.resolveTypes(this.markers, this, false);
		}
		
		for (Import i : this.staticImports)
		{
			i.resolveTypes(this.markers, this, true);
		}
		
		for (IClass i : this.classes)
		{
			i.resolveTypes(this.markers, this);
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
	public void checkTypes()
	{
		for (IClass i : this.classes)
		{
			i.checkTypes(this.markers, this);
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
			StringBuilder buf = new StringBuilder("Problems in Dyvil File ").append(this.inputFile).append(":\n\n");
			String code = this.inputFile.getCode();
			
			int warnings = this.markers.getWarnings();
			int errors = this.markers.getErrors();
			this.markers.sort();
			for (Marker marker : this.markers)
			{
				marker.log(code, buf);
			}
			buf.append(errors).append(errors == 1 ? " Error, " : " Errors, ").append(warnings).append(warnings == 1 ? " Warning" : " Warnings");
			DyvilCompiler.logger.info(buf.toString());
			if (errors > 0)
			{
				DyvilCompiler.logger.warning(this.name + " was not compiled due to errors in the Compilation Unit\n");
				return;
			}
		}
		
		for (IClass iclass : this.classes)
		{
			String name = iclass.getName().qualified;
			if (!name.equals(this.name))
			{
				name = this.name + "$" + name;
			}
			File file = new File(this.outputDirectory, name + ".class");
			ClassWriter.saveClass(file, iclass);
			
			IClassBody body = iclass.getBody();
			if (body != null)
			{
				int len = body.classCount();
				for (int i = 0; i < len; i++)
				{
					IClass iclass1 = body.getClass(i);
					name = this.name + "$" + iclass1.getName().qualified + ".class";
					file = new File(this.outputDirectory, name);
					ClassWriter.saveClass(file, iclass1);
				}
			}
		}
	}
	
	@Override
	public boolean isStatic()
	{
		return true;
	}
	
	@Override
	public Type getThisType()
	{
		return null;
	}
	
	@Override
	public Package resolvePackage(Name name)
	{
		return null;
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		// Own classes
		for (IClass c : this.classes)
		{
			if (c.getName() == name)
			{
				return c;
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
	public ITypeVariable resolveTypeVariable(Name name)
	{
		return null;
	}
	
	@Override
	public IField resolveField(Name name)
	{
		for (Import i : this.staticImports)
		{
			IField field = i.resolveField(name);
			if (field != null)
			{
				return field;
			}
		}
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		for (Import i : this.staticImports)
		{
			i.getMethodMatches(list, instance, name, arguments);
		}
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
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

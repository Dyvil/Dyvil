package dyvil.tools.compiler.ast.structure;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.CodeClass;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.imports.Import;
import dyvil.tools.compiler.ast.imports.PackageDecl;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.lexer.Dlex.TokenIterator;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.CodePosition;
import dyvil.tools.compiler.parser.classes.CompilationUnitParser;
import dyvil.util.FileUtils;

public class CompilationUnit extends ASTNode implements IContext
{
	public final CodeFile				inputFile;
	public final File					outputDirectory;
	public final File					outputFile;
	
	public final String					name;
	public final Package				pack;
	protected transient TokenIterator	tokens;
	protected List<Marker>				markers;
	
	protected PackageDecl				packageDeclaration;
	protected List<Import>				imports			= new ArrayList();
	protected List<Import>				staticImports	= new ArrayList();
	protected List<CodeClass>			classes			= new ArrayList();
	
	public CompilationUnit(Package pack, CodeFile input, File output)
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
		this.pack.classes.add(iclass);
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
	
	public void tokenize()
	{
		this.tokens = DyvilCompiler.parser.tokenize(this.inputFile);
	}
	
	public void parse()
	{
		DyvilCompiler.parser.setParser(new CompilationUnitParser(this));
		DyvilCompiler.parser.parse(this.inputFile, this.tokens);
		this.tokens = null;
	}
	
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
	
	public void resolve()
	{
		for (IClass i : this.classes)
		{
			i.resolve(this.markers, this);
		}
	}
	
	public void check()
	{
		switch (this.pack.check(this.packageDeclaration))
		{
		case 0: // OK
			break;
		case 1: // Missing package decl.
			this.markers.add(new SemanticError(new CodePosition((CodeFile) this.position, 0, 1, 0, 1), "Missing Package Declaration", "Add 'package "
					+ this.pack.name + ";' at the beginning of the file."));
			break;
		case 2: // Invalid package decl.
			this.markers.add(new SemanticError(this.packageDeclaration.getPosition(), "Invalid Package Declaration", "Change the package declaration to '"
					+ this.pack.name + "'."));
			break;
		case 3: // Package decl. in default package
			this.markers.add(new SemanticError(this.packageDeclaration.getPosition(), "Invalid Package Declaration", "Remove the package declaration."));
			break;
		}
		
		for (IClass i : this.classes)
		{
			i.check(this.markers, this);
		}
	}
	
	public void foldConstants()
	{
		for (IClass i : this.classes)
		{
			i.foldConstants();
		}
	}
	
	public void compile()
	{
		synchronized (this)
		{
			int size = this.markers.size();
			if (size > 0)
			{
				DyvilCompiler.logger.info("Markers in Compilation Unit " + this.name + ": " + size);
				for (Marker marker : this.markers)
				{
					marker.log(DyvilCompiler.logger);
				}
				DyvilCompiler.logger.warning(this.name + " was not compiled as there were Syntax Errors in the Compilation Unit.");
				return;
			}
			
			for (IClass iclass : this.classes)
			{
				String name = iclass.getName();
				if (!name.equals(this.name))
				{
					name = this.name + "$" + name;
				}
				File file = new File(this.outputDirectory, name + ".class");
				ClassWriter.saveClass(file, iclass);
			}
		}
	}
	
	public void print()
	{
		DyvilCompiler.logger.info(this.inputFile + ":\n" + this.toString());
	}
	
	public void format()
	{
		String s = this.toString();
		FileUtils.write(this.inputFile, s);
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
	public MethodMatch resolveMethod(ITyped instance, String name, List<? extends ITyped> arguments)
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
	public void getMethodMatches(List<MethodMatch> list, ITyped instance, String name, List<? extends ITyped> arguments)
	{
		for (Import i : this.staticImports)
		{
			i.getMethodMatches(list, instance, name, arguments);
		}
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String toString()
	{
		StringBuilder buffer = new StringBuilder();
		this.toString("", buffer);
		return buffer.toString();
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

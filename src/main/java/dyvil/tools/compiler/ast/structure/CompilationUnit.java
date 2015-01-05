package dyvil.tools.compiler.ast.structure;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.api.*;
import dyvil.tools.compiler.ast.classes.CodeClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.imports.PackageDecl;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.bytecode.ClassWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.lexer.Dlex.TokenIterator;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.CodePosition;
import dyvil.tools.compiler.parser.CompilationUnitParser;

public class CompilationUnit extends ASTNode implements IContext
{
	public final File					inputFile;
	public final File					outputDirectory;
	public final File					outputFile;
	
	public final String					name;
	public final Package				pack;
	protected transient TokenIterator	tokens;
	protected List<Marker>				markers;
	
	protected PackageDecl				packageDeclaration;
	protected List<IImport>				imports	= new ArrayList();
	protected List<CodeClass>			classes	= new ArrayList();
	
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
	
	public CodeFile getFile()
	{
		return (CodeFile) this.position;
	}
	
	public List<IImport> getImports()
	{
		return this.imports;
	}
	
	public void addImport(IImport iimport)
	{
		this.imports.add(iimport);
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
	
	public String getQualifiedName(String name)
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
		this.tokens = DyvilCompiler.parser.tokenize(this.getFile());
	}
	
	public void parse()
	{
		DyvilCompiler.parser.setParser(new CompilationUnitParser(this));
		DyvilCompiler.parser.parse(this.getFile(), this.tokens);
		this.tokens = null;
	}
	
	public void resolveTypes()
	{
		for (IImport i : this.imports)
		{
			i.resolveTypes(this.markers, this);
		}
		
		for (IClass i : this.classes)
		{
			i.resolveTypes(this.markers, this);
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
			this.markers.add(new SemanticError(new CodePosition((CodeFile) this.position, 1, 0, 1), "Missing Package Declaration", "Add 'package " + this.pack.name + ";' at the beginning of the file."));
			break;
		case 2: // Invalid package decl.
			this.markers.add(new SemanticError(this.packageDeclaration.getPosition(), "Invalid Package Declaration", "Change the package declaration to '" + this.pack.name + "'."));
			break;
		case 3: // Package decl. in default package
			this.markers.add(new SemanticError(this.packageDeclaration.getPosition(), "Invalid Package Declaration", "Remove the package declaration."));
			break;
		}
		
		for (IClass i : this.classes)
		{
			i.check(this.markers);
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
	
	public void debug()
	{
		DyvilCompiler.logger.info(this.getFile() + ":\n" + this.toString());
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
	public IClass resolveClass(String name)
	{
		// Own classes
		for (IClass aclass : this.classes)
		{
			if (name.equals(aclass.getName()))
			{
				return aclass;
			}
		}
		
		IClass iclass;
		
		// Imported Classes
		for (IImport i : this.imports)
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
	public FieldMatch resolveField(IContext context, String name)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public MethodMatch resolveMethod(IContext context, String name, IType... argumentTypes)
	{
		throw new UnsupportedOperationException();
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
		
		if (!this.imports.isEmpty())
		{
			for (IImport iimport : this.imports)
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

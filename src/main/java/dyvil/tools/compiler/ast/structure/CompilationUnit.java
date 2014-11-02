package dyvil.tools.compiler.ast.structure;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.Dyvilc;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.classes.AbstractClass;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.imports.IImport;
import dyvil.tools.compiler.ast.imports.PackageDecl;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.lexer.Dlex.TokenIterator;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.CodePosition;
import dyvil.tools.compiler.parser.CompilationUnitParser;

public class CompilationUnit extends ASTObject implements IContext
{
	public final String					name;
	public final Package				pack;
	protected transient TokenIterator	tokens;
	
	protected PackageDecl				packageDecl;
	protected List<IImport>				imports	= new ArrayList();
	protected List<AbstractClass>		classes	= new ArrayList();
	
	public CompilationUnit(Package pack, CodeFile file)
	{
		this.position = file;
		this.pack = pack;
		this.name = pack.fullName + "." + file.getName();
	}
	
	public CodeFile getFile()
	{
		return (CodeFile) this.position;
	}
	
	public PackageDecl getPackageDecl()
	{
		return this.packageDecl;
	}
	
	public List<IImport> getImportDecls()
	{
		return this.imports;
	}
	
	public List<AbstractClass> getClasses()
	{
		return this.classes;
	}
	
	public void setPackageDecl(PackageDecl packageDecl)
	{
		this.packageDecl = packageDecl;
	}
	
	public void addImport(IImport iimport)
	{
		this.imports.add(iimport);
	}
	
	public void addClass(AbstractClass iclass)
	{
		this.classes.add(iclass);
		this.pack.classes.add(iclass);
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
		for (AbstractClass aclass : this.classes)
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
		if (iclass != null) {
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
	public IField resolveField(String name)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public IMethod resolveMethodName(String name)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public IMethod resolveMethod(String name, Type... args)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public CompilationUnit applyState(CompilerState state, IContext context)
	{
		if (state == CompilerState.TOKENIZE)
		{
			this.tokens = Dyvilc.parser.tokenize(this.getFile());
			return this;
		}
		else if (state == CompilerState.PARSE)
		{
			Dyvilc.parser.setParser(new CompilationUnitParser(this));
			Dyvilc.parser.parse(this.getFile(), this.tokens);
			this.tokens = null;
			return this;
		}
		else if (state == CompilerState.RESOLVE_TYPES)
		{
			switch (this.pack.check(this.packageDecl))
			{
			case 0: // OK
				break;
			case 1: // Missing package decl.
				state.addMarker(new SemanticError(new CodePosition((CodeFile) this.position, 1, 0, 1), "Missing Package Declaration", "Add 'package " + this.pack.name + ";' at the beginning of the file."));
				break;
			case 2: // Invalid package decl.
				state.addMarker(new SemanticError(this.packageDecl.getPosition(), "Invalid Package Declaration", "Change the package declaration to '" + this.pack.name + "'."));
				break;
			case 3: // Package decl. in default package
				state.addMarker(new SemanticError(this.packageDecl.getPosition(), "Invalid Package Declaration", "Remove the package declaration."));
				break;
			}
			
			for (IImport i : this.imports)
			{
				i.applyState(state, this);
			}
		}
		else if (state == CompilerState.COMPILE)
		{
			// TODO COMPILE
			
			synchronized (this)
			{
				List<Marker> markers = this.getFile().markers;
				int size = markers.size();
				if (size > 0)
				{
					Dyvilc.logger.info("Markers in Compilation Unit " + this.name + ": " + size);
					for (Marker marker : this.getFile().markers)
					{
						marker.log(Dyvilc.logger);
					}
				}
			}
			return this;
		}
		else if (state == CompilerState.DEBUG)
		{
			Dyvilc.logger.info(this.getFile() + ":\n" + this.toString());
		}
		this.classes.replaceAll(c -> c.applyState(state, this));
		return this;
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
		if (this.packageDecl != null)
		{
			this.packageDecl.toString("", buffer);
			buffer.append('\n');
			if (Formatting.Package.newLine)
			{
				buffer.append('\n');
			}
		}
		
		if (!this.imports.isEmpty())
		{
			for (IImport iimport : this.imports)
			{
				iimport.toString("", buffer);
				buffer.append('\n');
			}
			if (Formatting.Import.newLine)
			{
				buffer.append('\n');
			}
		}
		
		for (IClass iclass : this.classes)
		{
			iclass.toString("", buffer);
			
			if (Formatting.Class.newLine)
			{
				buffer.append('\n');
			}
		}
	}
}

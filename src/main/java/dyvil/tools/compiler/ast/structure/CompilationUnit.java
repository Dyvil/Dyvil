package dyvil.tools.compiler.ast.structure;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.CompilerState;
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
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.position.CodePosition;

public class CompilationUnit extends ASTObject implements IContext
{
	public long						loadingTime	= System.currentTimeMillis();
	
	public final Package			pack;
	
	protected PackageDecl			packageDecl;
	protected List<IImport>			imports		= new ArrayList();
	protected List<AbstractClass>	classes		= new ArrayList();
	
	public CompilationUnit(Package pack, CodeFile file)
	{
		this.position = file;
		this.pack = pack;
	}
	
	public CodeFile getFile()
	{
		return (CodeFile) this.position;
	}
	
	public PackageDecl getPackageDecl()
	{
		return packageDecl;
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
	
	public void addClass(AbstractClass type)
	{
		this.classes.add(type);
		
		this.pack.addClass(type);
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
		
		// Imported Classes
		for (IImport i : this.imports)
		{
			IClass c = i.resolveClass(name);
			if (c != null)
			{
				return c;
			}
		}
		
		// Package classes
		return this.pack.resolveClass(name);
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
		if (state == CompilerState.RESOLVE)
		{
			switch (this.pack.check(this.packageDecl))
			{
			case 0: // OK
				break;
			case 1: // Missing package decl.
				state.addMarker(new SyntaxError(new CodePosition((CodeFile) this.position, 1, 0, 1), "Missing Package Declaration", "Add 'package " + this.pack.name + ";' at the beginning of the file."));
				break;
			case 2: // Invalid package decl.
				state.addMarker(new SyntaxError(this.packageDecl.getPosition(), "Invalid Package Declaration", "Change the package declaration to '" + this.pack.name + "'."));
				break;
			case 3: // Package decl. in default package
				state.addMarker(new SyntaxError(this.packageDecl.getPosition(), "Invalid Package Declaration", "Remove the package declaration."));
				break;
			}
			
			for (IImport i : this.imports)
			{
				i.applyState(state, this);
			}
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

package dyvil.tools.compiler.ast.structure;

import java.io.File;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.NestedClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.imports.HeaderComponent;
import dyvil.tools.compiler.ast.imports.PackageDecl;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.operator.Operator;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.lexer.Dlex;
import dyvil.tools.compiler.lexer.TokenIterator;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.classes.DyvilHeaderParser;

public class DyvilHeader implements ICompilationUnit, IDyvilHeader
{
	public final CodeFile			inputFile;
	public final File				outputDirectory;
	public final File				outputFile;
	
	public final String				name;
	public Package					pack;
	
	protected TokenIterator			tokens;
	protected MarkerList			markers			= new MarkerList();
	
	protected PackageDecl			packageDeclaration;
	
	protected HeaderComponent[]		imports			= new HeaderComponent[5];
	protected int					importCount;
	protected HeaderComponent[]		staticImports	= new HeaderComponent[1];
	protected int					staticImportCount;
	protected HeaderComponent[]		includes		= new HeaderComponent[2];
	protected int					includeCount;
	
	protected Map<Name, Operator>	operators		= new IdentityHashMap();
	
	public DyvilHeader(Package pack, CodeFile input, File output)
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
	public String getName()
	{
		return this.name;
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
	
	@Override
	public void addImport(HeaderComponent component)
	{
		int index = this.importCount++;
		if (index >= this.imports.length)
		{
			HeaderComponent[] temp = new HeaderComponent[index];
			System.arraycopy(this.imports, 0, temp, 0, this.imports.length);
			this.imports = temp;
		}
		this.imports[index] = component;
	}
	
	@Override
	public void addStaticImport(HeaderComponent component)
	{
		int index = this.staticImportCount++;
		if (index >= this.staticImports.length)
		{
			HeaderComponent[] temp = new HeaderComponent[index];
			System.arraycopy(this.staticImports, 0, temp, 0, this.staticImports.length);
			this.staticImports = temp;
		}
		this.staticImports[index] = component;
	}
	
	@Override
	public boolean hasStaticImports()
	{
		return this.staticImportCount > 0;
	}
	
	@Override
	public void addOperator(Operator op)
	{
		this.operators.put(op.name, op);
	}
	
	@Override
	public Operator getOperator(Name name)
	{
		return this.operators.get(name);
	}
	
	@Override
	public int classCount()
	{
		return 0;
	}
	
	@Override
	public IClass getClass(Name name)
	{
		return null;
	}
	
	@Override
	public void addClass(IClass iclass)
	{
	}
	
	@Override
	public IClass getClass(int index)
	{
		return null;
	}
	
	@Override
	public int innerClassCount()
	{
		return 0;
	}
	
	@Override
	public void addInnerClass(NestedClass iclass)
	{
	}
	
	@Override
	public NestedClass getInnerClass(int index)
	{
		return null;
	}
	
	@Override
	public void tokenize()
	{
		this.tokens = Dlex.tokenIterator(this.inputFile.getCode());
	}
	
	@Override
	public void parse()
	{
		ParserManager manager = new ParserManager(new DyvilHeaderParser(this));
		manager.semicolonInference = true;
		manager.operators = this.operators;
		manager.parse(this.markers, this.tokens);
		this.tokens = null;
	}
	
	@Override
	public void resolveTypes()
	{
		for (int i = 0; i < this.importCount; i++)
		{
			this.imports[i].resolveTypes(this.markers, this, false);
		}
		
		for (int i = 0; i < this.staticImportCount; i++)
		{
			this.staticImports[i].resolveTypes(this.markers, this, true);
		}
	}
	
	@Override
	public void resolve()
	{
	}
	
	@Override
	public void checkTypes()
	{
	}
	
	@Override
	public void check()
	{
		this.pack.check(this.packageDeclaration, this.inputFile, this.markers);
	}
	
	@Override
	public void foldConstants()
	{
	}
	
	@Override
	public void compile()
	{
	}
	
	@Override
	public boolean isStatic()
	{
		return true;
	}
	
	@Override
	public IClass getThisClass()
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
		IClass iclass;
		// Imported Classes
		for (int i = 0; i < this.importCount; i++)
		{
			iclass = this.imports[i].resolveClass(name);
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
		for (int i = 0; i < this.staticImportCount; i++)
		{
			IField field = this.staticImports[i].resolveField(name);
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
		for (int i = 0; i < this.staticImportCount; i++)
		{
			this.staticImports[i].getMethodMatches(list, instance, name, arguments);
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
	public String toString()
	{
		StringBuilder buf = new StringBuilder();
		this.toString("", buf);
		return buf.toString();
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
		
		if (this.importCount > 0)
		{
			for (int i = 0; i < this.importCount; i++)
			{
				buffer.append(prefix);
				this.imports[i].toString(prefix, buffer);
				buffer.append(";\n");
			}
			if (Formatting.Import.newLine)
			{
				buffer.append('\n');
			}
		}
		
		if (this.staticImportCount > 0)
		{
			for (int i = 0; i < this.staticImportCount; i++)
			{
				buffer.append(prefix);
				this.staticImports[i].toString(prefix, buffer);
				buffer.append(";\n");
			}
			if (Formatting.Import.newLine)
			{
				buffer.append('\n');
			}
		}
		
		if (!this.operators.isEmpty())
		{
			for (Entry<Name, Operator> entry : this.operators.entrySet())
			{
				buffer.append(prefix);
				entry.getValue().toString(buffer);
				buffer.append(";\n");
			}
			if (Formatting.Import.newLine)
			{
				buffer.append('\n');
			}
		}
	}
}

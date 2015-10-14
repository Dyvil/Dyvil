package dyvil.tools.compiler.ast.structure;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;

import dyvil.collection.Entry;
import dyvil.collection.List;
import dyvil.collection.Map;
import dyvil.collection.mutable.IdentityHashMap;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.external.ExternalClass;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.imports.ImportDeclaration;
import dyvil.tools.compiler.ast.imports.IncludeDeclaration;
import dyvil.tools.compiler.ast.imports.PackageDeclaration;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.operator.Operator;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.ClassType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.tools.compiler.ast.type.alias.TypeAlias;
import dyvil.tools.compiler.backend.IClassCompilable;
import dyvil.tools.compiler.backend.ObjectFormat;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.classes.DyvilHeaderParser;
import dyvil.tools.compiler.sources.FileType;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.parsing.CodeFile;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.TokenIterator;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.lexer.DyvilLexer;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class DyvilHeader implements ICompilationUnit, IDyvilHeader
{
	public final CodeFile	inputFile;
	public final File		outputDirectory;
	public final File		outputFile;
	
	protected Name		name;
	protected Package	pack;
	
	protected TokenIterator	tokens;
	protected MarkerList	markers	= new MarkerList();
	
	protected PackageDeclaration packageDeclaration;
	
	protected ImportDeclaration[]	imports	= new ImportDeclaration[5];
	protected int					importCount;
	protected ImportDeclaration[]	usings	= new ImportDeclaration[1];
	protected int					usingCount;
	protected IncludeDeclaration[]	includes;
	protected int					includeCount;
	
	protected Map<Name, Operator>	operators	= new IdentityHashMap();
	protected Map<Name, ITypeAlias>	typeAliases	= new IdentityHashMap();
	
	protected HeaderDeclaration headerDeclaration;
	
	public DyvilHeader()
	{
		this.inputFile = null;
		this.outputDirectory = null;
		this.outputFile = null;
	}
	
	public DyvilHeader(Name name)
	{
		this.inputFile = null;
		this.outputDirectory = null;
		this.outputFile = null;
		this.name = name;
	}
	
	public DyvilHeader(Package pack, CodeFile input, File output)
	{
		this.pack = pack;
		this.inputFile = input;
		
		String name = input.getAbsolutePath();
		int start = name.lastIndexOf(File.separatorChar);
		int end = name.lastIndexOf('.');
		this.name = Name.get(name.substring(start + 1, end));
		
		name = output.getPath();
		start = name.lastIndexOf(File.separatorChar);
		end = name.lastIndexOf('.');
		this.outputDirectory = new File(name.substring(0, start));
		this.outputFile = new File(name.substring(0, end) + FileType.OBJECT_EXTENSION);
	}
	
	@Override
	public boolean isHeader()
	{
		return true;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return ICodePosition.ORIGIN;
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
	}
	
	@Override
	public void setName(Name name)
	{
		this.name = name;
	}
	
	@Override
	public Name getName()
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
	public void setPackageDeclaration(PackageDeclaration packageDecl)
	{
		this.packageDeclaration = packageDecl;
	}
	
	@Override
	public PackageDeclaration getPackageDeclaration()
	{
		return this.packageDeclaration;
	}
	
	@Override
	public HeaderDeclaration getHeaderDeclaration()
	{
		return this.headerDeclaration;
	}
	
	@Override
	public void setHeaderDeclaration(HeaderDeclaration declaration)
	{
		this.headerDeclaration = declaration;
	}
	
	@Override
	public int importCount()
	{
		return this.importCount;
	}
	
	@Override
	public void addImport(ImportDeclaration component)
	{
		int index = this.importCount++;
		if (index >= this.imports.length)
		{
			ImportDeclaration[] temp = new ImportDeclaration[index + 1];
			System.arraycopy(this.imports, 0, temp, 0, this.imports.length);
			this.imports = temp;
		}
		this.imports[index] = component;
	}
	
	@Override
	public ImportDeclaration getImport(int index)
	{
		return this.imports[index];
	}
	
	@Override
	public int usingCount()
	{
		return this.usingCount;
	}
	
	@Override
	public void addUsing(ImportDeclaration component)
	{
		int index = this.usingCount++;
		if (index >= this.usings.length)
		{
			ImportDeclaration[] temp = new ImportDeclaration[index + 1];
			System.arraycopy(this.usings, 0, temp, 0, this.usings.length);
			this.usings = temp;
		}
		this.usings[index] = component;
	}
	
	@Override
	public ImportDeclaration getUsing(int index)
	{
		return this.usings[index];
	}
	
	@Override
	public int includeCount()
	{
		return this.includeCount;
	}
	
	@Override
	public void addInclude(IncludeDeclaration component)
	{
		if (this.includes == null)
		{
			this.includes = new IncludeDeclaration[2];
			this.includes[0] = component;
			this.includeCount = 1;
			return;
		}
		
		int index = this.includeCount++;
		if (index >= this.includes.length)
		{
			IncludeDeclaration[] temp = new IncludeDeclaration[index + 1];
			System.arraycopy(this.includes, 0, temp, 0, this.includes.length);
			this.includes = temp;
		}
		this.includes[index] = component;
	}
	
	@Override
	public IncludeDeclaration getInclude(int index)
	{
		return this.includes[index];
	}
	
	@Override
	public boolean hasMemberImports()
	{
		return this.usingCount > 0 || this.includeCount > 0;
	}
	
	@Override
	public Map<Name, Operator> getOperators()
	{
		return this.operators;
	}
	
	@Override
	public void addOperator(Operator op)
	{
		this.operators.put(op.name, op);
	}
	
	@Override
	public Operator getOperator(Name name)
	{
		Operator op = this.operators.get(name);
		if (op != null)
		{
			return op;
		}
		for (int i = 0; i < this.includeCount; i++)
		{
			op = this.includes[i].getHeader().getOperator(name);
			if (op != null)
			{
				return op;
			}
		}
		return null;
	}
	
	@Override
	public Map<Name, ITypeAlias> getTypeAliases()
	{
		return this.typeAliases;
	}
	
	@Override
	public void addTypeAlias(ITypeAlias typeAlias)
	{
		this.typeAliases.put(typeAlias.getName(), typeAlias);
	}
	
	@Override
	public ITypeAlias getTypeAlias(Name name)
	{
		return this.typeAliases.get(name);
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
	public void addInnerClass(IClassCompilable iclass)
	{
	}
	
	@Override
	public IClassCompilable getInnerClass(int index)
	{
		return null;
	}
	
	@Override
	public void tokenize()
	{
		this.tokens = new DyvilLexer(this.markers, DyvilSymbols.INSTANCE).tokenize(this.inputFile.getCode());
		this.tokens.inferSemicolons();
	}
	
	@Override
	public void parseHeader()
	{
		ParserManager manager = new ParserManager(new DyvilHeaderParser(this, false), this.markers, this);
		manager.parse(this.tokens);
	}
	
	@Override
	public void resolveHeader()
	{
		for (int i = 0; i < this.includeCount; i++)
		{
			IncludeDeclaration include = this.includes[i];
			include.resolve(this.markers);
		}
	}
	
	@Override
	public void parse()
	{
	}
	
	@Override
	public void resolveTypes()
	{
		for (int i = 0; i < this.importCount; i++)
		{
			this.imports[i].resolveTypes(this.markers, this, false);
		}
		
		for (int i = 0; i < this.usingCount; i++)
		{
			this.usings[i].resolveTypes(this.markers, this, true);
		}
		
		for (Entry<Name, ITypeAlias> entry : this.typeAliases)
		{
			entry.getValue().resolve(this.markers, this);
		}
	}
	
	@Override
	public void resolve()
	{
		if (this.headerDeclaration == null)
		{
			this.headerDeclaration = new HeaderDeclaration(this, ICodePosition.ORIGIN, this.name, Modifiers.PUBLIC, null);
		}
	}
	
	@Override
	public void checkTypes()
	{
	}
	
	@Override
	public void check()
	{
		this.pack.check(this.packageDeclaration, this.markers);
		
		if (this.headerDeclaration != null)
		{
			this.headerDeclaration.check(this.markers);
		}
	}
	
	@Override
	public void foldConstants()
	{
	}
	
	@Override
	public void cleanup()
	{
	}
	
	protected boolean printMarkers()
	{
		return ICompilationUnit.printMarkers(this.markers, "Dyvil Header", this.name, this.inputFile);
	}
	
	@Override
	public void compile()
	{
		if (this.printMarkers())
		{
			return;
		}
		
		ObjectFormat.write(this.outputFile, this);
	}
	
	@Override
	public IDyvilHeader getHeader()
	{
		return this;
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
		
		// Included Headers
		for (int i = 0; i < this.includeCount; i++)
		{
			iclass = this.includes[i].resolveClass(name);
			if (iclass != null)
			{
				return iclass;
			}
		}
		
		if (this.pack != null)
		{
			return this.pack.resolveClass(name);
		}
		return null;
	}
	
	@Override
	public IType resolveType(Name name)
	{
		ITypeAlias typeAlias = this.typeAliases.get(name);
		if (typeAlias != null)
		{
			return typeAlias.getType();
		}
		
		IClass iclass = this.resolveClass(name);
		if (iclass != null)
		{
			return new ClassType(iclass);
		}
		
		for (int i = 0; i < this.includeCount; i++)
		{
			IType t = this.includes[i].resolveType(name);
			if (t != null)
			{
				return t;
			}
		}
		return null;
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		for (int i = 0; i < this.usingCount; i++)
		{
			IDataMember field = this.usings[i].resolveField(name);
			if (field != null)
			{
				return field;
			}
		}
		
		for (int i = 0; i < this.includeCount; i++)
		{
			IDataMember field = this.includes[i].resolveField(name);
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
		for (int i = 0; i < this.usingCount; i++)
		{
			this.usings[i].getMethodMatches(list, instance, name, arguments);
		}
		
		for (int i = 0; i < this.includeCount; i++)
		{
			this.includes[i].getMethodMatches(list, instance, name, arguments);
		}
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
	}
	
	@Override
	public byte getVisibility(IClassMember member)
	{
		IClass iclass = member.getTheClass();
		
		int access = member.getAccessLevel();
		if ((access & Modifiers.INTERNAL) != 0)
		{
			if (iclass instanceof ExternalClass)
			{
				return INTERNAL;
			}
			// Clear the INTERNAL bit by ANDing with 0b1111
			access &= 0b1111;
		}
		
		if (access == Modifiers.PUBLIC)
		{
			return VISIBLE;
		}
		if (access == Modifiers.PROTECTED || access == Modifiers.PACKAGE)
		{
			IDyvilHeader header = iclass.getHeader();
			if (header == this || this.pack == header.getPackage())
			{
				return VISIBLE;
			}
		}
		return INVISIBLE;
	}
	
	@Override
	public String getFullName()
	{
		return this.pack.fullName + '.' + this.name;
	}
	
	@Override
	public String getFullName(Name name)
	{
		if (name != this.name)
		{
			return this.pack.fullName + '.' + this.name.qualified + '.' + name.qualified;
		}
		return this.pack.fullName + '.' + name.qualified;
	}
	
	@Override
	public String getInternalName()
	{
		return this.pack.getInternalName() + this.name;
	}
	
	@Override
	public String getInternalName(Name name)
	{
		if (name != this.name)
		{
			return this.pack.getInternalName() + this.name.qualified + '$' + name;
		}
		return this.pack.getInternalName() + name.qualified;
	}
	
	@Override
	public void write(DataOutput out) throws IOException
	{
		// Header Name
		this.headerDeclaration.write(out);
		
		// Include Declarations
		int includes = this.includeCount;
		out.writeShort(includes);
		for (int i = 0; i < includes; i++)
		{
			this.includes[i].write(out);
		}
		
		// Import Declarations
		int imports = this.importCount;
		out.writeShort(imports);
		for (int i = 0; i < imports; i++)
		{
			this.imports[i].write(out);
		}
		
		// Using Declarations
		int staticImports = this.usingCount;
		out.writeShort(staticImports);
		for (int i = 0; i < staticImports; i++)
		{
			this.usings[i].write(out);
		}
		
		// Operators Definitions
		Map<Name, Operator> operators = this.operators;
		out.writeShort(operators.size());
		for (Entry<Name, Operator> entry : operators)
		{
			entry.getValue().write(out);
		}
		
		// Type Aliases
		Map<Name, ITypeAlias> typeAliases = this.typeAliases;
		out.writeShort(typeAliases.size());
		for (Entry<Name, ITypeAlias> entry : typeAliases)
		{
			entry.getValue().write(out);
		}
		
		// Classes
		out.writeShort(0);
	}
	
	@Override
	public void read(DataInput in) throws IOException
	{
		this.headerDeclaration = new HeaderDeclaration(this);
		this.headerDeclaration.read(in);
		
		this.name = this.headerDeclaration.getName();
		
		// Include Declarations
		int includes = in.readShort();
		for (int i = 0; i < includes; i++)
		{
			IncludeDeclaration id = new IncludeDeclaration(null);
			id.read(in);
			this.addInclude(id);
		}
		
		// Import Declarations
		int imports = in.readShort();
		for (int i = 0; i < imports; i++)
		{
			ImportDeclaration id = new ImportDeclaration(null);
			id.read(in);
			this.addImport(id);
		}
		
		int staticImports = in.readShort();
		for (int i = 0; i < staticImports; i++)
		{
			ImportDeclaration id = new ImportDeclaration(null, true);
			id.read(in);
			this.addUsing(id);
		}
		
		int operators = in.readShort();
		for (int i = 0; i < operators; i++)
		{
			Operator op = Operator.read(in);
			this.addOperator(op);
		}
		
		int typeAliases = in.readShort();
		for (int i = 0; i < typeAliases; i++)
		{
			TypeAlias ta = new TypeAlias();
			ta.read(in);
			this.addTypeAlias(ta);
		}
	}
	
	@Override
	public String toString()
	{
		return IASTNode.toString(this);
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
		
		if (this.includeCount > 0)
		{
			for (int i = 0; i < this.includeCount; i++)
			{
				buffer.append(prefix);
				this.includes[i].toString(prefix, buffer);
				buffer.append(";\n");
			}
			buffer.append('\n');
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
		
		if (this.usingCount > 0)
		{
			for (int i = 0; i < this.usingCount; i++)
			{
				buffer.append(prefix);
				this.usings[i].toString(prefix, buffer);
				buffer.append(";\n");
			}
			if (Formatting.Import.newLine)
			{
				buffer.append('\n');
			}
		}
		
		if (!this.operators.isEmpty())
		{
			for (Entry<Name, Operator> entry : this.operators)
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
		
		if (!this.typeAliases.isEmpty())
		{
			for (Entry<Name, ITypeAlias> entry : this.typeAliases)
			{
				buffer.append(prefix);
				entry.getValue().toString("", buffer);
				buffer.append(";\n");
			}
			if (Formatting.Import.newLine)
			{
				buffer.append('\n');
			}
		}
		
		if (this.headerDeclaration != null)
		{
			this.headerDeclaration.toString(prefix, buffer);
			buffer.append('\n');
			buffer.append('\n');
		}
	}
}

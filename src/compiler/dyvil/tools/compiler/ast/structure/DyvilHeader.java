package dyvil.tools.compiler.ast.structure;

import dyvil.collection.Map;
import dyvil.collection.mutable.IdentityHashMap;
import dyvil.io.FileUtils;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.external.ExternalClass;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.header.HeaderDeclaration;
import dyvil.tools.compiler.ast.header.ImportDeclaration;
import dyvil.tools.compiler.ast.header.IncludeDeclaration;
import dyvil.tools.compiler.ast.header.PackageDeclaration;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.modifiers.FlagModifierSet;
import dyvil.tools.compiler.ast.operator.IOperator;
import dyvil.tools.compiler.ast.operator.Operator;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.tools.compiler.ast.type.alias.TypeAlias;
import dyvil.tools.compiler.backend.IClassCompilable;
import dyvil.tools.compiler.backend.ObjectFormat;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lang.I18n;
import dyvil.tools.compiler.parser.header.DyvilHeaderParser;
import dyvil.tools.compiler.sources.DyvilFileType;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.transform.SemicolonInference;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ParserManager;
import dyvil.tools.parsing.TokenIterator;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.lexer.DyvilLexer;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;

public class DyvilHeader implements ICompilationUnit, IDyvilHeader
{
	protected final DyvilCompiler compiler;

	public final File inputFile;
	public final File outputDirectory;
	public final File outputFile;

	protected String code;

	protected Name    name;
	protected Package pack;

	protected TokenIterator tokens;
	protected MarkerList markers = new MarkerList(Markers.INSTANCE);

	protected PackageDeclaration packageDeclaration;

	protected ImportDeclaration[] importDeclarations = new ImportDeclaration[5];
	protected int importCount;
	protected ImportDeclaration[] usingDeclarations = new ImportDeclaration[5];
	protected int                  usingCount;
	protected IncludeDeclaration[] includes;
	protected int                  includeCount;
	protected ITypeAlias[]         typeAliases;
	protected int                  typeAliasCount;
	protected IOperator[]          operators;
	protected int                  operatorCount;

	protected Map<Name, IOperator> infixOperatorMap;

	protected HeaderDeclaration headerDeclaration;

	public DyvilHeader(DyvilCompiler compiler)
	{
		this.compiler = compiler;
		this.inputFile = null;
		this.outputDirectory = null;
		this.outputFile = null;
	}

	public DyvilHeader(DyvilCompiler compiler, Name name)
	{
		this.compiler = compiler;

		this.inputFile = null;
		this.outputDirectory = null;
		this.outputFile = null;
		this.name = name;
	}

	public DyvilHeader(DyvilCompiler compiler, Package pack, File input, File output)
	{
		this.compiler = compiler;

		this.pack = pack;
		this.inputFile = input;

		String name = input.getAbsolutePath();
		int start = name.lastIndexOf(File.separatorChar);
		int end = name.lastIndexOf('.');
		this.name = Name.fromQualified(name.substring(start + 1, end));

		name = output.getPath();
		start = name.lastIndexOf(File.separatorChar);
		end = name.lastIndexOf('.');
		this.outputDirectory = new File(name.substring(0, start));
		this.outputFile = new File(name.substring(0, end) + DyvilFileType.OBJECT_EXTENSION);
	}

	@Override
	public DyvilCompiler getCompilationContext()
	{
		return this.compiler;
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
	public File getInputFile()
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
		final int index = this.importCount++;
		if (index >= this.importDeclarations.length)
		{
			ImportDeclaration[] temp = new ImportDeclaration[index * 2];
			System.arraycopy(this.importDeclarations, 0, temp, 0, this.importDeclarations.length);
			this.importDeclarations = temp;
		}
		this.importDeclarations[index] = component;
	}

	@Override
	public ImportDeclaration getImport(int index)
	{
		return this.importDeclarations[index];
	}

	@Override
	public int usingCount()
	{
		return this.usingCount;
	}

	@Override
	public void addUsing(ImportDeclaration usingDeclaration)
	{
		final int index = this.usingCount++;
		if (index >= this.usingDeclarations.length)
		{
			ImportDeclaration[] temp = new ImportDeclaration[index * 2];
			System.arraycopy(this.usingDeclarations, 0, temp, 0, this.usingDeclarations.length);
			this.usingDeclarations = temp;
		}
		this.usingDeclarations[index] = usingDeclaration;
	}

	@Override
	public ImportDeclaration getUsing(int index)
	{
		return this.usingDeclarations[index];
	}

	@Override
	public int includeCount()
	{
		return this.includeCount;
	}

	@Override
	public void addInclude(IncludeDeclaration includeDeclaration)
	{
		if (this.includes == null)
		{
			this.includes = new IncludeDeclaration[2];
			this.includes[0] = includeDeclaration;
			this.includeCount = 1;
			return;
		}

		final int index = this.includeCount++;
		if (index >= this.includes.length)
		{
			IncludeDeclaration[] temp = new IncludeDeclaration[index * 2];
			System.arraycopy(this.includes, 0, temp, 0, this.includes.length);
			this.includes = temp;
		}
		this.includes[index] = includeDeclaration;
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

	// Operators

	@Override
	public int operatorCount()
	{
		return this.operatorCount;
	}

	@Override
	public IOperator getOperator(int index)
	{
		return this.operators[index];
	}

	@Override
	public IOperator resolveOperator(Name name, int type)
	{
		if (type == IOperator.INFIX && this.infixOperatorMap != null)
		{
			final IOperator operator = this.infixOperatorMap.get(name);
			if (operator != null)
			{
				return operator;
			}
		}

		IOperator candidate = null;
		for (int i = 0; i < this.operatorCount; i++)
		{
			final IOperator operator = this.operators[i];
			if (operator.getName() == name)
			{
				if (operator.getType() == type)
				{
					return operator;
				}
				candidate = operator;
			}
		}
		if (candidate != null && candidate.getType() == type)
		{
			return candidate;
		}

		for (int i = 0; i < this.includeCount; i++)
		{
			final IOperator operator = this.includes[i].getHeader().resolveOperator(name, type);
			if (operator != null)
			{
				return operator;
			}
		}
		return candidate;
	}

	@Override
	public void setOperator(int index, IOperator operator)
	{
		this.operators[index] = operator;
	}

	@Override
	public void addOperator(IOperator operator)
	{
		if (this.operators == null)
		{
			this.operators = new Operator[4];
			this.operators[0] = operator;
			this.operatorCount = 1;
			return;
		}

		final int index = this.operatorCount++;
		if (index >= this.operators.length)
		{
			final IOperator[] temp = new IOperator[index * 2];
			System.arraycopy(this.operators, 0, temp, 0, index);
			this.operators = temp;
		}
		this.operators[index] = operator;

		this.putOperator(operator);
	}

	private void putOperator(IOperator operator)
	{
		if (operator.getType() != IOperator.INFIX)
		{
			return;
		}

		final Name name = operator.getName();
		if (this.infixOperatorMap == null)
		{
			this.infixOperatorMap = new IdentityHashMap<>();
			this.infixOperatorMap.put(name, operator);
			return;
		}

		final IOperator existing = this.infixOperatorMap.get(name);
		if (existing == null)
		{
			this.infixOperatorMap.put(name, operator);
		}
	}

	// Type Aliases

	@Override
	public int typeAliasCount()
	{
		return this.typeAliasCount;
	}

	@Override
	public ITypeAlias getTypeAlias(int index)
	{
		return this.typeAliases[index];
	}

	@Override
	public ITypeAlias resolveTypeAlias(Name name, int arity)
	{
		ITypeAlias candidate = null;
		for (int i = 0; i < this.typeAliasCount; i++)
		{
			final ITypeAlias typeAlias = this.typeAliases[i];
			if (typeAlias.getName() == name)
			{
				if (typeAlias.typeParameterCount() == arity)
				{
					return typeAlias;
				}

				if (candidate == null)
				{
					candidate = typeAlias;
				}
			}
		}
		if (candidate != null)
		{
			return candidate;
		}

		for (int i = 0; i < this.includeCount; i++)
		{
			final ITypeAlias includedTypeAlias = this.includes[i].resolveTypeAlias(name, arity);
			if (includedTypeAlias != null)
			{
				return includedTypeAlias;
			}
		}
		return null;
	}

	@Override
	public void setTypeAlias(int index, ITypeAlias typeAlias)
	{
		this.typeAliases[index] = typeAlias;
	}

	@Override
	public void addTypeAlias(ITypeAlias typeAlias)
	{
		if (this.typeAliases == null)
		{
			this.typeAliases = new ITypeAlias[8];
			this.typeAliases[0] = typeAlias;
			this.typeAliasCount = 1;
			return;
		}

		final int index = this.typeAliasCount++;
		if (index >= this.typeAliases.length)
		{
			final ITypeAlias[] temp = new ITypeAlias[index * 2];
			System.arraycopy(this.typeAliases, 0, temp, 0, index);
			this.typeAliases = temp;
		}
		this.typeAliases[index] = typeAlias;
	}

	// Classes

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

	protected boolean load()
	{
		try
		{
			this.code = FileUtils.read(this.inputFile);
			return true;
		}
		catch (IOException ex)
		{
			this.compiler.error(I18n.get("source.error", this.inputFile), ex);
			return false;
		}
	}

	@Override
	public void tokenize()
	{
		if (this.load())
		{
			this.tokens = new DyvilLexer(this.markers, DyvilSymbols.INSTANCE).tokenize(this.code);
			SemicolonInference.inferSemicolons(this.tokens.first());
		}
	}

	@Override
	public void parse()
	{
		new ParserManager(DyvilSymbols.INSTANCE, this.tokens, this.markers).parse(new DyvilHeaderParser(this));
	}

	@Override
	public void resolveTypes()
	{
		for (int i = 0; i < this.includeCount; i++)
		{
			this.includes[i].resolve(this.markers, this);
		}

		for (int i = 0; i < this.importCount; i++)
		{
			this.importDeclarations[i].resolveTypes(this.markers, this);
		}

		for (int i = 0; i < this.usingCount; i++)
		{
			this.usingDeclarations[i].resolveTypes(this.markers, this);
		}

		for (int i = 0; i < this.typeAliasCount; i++)
		{
			this.typeAliases[i].resolveTypes(this.markers, this);
		}
	}

	@Override
	public void resolve()
	{
		if (this.headerDeclaration == null)
		{
			this.headerDeclaration = new HeaderDeclaration(this, ICodePosition.ORIGIN, this.name,
			                                               new FlagModifierSet(Modifiers.PUBLIC), null);
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
		return ICompilationUnit
			       .printMarkers(this.compiler, this.markers, DyvilFileType.DYVIL_HEADER, this.name, this.inputFile,
			                     this.code);
	}

	@Override
	public void compile()
	{
		if (this.printMarkers())
		{
			return;
		}

		ObjectFormat.write(this.compiler, this.outputFile, this);
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
			iclass = this.importDeclarations[i].getContext().resolveClass(name);
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
	public IDataMember resolveField(Name name)
	{
		for (int i = 0; i < this.usingCount; i++)
		{
			IDataMember field = this.usingDeclarations[i].getContext().resolveField(name);
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
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, IArguments arguments)
	{
		for (int i = 0; i < this.usingCount; i++)
		{
			this.usingDeclarations[i].getContext().getMethodMatches(list, receiver, name, arguments);
		}

		for (int i = 0; i < this.includeCount; i++)
		{
			this.includes[i].getMethodMatches(list, receiver, name, arguments);
		}
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		for (int i = 0; i < this.usingCount; i++)
		{
			this.usingDeclarations[i].getContext().getImplicitMatches(list, value, targetType);
		}

		for (int i = 0; i < this.includeCount; i++)
		{
			this.includes[i].getImplicitMatches(list, value, targetType);
		}
	}

	@Override
	public void getConstructorMatches(MatchList<IConstructor> list, IArguments arguments)
	{
	}

	@Override
	public byte getVisibility(IClassMember member)
	{
		IClass iclass = member.getEnclosingClass();

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
			if (header != null && (header == this || this.pack == header.getPackage()))
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
			this.importDeclarations[i].write(out);
		}

		// Using Declarations
		int staticImports = this.usingCount;
		out.writeShort(staticImports);
		for (int i = 0; i < staticImports; i++)
		{
			this.usingDeclarations[i].write(out);
		}

		// Operators Definitions
		out.writeShort(this.operatorCount);
		for (int i = 0; i < this.operatorCount; i++)
		{
			this.operators[i].writeData(out);
		}

		// Type Aliases
		out.writeShort(this.typeAliasCount);
		for (int i = 0; i < this.typeAliasCount; i++)
		{
			this.typeAliases[i].write(out);
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
			IncludeDeclaration id = new IncludeDeclaration();
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
			ImportDeclaration id = new ImportDeclaration(null);
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
			if (Formatting.getBoolean("package.newline"))
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
				this.importDeclarations[i].toString(prefix, buffer);
				buffer.append(";\n");
			}
			if (Formatting.getBoolean("import.newline"))
			{
				buffer.append('\n');
			}
		}

		if (this.usingCount > 0)
		{
			for (int i = 0; i < this.usingCount; i++)
			{
				buffer.append(prefix);
				this.usingDeclarations[i].toString(prefix, buffer);
				buffer.append(";\n");
			}
			if (Formatting.getBoolean("import.newline"))
			{
				buffer.append('\n');
			}
		}

		if (this.operatorCount > 0)
		{
			for (int i = 0; i < this.operatorCount; i++)
			{
				buffer.append(prefix);
				this.operators[i].toString(buffer);
				buffer.append(";\n");
			}
			if (Formatting.getBoolean("import.newline"))
			{
				buffer.append('\n');
			}
		}

		if (this.typeAliasCount > 0)
		{
			for (int i = 0; i < this.typeAliasCount; i++)
			{
				buffer.append(prefix);
				this.typeAliases[i].toString(prefix, buffer);
				buffer.append(";\n");
			}
			if (Formatting.getBoolean("import.newline"))
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

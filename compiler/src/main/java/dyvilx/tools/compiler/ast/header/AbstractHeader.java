package dyvilx.tools.compiler.ast.header;

import dyvil.lang.Formattable;
import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.ast.classes.ClassList;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IStaticContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.operator.IOperator;
import dyvilx.tools.compiler.ast.expression.operator.Operator;
import dyvilx.tools.compiler.ast.external.ExternalClass;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.imports.IImportContext;
import dyvilx.tools.compiler.ast.imports.ImportDeclaration;
import dyvilx.tools.compiler.ast.member.ClassMember;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.ast.type.alias.ITypeAlias;
import dyvilx.tools.compiler.config.Formatting;

import java.util.IdentityHashMap;
import java.util.Map;

public abstract class AbstractHeader implements IHeaderUnit, IContext
{
	// =============== Fields ===============

	// --------------- In-Source Declarations ---------------

	protected PackageDeclaration packageDeclaration;

	protected HeaderDeclaration headerDeclaration;

	protected ImportDeclaration[] importDeclarations = new ImportDeclaration[8];
	protected int                 importCount;

	protected ITypeAlias[] typeAliases;
	protected int          typeAliasCount;

	protected IOperator[] operators;
	protected int         operatorCount;

	protected ClassList classes = new ClassList();

	// --------------- Metadata ---------------

	protected Name    name;
	protected Package pack;

	protected ICompilable[] innerClasses = new ICompilable[2];
	protected int           innerClassCount;

	// - - - - - - - - Caches - - - - - - - -

	protected Map<Name, IOperator> infixOperatorMap;

	// =============== Constructors ===============

	public AbstractHeader()
	{
	}

	public AbstractHeader(Name name)
	{
		this.name = name;
	}

	// =============== Methods ===============

	// --------------- Getters and Setters ---------------

	@Override
	public SourcePosition getPosition()
	{
		return SourcePosition.ORIGIN;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
	}

	@Override
	public Name getName()
	{
		return this.name;
	}

	@Override
	public void setName(Name name)
	{
		this.name = name;
	}

	@Override
	public Package getPackage()
	{
		return this.pack;
	}

	@Override
	public void setPackage(Package pack)
	{
		this.pack = pack;
	}

	@Override
	public PackageDeclaration getPackageDeclaration()
	{
		return this.packageDeclaration;
	}

	@Override
	public void setPackageDeclaration(PackageDeclaration pack)
	{
		this.packageDeclaration = pack;
	}

	@Override
	public boolean hasHeaderDeclaration()
	{
		return this.headerDeclaration != null;
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

	// --------------- Imports ---------------

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
	public boolean hasMemberImports()
	{
		return this.importCount > 0;
	}

	// --------------- Operators ---------------

	@Override
	public int operatorCount()
	{
		return this.operatorCount;
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

		this.infixOperatorMap.putIfAbsent(name, operator);
	}

	// --------------- Type Aliases ---------------

	@Override
	public int typeAliasCount()
	{
		return this.typeAliasCount;
	}

	@Override
	public void addTypeAlias(ITypeAlias typeAlias)
	{
		typeAlias.setEnclosingHeader(this);

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

	// --------------- Classes ---------------

	@Override
	public ClassList getClasses()
	{
		return this.classes;
	}

	@Override
	public void addClass(IClass iclass)
	{
		iclass.setHeader(this);
		this.getClasses().add(iclass);
	}

	// --------------- Compilables ---------------

	@Override
	public int compilableCount()
	{
		return this.innerClassCount;
	}

	@Override
	public void addCompilable(ICompilable compilable)
	{
		int index = this.innerClassCount++;
		if (index >= this.innerClasses.length)
		{
			ICompilable[] temp = new ICompilable[this.innerClassCount];
			System.arraycopy(this.innerClasses, 0, temp, 0, this.innerClasses.length);
			this.innerClasses = temp;
		}
		this.innerClasses[index] = compilable;
	}

	// --------------- Context Implementation ---------------

	@Override
	public IContext getContext()
	{
		return new HeaderContext(this);
	}

	@Override
	public IHeaderUnit getHeader()
	{
		return this;
	}

	@Override
	public IHeaderUnit resolveHeader(Name name)
	{
		return name == this.name ? this : null;
	}

	@Override
	public void resolveTypeAlias(MatchList<ITypeAlias> matches, IType receiver, Name name, TypeList arguments)
	{
		for (int i = 0; i < this.typeAliasCount; i++)
		{
			this.typeAliases[i].checkMatch(matches, receiver, name, arguments);
		}
	}

	@Override
	public IOperator resolveOperator(Name name, byte type)
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
			if (operator.getName() != name)
			{
				continue;
			}

			if (operator.isType(type))
			{
				return operator;
			}
			if (candidate == null)
			{
				candidate = operator;
			}
		}

		return candidate;
	}

	@Override
	public IClass resolveClass(Name name)
	{
		return this.getClasses().get(name);
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
		this.getClasses().getExtensionMethodMatches(list, receiver, name, arguments);
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		this.getClasses().getExtensionImplicitMatches(list, value, targetType);
	}

	@Override
	public byte getVisibility(ClassMember member)
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

		switch (access)
		{
		case Modifiers.PUBLIC:
			return VISIBLE;
		case Modifiers.PROTECTED:
		case Modifiers.PACKAGE:
			IHeaderUnit header = iclass.getHeader();
			if (header != null && (header == this || this.pack == header.getPackage()))
			{
				return VISIBLE;
			}
			// Fallthrough
		case Modifiers.PRIVATE:
		case Modifiers.PRIVATE_PROTECTED:
		default:
			return INVISIBLE;
		}
	}

	// --------------- Compilation ---------------

	@Override
	public String getFullName()
	{
		return this.pack.getFullName() + '.' + this.name;
	}

	@Override
	public String getInternalName()
	{
		if (this.pack != null && this.pack != Package.rootPackage)
		{
			return this.pack.getInternalName() + '/' + this.name.qualified;
		}
		return this.name.qualified;
	}

	// --------------- Formatting ---------------

	@Override
	public String toString()
	{
		return Formattable.toString(this);
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

		this.getClasses().toString(prefix, buffer);
	}
}

class HeaderContext implements IStaticContext
{
	private AbstractHeader header;

	public HeaderContext(AbstractHeader header)
	{
		this.header = header;
	}

	@Override
	public DyvilCompiler getCompilationContext()
	{
		return this.header.getCompilationContext();
	}

	@Override
	public IHeaderUnit getHeader()
	{
		return this.header;
	}

	@Override
	public Package resolvePackage(Name name)
	{
		Package result;
		final ImportDeclaration[] importDeclarations = this.header.importDeclarations;
		for (int i = 0, count = this.header.importCount; i < count; i++)
		{
			result = importDeclarations[i].getContext().resolvePackage(name);
			if (result != null)
			{
				return result;
			}
		}

		return this.header.resolvePackage(name);
	}

	@Override
	public IHeaderUnit resolveHeader(Name name)
	{
		return this.header.resolveHeader(name);
	}

	@Override
	public void resolveTypeAlias(MatchList<ITypeAlias> matches, IType receiver, Name name, TypeList arguments)
	{
		this.header.resolveTypeAlias(matches, receiver, name, arguments);
		if (matches.hasCandidate())
		{
			return;
		}

		final ImportDeclaration[] importDeclarations = this.header.importDeclarations;
		for (int i = 0; i < this.header.importCount; i++)
		{
			final IImportContext importContext = importDeclarations[i].getContext();
			final MatchList<ITypeAlias> subList = matches.emptyCopy();
			importContext.resolveTypeAlias(subList, receiver, name, arguments);
			matches.addAll(subList);
		}
	}

	@Override
	public IOperator resolveOperator(Name name, byte type)
	{
		final IOperator candidate = this.header.resolveOperator(name, type);
		if (candidate != null && candidate.getType() == type)
		{
			return candidate;
		}

		for (int i = 0; i < this.header.importCount; i++)
		{
			final IOperator operator = this.header.importDeclarations[i].getContext().resolveOperator(name, type);
			if (operator != null && operator.getType() == type)
			{
				return operator;
			}
		}

		return candidate;
	}

	@Override
	public IClass resolveClass(Name name)
	{
		IClass theClass = this.header.resolveClass(name);
		if (theClass != null)
		{
			return theClass;
		}

		// Imported Classes
		for (int i = 0; i < this.header.importCount; i++)
		{
			theClass = this.header.importDeclarations[i].getContext().resolveClass(name);
			if (theClass != null)
			{
				return theClass;
			}
		}

		if (this.header.pack != null)
		{
			return this.header.pack.resolveClass(name);
		}
		return null;
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		IDataMember field = this.header.resolveField(name);
		if (field != null)
		{
			return field;
		}

		for (int i = 0; i < this.header.importCount; i++)
		{
			field = this.header.importDeclarations[i].getContext().resolveField(name);
			if (field != null)
			{
				return field;
			}
		}

		return null;
	}

	@Override
	public IValue resolveImplicit(IType type)
	{
		final IValue headerValue = this.header.resolveImplicit(type);
		if (headerValue != null)
		{
			return headerValue;
		}

		IValue candidate = null;
		for (int i = 0; i < this.header.importCount; i++)
		{
			final IValue value = this.header.importDeclarations[i].getContext().resolveImplicit(type);
			if (value == null)
			{
				continue;
			}
			if (candidate != null)
			{
				return null; // ambiguous
			}
			candidate = value;
		}
		return candidate;
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
		// For ordinary headers, this is a no-op, since they (currently) cannot host any free-standing functions
		// The REPL however can, so we need this call
		this.header.getMethodMatches(list, receiver, name, arguments);
		if (list.hasCandidate())
		{
			return;
		}

		final ImportDeclaration[] imports = this.header.importDeclarations;
		for (int i = 0; i < this.header.importCount; i++)
		{
			final IImportContext importContext = imports[i].getContext();
			final MatchList<IMethod> subList = list.emptyCopy();
			importContext.getMethodMatches(subList, receiver, name, arguments);
			list.addAll(subList);
		}
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		// See comment in getMethodMatches for rationale
		this.header.getImplicitMatches(list, value, targetType);
		if (list.hasCandidate())
		{
			return;
		}

		final ImportDeclaration[] imports = this.header.importDeclarations;
		for (int i = 0; i < this.header.importCount; i++)
		{
			final IImportContext importContext = imports[i].getContext();
			final MatchList<IMethod> subList = list.emptyCopy();
			importContext.getImplicitMatches(subList, value, targetType);
			list.addAll(subList);
		}
	}
}

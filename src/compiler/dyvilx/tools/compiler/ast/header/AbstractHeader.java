package dyvilx.tools.compiler.ast.header;

import dyvil.collection.Map;
import dyvil.collection.mutable.IdentityHashMap;
import dyvil.lang.Formattable;
import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IStaticContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.operator.IOperator;
import dyvilx.tools.compiler.ast.expression.operator.Operator;
import dyvilx.tools.compiler.ast.external.ExternalClass;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.imports.ImportDeclaration;
import dyvilx.tools.compiler.ast.member.IClassMember;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.ast.type.alias.ITypeAlias;
import dyvilx.tools.compiler.ast.type.alias.TypeAlias;
import dyvilx.tools.compiler.config.Formatting;
import dyvil.lang.Name;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class AbstractHeader implements IHeaderUnit, IContext
{
	protected PackageDeclaration packageDeclaration;
	protected HeaderDeclaration  headerDeclaration;

	protected ImportDeclaration[] importDeclarations = new ImportDeclaration[8];
	protected int importCount;

	protected ITypeAlias[] typeAliases;
	protected int          typeAliasCount;

	protected IOperator[] operators;
	protected int         operatorCount;

	// Metadata
	protected Name    name;
	protected Package pack;

	protected Map<Name, IOperator> infixOperatorMap;

	public AbstractHeader()
	{
	}

	public AbstractHeader(Name name)
	{
		this.name = name;
	}

	@Override
	public boolean isHeader()
	{
		return true;
	}

	@Override
	public IContext getContext()
	{
		return new HeaderContext(this);
	}

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
	public boolean hasMemberImports()
	{
		return this.importCount > 0;
	}

	// Operators

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
	public int compilableCount()
	{
		return 0;
	}

	@Override
	public void addCompilable(ICompilable compilable)
	{
	}

	// IContext override implementations

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

	// Compilation

	@Override
	public String getFullName()
	{
		return this.pack.getFullName() + '.' + this.name;
	}

	@Override
	public String getFullName(Name subClass)
	{
		if (subClass != this.name)
		{
			return this.pack.getFullName() + '.' + this.name.qualified + '.' + subClass.qualified;
		}
		return this.pack.getFullName() + '.' + subClass.qualified;
	}

	@Override
	public String getInternalName()
	{
		return this.pack.getInternalName() + this.name;
	}

	@Override
	public String getInternalName(Name subClass)
	{
		if (subClass != this.name)
		{
			return this.pack.getInternalName() + this.name.qualified + '$' + subClass;
		}
		return this.pack.getInternalName() + subClass.qualified;
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		// Header Name
		this.headerDeclaration.write(out);

		// Import Declarations
		int imports = this.importCount;
		out.writeShort(imports);
		for (int i = 0; i < imports; i++)
		{
			this.importDeclarations[i].write(out);
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

		// Import Declarations
		int imports = in.readShort();
		for (int i = 0; i < imports; i++)
		{
			ImportDeclaration id = new ImportDeclaration(null);
			id.read(in);
			this.addImport(id);
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
			importDeclarations[i].getContext().resolveTypeAlias(matches, receiver, name, arguments);
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
		return this.header.resolveImplicit(type);
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

		for (int i = 0; i < this.header.importCount; i++)
		{
			this.header.importDeclarations[i].getContext().getMethodMatches(list, receiver, name, arguments);
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

		for (int i = 0; i < this.header.importCount; i++)
		{
			this.header.importDeclarations[i].getContext().getImplicitMatches(list, value, targetType);
		}
	}
}

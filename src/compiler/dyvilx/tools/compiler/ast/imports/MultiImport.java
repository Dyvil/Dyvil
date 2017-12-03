package dyvilx.tools.compiler.ast.imports;

import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.expression.operator.IOperator;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.header.IHeaderUnit;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.ast.type.alias.ITypeAlias;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.util.Util;
import dyvil.lang.Name;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class MultiImport extends Import implements IImportContext, IImportList
{
	private IImport[] imports = new IImport[2];
	private int importCount;

	public MultiImport()
	{
		super(null);
	}

	public MultiImport(SourcePosition position)
	{
		super(position);
	}

	@Override
	public int importTag()
	{
		return MULTI;
	}

	@Override
	public int importCount()
	{
		return this.importCount;
	}

	@Override
	public IImport getImport(int index)
	{
		return this.imports[index];
	}

	@Override
	public void setImport(int index, IImport iimport)
	{
		this.imports[index] = iimport;
	}

	@Override
	public void addImport(IImport iimport)
	{
		int index = this.importCount++;
		if (index >= this.imports.length)
		{
			IImport[] temp = new IImport[index + 1];
			System.arraycopy(this.imports, 0, temp, 0, this.imports.length);
			this.imports = temp;
		}
		this.imports[index] = iimport;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context, IImportContext parentContext, int mask)
	{
		if (this.parent != null)
		{
			this.parent.resolveTypes(markers, context, parentContext, KindedImport.PARENT);
			parentContext = this.parent.asParentContext();
		}

		for (int i = 0; i < this.importCount; i++)
		{
			this.imports[i].resolveTypes(markers, context, parentContext, mask);
		}
	}

	@Override
	public void resolve(MarkerList markers, IContext context, IImportContext parentContext, int mask)
	{
		if (this.parent != null)
		{
			parentContext = this.parent.asParentContext();
		}

		for (int i = 0; i < this.importCount; i++)
		{
			this.imports[i].resolve(markers, context, parentContext, mask);
		}
	}

	@Override
	public IImportContext asContext()
	{
		return this;
	}

	@Override
	public IImportContext asParentContext()
	{
		return null;
	}

	@Override
	public Package resolvePackage(Name name)
	{
		for (int i = 0; i < this.importCount; i++)
		{
			Package pack = this.imports[i].asContext().resolvePackage(name);
			if (pack != null)
			{
				return pack;
			}
		}
		return null;
	}

	@Override
	public IHeaderUnit resolveHeader(Name name)
	{
		for (int i = 0; i < this.importCount; i++)
		{
			final IHeaderUnit result = this.imports[i].asContext().resolveHeader(name);
			if (result != null)
			{
				return result;
			}
		}
		return null;
	}

	@Override
	public void resolveTypeAlias(MatchList<ITypeAlias> matches, IType receiver, Name name, TypeList arguments)
	{
		for (int i = 0; i < this.importCount; i++)
		{
			final MatchList<ITypeAlias> subList = matches.emptyCopy();
			this.imports[i].asContext().resolveTypeAlias(subList, receiver, name, arguments);
			matches.addAll(subList);
		}
	}

	@Override
	public IOperator resolveOperator(Name name, byte type)
	{
		for (int i = 0; i < this.importCount; i++)
		{
			final IOperator result = this.imports[i].asContext().resolveOperator(name, type);
			if (result != null)
			{
				return result;
			}
		}
		return null;
	}

	@Override
	public IClass resolveClass(Name name)
	{
		for (int i = 0; i < this.importCount; i++)
		{
			final IClass result = this.imports[i].asContext().resolveClass(name);
			if (result != null)
			{
				return result;
			}
		}
		return null;
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		for (int i = 0; i < this.importCount; i++)
		{
			final IDataMember field = this.imports[i].asContext().resolveField(name);
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
		IValue candidate = null;
		for (int i = 0; i < this.importCount; i++)
		{
			final IValue value = this.imports[i].asContext().resolveImplicit(type);
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
		for (int i = 0; i < this.importCount; i++)
		{
			final MatchList<IMethod> subList = list.emptyCopy();
			this.imports[i].asContext().getMethodMatches(subList, receiver, name, arguments);
			list.addAll(subList);
		}
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		for (int i = 0; i < this.importCount; i++)
		{
			final MatchList<IMethod> subList = list.emptyCopy();
			this.imports[i].asContext().getImplicitMatches(subList, value, targetType);
			list.addAll(subList);
		}
	}

	@Override
	public void writeData(DataOutput out) throws IOException
	{
		IImport.writeImport(this.parent, out);

		out.writeShort(this.importCount);
		for (int i = 0; i < this.importCount; i++)
		{
			IImport.writeImport(this.imports[i], out);
		}
	}

	@Override
	public void readData(DataInput in) throws IOException
	{
		this.parent = IImport.readImport(in);

		this.importCount = in.readShort();
		this.imports = new IImport[this.importCount];
		for (int i = 0; i < this.importCount; i++)
		{
			this.imports[i] = IImport.readImport(in);
		}
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.appendParent(prefix, buffer);

		Formatting.appendSeparator(buffer, "import.multi.open_brace", '{');
		Util.astToString(prefix, this.imports, this.importCount, Formatting.getSeparator("import.multi.separator", ','),
		                 buffer);

		if (Formatting.getBoolean("import.multi.close_brace.space_before"))
		{
			buffer.append(' ');
		}
		buffer.append('}');
	}
}

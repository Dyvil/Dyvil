package dyvilx.tools.compiler.phase;

import dyvil.annotation.internal.NonNull;
import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.ast.header.ICompilationUnit;
import dyvilx.tools.compiler.lang.I18n;

import java.util.function.Consumer;

public class SequentialCompilerPhase implements ICompilerPhase
{
	// =============== Fields ===============

	private final int    id;
	private final String name;

	private final Consumer<ICompilationUnit> apply;

	// =============== Constructors ===============

	public SequentialCompilerPhase(int id, String name, Consumer<ICompilationUnit> apply)
	{
		this.id = id;
		this.name = name;
		this.apply = apply;
	}

	// =============== Properties ===============

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public int getID()
	{
		return this.id;
	}

	// =============== Methods ===============

	// --------------- Phase Apply ---------------

	@Override
	public void apply(DyvilCompiler compiler)
	{
		for (ICompilationUnit unit : compiler.fileFinder.units)
		{
			try
			{
				this.apply.accept(unit);
			}
			catch (Exception e)
			{
				compiler.error(I18n.get("phase.failed.unit", this.getName(), unit.getFileSource().file()), e);
			}
		}
	}

	// --------------- Comparison, Equals and Hash Code ---------------

	@Override
	public int compareTo(@NonNull ICompilerPhase o)
	{
		return Integer.compare(this.id, o.getID());
	}

	@Override
	public boolean equals(Object o)
	{
		return o instanceof ICompilerPhase && this.getID() == ((ICompilerPhase) o).getID();
	}

	@Override
	public int hashCode()
	{
		return this.getID();
	}

	// --------------- Formatting ---------------

	@Override
	public String toString()
	{
		return this.name;
	}
}

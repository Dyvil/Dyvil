package dyvil.tools.compiler.ast.header;

import dyvil.tools.compiler.ast.context.IDefaultContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class WildcardImport extends Import
{
	private IImportContext context;

	public WildcardImport()
	{
		super(null);
	}

	public WildcardImport(ICodePosition position)
	{
		super(position);
	}

	@Override
	public int importTag()
	{
		return WILDCARD;
	}

	@Override
	public void resolveTypes(MarkerList markers, IImportContext context, int mask)
	{
		if (this.parent != null)
		{
			this.parent.resolveTypes(markers, context, KindedImport.parent(mask));
			context = this.parent.asParentContext();
		}

		if ((mask & KindedImport.STATIC) != 0 && context != null)
		{
			this.context = context;
			return;
		}

		if (!(context instanceof Package))
		{
			markers.add(Markers.semanticError(this.position, "import.wildcard.invalid"));
			this.context = IDefaultContext.DEFAULT;
			return;
		}
		this.context = context;
	}

	@Override
	public void resolve(MarkerList markers, IImportContext context, int mask)
	{
	}

	@Override
	public IImportContext asContext()
	{
		return this.context;
	}

	@Override
	public IImportContext asParentContext()
	{
		return null;
	}

	@Override
	public void writeData(DataOutput out) throws IOException
	{
		IImport.writeImport(this.parent, out);
	}

	@Override
	public void readData(DataInput in) throws IOException
	{
		this.parent = IImport.readImport(in);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.appendParent(prefix, buffer);
		buffer.append('_');
	}
}

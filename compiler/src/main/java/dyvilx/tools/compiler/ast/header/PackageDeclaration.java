package dyvilx.tools.compiler.ast.header;

import dyvil.lang.Formattable;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.ASTNode;
import dyvilx.tools.parsing.marker.MarkerList;

public class PackageDeclaration implements ASTNode
{
	// =============== Fields ===============

	protected SourcePosition position;
	protected String         fullPackageName;

	// =============== Constructors ===============

	public PackageDeclaration(SourcePosition position)
	{
		this.position = position;
	}

	public PackageDeclaration(SourcePosition position, String fullPackageName)
	{
		this.position = position;
		this.fullPackageName = fullPackageName;
	}

	// =============== Properties ===============

	public String getPackage()
	{
		return this.fullPackageName;
	}

	public void setPackage(String thePackage)
	{
		this.fullPackageName = thePackage;
	}

	@Override
	public SourcePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
		this.position = position;
	}

	// =============== Methods ===============

	// --------------- Phases ---------------

	public static void check(PackageDeclaration packageDeclaration, MarkerList markers, Package enclosingPackage)
	{
		if (packageDeclaration != null)
		{
			packageDeclaration.check(markers, enclosingPackage);
			return;
		}

		if (enclosingPackage != Package.rootPackage)
		{
			markers.add(Markers.semanticError(SourcePosition.ORIGIN, "package_declaration.missing",
			                                  enclosingPackage.getFullName()));
		}
	}

	public void check(MarkerList markers, Package enclosingPackage)
	{
		final String fullName = enclosingPackage.getFullName();
		if (enclosingPackage == Package.rootPackage)
		{
			markers.add(Markers.semanticError(this.position, "package_declaration.default_package", fullName));
			return;
		}

		if (!this.fullPackageName.equals(fullName))
		{
			markers.add(
				Markers.semanticError(this.position, "package_declaration.mismatch", this.fullPackageName, fullName));
		}
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
		buffer.append("package ").append(this.fullPackageName);
	}
}

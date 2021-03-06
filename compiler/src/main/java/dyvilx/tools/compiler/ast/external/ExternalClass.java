package dyvilx.tools.compiler.ast.external;

import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.classes.AbstractClass;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.classes.metadata.IClassMetadata;
import dyvilx.tools.compiler.ast.context.CombiningContext;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.generic.ITypeParameter;
import dyvilx.tools.compiler.ast.generic.TypeParameterList;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.structure.RootPackage;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.backend.classes.ClassWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.sources.DyvilFileType;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.HashMap;
import java.util.Map;

public final class ExternalClass extends AbstractClass
{
	// =============== Constants ===============

	private static final int METADATA       = 1;
	private static final int SUPER_TYPE     = 1 << 1;
	private static final int GENERICS       = 1 << 2;
	private static final int INTERFACES     = 1 << 3;
	private static final int ANNOTATIONS    = 1 << 5;
	private static final int MEMBER_CLASSES = 1 << 6;

	// =============== Fields ===============

	private byte                resolved;
	private Map<String, String> innerTypes; // inner name -> full internal name

	// =============== Properties ===============

	@Override
	public SourcePosition getPosition()
	{
		return null;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
	}

	public Map<String, String> getInnerTypeNames()
	{
		if (this.innerTypes == null)
		{
			this.innerTypes = new HashMap<>();
		}

		return this.innerTypes;
	}

	@Override
	public String getFullName()
	{
		if (this.fullName != null)
		{
			return this.fullName;
		}
		if (this.enclosingClass != null)
		{
			return this.fullName = this.enclosingClass.getFullName() + '.' + this.getName();
		}
		if (this.enclosingPackage != null)
		{
			return this.fullName = this.enclosingPackage.getFullName() + '.' + this.getName();
		}
		return this.fullName = this.getName().toString();
	}

	@Override
	public IType getSuperType()
	{
		this.resolveSuperType();
		return super.getSuperType();
	}

	@Override
	public TypeList getInterfaces()
	{
		this.resolveInterfaces();
		return super.getInterfaces();
	}

	@Override
	public TypeParameterList getTypeParameters()
	{
		this.resolveGenerics();
		return super.getTypeParameters();
	}

	@Override
	public AttributeList getAttributes()
	{
		this.resolveAnnotations();
		return super.getAttributes();
	}

	@Override
	public IClassMetadata getMetadata()
	{
		this.resolveMetadata();
		return this.metadata;
	}

	// =============== Methods ===============

	public void clearResolved()
	{
		this.resolved = 0;
	}

	private IContext getCombiningContext()
	{
		return new CombiningContext(this, Package.rootPackage);
	}

	private void resolveMetadata()
	{
		if ((this.resolved & METADATA) != 0)
		{
			return;
		}

		this.resolved |= METADATA;

		final IContext context = this.getCombiningContext();

		this.metadata = IClassMetadata.getClassMetadata(this, this.attributes.flags());
		this.metadata.resolveTypesBeforeBody(null, context);
		this.metadata.resolveTypesAfterBody(null, context);
	}

	private void resolveGenerics()
	{
		if ((this.resolved & GENERICS) != 0)
		{
			return;
		}

		this.resolved |= GENERICS;

		final int typeParams;
		if (this.typeParameters == null || (typeParams = this.typeParameters.size()) <= 0)
		{
			return;
		}

		final IContext context = this.getCombiningContext();

		for (int i = 0; i < typeParams; i++)
		{
			final ITypeParameter typeParameter = this.typeParameters.get(i);
			typeParameter.resolveTypes(null, context);
		}
	}

	private void resolveSuperType()
	{
		if ((this.resolved & SUPER_TYPE) != 0)
		{
			return;
		}

		this.resolved |= SUPER_TYPE;

		if (this.superType != null)
		{
			final IContext context = this.getCombiningContext();
			this.superType = this.superType.resolveType(null, context);
		}
	}

	private void resolveInterfaces()
	{
		if ((this.resolved & INTERFACES) != 0)
		{
			return;
		}

		this.resolved |= INTERFACES;
		this.interfaces.resolveTypes(null, this.getCombiningContext());
	}

	private void resolveAnnotations()
	{
		if ((this.resolved & ANNOTATIONS) != 0)
		{
			return;
		}

		this.resolved |= ANNOTATIONS;
		this.attributes.resolveTypes(null, RootPackage.rootPackage, this);
	}

	private void resolveMemberClasses()
	{
		if ((this.resolved & MEMBER_CLASSES) != 0)
		{
			return;
		}

		this.resolved |= MEMBER_CLASSES;
		if (this.innerTypes == null)
		{
			return;
		}

		for (Map.Entry<String, String> entry : this.innerTypes.entrySet())
		{
			final String innerName = entry.getKey();
			this.resolveClass(Name.fromRaw(innerName)); // adds the class to the body
		}
		this.innerTypes.clear(); // we no longer need this
		this.innerTypes = null;
	}

	// --------------- Resolution ---------------

	@Override
	public IClass resolveClass(Name name)
	{
		final IClass bodyClass = this.body.getClasses().get(name);
		if (bodyClass != null)
		{
			return bodyClass;
		}

		if (this.innerTypes == null)
		{
			return null;
		}

		final String internal = this.innerTypes.get(name.qualified);
		if (internal == null)
		{
			return null;
		}

		// Resolve the class name and add it to the body
		final String fileName = internal + DyvilFileType.CLASS_EXTENSION;
		return Package.loadExternalClass(fileName, this.body::addClass);
	}

	@Override
	public IValue resolveImplicit(IType type)
	{
		this.resolveMemberClasses();
		return super.resolveImplicit(type);
	}

	// --------------- Phases ---------------

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
	}

	@Override
	public void foldConstants()
	{
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
	}

	// --------------- Compilation ---------------

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
	}

	@Override
	public void writeClassInit(MethodWriter writer) throws BytecodeException
	{
	}

	@Override
	public void writeStaticInit(MethodWriter writer) throws BytecodeException
	{
	}

	@Override
	public void writeSignature(DataOutput out)
	{
	}

	@Override
	public void readSignature(DataInput in)
	{
	}
}

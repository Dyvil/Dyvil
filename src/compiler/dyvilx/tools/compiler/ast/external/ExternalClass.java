package dyvilx.tools.compiler.ast.external;

import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.classes.AbstractClass;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.classes.metadata.IClassMetadata;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.context.CombiningContext;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.generic.ITypeParameter;
import dyvilx.tools.compiler.ast.generic.TypeParameterList;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.parameter.IParameter;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.structure.RootPackage;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.classes.ClassWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.sources.DyvilFileType;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class ExternalClass extends AbstractClass
{
	private static final int METADATA       = 1;
	private static final int SUPER_TYPES    = 1 << 1;
	private static final int GENERICS       = 1 << 2;
	private static final int ANNOTATIONS    = 1 << 5;
	private static final int MEMBER_CLASSES = 1 << 6;

	private byte                resolved;
	private Map<String, String> innerTypes; // inner name -> full internal name

	public ExternalClass()
	{
	}

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

		this.metadata = IClass.getClassMetadata(this, this.attributes.flags());
		this.metadata.resolveTypesHeader(null, context);
		this.metadata.resolveTypesBody(null, context);
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

	private void resolveSuperTypes()
	{
		if ((this.resolved & SUPER_TYPES) != 0)
		{
			return;
		}

		this.resolved |= SUPER_TYPES;

		final IContext context = this.getCombiningContext();
		if (this.superType != null)
		{
			this.superType = this.superType.resolveType(null, context);
		}

		if (this.interfaces != null)
		{
			this.interfaces.resolveTypes(null, context);
		}
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
	public IClass getThisClass()
	{
		this.resolveGenerics();
		return this;
	}

	@Override
	public IType getThisType()
	{
		this.resolveGenerics();
		return super.getThisType();
	}

	@Override
	public IType getSuperType()
	{
		this.resolveSuperTypes();
		return this.superType;
	}

	@Override
	public boolean isSubClassOf(IType type)
	{
		this.resolveSuperTypes();
		return super.isSubClassOf(type);
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
	public IType resolveType(ITypeParameter typeParameter, IType concrete)
	{
		this.resolveGenerics();
		this.resolveSuperTypes();
		return super.resolveType(typeParameter, concrete);
	}

	@Override
	public IClassMetadata getMetadata()
	{
		this.resolveMetadata();
		return this.metadata;
	}

	@Override
	public IMethod getFunctionalMethod()
	{
		this.resolveMetadata();
		return super.getFunctionalMethod();
	}

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
	public boolean checkImplements(IMethod candidate, ITypeContext typeContext)
	{
		this.resolveMetadata();
		this.resolveGenerics();
		this.resolveSuperTypes();
		return super.checkImplements(candidate, typeContext);
	}

	@Override
	public void checkMethods(MarkerList markers, IClass checkedClass, ITypeContext typeContext,
		Set<IClass> checkedClasses)
	{
		this.resolveGenerics();
		this.resolveSuperTypes();
		super.checkMethods(markers, checkedClass, typeContext, checkedClasses);
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
	public IDataMember resolveField(Name name)
	{
		final IParameter parameter = this.resolveClassParameter(name);
		if (parameter != null)
		{
			return parameter;
		}

		// Own fields
		IDataMember field = this.body.getField(name);
		if (field != null)
		{
			return field;
		}

		this.resolveSuperTypes();

		// Inherited Fields
		if (this.superType != null)
		{
			field = this.superType.resolveField(name);
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
		this.resolveMemberClasses();
		return super.resolveImplicit(type);
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
		this.resolveGenerics();

		/*
		Note: unlike AbstractClass.getMethodMatches, this does not check the Class Parameter Properties, because
		External classes do not have any class parameters with associated properties
		*/
		this.body.getMethodMatches(list, receiver, name, arguments);
		// The same applies for the Metadata

		if (list.hasCandidate())
		{
			return;
		}

		this.resolveSuperTypes();

		if (this.superType != null)
		{
			this.superType.getMethodMatches(list, receiver, name, arguments);
		}

		if (list.hasCandidate() || this.interfaces == null)
		{
			return;
		}

		for (IType type : this.interfaces)
		{
			type.getMethodMatches(list, receiver, name, arguments);
		}
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		this.resolveGenerics();
		this.body.getImplicitMatches(list, value, targetType);
	}

	@Override
	public void getConstructorMatches(MatchList<IConstructor> list, ArgumentList arguments)
	{
		this.resolveSuperTypes();
		this.resolveGenerics();

		this.body.getConstructorMatches(list, arguments);
	}

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
	public void writeSignature(DataOutput out) throws IOException
	{
	}

	@Override
	public void readSignature(DataInput in) throws IOException
	{
	}
}

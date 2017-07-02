package dyvil.tools.compiler.ast.external;

import dyvil.collection.Map;
import dyvil.collection.Set;
import dyvil.collection.immutable.ArraySet;
import dyvil.collection.mutable.HashMap;
import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvil.tools.asm.*;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.AnnotationUtil;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.AbstractClass;
import dyvil.tools.compiler.ast.classes.ClassBody;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.metadata.IClassMetadata;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.context.CombiningContext;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.generic.TypeParameterList;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.header.IHeaderUnit;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.parameter.ClassParameter;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.ParameterList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.structure.RootPackage;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.TypeList;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.backend.visitor.*;
import dyvil.tools.compiler.sources.DyvilFileType;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static dyvil.tools.compiler.backend.ClassFormat.*;

public final class ExternalClass extends AbstractClass
{
	private static final int METADATA            = 1;
	private static final int SUPER_TYPES         = 1 << 1;
	private static final int GENERICS            = 1 << 2;
	private static final int BODY_METHOD_CACHE   = 1 << 3;
	private static final int BODY_IMPLICIT_CACHE = 1 << 4;

	protected Package thePackage;

	private byte                resolved;
	private Map<String, String> innerTypes;
	private Set<String>         classParameters;

	public ExternalClass(Name name)
	{
		this.name = name;
		// this.modifiers = new FlagModifierSet();
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

		this.metadata = IClass.getClassMetadata(this, this.modifiers.toFlags());
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

	private void resolveMethodCache()
	{
		if ((this.resolved & BODY_METHOD_CACHE) != 0)
		{
			return;
		}

		this.body.initExternalMethodCache();
		this.resolved |= BODY_METHOD_CACHE;
	}

	private void resolveImplicitCache()
	{
		if ((this.resolved & BODY_IMPLICIT_CACHE) != 0)
		{
			return;
		}

		this.body.initExternalImplicitCache();
		this.resolved |= BODY_IMPLICIT_CACHE;
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
		if (this.annotations != null)
		{
			this.annotations.resolveTypes(null, RootPackage.rootPackage, this);
		}
	}

	public void setClassParameters(String[] classParameters)
	{
		this.classParameters = ArraySet.apply(classParameters);
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
		if (this.thePackage != null)
		{
			return this.fullName = this.thePackage.getFullName() + '.' + this.getName();
		}
		return this.fullName = this.getName().toString();
	}

	@Override
	public IHeaderUnit getHeader()
	{
		return null;
	}

	@Override
	public void setHeader(IHeaderUnit unit)
	{
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
	public AnnotationList getAnnotations()
	{
		this.resolveAnnotations();
		return super.getAnnotations();
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
		this.resolveMethodCache();
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
		final IClass bodyClass = this.body.getClass(name);
		if (bodyClass != null)
		{
			return bodyClass;
		}

		if (this.innerTypes == null)
		{
			return null;
		}

		String internal = this.innerTypes.get(name.qualified);
		if (internal == null)
		{
			return null;
		}

		// Resolve the class name
		final String fileName = internal + DyvilFileType.CLASS_EXTENSION;
		return Package.loadClass(fileName, name, this.body);
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		final IParameter parameter = this.parameters.get(name);
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
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
		this.resolveGenerics();
		this.resolveMethodCache();

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
		this.resolveImplicitCache();

		this.body.getImplicitMatches(list, value, targetType);
	}

	@Override
	public void getConstructorMatches(MatchList<IConstructor> list, ArgumentList arguments)
	{
		this.resolveSuperTypes();
		this.resolveGenerics();

		this.body.getConstructorMatches(list, arguments);
	}

	public void visit(int access, String name, String signature, String superName, String[] interfaces)
	{
		this.modifiers = readModifiers(access);
		this.internalName = name;

		this.body = new ClassBody(this);
		if (interfaces != null)
		{
			this.interfaces = new TypeList(interfaces.length);
		}

		int index = name.lastIndexOf('$');
		if (index == -1)
		{
			index = name.lastIndexOf('/');
		}
		if (index == -1)
		{
			this.name = Name.fromQualified(name);
			this.thePackage = Package.rootPackage;
			this.fullName = name;
		}
		else
		{
			this.name = Name.fromQualified(name.substring(index + 1));
			// Do not set 'fullName' here
			this.thePackage = Package.rootPackage.resolvePackageInternal(name.substring(0, index));
		}

		if (signature != null)
		{
			ClassFormat.readClassSignature(signature, this);
		}
		else
		{
			this.superType = superName != null ? ClassFormat.internalToType(superName) : null;

			if (interfaces != null)
			{
				for (String internal : interfaces)
				{
					this.interfaces.add(ClassFormat.internalToType(internal));
				}
			}
		}
	}

	public AnnotationVisitor visitAnnotation(String type)
	{
		switch (type)
		{
		case ModifierUtil.DYVIL_MODIFIERS:
			return new ModifierVisitor(this.modifiers);
		case AnnotationUtil.CLASS_PARAMETERS:
			return new ClassParameterAnnotationVisitor(this);
		}

		String internal = ClassFormat.extendedToInternal(type);
		if (this.addRawAnnotation(internal, null))
		{
			if (this.annotations == null)
			{
				this.annotations = new AnnotationList();
			}

			Annotation annotation = new Annotation(null, ClassFormat.internalToType(internal));
			return new AnnotationReader(this.getAnnotations(), annotation);
		}
		return null;
	}

	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc)
	{
		IAnnotation annotation = new Annotation(ClassFormat.extendedToType(desc));
		switch (TypeReference.getSort(typeRef))
		{
		case TypeReference.CLASS_EXTENDS:
		{
			final int index = TypeReference.getSuperTypeIndex(typeRef);
			if (index < 0)
			{
				this.superType = IType.withAnnotation(this.superType, annotation, typePath);
			}
			else
			{
				this.interfaces.set(index, IType.withAnnotation(this.interfaces.get(index), annotation, typePath));
			}
			break;
		}
		case TypeReference.CLASS_TYPE_PARAMETER:
		{
			ITypeParameter typeVar = this.typeParameters.get(TypeReference.getTypeParameterIndex(typeRef));
			if (typeVar.addRawAnnotation(desc, annotation))
			{
				return null;
			}

			typeVar.getAnnotations().add(annotation);
			break;
		}
		case TypeReference.CLASS_TYPE_PARAMETER_BOUND:
		{
			ITypeParameter typeVar = this.typeParameters.get(TypeReference.getTypeParameterIndex(typeRef));
			typeVar.addBoundAnnotation(annotation, TypeReference.getTypeParameterBoundIndex(typeRef), typePath);
			break;
		}
		}
		return new AnnotationReader(null, annotation);
	}

	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value)
	{
		IType type = ClassFormat.readFieldType(signature == null ? desc : signature);

		if (this.classParameters != null && this.classParameters.contains(name))
		{
			final ClassParameter param = new ExternalClassParameter(this, Name.fromQualified(name), desc, type,
			                                                        readModifiers(access));
			this.parameters.add(param);
			return new SimpleFieldVisitor(param);
		}

		final ExternalField field = new ExternalField(this, Name.fromQualified(name), desc, type,
		                                              readModifiers(access));

		if (value != null)
		{
			field.setConstantValue(value);
		}

		this.body.addDataMember(field);

		return new SimpleFieldVisitor(field);
	}

	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
	{
		if ((access & Modifiers.SYNTHETIC) != 0)
		{
			return null;
		}

		switch (name)
		{
		case "<clinit>":
			return null;
		case "<init>":
			if (this.hasModifier(Modifiers.ENUM))
			{
				return null;
			}

			final ExternalConstructor ctor = new ExternalConstructor(this);
			ctor.setModifiers(readModifiers(access));

			if (signature != null)
			{
				readConstructorType(signature, ctor);
			}
			else
			{
				readConstructorType(desc, ctor);

				if (exceptions != null)
				{
					readExceptions(exceptions, ctor.getExceptions());
				}
			}

			if ((access & Modifiers.ACC_VARARGS) != 0)
			{
				final ParameterList parameterList = ctor.getExternalParameterList();
				parameterList.get(parameterList.size() - 1).setVarargs();
			}

			this.body.addConstructor(ctor);

			return new SimpleMethodVisitor(ctor);
		}

		if (this.isAnnotation() && (access & Modifiers.STATIC) == 0)
		{
			final ClassParameter param = new ExternalClassParameter(this, Name.fromQualified(name), desc.substring(2),
			                                                        readReturnType(desc), readModifiers(access));
			this.parameters.add(param);
			return new AnnotationClassVisitor(param);
		}

		final ExternalMethod method = new ExternalMethod(this, name, desc, signature, readModifiers(access));

		if (signature != null)
		{
			readMethodType(signature, method);
		}
		else
		{
			readMethodType(desc, method);

			if (exceptions != null)
			{
				readExceptions(exceptions, method.getExceptions());
			}
		}

		if ((access & Modifiers.ACC_VARARGS) != 0)
		{
			final ParameterList parameterList = method.getExternalParameterList();
			parameterList.get(parameterList.size() - 1).setVarargs();
		}

		this.body.addMethod(method);
		return new SimpleMethodVisitor(method);
	}

	public void visitInnerClass(String name, String outerName, String innerName)
	{
		if (innerName == null || !this.internalName.equals(outerName))
		{
			return;
		}

		if (this.innerTypes == null)
		{
			this.innerTypes = new HashMap<>();
		}

		this.innerTypes.put(innerName, name);
	}

	public void visitEnd()
	{
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

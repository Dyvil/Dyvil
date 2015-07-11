package dyvil.tools.compiler.ast.external;

import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.access.MethodCall;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.ClassBody;
import dyvil.tools.compiler.ast.classes.CodeClass;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassMetadata;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.field.Property;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.generic.Variance;
import dyvil.tools.compiler.ast.generic.type.ClassGenericType;
import dyvil.tools.compiler.ast.generic.type.TypeVarType;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.Constructor;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.ClassParameter;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.visitor.AnnotationClassVisitor;
import dyvil.tools.compiler.backend.visitor.BytecodeVisitor;
import dyvil.tools.compiler.backend.visitor.SimpleFieldVisitor;
import dyvil.tools.compiler.backend.visitor.SimpleMethodVisitor;
import dyvil.tools.compiler.lexer.marker.MarkerList;

import org.objectweb.asm.*;

public final class ExternalClass extends CodeClass
{
	public Package		thePackage;
	
	private List<IType>	innerTypes;
	
	private boolean		metadataResolved;
	private boolean		superTypesResolved;
	private boolean		genericsResolved;
	private boolean		annotationsResolved;
	private boolean		innerTypesResolved;
	
	public ExternalClass(Name name)
	{
		this.name = name;
	}
	
	private void resolveMetadata()
	{
		this.metadata = IClass.getClassMetadata(this, this.modifiers);
		this.metadata.resolve(null, this);
	}
	
	private void resolveGenerics()
	{
		this.genericsResolved = true;
		if (this.genericCount > 0)
		{
			ClassGenericType type = new ClassGenericType(this);
			
			for (int i = 0; i < this.genericCount; i++)
			{
				ITypeVariable var = this.generics[i];
				var.resolveTypes(null, Package.rootPackage);
				type.addType(new TypeVarType(var));
			}
			
			this.type = type;
		}
	}
	
	private void resolveSuperTypes()
	{
		this.superTypesResolved = true;
		if (this.superType != null)
		{
			this.superType = this.superType.resolve(null, this, TypePosition.SUPER_TYPE);
		}
		
		for (int i = 0; i < this.interfaceCount; i++)
		{
			this.interfaces[i] = this.interfaces[i].resolve(null, this, TypePosition.SUPER_TYPE);
		}
		
		if (!this.metadataResolved)
		{
			this.resolveMetadata();
		}
	}
	
	private void resolveAnnotations()
	{
		this.annotationsResolved = true;
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].resolveTypes(null, Package.rootPackage);
			
			String internalName = this.annotations[i].type.getInternalName();
			if (!this.addRawAnnotation(internalName))
			{
				this.removeAnnotation(i--);
			}
		}
	}
	
	private void resolveInnerTypes()
	{
		this.innerTypesResolved = true;
		
		if (this.innerTypes != null)
		{
			int len = this.innerTypes.size();
			for (int i = 0; i < len; i++)
			{
				IType t = this.innerTypes.get(i);
				this.innerTypes.set(i, t.resolve(null, Package.rootPackage, TypePosition.CLASS));
				t.getTheClass().setOuterClass(this);
			}
		}
	}
	
	@Override
	public IType getType()
	{
		if (!this.genericsResolved)
		{
			this.resolveGenerics();
		}
		return this.type;
	}
	
	@Override
	public IClass getThisClass()
	{
		if (!this.genericsResolved)
		{
			this.resolveGenerics();
		}
		return this;
	}
	
	@Override
	public IType getSuperType()
	{
		if (!this.superTypesResolved)
		{
			this.resolveSuperTypes();
		}
		return this.superType;
	}
	
	@Override
	public IClass getOuterClass()
	{
		if (!this.innerTypesResolved)
		{
			this.resolveInnerTypes();
		}
		return super.getOuterClass();
	}
	
	@Override
	public boolean isSubTypeOf(IType type)
	{
		if (!this.superTypesResolved)
		{
			this.resolveSuperTypes();
		}
		return super.isSubTypeOf(type);
	}
	
	@Override
	public ITypeVariable getTypeVariable(int index)
	{
		if (!this.genericsResolved)
		{
			this.resolveGenerics();
		}
		return super.getTypeVariable(index);
	}
	
	@Override
	public Annotation getAnnotation(IClass type)
	{
		if (!this.annotationsResolved)
		{
			this.resolveAnnotations();
		}
		return super.getAnnotation(type);
	}
	
	@Override
	public void addParameter(IParameter param)
	{
		if (!this.metadataResolved)
		{
			this.resolveMetadata();
		}
		super.addParameter(param);
	}
	
	@Override
	public IType resolveType(ITypeVariable typeVar, IType concrete)
	{
		if (!this.genericsResolved)
		{
			this.resolveGenerics();
		}
		if (!this.superTypesResolved)
		{
			this.resolveSuperTypes();
		}
		return super.resolveType(typeVar, concrete);
	}
	
	@Override
	public IClassMetadata getMetadata()
	{
		if (!this.metadataResolved)
		{
			this.resolveMetadata();
		}
		return this.metadata;
	}
	
	@Override
	public IMethod getFunctionalMethod()
	{
		if ((this.modifiers & Modifiers.ABSTRACT | Modifiers.INTERFACE_CLASS) == 0)
		{
			return null;
		}
		
		IMethod m;
		if (this.body != null)
		{
			m = this.body.getFunctionalMethod();
			if (m != null)
			{
				return m;
			}
		}
		
		if (!this.superTypesResolved)
		{
			this.resolveSuperTypes();
		}
		
		if (this.superType != null)
		{
			m = this.superType.getFunctionalMethod();
			if (m != null)
			{
				return m;
			}
		}
		
		for (int i = 0; i < this.interfaceCount; i++)
		{
			m = this.interfaces[i].getFunctionalMethod();
			if (m != null)
			{
				return m;
			}
		}
		
		return null;
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
	public void check(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public void foldConstants()
	{
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		if (!this.innerTypesResolved)
		{
			this.resolveInnerTypes();
		}
		
		if (this.innerTypes != null)
		{
			for (IType t : this.innerTypes)
			{
				if (t.getName() == name)
				{
					return t.getTheClass();
				}
			}
		}
		
		return null;
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		if (!this.genericsResolved)
		{
			this.resolveGenerics();
		}
		
		// Own properties
		IDataMember field = this.body.getProperty(name);
		if (field != null)
		{
			return field;
		}
		
		// Own fields
		field = this.body.getField(name);
		if (field != null)
		{
			return field;
		}
		
		if (!this.superTypesResolved)
		{
			this.resolveSuperTypes();
		}
		
		// Inherited Fields
		if (this.superType != null)
		{
			IDataMember match = this.superType.resolveField(name);
			if (match != null)
			{
				return match;
			}
		}
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		if (!this.genericsResolved)
		{
			this.resolveGenerics();
		}
		
		this.body.getMethodMatches(list, instance, name, arguments);
		
		if (!list.isEmpty())
		{
			return;
		}
		
		if (!this.superTypesResolved)
		{
			this.resolveSuperTypes();
		}
		
		if (this.superType != null)
		{
			this.superType.getMethodMatches(list, instance, name, arguments);
		}
		
		if (!list.isEmpty())
		{
			return;
		}
		
		for (int i = 0; i < this.interfaceCount; i++)
		{
			this.interfaces[i].getMethodMatches(list, instance, name, arguments);
		}
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
		if (!this.superTypesResolved)
		{
			this.resolveSuperTypes();
		}
		
		this.body.getConstructorMatches(list, arguments);
	}
	
	public boolean addSpecialMethod(String specialType, String name, IMethod method)
	{
		if ("get".equals(specialType) || "set".equals(specialType))
		{
			Name name1 = Name.getQualified(name);
			IProperty property = this.body.getProperty(name1);
			if (property == null)
			{
				Property prop = new ExternalProperty(this, name1, method.getType());
				prop.modifiers = method.getModifiers() & ~Modifiers.SYNTHETIC;
				this.body.addProperty(prop);
			}
		}
		if ("parDefault".equals(specialType))
		{
			int i = name.indexOf('$');
			Name name1 = Name.getQualified(name.substring(0, i));
			IMethod method1 = this.body.getMethod(name1);
			int parIndex = Integer.parseInt(name.substring(i + 1));
			
			MethodCall call = new MethodCall(null);
			call.method = method;
			call.name = name1;
			method1.getParameter(parIndex).setValue(call);
			return false;
		}
		return true;
	}
	
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
	{
		this.modifiers = access;
		this.internalName = name;
		
		this.body = new ClassBody(this);
		if (interfaces != null)
		{
			this.interfaces = new IType[interfaces.length];
		}
		
		int index = name.lastIndexOf('$');
		if (index == -1)
		{
			index = name.lastIndexOf('/');
		}
		if (index == -1)
		{
			this.name = Name.getQualified(name);
			this.thePackage = Package.rootPackage;
			this.fullName = name;
		}
		else
		{
			this.name = Name.getQualified(name.substring(index + 1));
			this.fullName = name.replace('/', '.');
			this.thePackage = Package.rootPackage.resolvePackage(Name.getQualified(this.fullName.substring(0, index)));
		}
		
		if (signature != null)
		{
			this.generics = new ITypeVariable[2];
			ClassFormat.readClassSignature(signature, this);
		}
		else
		{
			if (superName != null)
			{
				this.superType = ClassFormat.internalToType(superName);
			}
			else
			{
				this.superType = null;
			}
			
			if (interfaces != null)
			{
				this.interfaceCount = interfaces.length;
				this.interfaces = new IType[this.interfaceCount];
				for (int i = 0; i < this.interfaceCount; i++)
				{
					this.interfaces[i] = ClassFormat.internalToType(interfaces[i]);
				}
			}
		}
		
		this.type = new dyvil.tools.compiler.ast.type.ClassType(this);
	}
	
	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible)
	{
		TypeReference ref = new TypeReference(typeRef);
		switch (ref.getSort())
		{
		// TODO implement other sorts
		case TypeReference.CLASS_TYPE_PARAMETER:
			ITypeVariable typeVar = this.generics[ref.getTypeParameterIndex()];
			switch (desc)
			{
			case "Ldyvil/annotation/Covariant;":
				typeVar.setVariance(Variance.COVARIANT);
				break;
			case "Ldyvil/annotation/Contravariant;":
				typeVar.setVariance(Variance.CONTRAVARIANT);
				break;
			}
			// TODO implement other type parameter annotations
		}
		return null;
	}
	
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value)
	{
		Field field = new ExternalField(this);
		field.setName(Name.get(name));
		field.setModifiers(access);
		
		field.setType(ClassFormat.extendedToType(signature == null ? desc : signature));
		
		if (value != null)
		{
			field.setValue(IValue.fromObject(value));
		}
		
		if ((this.modifiers & Modifiers.OBJECT_CLASS) != 0 && name.equals("$instance"))
		{
			// This is the instance field of a singleton object class, ignore
			// annotations as it shouldn't have any
			this.metadata.setInstanceField(field);
			return null;
		}
		
		this.body.addField(field);
		return new SimpleFieldVisitor(field);
	}
	
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
	{
		Name name1 = Name.get(name);
		
		if ((this.modifiers & Modifiers.ANNOTATION) != 0)
		{
			ClassParameter param = new ClassParameter();
			param.modifiers = access;
			param.name = name1;
			param.type = ClassFormat.readReturnType(desc);
			this.addParameter(param);
			return new AnnotationClassVisitor(param);
		}
		
		if ("<init>".equals(name))
		{
			Constructor constructor = new ExternalConstructor(this);
			constructor.setModifiers(access);
			
			ClassFormat.readConstructorType(desc, constructor);
			
			if ((access & Modifiers.VARARGS) != 0)
			{
				constructor.getParameter(constructor.parameterCount() - 1).setVarargs(true);
			}
			
			this.body.addConstructor(constructor);
			
			return new SimpleMethodVisitor(constructor);
		}
		
		ExternalMethod method = new ExternalMethod(this);
		method.name = name1;
		method.modifiers = access;
		method.descriptor = desc;
		
		if (signature != null)
		{
			method.setGeneric();
			ClassFormat.readMethodType(signature, method);
		}
		else
		{
			ClassFormat.readMethodType(desc, method);
			
			if (exceptions != null)
			{
				for (String s : exceptions)
				{
					method.addException(ClassFormat.internalToType(s));
				}
			}
		}
		
		if ((access & Modifiers.VARARGS) != 0)
		{
			method.setVarargsParameter();
		}
		
		boolean flag = true;
		if ((access & Modifiers.SYNTHETIC) != 0)
		{
			int index = name.indexOf('$');
			if (index != -1)
			{
				flag = this.addSpecialMethod(name.substring(0, index), name.substring(index + 1), method);
			}
		}
		
		if (flag)
		{
			this.body.addMethod(method);
		}
		
		return new BytecodeVisitor(method);
	}
	
	public void visitInnerClass(String name, String outerName, String innerName, int access)
	{
		if (this.innerTypes == null)
		{
			this.innerTypes = new ArrayList(1);
		}
		
		IType type = ClassFormat.internalToType(name);
		this.innerTypes.add(type);
	}
}

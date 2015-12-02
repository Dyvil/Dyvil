package dyvil.tools.compiler.ast.method;

public final class ConstructorMatchList
{
	private IConstructor[] constructors = new IConstructor[4];
	private float[]        values       = new float[4];
	private int size;
	
	public int size()
	{
		return this.size;
	}
	
	public boolean isEmpty()
	{
		return this.size <= 0;
	}
	
	public void ensureCapacity(int capacity)
	{
		if (capacity <= this.constructors.length)
		{
			return;
		}
		
		IConstructor[] tempCtrs = new IConstructor[capacity];
		System.arraycopy(this.constructors, 0, tempCtrs, 0, this.size);
		this.constructors = tempCtrs;
		
		float[] tempValues = new float[capacity];
		System.arraycopy(this.values, 0, tempValues, 0, this.size);
		this.values = tempValues;
	}
	
	public void add(IConstructor method, float match)
	{
		this.ensureCapacity(this.size + 1);
		this.constructors[this.size] = method;
		this.values[this.size] = match;
		this.size++;
	}
	
	public IConstructor getBestConstructor()
	{
		switch (this.size)
		{
		case 0:
			return null;
		case 1:
			return this.constructors[0];
		}
		
		IConstructor bestCtor = this.constructors[0];
		float bestMatch = this.values[0];
		for (int i = 1; i < this.size; i++)
		{
			float match = this.values[i];
			if (match < bestMatch)
			{
				bestCtor = this.constructors[i];
				bestMatch = match;
			}
		}
		
		return bestCtor;
	}
}

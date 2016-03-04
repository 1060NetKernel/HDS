package org.netkernel.mod.hds.impl;

import org.netkernel.mod.hds.*;

public class HDSDocumentImpl implements IHDSDocument
{
	private final IHDSNode mRoot;
	private final HDSKeyManager mKeyManager;
	private int mHashCode;
	
	public HDSDocumentImpl(IHDSNode aRoot, HDSKeyManager aKeyManager)
	{	mRoot=aRoot;
		mKeyManager=aKeyManager;
		try
		{	mHashCode=recurseHash(mRoot,50);
		}
		catch (Exception e)
		{
			System.out.println("here");
		}
	}
	
	private static int recurseHash(IHDSNode aNode, int n)
	{	int hash=0;
		if (aNode.getName()!=null)
		{	hash=aNode.getName().hashCode();
		}
		if (aNode.getValue()!=null)
		{	hash^=aNode.getValue().hashCode();
		}
		for (IHDSNode child: aNode.getChildren())
		{	hash^=recurseHash(child,n-1);
		}
		return hash;
	}
	
	@Override
	public int hashCode()
	{	return mHashCode;
	}
	
	@Override
	public boolean equals(Object aOther)
	{	boolean result=this==aOther;
		if (!result && aOther instanceof HDSDocumentImpl)
		{	HDSDocumentImpl other=(HDSDocumentImpl)aOther;
			result=mHashCode==other.mHashCode;
			if (result)
			{	result=recurseEquals(mRoot,other.mRoot);
			}
		}
		return result;
	}
	
	private static boolean recurseEquals(IHDSNode n1, IHDSNode n2)
	{	boolean result=((n1.getName()==n2.getName()) || (n1.getName()!=null && n1.getName().equals(n2.getName()))) && ((n1.getValue()==n2.getValue()) || (n1.getValue()!=null && n1.getValue().equals(n2.getValue()))) && n1.getChildren().length==n2.getChildren().length;
		if (result)
		{	for (int i=0; i<n1.getChildren().length && result; i++)
			{	result=recurseEquals(n1.getChildren()[i],n2.getChildren()[i]);
			}
		}
		return result;
	}
	
	@Override
	public IHDSNode getRootNode()
	{	return mRoot;
	}

	@Override
	public IHDSMutator getMutableClone()
	{	return new HDSMutatorImpl(mRoot, true, mKeyManager);
	}

	@Override
	public IHDSReader getReader()
	{	return new HDSReaderImpl(mRoot, mKeyManager);
	}
	
	public String toString()
	{	return mRoot.toString();
	}

}

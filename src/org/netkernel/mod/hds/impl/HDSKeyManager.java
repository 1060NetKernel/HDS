package org.netkernel.mod.hds.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jxpath.BasicNodeSet;
import org.apache.commons.jxpath.ExtendedKeyManager;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.NodeSet;
import org.apache.commons.jxpath.Pointer;
import org.netkernel.layer0.util.CheapMap;
import org.netkernel.layer0.util.MultiMap;

public class HDSKeyManager implements ExtendedKeyManager
{
	private static class KeyIndex
	{
		private final String mMatch;
		private final String mUse;
		private MultiMap mIndex=null;
		private JXPathContext mContext;
		
		public KeyIndex(JXPathContext aContext, String aMatch, String aUse)
		{	mMatch=aMatch;
			mUse=aUse;
			mContext=aContext;
		}
		
		public void setDirty(JXPathContext aContext)
		{	mIndex=null;
			mContext=aContext;
		}
		
		public List<Pointer> get(Object aValue)
		{	MultiMap index=mIndex;
			if (index==null)
			{	synchronized(this)
				{	if (mIndex==null)
					{	index=new MultiMap(8, 4);
						for (Iterator i=mContext.iteratePointers(mMatch); i.hasNext(); )
						{	Pointer pointer=(Pointer)i.next();
							JXPathContext ctx=mContext.getRelativeContext(pointer);
							Object value=ctx.getValue(mUse);
							if (value!=null)
							{	index.put(value, pointer);
							}
						}
						mIndex=index;
						mContext=null;
					}
					else
					{	index=mIndex;
					}
				}
			}
			return index.get(aValue);
		}
		
		public String getMatch()
		{	return mMatch;
		}
		
		public String getUse()
		{	return mUse;
		}
	}
	
	private final Map<String,KeyIndex> mIndexByName=new HashMap<String, HDSKeyManager.KeyIndex>(4);
	
	public HDSKeyManager clone(JXPathContext aContext)
	{	HDSKeyManager km=new HDSKeyManager();
		for (Map.Entry<String, KeyIndex> entry : mIndexByName.entrySet())
		{	String key=entry.getKey();
			KeyIndex index=entry.getValue();
			km.declareKey(aContext, key, index.getMatch(), index.getUse());
		}
		return km;
	}
	
	public void setDirty(JXPathContext aContext)
	{	for (KeyIndex ki : mIndexByName.values())
		{	ki.setDirty(aContext);
		}
	}
	
	public void declareKey(JXPathContext mContext, String aName, String aMatch, String aUse)
	{	KeyIndex ki=new KeyIndex(mContext,aMatch,aUse);
		mIndexByName.put(aName, ki);
	}
	
	public void removeKey(String aName)
	{	mIndexByName.remove(aName);
	}
	
	Set<String> getDeclaredKeys()
	{	return mIndexByName.keySet();
	}
	
	@Override
	public Pointer getPointerByKey(JXPathContext context, String keyName, String keyValue)
	{	NodeSet ns=getNodeSetByKey(context,keyName,keyValue);
		List<Pointer> pointers=ns.getPointers();
		Pointer result=pointers.size()>0?pointers.get(0):null;
		return result;
	}

	@Override
	public NodeSet getNodeSetByKey(JXPathContext aContext, String aKey, Object aValue)
	{	KeyIndex ki=mIndexByName.get(aKey);
		if (ki!=null)
		{	BasicNodeSet ns= new BasicNodeSet();
			for (Pointer p : ki.get(aValue))
			{	ns.add(p);
			}
			return ns;
		}
		else
		{	throw new JXPathException("Cannot find an element by key - key "+aKey+" not found");
		}		
	}
}
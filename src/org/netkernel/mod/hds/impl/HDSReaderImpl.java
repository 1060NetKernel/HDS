package org.netkernel.mod.hds.impl;

import java.util.*;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathNotFoundException;
import org.apache.commons.jxpath.KeyManager;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;

import org.netkernel.mod.hds.*;

class HDSReaderImpl implements IHDSReader
{
	static
	{	JXPathContextReferenceImpl.addNodePointerFactory(new HDSPointerFactory());
	}
	
	private final JXPathContext mContext;
	
	public HDSReaderImpl(IHDSNode aRoot, KeyManager aKeyManager)
	{	mContext = JXPathContext.newContext(aRoot);
		if (aKeyManager!=null) mContext.setKeyManager(aKeyManager);
	}
	
	private HDSReaderImpl(JXPathContext aCtx)
	{	mContext=aCtx;
	}
	
	public boolean equals(Object aOther)
	{	return (aOther instanceof HDSReaderImpl && ((HDSReaderImpl)aOther).mContext.getContextBean()==mContext.getContextBean());
	}
	
	public int hashCode()
	{	return mContext.getContextBean().hashCode();
	}
	
	public IHDSDocument toDocument()
	{	IHDSNode node=getContextNode();
		if (node.getName()!=null || node.getValue()!=null)
		{	HDSNodeImpl root=new HDSNodeImpl(null, null);
			root.setChildren(new IHDSNode[]{node});
			node=root;
		}
		return new HDSDocumentImpl(node,HDSMutatorImpl.getKeyManager(mContext,false));
	}
	
	public String getContextXPath()
	{	return mContext.getContextPointer().asPath();
	}

	public IHDSNode getContextNode()
	{	return (IHDSNode)mContext.getContextBean();
	}
	
	public Object getFirstValue(String aXPath) throws XPathNotFoundException
	{	try
		{	return mContext.getValue(aXPath);
		}
		catch (JXPathNotFoundException e)
		{	throw new XPathNotFoundException(e.getMessage());
		}
	}
	
	public Object getFirstValueOrNull(String aXPath) throws XPathNotFoundException
	{	try
		{	return mContext.getValue(aXPath);
		}
		catch (JXPathNotFoundException e)
		{	return null;
		}
	}
	
	public List<Object> getValues(String aXPath)
	{	List<Object> result;
		Iterator<Object> i=mContext.iterate(aXPath);
		if (i.hasNext())
		{	result=new ArrayList<Object>();
			while (i.hasNext())
			{	result.add(i.next());
			}
		}
		else
		{	result=Collections.EMPTY_LIST;
		}
		return result;
	}
	
	public IHDSReader getFirstNode(String aXPath) throws XPathNotFoundException
	{	return getFirstNodeInner(aXPath,false);
	}

	public IHDSReader getFirstNodeOrNull(String aXPath)
	{	return getFirstNodeInner(aXPath,true);
	}

	private HDSReaderImpl getFirstNodeInner(String aXPath, boolean aNullWhenNone) throws XPathNotFoundException
	{	try
		{	Pointer pointer=mContext.getPointer(aXPath);
			if (pointer.getNode()==null)
			{	if (aNullWhenNone)
				{	return null;
				}
				else throw new XPathNotFoundException("xpath ["+aXPath+"] does not evaluate to node");
			}
			JXPathContext jxpctx=mContext.getRelativeContext(pointer);
			if (jxpctx.getContextBean() instanceof HDSNodeImpl)
			{	return new HDSReaderImpl(jxpctx);
			}
			else
			{	if (aNullWhenNone)
				{	return null;
				}
				else
				{	throw new XPathNotFoundException("xpath ["+aXPath+"] does not evaluate to node");
				}
			}
		}
		catch (JXPathNotFoundException e)
		{	if (aNullWhenNone)
			{	return null;
			}
			else
			{	throw new XPathNotFoundException(e.getMessage());
			}
		}
	}
	
	public List<IHDSReader> getNodes(String aXPath)
	{	return getNodesInner(aXPath);
	}

	private List getNodesInner(String aXPath)
	{	List<HDSReaderImpl> result=new ArrayList<HDSReaderImpl>();
		Object last=null;
		for (Iterator i=mContext.iteratePointers(aXPath); i.hasNext(); )
		{
			Pointer pointer=(Pointer)i.next();
			if (pointer.getNode()==last) continue;
			last=pointer.getNode();
			JXPathContext jxpctx=mContext.getRelativeContext(pointer);
			HDSReaderImpl hdsctx=new HDSReaderImpl(jxpctx);
			result.add(hdsctx);
		}
		return result;
	}
	
	public String toString()
	{	return mContext.getContextBean().toString();
	}
	
	public void declareKey(String aName, String aMatch, String aUse)
	{	HDSMutatorImpl.getKeyManager(mContext,true).declareKey(mContext, aName, aMatch, aUse);
	}
	
	public void removeKey(String aName)
	{	HDSMutatorImpl.getKeyManager(mContext,false).removeKey(aName);
	}
	
	public Set<String> getDeclaredKeys()
	{	return HDSMutatorImpl.getKeyManager(mContext,false).getDeclaredKeys();
	}
}

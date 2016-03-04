package org.netkernel.mod.hds.impl;

import java.util.*;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathNotFoundException;
import org.apache.commons.jxpath.KeyManager;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.model.NodePointer;

import org.netkernel.mod.hds.*;

public class HDSMutatorImpl implements IHDSMutator
{
	static
	{	JXPathContextReferenceImpl.addNodePointerFactory(new HDSPointerFactory());
	}
	
	private final JXPathContext mContext;
	private HDSNodePointer mCursor;
	
	public HDSMutatorImpl()
	{	this(new HDSNodeImpl(null, null),false, null);
	}
	
	public HDSMutatorImpl(IHDSNode aRoot, boolean aClone, HDSKeyManager aKeyManager)
	{	IHDSNode root;
		if (aClone)
		{	root=importHDS(aRoot);
		}
		else
		{	root=aRoot;
		}
		mContext = JXPathContext.newContext(root);
		mCursor=(HDSNodePointer)mContext.getPointer("/");
		if (aKeyManager!=null)
		{	
			if (aClone)
			{	mContext.setKeyManager(aKeyManager.clone(mContext));
			}
			else
			{	mContext.setKeyManager(aKeyManager);
			}
		}
		
	}
	
	private HDSMutatorImpl(JXPathContext aCtx)
	{	mContext=aCtx;
		mCursor=(HDSNodePointer)mContext.getPointer(".");
	}
	
	private void setDirty()
	{	rebuildKeyManager();
	}
	
	public boolean equals(Object aOther)
	{	return (aOther instanceof HDSMutatorImpl && ((HDSMutatorImpl)aOther).mContext.getContextBean()==mContext.getContextBean());
	}
	
	public int hashCode()
	{	return mContext.getContextBean().hashCode();
	}
	
	private void setRoot(IHDSNode aRoot)
	{	
		
	}
	
	public String getContextXPath()
	{	return mContext.getContextPointer().asPath();
	}

	public IHDSNode getContextNode()
	{	return (IHDSNode)mContext.getContextBean();
	}
	
	public IHDSDocument toDocument(boolean aClone)
	{
		IHDSDocument result;
		if (aClone)
		{	IHDSNode node=recurseCloneNode((IHDSNode)mContext.getContextBean());
			JXPathContext ctx=JXPathContext.newContext(node);
			HDSKeyManager oldKeyManager=getKeyManager(mContext,false);
			HDSKeyManager newKeyManager=oldKeyManager==null?null:oldKeyManager.clone(ctx);
			result=new HDSDocumentImpl(node,newKeyManager);
		}
		else
		{	IHDSNode node=(IHDSNode)mContext.getContextBean();
			if (node.getName()!=null || node.getValue()!=null)
			{	HDSNodeImpl root=new HDSNodeImpl(null, null);
				root.setChildren(new IHDSNode[]{node});
				node=root;
			}
			result=new HDSDocumentImpl(node,getKeyManager(mContext,false));
		}
		return result;
	}
	
	private IHDSNode recurseCloneNode(IHDSNode aNode)
	{	HDSNodeImpl result=new HDSNodeImpl(aNode.getName(),aNode.getValue());
		IHDSNode[] childrenIn=aNode.getChildren();
		IHDSNode[] childrenOut=new IHDSNode[childrenIn.length];
		for (int i=0; i<childrenIn.length; i++)
		{	childrenOut[i]=recurseCloneNode(childrenIn[i]);
		}
		result.setChildren(childrenOut);
		return result;
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
	
	public IHDSMutator getFirstNode(String aXPath) throws XPathNotFoundException
	{	return getFirstNodeInner(aXPath,false);
	}

	public IHDSMutator getFirstNodeOrNull(String aXPath)
	{	return getFirstNodeInner(aXPath,true);
	}

	private HDSMutatorImpl getFirstNodeInner(String aXPath, boolean aNullWhenNone) throws XPathNotFoundException
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
			{	return new HDSMutatorImpl(jxpctx);
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
	
	public List<IHDSMutator> getNodes(String aXPath)
	{	return getNodesInner(aXPath);
	}

	private List getNodesInner(String aXPath)
	{	List<HDSMutatorImpl> result=new ArrayList<HDSMutatorImpl>();
		Object last=null;
		for (Iterator i=mContext.iteratePointers(aXPath); i.hasNext(); )
		{
			Pointer pointer=(Pointer)i.next();
			if (pointer.getNode()==last) continue;
			last=pointer.getNode();
			JXPathContext jxpctx=mContext.getRelativeContext(pointer);
			HDSMutatorImpl hdsctx=new HDSMutatorImpl(jxpctx);
			result.add(hdsctx);
		}
		return result;
	}

	private static HDSNodeImpl importHDS(IHDSNode aNode)
	{	HDSNodeImpl result=new HDSNodeImpl(aNode.getName(),aNode.getValue());
		IHDSNode[] childrenIn=aNode.getChildren();
		HDSNodeImpl[] children=new HDSNodeImpl[childrenIn.length];
		for (int i=0; i<children.length; i++)
		{	children[i]=importHDS(childrenIn[i]);
		}
		result.setChildren(children);
		return result;
	}
	
	public String toString()
	{	return mContext.getContextBean().toString();
	}

	public IHDSMutator setCursor(String aXPath)
	{	Iterator i=mContext.iteratePointers(aXPath);
		if (!i.hasNext())
		{	throw new IllegalArgumentException("setCursor expected single node but found none");
		}
		else
		{	HDSNodePointer p=(HDSNodePointer)i.next();
			if (i.hasNext())
			{	throw new IllegalArgumentException("setCursor expected single node but found multiple");
			}
			else
			{	mCursor=p;
			}
		}
		return this;
	}
	
	public IHDSMutator resetCursor()
	{	mCursor=(HDSNodePointer)mContext.getPointer(".");
		return this;
	}

	public String getCursorXPath()
	{	return mCursor.asPath();
	}
	
	public IHDSMutator pushNode(String aName)
	{	setDirty();
		return pushNode(aName,null);
	}

	public IHDSMutator pushNode(String aName, Object aValue)
	{	setDirty();
		HDSNodeImpl node=new HDSNodeImpl(aName, aValue);
		HDSNodeImpl cursorNode=(HDSNodeImpl)mCursor.getNode();
		mCursor=addChild(mCursor,node,-1);
		return this;
	}
	
	public IHDSMutator popNode()
	{	NodePointer p=mCursor.getParent();
		if (p!=null)
		{	mCursor=(HDSNodePointer)p;
		}
		else
		{	throw new IllegalStateException("A pop too far");
		}
		return this;
	}
	
	public IHDSMutator addNode(String aName, Object aValue)
	{	setDirty();
		HDSNodeImpl node=new HDSNodeImpl(aName, aValue);
		addChild(mCursor,node,-1);
		return this;
	}

	private static HDSNodePointer addChild(HDSNodePointer aParent, IHDSNode aChild, int aPosition)
	{	HDSNodeImpl cursorNode=(HDSNodeImpl)aParent.getNode();
		checkForCircularity(cursorNode,aChild);
		IHDSNode[] children=cursorNode.getChildren();
		if (aPosition<0) aPosition=children.length;
		if (aPosition<0 || aPosition>children.length) throw new IndexOutOfBoundsException();
		IHDSNode[] children2=new HDSNodeImpl[children.length+1];
		if (aPosition>0)
		{	System.arraycopy(children, 0, children2, 0, aPosition);
		}
		children2[aPosition]=aChild;
		if (aPosition<children.length)
		{	System.arraycopy(children, aPosition, children2, aPosition+1, children.length-aPosition);
		}
		cursorNode.setChildren(children2);
		return new HDSNodePointer(aChild,aParent);
	}
	
	private static HDSNodePointer replaceChild(HDSNodePointer aParent, HDSNodeImpl aChild, int aPosition)
	{	HDSNodeImpl cursorNode=(HDSNodeImpl)aParent.getNode();
		checkForCircularity(cursorNode,aChild);
		IHDSNode[] children=cursorNode.getChildren();
		if (aPosition<0) aPosition=children.length;
		if (aPosition<0 || aPosition>children.length) throw new IndexOutOfBoundsException();
		HDSNodeImpl[] children2=new HDSNodeImpl[children.length];
		if (aPosition>0)
		{	System.arraycopy(children, 0, children2, 0, aPosition);
		}
		children2[aPosition]=aChild;
		if (aPosition<children.length-1)
		{	System.arraycopy(children, aPosition+1, children2, aPosition+1, children.length-aPosition-1);
		}
		cursorNode.setChildren(children2);
		return new HDSNodePointer(aChild,aParent);
	}
	
	private static void removeChild(HDSNodePointer aParent, int aPosition)
	{	HDSNodeImpl cursorNode=(HDSNodeImpl)aParent.getNode();
		IHDSNode[] children=cursorNode.getChildren();
		if (aPosition<0) aPosition=children.length;
		if (aPosition<0 || aPosition>children.length) throw new IndexOutOfBoundsException();
		HDSNodeImpl[] children2=new HDSNodeImpl[children.length-1];
		if (aPosition>0)
		{	System.arraycopy(children, 0, children2, 0, aPosition);
		}
		if (aPosition<children.length-1)
		{	System.arraycopy(children, aPosition+1, children2, aPosition, children.length-aPosition-1);
		}
		cursorNode.setChildren(children2);
	}
	
	private static void checkForCircularity(IHDSNode aParent, IHDSNode aChild)
	{	if (aChild==aParent)
		{	throw new IllegalArgumentException("mutation will cause circularity");
		}
		for (IHDSNode child : aChild.getChildren())
		{	checkForCircularity(aParent,child);
		}
	}

	public IHDSMutator append(IHDSContext aNode)
	{	setDirty();
		IHDSNode clone=importHDS(aNode.getContextNode());
		mCursor=addChild(mCursor,clone,-1);
		return this;
	}
	
	public IHDSMutator prepend(IHDSContext aNode)
	{	setDirty();
		IHDSNode clone=importHDS(aNode.getContextNode());
		mCursor=addChild(mCursor,clone,0);
		return this;
	}

	public IHDSMutator appendChildren(IHDSContext aNode)
	{	setDirty();
		IHDSNode n=importHDS(aNode.getContextNode());
		for (IHDSNode child : n.getChildren())
		{	addChild(mCursor,child,-1);
		}
		return this;
	}

	
	
	public IHDSMutator insertBefore(IHDSContext aNode)
	{	setDirty();
		HDSNodePointer parent=(HDSNodePointer)mCursor.getParent();
		if (parent==null)
		{	throw new IllegalArgumentException("cannot insert before root");
		}
		int p=indexOfChild((HDSNodeImpl)parent.getNode(),(HDSNodeImpl)mCursor.getNode());
		IHDSNode clone=importHDS(aNode.getContextNode());
		mCursor=addChild(parent,clone,p);
		return this;
	}
	
	public IHDSMutator insertAfter(IHDSContext aNode)
	{	setDirty();
		HDSNodePointer parent=(HDSNodePointer)mCursor.getParent();
		if (parent==null)
		{	throw new IllegalArgumentException("cannot insert after root");
		}
		int p=indexOfChild((HDSNodeImpl)parent.getNode(),(HDSNodeImpl)mCursor.getNode());
		IHDSNode clone=importHDS(aNode.getContextNode());
		mCursor=addChild(parent,clone,p+1);
		return this;
	}
	
	private static int indexOfChild(HDSNodeImpl aParent, HDSNodeImpl aChild)
	{	int p=0;
		for (IHDSNode child : aParent.getChildren())
		{	if (child==aChild)
			{	return p;
			}
			p++;
		}
		throw new IllegalArgumentException("not a child");
	}
	
	public IHDSMutator rename(String aName)
	{	setDirty();
		HDSNodeImpl node=(HDSNodeImpl)mCursor.getNode();
		HDSNodeImpl newNode=new HDSNodeImpl(aName, node.getValue());
		newNode.setChildren(Arrays.copyOf(node.getChildren(),node.getChildren().length));
		HDSNodePointer parent=(HDSNodePointer)mCursor.getParent();
		if (parent!=null)
		{ innerReplace(newNode);
		}
		return this;
	}
	
	public IHDSMutator setValue(Object aValue)
	{	setDirty();
		HDSNodeImpl node=(HDSNodeImpl)mCursor.getNode();
		HDSNodeImpl newNode=new HDSNodeImpl(node.getName(), aValue);
		newNode.setChildren(node.getChildren());
		//newNode.setChildren(Arrays.copyOf(node.getChildren(),node.getChildren().length));
		HDSNodePointer parent=(HDSNodePointer)mCursor.getParent();
		if (parent!=null)
		{ innerReplace(newNode);
		}
		return this;
	}
	
	public IHDSMutator replace(IHDSContext aNode)
	{	HDSNodeImpl clone=importHDS(aNode.getContextNode());
		innerReplace(clone);
		return this;
	}
		
	private IHDSMutator innerReplace(HDSNodeImpl aNode)
	{	HDSNodePointer parent=(HDSNodePointer)mCursor.getParent();
		if (parent!=null)
		{	int p=indexOfChild((HDSNodeImpl)parent.getNode(),(HDSNodeImpl)mCursor.getNode());
			mCursor=replaceChild(parent,aNode,p);
		}
		else
		{	setRoot(aNode);
		}
		return this;
	}
	
	public IHDSMutator delete()
	{	setDirty();
		HDSNodePointer parent=(HDSNodePointer)mCursor.getParent();
		if (parent!=null)
		{	int p=indexOfChild((HDSNodeImpl)parent.getNode(),(HDSNodeImpl)mCursor.getNode());
			removeChild(parent,p);
			mCursor=parent;
		}
		else
		{	throw new IllegalArgumentException("cannot delete root");
		}
		return this;
	}
	
	public IHDSMutator createIfNotExists(String aXPath)
	{	setDirty();
		if (aXPath.startsWith("/"))
		{	throw new IllegalArgumentException("XPath must be relative");
		}
		StringTokenizer st=new StringTokenizer(aXPath,"/");
		IHDSNode node=	(IHDSNode)mCursor.getNode();
		while (st.hasMoreTokens())
		{	String token=st.nextToken();
			IHDSNode nextChild=null;
			for (IHDSNode child: node.getChildren())
			{	if (child.getName().equals(token))
				{	nextChild=child;
					break;
				}
			}
			if (nextChild!=null)
			{	node=nextChild;
				mCursor=new HDSNodePointer(nextChild,mCursor);
			}
			else
			{	node=new HDSNodeImpl(token, null);
				mCursor=addChild(mCursor,node,-1);
			}
		}
		return this;
	}
	
	static HDSKeyManager getKeyManager(JXPathContext aContext, boolean aCreate)
	{	HDSKeyManager km=(HDSKeyManager)aContext.getKeyManager();
		if (km==null && aCreate)
		{	km=new HDSKeyManager();
			aContext.setKeyManager(km);
		}
		return km;
	}
	
	private void rebuildKeyManager()
	{	HDSKeyManager km=(HDSKeyManager)mContext.getKeyManager();
		if (km!=null)
		{	km.setDirty(mContext);
		}
	}
	
	public void declareKey(String aName, String aMatch, String aUse)
	{	getKeyManager(mContext,true).declareKey(mContext, aName, aMatch, aUse);
	}
	
	public void removeKey(String aName)
	{	HDSKeyManager km=getKeyManager(mContext,false);
		if (km!=null) km.removeKey(aName);
	}
	
	public Set<String> getDeclaredKeys()
	{	HDSKeyManager km=getKeyManager(mContext,false);
		if (km!=null)
		{	return km.getDeclaredKeys();
		}
		else
		{	return Collections.EMPTY_SET;
		}
	}
	
}

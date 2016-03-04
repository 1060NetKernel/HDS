package org.netkernel.mod.hds;

import java.util.List;
import java.util.Set;

/** A wrapper around a HDS document with full XPath based querying, mutation
 * and builder functionality. Similar to IXDA in core.xml but for HDS. Uses
 * Apache JXPath internally. This is not a representation as it is mutable -
 * please use toHDS() or toHDS2() to obtain a IHDSNode representation.
 * @author tab
 *
 */
public interface IHDSMutator extends IHDSContext
{	
	/**
	 * @param aClone set to true if you want to continue to mutate and this
	 * method will return a clone rather the underlying data structure. Setting
	 * to true will give better performance. 
	 * @return immutable representation of HDS document from the root of this
	 * mutator 
	 */
	IHDSDocument toDocument(boolean aClone);
	
	/** evaluate the given XPath expression relative to this context and return the first value (if multiple)
	 * @param aXPath
	 * @return result of xpath evaluation
	 * @exception XPathNotFoundException thrown if expression evaluates to node and no node is found
	 */
	Object getFirstValue(String aXPath) throws XPathNotFoundException;
	
	/** evaluate the given XPath expression relative to this context and return the first value (if multiple)
	 * @param aXPath
	 * @return result of xpath evaluation or null if no value found
	 */
	Object getFirstValueOrNull(String aXPath);
	
	/** evaluate the given XPath expression relative to this context and return all the results in an array
	 * @param aXPath
	 * @return an array of results from the xpath expression, if no results the array will be of zero length
	 */
	List<Object> getValues(String aXPath);
	
	/** evaluate the given XPath expression relative to this context and return an offset HDS context.
	 * Relative XPath expressions on returned context will evaluate relative to the found node. 
	 * @param aXPath
	 * @return a relative IHDSContext
	 * @exception XPathNotFoundException thrown if expression evaluates to node and no node is found
	 */
	IHDSMutator getFirstNode(String aXPath) throws XPathNotFoundException;
	
	/** evaluate the given XPath expression relative to this context and return an offset HDS context.
	 * Relative XPath expressions on returned context will evaluate relative to the found node. 
	 * @param aXPath
	 * @return a relative IHDSContext or null if no node found
	 */
	IHDSMutator getFirstNodeOrNull(String aXPath);

	/** evaluate the given XPath expression relative to this context and return a list of offset HDS contexts
	 * @param aXPath
	 * @return a list of relative IHDSContext, this will be empty if no nodes found
	 */
	List<IHDSMutator> getNodes(String aXPath);
	
	/** Set the mutation cursor. All mutation methods act on the cursor node. The cursor will
	 * initially be on the origin node of the context.
	 * @param aXPath
	 */
	IHDSMutator setCursor(String aXPath);
	
	/** Reset cursor back to origin node of context
	 */
	IHDSMutator resetCursor();
	
	/** Return a canonical XPath to the cursor  
	 * @return
	 */
	String getCursorXPath();
	
	/** Add a new child node (as the last child) to at the cursor location and move the cursor to this new node
	 * @param aName name of the new node
	 * @return this context
	 */
	IHDSMutator pushNode(String aName);
	
	/** Add a new child node (as the last child) to at the cursor location and move the cursor to this new node
	 * @param aName name of the new node
	 * @param aValue value of the new node
	 * @return this context
	 */
	IHDSMutator pushNode(String aName, Object aValue);
	
	/** Move the cursor to the parent of the current cursor
	 * @return this context
	 */
	IHDSMutator popNode();
	
	/** Add a new child node (as the last child) to the cursor location without moving cursor
	 * @param aName name of the new node
	 * @param aValue value of the new node
	 * @return this context
	 */
	IHDSMutator addNode(String aName, Object aValue);
	
	/** Import the given node and add as the last child of the cursor location and move the cursor to this imported node
	 * @param aNode node to import
	 * @return this context
	 */
	IHDSMutator append(IHDSContext aNode);
	
	/** Import the given node and add as the first child of the cursor location and move the cursor to this imported node
	 * @param aNode node to import
	 * @return this context
	 */
	IHDSMutator prepend(IHDSContext aNode);

	/** Import the children of the given node and add them in order as the last
	 *  child of the cursor location. The cursor remains unmoved.
	 * @param aNode node to import
	 * @return this context
	 */
	IHDSMutator appendChildren(IHDSContext aNode);
	
	/** Import the given node and add it to the parent of the cursor location directly before the cursor. Move the cursor to this imported node.
	 * @param aNode node to import
	 * @return this context
	 */
	IHDSMutator insertBefore(IHDSContext aNode);

	/** Import the given node and add it to the parent of the cursor location directly after the cursor. Move the cursor to this imported node.
	 * @param aNode node to import
	 * @return this context
	 */
	IHDSMutator insertAfter(IHDSContext aNode);
	
	/** Import the given node and add it to the parent of the cursor location replacing the cursor. Move the cursor to this imported node.
	 * @param aNode node to import
	 * @return this context
	 */	
	IHDSMutator replace(IHDSContext aNode);
	
	/** Rename the node at the cursor location
	 * @param aName the new name
	 * @return this context
	 */
	IHDSMutator rename(String aName);
	
	/** Change the value of the node at the cursor location
	 * @param aValue the new value
	 * @return this context
	 */
	IHDSMutator setValue(Object aValue);
	
	/** Delete the node at the cursor location and move the cursor to the parent node
	 * @return this context
	 */
	IHDSMutator delete();
	
	/** Create a chain of one or more nodes below the cursor if they do not exist then move the
	 * cursor to the end of the chain.
	 * @param aXPath a simple slash delimited list of node names
	 * @return this context
	 */
	IHDSMutator createIfNotExists(String aXPath);
	
	
	
	
}

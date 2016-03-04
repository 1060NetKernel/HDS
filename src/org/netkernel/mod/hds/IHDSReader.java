package org.netkernel.mod.hds;

import java.util.List;

/** A wrapper around a HDS document with full XPath based querying.
 * Similar to IXDAReadOnly in core.xml but for HDS. Uses
 * Apache JXPath internally. This is not a representation as it is mutable -
 * please use toHDS() or toHDS2() to obtain a IHDSNode representation.
 * @author tab
 */
public interface IHDSReader extends IHDSContext
{	
	/**
	 * @return immutable representation of HDS document from the root of this
	 * reader 
	 */
	IHDSDocument toDocument();

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
	IHDSReader getFirstNode(String aXPath) throws XPathNotFoundException;
	
	/** evaluate the given XPath expression relative to this context and return an offset HDS context.
	 * Relative XPath expressions on returned context will evaluate relative to the found node.
	 * @param aXPath
	 * @return a relative IHDSContext or null if no node is found
	 */
	IHDSReader getFirstNodeOrNull(String aXPath);

	/** evaluate the given XPath expression relative to this context and return a list of offset HDS contexts
	 * @param aXPath
	 * @return a list of relative IHDSContext, this will be empty if no nodes found
	 */
	List<IHDSReader> getNodes(String aXPath);
}

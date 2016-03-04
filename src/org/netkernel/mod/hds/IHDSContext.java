package org.netkernel.mod.hds;

import java.util.Set;

public interface IHDSContext
{
	/**
	 * @return human readable debug representation of this context
	 */
	String toString();

	/** @return the context node
	 */  
	IHDSNode getContextNode();

	/** @return a canonical XPath to the context node
	 */  
	String getContextXPath();
	
	/** Declare a key for use by the XPath key() function
	 * @param aName name of the key, declaring a key that exists will replace existing. 
	 * @param aMatch XPath expression for nodes that will be indexed.
	 * @param aUse relative XPath expression from matched nodes that will be used for the key
	 */
	void declareKey(String aName, String aMatch, String aUse);
	
	/** Remove an existing key
	 * @param aName name of key to remove
	 */
	void removeKey(String aName);
	
	/**
	 * @return Set of all declared keys
	 */
	Set<String> getDeclaredKeys();
}

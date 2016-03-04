package org.netkernel.mod.hds.accessor;

import java.util.List;

import org.netkernel.layer0.meta.impl.SourcedArgumentMetaImpl;
import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.INKFResponse;
import org.netkernel.layer0.nkf.NKFException;
import org.netkernel.mod.hds.IHDSDocument;
import org.netkernel.mod.hds.IHDSReader;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

public class HDSFragmentor extends StandardAccessorImpl
{
	public HDSFragmentor()
	{	this.declareThreadSafe();
		this.declareArgument(new SourcedArgumentMetaImpl("operand", "document to extract fragment from", null, new Class[]{IHDSDocument.class}));
		this.declareArgument(new SourcedArgumentMetaImpl("xpath", "well formed xpath string", null, new Class[]{String.class}));
		this.declareSourceRepresentation(IHDSDocument.class);
	}
	
	public void onSource(INKFRequestContext aContext) throws Exception
	{	IHDSReader operand=aContext.source("arg:operand",IHDSDocument.class).getReader();
		String xpath=aContext.source("arg:xpath",String.class);
		List<IHDSReader> results=operand.getNodes(xpath);
		if (results.size()!=1)
		{	throw new NKFException("Expected single fragment", "found "+results.size());
		}
		INKFResponse resp=aContext.createResponseFrom(results.get(0).toDocument());
	}
}
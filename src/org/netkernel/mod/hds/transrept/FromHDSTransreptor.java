package org.netkernel.mod.hds.transrept;

import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.layer0.nkf.INKFResponse;
import org.netkernel.layer0.nkf.INKFResponseReadOnly;
import org.netkernel.layer0.representation.IReadableBinaryStreamRepresentation;
import org.netkernel.layer0.util.XMLUtils;
import org.netkernel.mod.hds.IHDSDocument;
import org.netkernel.module.standard.endpoint.StandardTransreptorImpl;
import org.w3c.dom.Document;

public class FromHDSTransreptor extends StandardTransreptorImpl
{
	public FromHDSTransreptor()
	{	this.declareThreadSafe();
		this.declareFromRepresentation(IHDSDocument.class);

		this.declareToRepresentation(IReadableBinaryStreamRepresentation.class);
		this.declareToRepresentation(String.class);
		this.declareToRepresentation(Document.class);
		this.declareToRepresentation(org.netkernel.layer0.representation.IHDSNode.class);
	}
	
	@Override
	public void onTransrept(INKFRequestContext aContext) throws Exception
	{
		INKFResponseReadOnly primaryResponse = aContext.getThisRequest().getPrimaryAsResponse();
		IHDSDocument primary=(IHDSDocument)primaryResponse.getRepresentation();
		
		Class repClass=aContext.getThisRequest().getRepresentationClass();
		Object rep=null;
		if (repClass.isAssignableFrom(Document.class))
		{	rep=HDSConversions.toDOM(primary,false);
		}
		else if (repClass.isAssignableFrom(IReadableBinaryStreamRepresentation.class))
		{	//Document d=HDSConversions.toDOM(primary,false);
			//rep=XMLUtils.toXML(d, false, false, "UTF-8");
			rep=(IReadableBinaryStreamRepresentation)HDSConversions.serializeHDS(primary, IReadableBinaryStreamRepresentation.class);
		}
		else if (repClass.isAssignableFrom(String.class))
		{	//Document d=HDSConversions.toDOM(primary,false);
			//rep=XMLUtils.toXML(d, false, false);
			rep=(String)HDSConversions.serializeHDS(primary, String.class);
		}
		else if (repClass.isAssignableFrom(org.netkernel.layer0.representation.IHDSNode.class))
		{	rep=HDSConversions.convertToHDS1(primary);
		}
		
		INKFResponse responseOut=aContext.createResponseFrom(rep);
		if(primaryResponse.getMimeType().equals(INKFResponseReadOnly.MIME_UNKNOWN))
		{	responseOut.setMimeType("application/xml");
		}		
	}
	
	
}

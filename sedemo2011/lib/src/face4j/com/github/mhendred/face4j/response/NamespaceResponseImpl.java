package com.github.mhendred.face4j.response;

import static com.github.mhendred.face4j.response.ResponseHelper.toNamespaceList;

import java.util.List;

import org.json.JSONException;

import com.github.mhendred.face4j.exception.FaceClientException;
import com.github.mhendred.face4j.model.Namespace;


public class NamespaceResponseImpl extends AbstractResponse implements NamespaceResponse
{	
	private final List<Namespace> namespaces;
	
	public NamespaceResponseImpl(String json) throws FaceClientException
	{
		super(json);
		
		try
		{
			namespaces = toNamespaceList(response.getJSONArray("namespaces"));
		}
		
		catch (JSONException jex)
		{
			logger.error("Error: ", jex);
			throw new FaceClientException(jex);
		}
	}
	
	/* (non-Javadoc)
	 * @see face4j.response.NamespaceResponse#getNamespaces()
	 */
	public List<Namespace> getNamespaces()
	{
		return namespaces;
	}
}
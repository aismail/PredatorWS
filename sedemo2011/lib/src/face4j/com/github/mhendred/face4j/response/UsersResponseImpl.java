package com.github.mhendred.face4j.response;

import static com.github.mhendred.face4j.response.ResponseHelper.toStringList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.github.mhendred.face4j.exception.FaceClientException;


public class UsersResponseImpl extends AbstractResponse implements UsersResponse
{
	private final Map<String, List<String>> userNamespaceMap;
	
	public UsersResponseImpl(String json, String namespaces) throws FaceClientException
	{
		super(json);
		
		try
		{
			userNamespaceMap = new HashMap<String, List<String>>();
			JSONObject jObj = response.getJSONObject("users");
		
			for (String namespace : namespaces.split(","))
			{
				userNamespaceMap.put(namespace, 
						toStringList(jObj.getJSONArray(namespace)));
			}
		}
		
		catch (JSONException jex)
		{
			logger.error("Error: ", jex);
			throw new FaceClientException(jex);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see face4j.response.UsersResponse#getUsers(java.lang.String)
	 */
	public List<String> getUsers(String namespace)
	{
		return userNamespaceMap.get(namespace);
	}
	
	public String toString()
	{
		return userNamespaceMap.toString();
	}
}
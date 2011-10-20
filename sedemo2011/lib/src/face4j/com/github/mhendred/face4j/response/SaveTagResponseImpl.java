package com.github.mhendred.face4j.response;

import static com.github.mhendred.face4j.response.ResponseHelper.toSavedTagList;

import java.util.List;

import org.json.JSONException;

import com.github.mhendred.face4j.exception.FaceClientException;
import com.github.mhendred.face4j.model.SavedTag;


public class SaveTagResponseImpl extends AbstractResponse implements SaveTagResponse
{	
	private final List<SavedTag> tags;

	public SaveTagResponseImpl(String json) throws FaceClientException
	{
		super (json);
		
		try
		{
			tags = toSavedTagList (response.getJSONArray("saved_tags"));
		}
		
		catch (JSONException jex)
		{
			logger.error("Error getting saved_tags: " + jex.getMessage(), jex);
			throw new FaceClientException(jex);
		}
	}

	@Override
	public List<SavedTag> getSavedTags ()
	{
		return tags;
	}
	
	@Override
	public String toString ()
	{
		return super.toString();
	}
}
package com.github.mhendred.face4j.response;

import static com.github.mhendred.face4j.response.ResponseHelper.toPhotoList;

import java.util.List;

import org.json.JSONException;

import com.github.mhendred.face4j.exception.FaceClientException;
import com.github.mhendred.face4j.model.Photo;

public class GetTagsResponseImpl extends AbstractResponse implements GetTagsResponse
{
	private final List<Photo> photos;
	
	public GetTagsResponseImpl(final String json) throws FaceClientException
	{
		super(json);

		try
		{
			photos = toPhotoList(response.getJSONArray("photos"));
		}
		
		catch (JSONException jex)
		{
			logger.error("Error getting photos: " + jex.getMessage(), jex);
			throw new FaceClientException(jex);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.github.mhendred.face4j.response.GetTagsResponse#getPhotos()
	 */
	public List<Photo> getPhotos ()
	{
		return photos;
	}
	
	public String toString ()
	{
		return super.toString();
	}
}
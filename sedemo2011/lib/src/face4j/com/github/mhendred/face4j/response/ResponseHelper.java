package com.github.mhendred.face4j.response;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.mhendred.face4j.model.Group;
import com.github.mhendred.face4j.model.Namespace;
import com.github.mhendred.face4j.model.Photo;
import com.github.mhendred.face4j.model.RemovedTag;
import com.github.mhendred.face4j.model.SavedTag;
import com.github.mhendred.face4j.model.UserStatus;


/**
 * This class contains static methods to convert JSON
 * responses into model classes. This could be done using
 * reflection but isn't for performance reasons.
 * 
 * @author mhendred
 *
 */
final class ResponseHelper
{
	// No Instances
	private ResponseHelper ()
	{
		throw new AssertionError();
	}
	
	static List<SavedTag> toSavedTagList (final JSONArray jArr) throws JSONException
	{
		final List<SavedTag> savedTags = new ArrayList<SavedTag>();
		
		if (jArr != null)
		{
			for (int i = 0; i < jArr.length(); i++)
			{
				savedTags.add(new SavedTag(jArr.getJSONObject(i)));
			}
		}
		
		return savedTags;
	}
	
	static List<Photo> toPhotoList (JSONArray jArr) throws JSONException
	{
		final List<Photo> photos = new ArrayList<Photo>();

		for (int i = 0; i < jArr.length(); i++)
		{
			photos.add(new Photo(jArr.getJSONObject(i)));
		}
		
		return photos;
	}
	
	static List<UserStatus> toUserStatusList (JSONArray jArr) throws JSONException
	{
		final List<UserStatus> status = new LinkedList<UserStatus>();
		
		if (jArr != null) 
		{
			for (int i = 0; i < jArr.length(); i++)
			{
				status.add(new UserStatus(jArr.getJSONObject(i)));
			}
		}
		
		return status;
	}
	
	static List<RemovedTag> toRemovedTagList (JSONArray jArr) throws JSONException
	{
		final List<RemovedTag> removedTags = new LinkedList<RemovedTag>();
		
		for (int i = 0; i < jArr.length(); i++)
		{
			removedTags.add(new RemovedTag(jArr.getJSONObject(i)));
		}
		
		return removedTags;
	}
	
	static List<Group> toGroupList (JSONArray jArr) throws JSONException
	{
		final List<Group> groups = new ArrayList<Group>();
		
		for (int i = 0; i < jArr.length(); i++)
		{
			groups.add(new Group(jArr.getJSONObject(i)));
		}
		
		return groups;
	}
	
	static List<String> toStringList (JSONArray jArr) throws JSONException
	{
		final List<String> strings = new ArrayList<String>();
		
		for (int i = 0; i < jArr.length(); i++)
		{
			strings.add(jArr.getString(i));
		}
		
		return strings;
	}
	
	static List<Namespace> toNamespaceList (JSONArray jArr) throws JSONException
	{
		final List<Namespace> namespaces = new ArrayList<Namespace>();
		
		for (int i = 0; i < jArr.length(); i++)
		{
			namespaces.add(new Namespace(jArr.getJSONObject(i)));
		}
		
		return namespaces;
	}
	
	static int optInt (JSONObject jObj, String name) throws JSONException
	{
		return jObj.isNull(name) ? -1 : jObj.getInt(name);
	}
}
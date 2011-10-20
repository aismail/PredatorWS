package com.github.mhendred.face4j.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Namespace
{
	private final String shareMode;

	private final String name;
	
	private final boolean isOwner;

	private final int size;
	
	public Namespace(JSONObject jObj) throws JSONException
	{
		shareMode = jObj.getString("share_mode");
		isOwner   = jObj.getBoolean("owner");
		size	  = jObj.getInt("size"); 
		name 	  = jObj.getString("name");	
	}

	public String getShareMode ()
	{
		return shareMode;
	}

	public String getName ()
	{
		return name;
	}

	public boolean isOwner ()
	{
		return isOwner;
	}

	public int getSize ()
	{
		return size;
	}

	@Override
	public String toString ()
	{
		StringBuilder builder = new StringBuilder();
		
		builder.append("Namespace [isOwner=").append(isOwner)
			   .append(", name=").append(name)
			   .append(", shareMode=").append(shareMode)
			   .append(", size=").append(size).append("]");
		
		return builder.toString();
	}
	
}

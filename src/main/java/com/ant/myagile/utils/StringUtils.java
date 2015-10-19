package com.ant.myagile.utils;

import java.util.ArrayList;
import java.util.List;

public final class StringUtils 
{
    
    public static String parseListToString(List<Long> input)
    {
	String result = ""; 
	
	if (input != null && input.size() > 0)
	{
	    for (Long item : input) 
	    {
		if (result.isEmpty())
		{
		    result = "" + item.longValue();
		}else {
		    result += "," + item.longValue();
		}
	    }
	}
	
	return result;
    }
    
    public static List<Long> parseStringToList(String input)
    {
	List<Long> result = new ArrayList<Long>();
	
	if (input != null && input.trim().isEmpty() == false)
	{
	    if (input.contains(","))
	    {
		String[] arrIds = input.split(",");
		for (int i = 0; i < arrIds.length; i++)
		{
		    result.add(Long.parseLong(arrIds[i]));
		}
	    } else {
		result.add(Long.parseLong(input));
	    }
	}
	
	return result;
	
    }

}

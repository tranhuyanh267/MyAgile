package com.ant.myagile.utils;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class StringUtilsTest {
    String stringTest = "1234,1235,1236,1237,1238";
    List<Long> listTest = new ArrayList<Long>();

    @Before
    public void setUp() throws Exception 
    {
	listTest.add(Long.parseLong("1234"));
	listTest.add(Long.parseLong("1235"));
	listTest.add(Long.parseLong("1236"));
	listTest.add(Long.parseLong("1237"));
	listTest.add(Long.parseLong("1238"));
    }

    @Test
    public void parseListToStringTest() {

	String result = StringUtils.parseListToString(listTest);
	assertTrue(result == stringTest);
    }
    
    @Test
    public void parseStringToListTest() {

	List<Long> result = StringUtils.parseStringToList(stringTest);
	assertTrue(result.size() == listTest.size());
    }

}

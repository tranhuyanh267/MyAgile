package com.ant.myagile.utils;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:test-context.xml")
public class UtilsTest {
	@Autowired
	Utils util;
	
	public UtilsTest() {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testBelongToPriorityValues() {
		String[] values = { "MUST", "SHOULD", "COULD", "WONT", "NONE", "should", "ShoUlD", "Should", "must" };
		for (String str : values) {
			assertTrue(util.isBelongToUserStoryPriorityValues(str));
		}
	}
	
	@Test
	public void testNotBelongToPriorityValues() {
		String[] values = { null, "", "123", "@#$", "TEST" };
		for (String str : values) {
			assertTrue(!util.isBelongToUserStoryPriorityValues(str));
		}
	}
	
	@Test
	public void testIsValueOfRange(){
		String[] values ={"0", "10", "99", "100"};
		for (String str : values) {
			assertTrue(util.isValueOfRange(str, 0, 100));
		}
	}
	
	@Test
	public void testIsNotValueOfRange() {
		String[] values = { null, "-123", "@#$", "test", "101", "145", "-1",""};
		for (String str : values) {
			assertTrue(!util.isValueOfRange(str, 0, 100));
		}
	}
	
	@Test
	public void testIsExistTooLongWord(){
		String[] values = {"pneumonoultramicroscopicsilicovolcanoconiosis1",
							"pneumonoultramicroscopicsilicovolcanoconiosis11 jkjlkj lkjkljlk lkjlkjlk", 
							"mnmnm      pneumonoultramicroscopicsilicovolcanoconiosis11     jkjlkj lkjkljlk"};
		for (String str : values) {
			assertTrue(util.isExistTooLongWord(str));
		}
	}
	
	@Test
	public void testIsNotExistTooLongWord(){
		String[] values = {null, "",
							"hello", 
							"pneumonoultramicroscopicsilicovolcanoconiosis",
							"                                                                                          "};
		for (String str : values) {
			assertTrue(!util.isExistTooLongWord(str));
		}
	}
}

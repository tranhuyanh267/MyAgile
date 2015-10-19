package com.ant.myagile.managedbean;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("timeBean")
@Scope("request")
public class TimeBean {
	public String getCurrentYear(){
		SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
		Date currentDate = new Date();
		return yearFormat.format(currentDate);
	}
}

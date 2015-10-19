package com.ant.myagile.job;

/**
 * 
 * @author tntsu
 * 
 */
public class SkypeStatus {
    private int statusNum;

    public SkypeStatus() {
	this.statusNum = 0;
    }

    public String getStatusType() {
	switch (statusNum) {
	case 0:
	    return "unknown";
	case 1:
	    return "offline";
	case 2:
	    return "online";
	case 3:
	    return "away";
	case 4:
	    return "notavailable";
	case 5:
	    return "busy";
	case 6:
	    return "invisible";
	case 7:
	    return "skypeme";
	}
	return "unknown";
    }

    public int getStatusNum() {
	return statusNum;
    }

    public void setStatusNum(int statusNum) {

	this.statusNum = statusNum;
    }

}

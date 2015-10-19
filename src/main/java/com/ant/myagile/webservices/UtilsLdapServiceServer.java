package com.ant.myagile.webservices;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.ant.myagile.model.Member;
import com.ant.myagile.service.MemberService;
import com.ant.myagile.utils.SpringContext;
import com.ant.myagile.utils.Utils;
import com.ant.myagile.utils.ActiveDirectory.Encrypt;

@Path("LdapService")
public class UtilsLdapServiceServer {
	 
      /**
       * Check exist ldap username in database
     * @param lDapUsername
     * @return
     */
      @Path("/CheckExistedLdapUserName/{lDapUsername}")
      @GET
      @Produces(MediaType.APPLICATION_JSON)
      public String checkExistedLdapUsername(@PathParam("lDapUsername") String lDapUsername){
    	  	MemberService memberService = (MemberService) SpringContext.getApplicationContext().getBean(MemberService.class);
    	  	String flagExist = "true";
			Member member = memberService.findMemberByLDapUsername(lDapUsername);
			if(member == null){
				flagExist = "false";
			}
			return flagExist; 
      }
      
      
      /**
       * Return timestamp to make a token, use to compare token with local
     * @return
     * @throws Exception
     */
    @Path("/GetTimeStamp")
      @GET
      @Produces(MediaType.APPLICATION_JSON)
      public String getTimeStamp() throws Exception{
    	  String encode = Encrypt.encryptString(Utils.getTimeStamp());
    	  return encode;
      }
      
      /**
       * Update Ldap Username to the current member when the current member have not Ldap Username
     * @param email
     * @param lDapUsername
     * @return
     * @throws Exception 
     */
      @Path("/UpdateLdapAccount/{email}/{lDapUsername}/{loginToken}")
      @GET
      @Produces(MediaType.APPLICATION_JSON)
      public String updateLdapAccount(@PathParam("email") String email, @PathParam("lDapUsername") String lDapUsername, @PathParam("loginToken") String loginToken) throws Exception{
    	  MemberService memberService = (MemberService) SpringContext.getApplicationContext().getBean(MemberService.class);
    	  Utils utils = (Utils) SpringContext.getApplicationContext().getBean(Utils.class);
    	  Member memberEmail = memberService.findMemberByEmail(email);
    	  boolean flag = false;
    	  if(memberEmail != null){
    		  if(memberEmail.getlDapUsername().equals("")){
    			  String password = Encrypt.decryptString(loginToken.trim());
    			  if(utils.encodePassword(password.trim()).equals(memberEmail.getPassword())){
		    		  memberEmail.setlDapUsername(lDapUsername);
		    		  flag = memberService.update(memberEmail);
    			  }
    		  }
    	  }
    	  return String.valueOf(flag);
      }
}

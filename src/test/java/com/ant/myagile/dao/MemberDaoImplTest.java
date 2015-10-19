package com.ant.myagile.dao;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextLoader;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import com.ant.myagile.model.Member;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.TeamMember;

@ContextConfiguration(locations = {"classpath:test-context.xml"}, loader = ContextLoader.class)
public class MemberDaoImplTest extends AbstractTransactionalJUnit4SpringContextTests {
	@Autowired
	private MemberDao memberDao;
	
	@Autowired
	private TeamDao teamDao;
	
	
	@Test
	public void TestFindDevelopmentMembersByTeamId() {
		long teamId = 1;
		Team team = teamDao.findTeamByTeamId(teamId);
		assertEquals(Double.valueOf(team.getTeamId()), Double.valueOf(teamId));
		
		List<Member> members = memberDao.findDevelopmentMembersByTeamId(teamId);
		assertNotNull(members);
		assertTrue((members.size() >= 1));

		Member member = members.get(0);
		assertNotNull(member);
		assertEquals(member.getFirstName(), "Huy");
		assertEquals(member.getTeamMembers().get(0).getPosition(), TeamMember.SCRUM_MASTER);
		assertEquals(member.getTeamMembers().get(0).getIsAccepted(), true);
		assertEquals(member.isActive(), true);
	}

    @Test
    public void testFindMemberInTheSameTeamsByMemberId() {
        long memberId = 11;
        List<Member> inTeamMembers = memberDao.findMembersInTheSameTeamByMemberId(memberId);
        assertEquals(3, inTeamMembers.size());
    }

    @Test
    public void testFindMembersRelatedToMemberId() {
        long memberId = 11;
        List<Member> inTeamMembers = memberDao.findMembersRelatedToMemberId(memberId);
        assertEquals(0, inTeamMembers.size());
    }
    
    @Test
    public void testCheckExistMember(){
    	String emailExist = "sebastian.sussmann1@axonactive.vn";
    	String emailNotExist = "sebastian@axonactive.vn";
    	int flag = 0;
    	//test member exist
    	Member member =memberDao.findMemberByEmail(emailExist); 
    	if(member != null){
    		flag = 1;
    	}
    	assertEquals(1,flag);
    	
    	//test member not exist
    	member =memberDao.findMemberByEmail(emailNotExist); 
    	if(member != null){
    		flag = 1;
    	}else{
    		flag = 0;
    	}
    	assertNotEquals(1,flag);
    }
    
    
}

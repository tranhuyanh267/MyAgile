package com.ant.myagile.utils;

import java.text.SimpleDateFormat;
import java.util.List;

import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Team;
import com.ant.myagile.service.MemberService;
import com.ant.myagile.service.SprintService;
import com.ant.myagile.service.TeamService;
import com.ant.myagile.utils.SpringContext;
import com.google.gson.JsonObject;

public class WebServicesUtils {
	static MemberService memberService = SpringContext.getApplicationContext().getBean(MemberService.class);
	static TeamService teamService = SpringContext.getApplicationContext().getBean(TeamService.class);
	static SprintService sprintService = SpringContext.getApplicationContext().getBean(SprintService.class);
	public static final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");

	public static JsonObject getTeamByUsername(String username) {
		JsonObject response = new JsonObject();
		List<Team> listTeam = teamService.findTeamsByMemberAndOwner(memberService.findMemberByEmail(username));
		for (Team team : listTeam) {
			JsonObject jsonTeam = new JsonObject();
			jsonTeam.addProperty("teamId", team.getTeamId());
			jsonTeam.addProperty("teamName", team.getTeamName());
			jsonTeam.addProperty("email", team.getMailGroup());
			response.add(listTeam.indexOf(team) + "", jsonTeam);
		}
		return response;
	}

	public static JsonObject getSprintByTeam(String username) {
		List<Team> teams = teamService.findTeamsByMemberAndOwner(memberService.findMemberByEmail(username));
		JsonObject parentNode = new JsonObject();
		for (Team team : teams) {
			JsonObject response = new JsonObject();
			List<Sprint> listSprint = sprintService.findSprintsAreOpen(team.getTeamId());
			for (Sprint sprint : listSprint) {

				JsonObject jsonSprint = new JsonObject();
				jsonSprint.addProperty("sprintId", sprint.getSprintId());
				jsonSprint.addProperty("sprintName", sprint.getSprintName());
				jsonSprint.addProperty("dateStart", formatter.format(sprint.getDateStart()));
				jsonSprint.addProperty("dateEnd", formatter.format(sprint.getDateEnd()));
				response.add(listSprint.indexOf(sprint) + "", jsonSprint);
			}
			parentNode.add(team.getTeamId() + "", response);
		}
		return parentNode;
	}
}

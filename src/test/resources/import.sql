SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = myagiletest, pg_catalog;

--
-- TOC entry 2085 (class 0 OID 0)
-- Dependencies: 168
-- Name: hibernate_sequence; Type: SEQUENCE SET; Schema: myagile; Owner: postgres
--

SELECT pg_catalog.setval('hibernate_sequence', 225, true);

INSERT INTO member (id, account_expired, account_locked, avatar, company, enabled, first_name, gender, is_active, last_name, mobile, password, password_expired, skype, token, token_date, token_pass, username) VALUES (3, false, false, '15.jpg', 'Axon Active Vietnam', true, 'Sebastian', 'Male', true, 'Sussmann', '01239911602', '7c4a8d09ca3762af61e59520943dc26494f8941b', false, 'sebastian.sussmann.home', '5b45072f2e2a4c2696b9e9a552307b34', '2013-07-23 11:03:58.869', '7c4a8d09ca3762af61e59520943dc26494f8941b', 'sebastian.sussmann1@axonactive.vn');
INSERT INTO member (id, account_expired, account_locked, avatar, company, enabled, first_name, gender, is_active, last_name, mobile, password, password_expired, skype, token, token_date, token_pass, username) VALUES (14, false, false, '12.jpg', 'Axon Active Vietnam', true, 'Ngộ', 'Male', true, 'Đinh Minh', '01215121359', '7c4a8d09ca3762af61e59520943dc26494f8941b', false, 'dinhminhngo91', '5b45072f2e2a4c2696b9e9a552307b34', '2013-07-23 11:03:58.969', 'cc07d8728c30496fa7f2578cdeee210a', 'ngo.dinh@axonactive.vn');
INSERT INTO member (id, account_expired, account_locked, avatar, company, enabled, first_name, gender, is_active, last_name, mobile, password, password_expired, skype, token, token_date, token_pass, username) VALUES (4, false, false, '16.jpg', 'Axon Active Vietnam', true, 'Huy', 'Male', true, 'Nguyễn Quốc', '0908428246', '7c4a8d09ca3762af61e59520943dc26494f8941b', false, 'huyedumgr', '5b45072f2e2a4c2696b9e9a552307b34', '2013-07-23 11:03:58.88', 'b00146d08b2e428dbb531f7a6b5979c6', 'huy.nguyen@axonactive.vn');
INSERT INTO member (id, account_expired, account_locked, avatar, company, enabled, first_name, gender, is_active, last_name, mobile, password, password_expired, skype, token, token_date, token_pass, username) VALUES (11, false, false, '9.jpg', 'Axon Active Vietnam', true, 'Trực', 'Male', true, 'Trần Cương', '0933941274', '7c4a8d09ca3762af61e59520943dc26494f8941b', false, 'cuongtruc.tran', '5b45072f2e2a4c2696b9e9a552307b34', '2013-07-23 11:03:58.944', '65e2e9ec0cd64a72bb05a517913a2768', 'truc.tran@axonactive.vn');
INSERT INTO member (id, account_expired, account_locked, avatar, company, enabled, first_name, gender, is_active, last_name, mobile, password, password_expired, skype, token, token_date, token_pass, username) VALUES (10, false, false, '7.jpg', 'Axon Active Vietnam', true, 'Vinh', 'Male', true, 'Nguyễn Xuân', '0949976155', '7c4a8d09ca3762af61e59520943dc26494f8941b', false, 'xnvinh', '5b45072f2e2a4c2696b9e9a552307b34', '2013-07-23 11:03:58.936', '9c3ceaa9bb2e4f4bac4c74e178b344d9', 'vinh.nguyenxuan@axonactive.vn');

INSERT INTO project (id, description, image_path, is_public, owner_id, project_name) VALUES (22, 'Scrum project management', 'scrum.png', false, 3, 'My Agile');

INSERT INTO team (id, description, established_date, logo, mail_group, owner_id, team_name) VALUES (1, 'solidarity', '2013-06-03 00:00:00', 'logo-ant.png', 'ant@axonactive.vn', 3, 'Ant');
INSERT INTO team (id, description, established_date, logo, mail_group, owner_id, team_name) VALUES (2, 'powerful', '2013-06-03 00:00:00', 'logo-ant.png', 'ant@axonactive.vn', 3, 'Beagle');

INSERT INTO sprint (id, date_end, date_start, sprint_name, status, team_id) VALUES (52, '2013-06-18 00:00:00', '2013-06-10 00:00:00', 'Ant.Sprint-0', 'closed', 1);
INSERT INTO sprint (id, date_end, date_start, sprint_name, status, team_id) VALUES (53, '2013-06-23 00:00:00', '2013-06-19 00:00:00', 'Ant.Sprint-1', 'closed', 1);
INSERT INTO sprint (id, date_end, date_start, sprint_name, status, team_id) VALUES (54, '2013-07-23 00:00:00', '2013-07-05 00:00:00', 'Ant.Sprint-2', 'open', 1);

INSERT INTO sprint (id, date_start, date_end, sprint_name, status, team_id) VALUES (10, '2013-08-01 00:00:00', '2013-08-10 00:00:00', 'Beagle.Sprint-1', 'closed', 2);
INSERT INTO sprint (id, date_start, date_end, sprint_name, status, team_id) VALUES (11, '2013-08-11 00:00:00', '2013-08-20 00:00:00', 'Beagle.Sprint-2', 'closed', 2);
INSERT INTO sprint (id, date_start, date_end, sprint_name, status, team_id) VALUES (12, '2013-08-21 00:00:00', '2013-08-31 00:00:00', 'Beagle.Sprint-3', 'closed', 2);
INSERT INTO sprint (id, date_start, date_end, sprint_name, status, team_id) VALUES (13, '2013-08-31 00:00:00', '2013-09-09 00:00:00', 'Beagle.Sprint-4', 'open', 2);



INSERT INTO user_story (id, sort_id, description, name, project_id, status, value, risk) VALUES (84, 1, 'As a PO, I want to have possibility for more than one team that work on the same project.', 'Many teams on the same project', 22, 'TODO', 12, 0);
INSERT INTO user_story (id, sort_id, description, name, project_id, status, value, risk) VALUES (85, 2, 'As a scrum team, I want get three backlogs available: Product backlog, Sprint backlog, Impediment backlog', 'Product, Sprint, Impediment backlogs', 22, 'IN_PROGRESS', 6, 0);

INSERT INTO status (id, color, type, name, sprint_id) VALUES (1, '#368EE0', 'START', 'To do', 52);
INSERT INTO status (id, color, type, name, sprint_id) VALUES (2, '#368EE0', 'IN_PROGRESS', 'Developing', 52);
INSERT INTO status (id, color, type, name, sprint_id) VALUES (3, '#368EE0', 'IN_PROGRESS', 'Testing', 52);
INSERT INTO status (id, color, type, name, sprint_id) VALUES (4, '#368EE0', 'DONE', 'Done', 52);
INSERT INTO status (id, color, type, name, sprint_id) VALUES (5, '#368EE0', 'ACCEPTED_HIDE', 'Accepted', 52);

INSERT INTO issue (id, assigned_id, description, estimate, priority, remain, sprint_id, status_id, subject, type, user_story_id) VALUES (93, 3, 'Many teams on the same project', 'D5T3', 'Must', 'D2T1', 52, 1, 'Many teams on the same project', 'UserStory', 84);
INSERT INTO issue (id, assigned_id, description, estimate, priority, remain, sprint_id, status_id, subject, type, user_story_id) VALUES (94, 3, 'Many teams on the same project', 'D3T3', 'Must', 'D2T1', 52, 1, 'Many teams on the same project', 'UserStory', 84);
INSERT INTO issue (id, assigned_id, description, estimate, priority, remain, sprint_id, status_id, subject, type, user_story_id) VALUES (95, NULL, 'Drag & Drop US from product backlog to sprint backlog', 'D2T3', 'Should', 'D2T1', 52, 1, 'Drag & Drop in backlog', 'Bug', 84);
INSERT INTO issue (id, assigned_id, description, estimate, priority, remain, sprint_id, status_id, subject, type, user_story_id) VALUES (96, 3, 'A userstory can have many subuserstory', 'D7T3', 'Could', 'D2T1', 52, 1, 'Sub userstory', 'Bug', 84);

INSERT INTO team_member (id, is_accepted, member_id, "position", team_id, token, token_date) VALUES (1, true, 4, 'SCRUM_MASTER', 1, '', '2013-07-23 11:03:59.142');
INSERT INTO team_member (id, is_accepted, member_id, "position", team_id, token, token_date) VALUES (2, false, 14, 'DEVELOPER', 1, '', '2013-07-23 11:03:59.147');
INSERT INTO team_member (id, is_accepted, member_id, "position", team_id, token, token_date) VALUES (3, true, 11, 'DEVELOPER', 1, '', '2013-07-23 11:03:59.146');
INSERT INTO team_member (id, is_accepted, member_id, "position", team_id, token, token_date) VALUES (4, true, 10, 'DEVELOPER', 1, '', '2013-07-23 11:03:59.146');

INSERT INTO history (id, action_type, container_id, container_type, created_on, author_id) VALUES (1, 'Create', 94, 'Issue', '2013-09-03 00:00:00', 3);
INSERT INTO history (id, action_type, container_id, container_type, created_on, author_id) VALUES (2, 'Update', 94, 'Issue', '2013-09-04 00:00:00', 3);
INSERT INTO history (id, action_type, container_id, container_type, created_on, author_id) VALUES (3, 'Update', 94, 'Issue', '2013-09-05 00:00:00', 4);
INSERT INTO history (id, action_type, container_id, container_type, created_on, author_id) VALUES (4, 'Update', 95, 'Issue', '2013-09-05 00:00:00', 3);
INSERT INTO history (id, action_type, container_id, container_type, created_on, author_id) VALUES (5, 'Update', 22, 'Project', '2013-09-05 00:00:00', 3);
INSERT INTO history (id, action_type, container_id, container_type, created_on, author_id) VALUES (6, 'Update', 1, 'Team', '2013-09-05 00:00:00', 3);

INSERT INTO history_field_change (id, field_name, new_value, old_value, history_id) VALUES (1, 'Assigned To', 'Sứ Trần Nguyên Tiến', 'None', 2);
INSERT INTO history_field_change (id, field_name, new_value, old_value, history_id) VALUES (2, 'Remain point', 'D4T2', 'D5T2', 2);
INSERT INTO history_field_change (id, field_name, new_value, old_value, history_id) VALUES (3, 'Remain point', 'D2T2', 'D4T2', 3);
INSERT INTO history_field_change (id, field_name, new_value, old_value, history_id) VALUES (4, 'Remain point', 'D2T2', 'D0T0', 4);
INSERT INTO history_field_change (id, field_name, new_value, old_value, history_id) VALUES (5, 'Subject', 'Cannot create a team new', 'Cannot create a team', 4);
INSERT INTO history_field_change (id, field_name, new_value, old_value, history_id) VALUES (6, 'Project Name', 'My Agile edited', 'My Agile', 5);
INSERT INTO history_field_change (id, field_name, new_value, old_value, history_id) VALUES (7, 'Team Name', 'Ant edited', 'Ant', 6);

INSERT INTO sprint_statistic(id, sprint_id, available_man_day, point_plan, point_delivered, sprint_size, team_size, velocity_of_sprint_plan, velocity_of_sprint_delivered, user_story_delivered, user_story_plan, note) VALUES (3, 52, 68, 80, 80, 2, 10, 1.0, 1.0, 10, 10, 'Department get two days off');

INSERT INTO holiday(id, leave_date, leave_type, reason, member_id, sprint_id) VALUES (1, '2013-07-23 00:00:00', 'MORNING', 'Leave in the morning', 3,54);
INSERT INTO holiday(id, leave_date, leave_type, reason, member_id, sprint_id) VALUES (2, '2013-07-24 00:00:00', 'FULLDAY', 'Leave fullday', 3,54);
INSERT INTO holiday(id, leave_date, leave_type, reason, member_id, sprint_id) VALUES (3, '2013-07-23 00:00:00', 'FULLDAY', 'Leave fullday', 4,54);
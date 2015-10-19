package com.ant.myagile.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ant.myagile.dao.MyTeamLinkedDao;
import com.ant.myagile.model.MyTeamLinked;
import com.ant.myagile.service.MyTeamLinkedService;

@Service
@Transactional
public class MyTeamLinkedServiceImpl implements MyTeamLinkedService {
	@Autowired
	private MyTeamLinkedDao myTeamLinkedDao;

	@Override
	public boolean save(MyTeamLinked myteamlinked) {
		return myTeamLinkedDao.save(myteamlinked);
	}

	@Override
	public boolean delete(MyTeamLinked myteamlinked) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean update(MyTeamLinked myteamlinked) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String findIdByEmail(String email) {
		return myTeamLinkedDao.findIdByEmail(email);
	}

	@Override
	public List<MyTeamLinked> findAll() {
		return myTeamLinkedDao.findAll();
	}

}

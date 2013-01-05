package com.inforstack.openstack.test.api;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.inforstack.openstack.api.token.Access;
import com.inforstack.openstack.api.token.TokenService;
import com.inforstack.openstack.configuration.ConfigurationDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/test-context.xml"})
public class TokenTest {

	@Autowired
	private TokenService tokenService;
	
	@Autowired
	private ConfigurationDao configurationDao;
	
	private String tenant;
	
	private String username;
	
	private String password;
	
	@Before
	public void setUp() throws Exception {
		this.tenant = this.configurationDao.findByName(TokenService.TENANT_ADMIN_ID).getValue();
		this.username = this.configurationDao.findByName(TokenService.USER_ADMIN_NAME).getValue();
		this.password = this.configurationDao.findByName(TokenService.USER_ADMIN_PASS).getValue();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetToken() {
		Access access = this.tokenService.getAccess(this.username, this.password, tenant, false);
		if (access == null) {
			access = this.tokenService.getAccess(this.username, this.password, tenant, true);
		}
		Assert.assertNotNull("Could not get access", access);
		Assert.assertFalse(this.tokenService.isExpired(access));
	}

	@Test
	public void testApplyToken() {
		Access access = this.tokenService.applyAccess(this.username, this.password, tenant);
		Assert.assertNotNull("Could not apply new access", access);
		Assert.assertFalse(this.tokenService.isExpired(access));
	}

}

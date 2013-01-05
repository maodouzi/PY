package com.inforstack.openstack.api.token;

public interface TokenService {
	
	public final static String ENDPOINT_TOKEN	= "openstack.endpoint.token";
	public final static String TENANT_ADMIN_ID	= "openstack.tenant.admin";
	public final static String USER_ADMIN_NAME	= "openstack.user.admin.name";
	public final static String USER_ADMIN_PASS	= "openstack.user.admin.pass";

	public Access getAccess(String name, String pass, String tenant, boolean apply);
	
	public boolean isExpired(Access access);
	
	public Access applyAccess(String name, String pass, String tenant);
	
}
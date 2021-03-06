package com.inforstack.openstack.security.auth;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.inforstack.openstack.log.Logger;
import com.inforstack.openstack.security.permission.Permission;
import com.inforstack.openstack.tenant.Tenant;
import com.inforstack.openstack.user.User;
import com.inforstack.openstack.user.UserService;
import com.inforstack.openstack.utils.SecurityUtils;

@Component("openstackUserDetails")
@Scope("prototype")
public class OpenstackUserDetails implements UserDetails {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -423843895615465679L;
	private static final Logger log = new Logger(OpenstackUserDetails.class);
	private User user;
	private Tenant tenant;
	
	@Autowired
	private UserService userService;
	
	private Map<String, Collection<GrantedAuthority>> permissionMap = new WeakHashMap<String, Collection<GrantedAuthority>>();
	
	public OpenstackUserDetails(){
	}
	
	public void clearCache(User userId){
		permissionMap.remove(userId);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		log.debug("Invoke Method - getAuthorities");
		Collection<GrantedAuthority> grantedAuthorities = permissionMap.get(user.getId());
		if(grantedAuthorities != null){
			log.debug("Get permissions in cache");
			return grantedAuthorities;
		}
		
		grantedAuthorities = new HashSet<GrantedAuthority>(); 
		Set<Permission> permissions = userService.getPermissions(user.getId());
		for(Permission p : permissions){
			grantedAuthorities.add(new SimpleGrantedAuthority(p.getName()));
		}
		
		log.debug(grantedAuthorities.size() + " GrantedAuthority found");
        return grantedAuthorities;  
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return SecurityUtils.isUserEnabled(user);
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Tenant getTenant() {
		return tenant;
	}

	public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}

}

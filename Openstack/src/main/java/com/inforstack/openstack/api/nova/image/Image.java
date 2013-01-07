package com.inforstack.openstack.api.nova.image;

import java.util.Date;
import java.util.Map;

public class Image {
	
	private String id;
	
	private String name;
	
	private Date created;
	
	private Date updated;
	
	private String tenant;
	
	private String user;
	
	private String status;
	
	private Map<String, String> meatadata;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Map<String, String> getMeatadata() {
		return meatadata;
	}

	public void setMeatadata(Map<String, String> meatadata) {
		this.meatadata = meatadata;
	}
	
}

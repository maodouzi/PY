package com.inforstack.openstack.controller.model;

import java.util.Date;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;

import com.inforstack.openstack.utils.Constants;
import com.inforstack.openstack.utils.OpenstackUtil;

/**
 * this is for json resposne
 * 
 * @author shaw
 * 
 */
public class ImageModel {

  private String imgId;

  @Size(min = Constants.DEFAULT_NAME_MIN_LENGTH, max = Constants.DEFAULT_NAME_MAX_LENGTH, message = "{size.not.valid}")
  @Pattern(regexp = "^[0-9a-zA-Z_]+$", message = "{not.valid}")
  private String imgName;
  
  private String family;

  private Date created;

  // for client converted to locale string with time
  private Long createdTime;

  private Date updated;

  private Long updatedTime;

  private String tenant;

  private String user;

  private String status;
  private String statusDisplay;

  private int progress;

  // DEFAULT GB
  @Range(min = Constants.DEFAULT_DISK_SIZE_MIN_LIMIT, max = Constants.DEFAULT_DISK_SIZE_MAX_LIMIT, message = "{size.not.valid}")
  private int minDisk;
  // DEFAULT UNIT MB
  @Range(min = Constants.DEFAULT_RAM_SIZE_MIN_LIMIT, max = Constants.DEFAULT_RAM_SIZE_MAX_LIMIT, message = "{size.not.valid}")
  private int minRam;

  public ImageModel() {
    super();
    // TODO Auto-generated constructor stub
  }

  public String getImgId() {
    return imgId;
  }

  public void setImgId(String imgId) {
    this.imgId = imgId;
  }

  public String getImgName() {
    return imgName;
  }

  public void setImgName(String imgName) {
    this.imgName = imgName;
  }

  public String getFamily() {
	return family;
}

public void setFamily(String family) {
	this.family = family;
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

  public int getProgress() {
    return progress;
  }

  public void setProgress(int progress) {
    this.progress = progress;
  }

  public int getMinDisk() {
    return minDisk;
  }

  public void setMinDisk(int minDisk) {
    this.minDisk = minDisk;
  }

  public int getMinRam() {
    return minRam;
  }

  public void setMinRam(int minRam) {
    this.minRam = minRam;
  }

  public Long getCreatedTime() {
    if (created != null) {
      return created.getTime();
    }
    return createdTime;
  }

  public void setCreatedTime(Long createdTime) {
    this.createdTime = createdTime;
  }

  public Long getUpdateDTime() {
    if (updated != null) {
      return updated.getTime();
    }
    return updatedTime;
  }

  public void setUpdateDTime(Long updatedTime) {
    this.updatedTime = updatedTime;
  }

  public Long getUpdatedTime() {
    return updatedTime;
  }

  public void setUpdatedTime(Long updatedTime) {
    this.updatedTime = updatedTime;
  }

  public String getStatusDisplay() {
    if (status != null) {
      return OpenstackUtil.getMessage("admin.image." + status.toLowerCase() + ".status");
    } else {
      return status;
    }
  }

  public void setStatusDisplay(String statusDisplay) {
    this.statusDisplay = statusDisplay;
  }

}

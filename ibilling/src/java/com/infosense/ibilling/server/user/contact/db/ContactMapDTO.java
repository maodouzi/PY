/*
 * JBILLING CONFIDENTIAL
 * _____________________
 *
 * [2003] - [2012] Enterprise jBilling Software Ltd.
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Enterprise jBilling Software.
 * The intellectual and technical concepts contained
 * herein are proprietary to Enterprise jBilling Software
 * and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden.
 */
package com.infosense.ibilling.server.user.contact.db;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.infosense.ibilling.server.util.db.IbillingTable;

@Entity
@TableGenerator(
        name="contact_map_GEN",
        table="ibilling_seqs",
        pkColumnName = "name",
        valueColumnName = "next_id",
        pkColumnValue="contact_map",
        allocationSize = 100
        )
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name="contact_map"
    , uniqueConstraints = @UniqueConstraint(columnNames="contact_id") 
)
public class ContactMapDTO  implements java.io.Serializable {


     private int id;
     private IbillingTable ibillingTable;
     private ContactTypeDTO contactType;
     private ContactDTO contact;
     private int foreignId;
     private int versionNum;

    public ContactMapDTO() {
    }

    
    public ContactMapDTO(int id, IbillingTable ibillingTable, ContactTypeDTO contactType, int foreignId) {
        this.id = id;
        this.ibillingTable = ibillingTable;
        this.contactType = contactType;
        this.foreignId = foreignId;
    }
    public ContactMapDTO(int id, IbillingTable ibillingTable, ContactTypeDTO contactType, ContactDTO contact, int foreignId) {
       this.id = id;
       this.ibillingTable = ibillingTable;
       this.contactType = contactType;
       this.contact = contact;
       this.foreignId = foreignId;
    }
   
    @Id @GeneratedValue(strategy=GenerationType.TABLE, generator="contact_map_GEN")
    @Column(name="id", unique=true, nullable=false)
    public int getId() {
        return this.id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="table_id", nullable=false)
    public IbillingTable getIbillingTable() {
        return this.ibillingTable;
    }
    
    public void setIbillingTable(IbillingTable ibillingTable) {
        this.ibillingTable = ibillingTable;
    }
@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="type_id", nullable=false)
    public ContactTypeDTO getContactType() {
        return this.contactType;
    }
    
    public void setContactType(ContactTypeDTO contactType) {
        this.contactType = contactType;
    }
@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="contact_id", unique=true)
    public ContactDTO getContact() {
        return this.contact;
    }
    
    public void setContact(ContactDTO contact) {
        this.contact = contact;
    }
    
    @Column(name="foreign_id", nullable=false)
    public int getForeignId() {
        return this.foreignId;
    }
    
    public void setForeignId(int foreignId) {
        this.foreignId = foreignId;
    }

    @Version
    @Column(name="OPTLOCK")
    public Integer getVersionNum() {
        return versionNum;
    }
    public void setVersionNum(Integer versionNum) {
        this.versionNum = versionNum;
    }
}


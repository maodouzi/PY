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
package com.infosense.ibilling.server.notification.db;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OrderBy;

import com.infosense.ibilling.server.user.db.CompanyDTO;
import com.infosense.ibilling.server.util.db.LanguageDTO;

@Entity
@TableGenerator(
        name = "notification_message_GEN", 
        table = "ibilling_seqs", 
        pkColumnName = "name", 
        valueColumnName = "next_id", 
        pkColumnValue = "notification_message", 
        allocationSize = 100)
@Table(name = "notification_message")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class NotificationMessageDTO implements Serializable {

    private int id;
    private NotificationMessageTypeDTO notificationMessageType;
    private CompanyDTO entity;
    private LanguageDTO language;
    private short useFlag;
    private Set<NotificationMessageSectionDTO> notificationMessageSections = new HashSet<NotificationMessageSectionDTO>(
            0);
    private int versionNum;

    public NotificationMessageDTO() {

    }

    public NotificationMessageDTO(int id, CompanyDTO entity,
            LanguageDTO language, short useFlag) {
        this.id = id;
        this.entity = entity;
        this.language = language;
        this.useFlag = useFlag;
    }

    public NotificationMessageDTO(int id,
            NotificationMessageTypeDTO notificationMessageType,
            CompanyDTO entity, LanguageDTO language, short useFlag,
            Set<NotificationMessageSectionDTO> notificationMessageSections) {
        this.id = id;
        this.notificationMessageType = notificationMessageType;
        this.entity = entity;
        this.language = language;
        this.useFlag = useFlag;
        this.notificationMessageSections = notificationMessageSections;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "notification_message_GEN")
    @Column(name = "id", unique = true, nullable = false)
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    public NotificationMessageTypeDTO getNotificationMessageType() {
        return this.notificationMessageType;
    }

    public void setNotificationMessageType(
            NotificationMessageTypeDTO notificationMessageType) {
        this.notificationMessageType = notificationMessageType;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_id", nullable = false)
    public CompanyDTO getEntity() {
        return this.entity;
    }

    public void setEntity(CompanyDTO entity) {
        this.entity = entity;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id", nullable = false)
    public LanguageDTO getLanguage() {
        return this.language;
    }

    public void setLanguage(LanguageDTO language) {
        this.language = language;
    }

    @Column(name = "use_flag", nullable = false)
    public short getUseFlag() {
        return this.useFlag;
    }

    public void setUseFlag(short useFlag) {
        this.useFlag = useFlag;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "notificationMessage")
    @OrderBy(clause="section")
    @Fetch( FetchMode.JOIN )
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    public Set<NotificationMessageSectionDTO> getNotificationMessageSections() {
        return this.notificationMessageSections;
    }

    public void setNotificationMessageSections(
            Set<NotificationMessageSectionDTO> notificationMessageSections) {
        this.notificationMessageSections = notificationMessageSections;
    }
    
    @Version
    @Column(name="OPTLOCK")
    public int getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(int versionNum) {
        this.versionNum = versionNum;
    }

}
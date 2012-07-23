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
package com.infosense.ibilling.server.process.db;

import java.util.Date;
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

@Entity
@TableGenerator(
        name = "process_run_GEN", 
        table = "ibilling_seqs", 
        pkColumnName = "name", 
        valueColumnName = "next_id", 
        pkColumnValue = "process_run", 
        allocationSize = 100)
@Table(name = "process_run")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ProcessRunDTO implements java.io.Serializable {

    private int id;
    private BillingProcessDTO billingProcessDTO;
    private Date runDate;
    private Date started;
    private Date finished;
    private Integer invoicesGenerated;
    private Date paymentFinished;
    private Set<ProcessRunTotalDTO> processRunTotals = new HashSet<ProcessRunTotalDTO>(0);
    private ProcessRunStatusDTO status;
    private int versionNum;

    public ProcessRunDTO() {
    }

    public ProcessRunDTO(int id, Date runDate, Date started) {
        this.id = id;
        this.runDate = runDate;
        this.started = started;
    }

    public ProcessRunDTO(int id, BillingProcessDTO billingProcessDTO,
            Date runDate, Date started, Date finished,
            Integer invoicesGenerated, Date paymentFinished,
            Set<ProcessRunTotalDTO> processRunTotals) {
        this.id = id;
        this.billingProcessDTO = billingProcessDTO;
        this.runDate = runDate;
        this.started = started;
        this.finished = finished;
        this.invoicesGenerated = invoicesGenerated;
        this.paymentFinished = paymentFinished;
        this.processRunTotals = processRunTotals;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "process_run_GEN")
    @Column(name = "id", unique = true, nullable = false)
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id")
    public BillingProcessDTO getBillingProcess() {
        return this.billingProcessDTO;
    }

    public void setBillingProcess(BillingProcessDTO billingProcessDTO) {
        this.billingProcessDTO = billingProcessDTO;
    }

    @Column(name = "run_date", nullable = false, length = 13)
    public Date getRunDate() {
        return this.runDate;
    }

    public void setRunDate(Date runDate) {
        this.runDate = runDate;
    }

    @Column(name = "started", nullable = false, length = 29)
    public Date getStarted() {
        return this.started;
    }

    public void setStarted(Date started) {
        this.started = started;
    }

    @Column(name = "finished", length = 29)
    public Date getFinished() {
        return this.finished;
    }

    public void setFinished(Date finished) {
        this.finished = finished;
    }

    @Column(name = "invoices_generated")
    public Integer getInvoicesGenerated() {
        return this.invoicesGenerated;
    }

    public void setInvoicesGenerated(Integer invoicesGenerated) {
        this.invoicesGenerated = invoicesGenerated;
    }

    @Column(name = "payment_finished", length = 29)
    public Date getPaymentFinished() {
        return this.paymentFinished;
    }

    public void setPaymentFinished(Date paymentFinished) {
        this.paymentFinished = paymentFinished;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "processRun")
    public Set<ProcessRunTotalDTO> getProcessRunTotals() {
        return this.processRunTotals;
    }

    public void setProcessRunTotals(Set<ProcessRunTotalDTO> processRunTotals) {
        this.processRunTotals = processRunTotals;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    public ProcessRunStatusDTO getStatus() {
        return status;
    }

    public void setStatus(ProcessRunStatusDTO status) {
        this.status = status;
    }

    @Version
    @Column(name = "OPTLOCK")
    public int getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(int versionNum) {
        this.versionNum = versionNum;
    }

    public String toString() {
        StringBuffer ret = new StringBuffer(" ProcesRunDTO: id: " + id + " runDate: " + runDate + " started: " + started +
                "  finished " + finished + " invoiceGenerated " + invoicesGenerated +
                " status: " + status + " paymentFinished: " + paymentFinished + 
                " processRunTotals: ");

        for (ProcessRunTotalDTO total : processRunTotals) {
            ret.append(total.toString());
        }

        return ret.toString();
    }
}
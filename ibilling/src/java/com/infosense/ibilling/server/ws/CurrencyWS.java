package com.infosense.ibilling.server.ws;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import com.infosense.ibilling.server.util.api.validation.CreateValidationGroup;
import com.infosense.ibilling.server.util.db.CurrencyDTO;

/**
 * CurrencyWS
 */
public class CurrencyWS implements Serializable {

    private Integer id;

    @NotNull(message = "validation.error.notnull", groups = CreateValidationGroup.class)
    @NotEmpty(message = "validation.error.notnull", groups = CreateValidationGroup.class)
    private String description;
    @NotNull(message = "validation.error.notnull")
    @Size(min = 1, max = 10, message = "validation.error.size,1,10")
    private String symbol;
    @NotNull(message = "validation.error.notnull")
    @Size(min = 1, max = 3, message = "validation.error.size,1,3")
    private String code;
    @NotNull(message = "validation.error.notnull")
    @Size(min = 2, max = 2, message = "validation.error.size.exact,2")
    private String countryCode;
    private Boolean inUse;

    @Digits(integer = 10, fraction = 4, message = "validation.error.invalid.number.or.fraction")
    private String rate;
    @NotNull(message = "validation.error.notnull")
    @Digits(integer = 10, fraction = 4, message = "validation.error.invalid.number.or.fraction")
    private String sysRate;

    private boolean defaultCurrency;

    public CurrencyWS() {
    }

    public CurrencyWS(CurrencyDTO dto, boolean defaultCurrency) {
        this.id = dto.getId();
        this.description = dto.getDescription();
        this.symbol = dto.getSymbol();
        this.code = dto.getCode();
        this.countryCode = dto.getCountryCode();
        this.inUse = dto.getInUse();

        setRate(dto.getRate());
        setSysRate(dto.getSysRate());

        this.defaultCurrency = defaultCurrency;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Boolean getInUse() {
        return inUse;
    }

    public void setInUse(Boolean inUse) {
        this.inUse = inUse;
    }

    public String getRate() {
        return rate;
    }

    public BigDecimal getRateAsDecimal() {
        return rate != null ? new BigDecimal(rate) : null;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = (rate != null ? rate.toString() : null);
    }

    public void setRateAsDecimal(BigDecimal rate) {
        setRate(rate);
    }

    public String getSysRate() {
        return sysRate;
    }

    public BigDecimal getSysRateAsDecimal() {
        return sysRate != null ? new BigDecimal(sysRate) : null;
    }

    public void setSysRate(String sysRate) {
        this.sysRate = sysRate;
    }

    public void setSysRate(BigDecimal systemRate) {
        this.sysRate = (systemRate != null ? systemRate.toString() : null);
    }

    public void setSysRateAsDecimal(BigDecimal systemRate) {
        setSysRate(systemRate);
    }

    public boolean isDefaultCurrency() {
        return defaultCurrency;
    }

    public boolean getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(boolean defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    @Override
    public String toString() {
        return "CurrencyWS{"
               + "id=" + id
               + ", symbol='" + symbol + '\''
               + ", code='" + code + '\''
               + ", countryCode='" + countryCode + '\''
               + ", inUse=" + inUse
               + ", rate='" + rate + '\''
               + ", systemRate='" + sysRate + '\''
               + ", isDefaultCurrency=" + defaultCurrency
               + '}';
    }
}

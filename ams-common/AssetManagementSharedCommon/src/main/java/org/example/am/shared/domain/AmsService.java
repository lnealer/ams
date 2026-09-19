package org.example.am.shared.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * A service subscription held by a customer; an asset may only be ordered against an active one.
 */
public class AmsService implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long serviceId;
    private Long customerId;
    private ServiceType serviceType;
    private ServiceStatusType serviceStatusType;
    private Date startDate;
    private Date endDate;
    private String description;
    private String externalServiceReference;

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(final Long serviceId) {
        this.serviceId = serviceId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(final Long customerId) {
        this.customerId = customerId;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(final ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public ServiceStatusType getServiceStatusType() {
        return serviceStatusType;
    }

    public void setServiceStatusType(final ServiceStatusType serviceStatusType) {
        this.serviceStatusType = serviceStatusType;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(final Date endDate) {
        this.endDate = endDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getExternalServiceReference() {
        return externalServiceReference;
    }

    public void setExternalServiceReference(final String externalServiceReference) {
        this.externalServiceReference = externalServiceReference;
    }

    public boolean isActive() {
        return ServiceStatusType.ACTIVE.equals(serviceStatusType);
    }

}

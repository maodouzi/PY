/*
 JBILLING CONFIDENTIAL
 _____________________

 [2003] - [2012] Enterprise jBilling Software Ltd.
 All Rights Reserved.

 NOTICE:  All information contained herein is, and remains
 the property of Enterprise jBilling Software.
 The intellectual and technical concepts contained
 herein are proprietary to Enterprise jBilling Software
 and are protected by trade secret or copyright law.
 Dissemination of this information or reproduction of this material
 is strictly forbidden.
 */

package com.infosense.ibilling.server.mediation.task;

import com.infosense.ibilling.server.item.ItemBL;
import com.infosense.ibilling.server.item.PricingField;
import com.infosense.ibilling.server.mediation.Record;
import com.infosense.ibilling.server.order.OrderBL;
import com.infosense.ibilling.server.order.OrderLineBL;
import com.infosense.ibilling.server.order.db.OrderDAS;
import com.infosense.ibilling.server.order.db.OrderDTO;
import com.infosense.ibilling.server.order.db.OrderLineDTO;
import com.infosense.ibilling.server.pluggableTask.PluggableTask;
import com.infosense.ibilling.server.pluggableTask.TaskException;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIGlobalBinding;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.List;

/**
 * ResolverMediationTask
 *
 * @author Brian Cowdery
 * @since 15/09/11
 */
public abstract class AbstractResolverMediationTask extends PluggableTask implements IMediationProcess {

    private static final Logger LOG = Logger.getLogger(AbstractResolverMediationTask.class);

    public void process(List<Record> records, List<MediationResult> results, String configurationName)
            throws TaskException {

        LOG.debug("Running mediation resolver for configuration '" + configurationName + "'");

        if (results == null) {
            throw new TaskException("Results list cannot be null.");
        }

        if (!results.isEmpty() && results.size() != records.size()) {
            throw new TaskException("Results list must be empty, or have the same size as the records list.");
        }

        LOG.debug("Processing " + results.size() + " result(s).");

        int index = results.isEmpty() ? -1 : 0;
        for (Record record : records) {

            // get mediation result
            MediationResult result = null;
            if (index >= 0) {
               result = results.get(index++);
            } else {
               result = new MediationResult(configurationName, true);
            }

            LOG.debug("Processing mediation result " + result.getId());

            // resolve mediation pricing fields
            result.setRecordKey(record.getKey());
            resolve(result, record.getFields());
            results.add(result);

            // done!
            if (result.getUserId() != null && result.getCurrencyId() != null && result.getCurrentOrder() != null) {
                complete(result, record.getFields());
            }
        }
    }

    /**
     * Resolve the given pricing fields into an item added to a customers current order.
     *
     * @param result result to process
     * @param fields pricing fields of the mediation record being processed
     */
    protected void resolve(MediationResult result, List<PricingField> fields) {
        // resolve target user & event date
        resolveUser(result, fields);
        resolveDate(result, fields);

        // complete the mediation action for this event
        if (isActionable(result, fields)) {
            doEventAction(result, fields);
        }
    }

    /**
     * Finish the mediation result.
     *
     * Adds the resolved lines to the user's current order, calculates the diff and marks the
     * mediation result as "done".
     *
     * If there are no lines resolved, then the record will simply be marked as "done" and
     * the mediation record will be handled as an error.
     *
     * @param result result to complete
     */
    protected void complete(MediationResult result, List<PricingField> fields) {
        if (result.getCurrentOrder() != null) {
            // add resolved lines to current order
            calculatePrices(result, fields);
            addLinesToOrder(result);

            // save order
            beforeSave(result);
            if (result.getPersist()) {
                new OrderDAS().save(result.getCurrentOrder());
            }
            afterSave(result);

            // calculate diff
            addDiffLines(result);
        }

        result.setDone(true);
    }

    /**
     * Calculate prices for mediated lines. Prices can be set during line creation, or they can
     * be left blank to force the system to calculate the prices using the pricing engine.
     *
     * @param result mediation result with order lines
     * @param fields pricing fields
     */
    protected void calculatePrices(MediationResult result, List<PricingField> fields) {
        for (OrderLineDTO line : result.getLines()) {
            if (line.getPrice() == null) {
                LOG.debug("Calculating price for line " + line);

                ItemBL itemService = new ItemBL(line.getItemId());
                itemService.setPricingFields(fields);
                BigDecimal price = itemService.getPrice(result.getUserId(),
                                                        result.getCurrencyId(),
                                                        line.getQuantity(),
                                                        getEntityId(),
                                                        result.getCurrentOrder(),
                                                        true);
                line.setPrice(price);
            }

            line.setAmount(line.getPrice().multiply(line.getQuantity()));
        }
    }

    /**
     * Adds lines from the mediation result to the current order and sets the results
     * "old lines" so that the result can be diffed.
     *
     * @param result mediation result to process
     */
    protected void addLinesToOrder(MediationResult result) {
        if (result.getOldLines() == null || result.getOldLines().isEmpty()) {
            result.setOldLines(OrderLineBL.copy(result.getCurrentOrder().getLines()));
        }

        // update the lines in the current order
        for (OrderLineDTO line : result.getLines()) {
            OrderLineBL.addLine(result.getCurrentOrder(), line, false);
        }
    }

    /**
     * Calculates the diff between the current order's mediation result lines and the
     * lines of the current order. Effectively showing what was changed by the mediation run.
     *
     * @param result mediation result to diff
     */
    protected void addDiffLines(MediationResult result) {
        if (result.getCurrentOrder().getLines() != null && result.getOldLines() != null) {
            // calculate diff
            result.setDiffLines(OrderLineBL.diffOrderLines(result.getOldLines(), result.getCurrentOrder().getLines()));

            // check order line quantities
            if (result.getPersist()) {
                new OrderBL().checkOrderLineQuantities(result.getOldLines(),
                                                       result.getCurrentOrder().getLines(),
                                                       getEntityId(),
                                                       result.getCurrentOrder().getId(),
                                                       true);
            }
        }
    }


    /*
        Hooks that can be overridden to perform actions before and after the order is saved (like adding taxes)
     */

    protected void beforeSave(MediationResult result) { }
    protected void afterSave(MediationResult result) { }


    /*
        Abstract methods to be implemented to do the actual mediation work.
     */

    public abstract void resolveDate(MediationResult result, List<PricingField> fields);
    public abstract void resolveUser(MediationResult result, List<PricingField> fields);
    public abstract boolean isActionable(MediationResult result, List<PricingField> fields);
    public abstract void doEventAction(MediationResult result, List<PricingField> fields);



    /**
     * Convenience method to find a pricing field by name.
     *
     * @param fields pricing fields
     * @param fieldName name
     * @return found pricing field or null if no field found.
     */
    public static PricingField find(List<PricingField> fields, String fieldName) {
        for (PricingField field : fields) {
            if (field.getName().equals(fieldName))
                return field;
        }
        return null;
    }

    /**
     * Convenience method to find a specific order line by item ID.
     *
     * @param order order to search
     * @param itemId item id
     * @return order line
     */
    public static OrderLineDTO find(OrderDTO order, Integer itemId) {
        for (OrderLineDTO line : order.getLines()) {
            if (line.getItemId().equals(itemId)) {
                return line;
            }
        }
        return null;
    }

    /**
     * Convenience method to create a new line with a given item ID and quantity and to set a simple price
     * WITHOUT the influence of the pricing engine.
     *
     * @param itemId item id
     * @param quantity quantity
     * @return order line
     */
    public static OrderLineDTO newLine(Integer itemId, BigDecimal quantity) {
        OrderLineDTO line = new OrderLineDTO();
        line.setItemId(itemId);
        line.setQuantity(quantity);
        line.setDefaults();
        return line;
    }

    /**
     * Convenience method to create a new line with a given item ID, quantity and price.
     *
     * @param itemId item id
     * @param quantity quantity
     * @param price price
     * @return order line
     */
    public static OrderLineDTO newLine(Integer itemId, BigDecimal quantity, BigDecimal price) {
        OrderLineDTO line = new OrderLineDTO();
        line.setItemId(itemId);
        line.setQuantity(quantity);
        line.setPrice(price);
        line.setDefaults();
        return line;
    }
}
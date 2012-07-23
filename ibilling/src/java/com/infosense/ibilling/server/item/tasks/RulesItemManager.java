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
package com.infosense.ibilling.server.item.tasks;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.drools.runtime.rule.FactHandle;

import com.infosense.ibilling.common.Constants;
import com.infosense.ibilling.server.item.ItemBL;
import com.infosense.ibilling.server.item.PricingField;
import com.infosense.ibilling.server.item.db.ItemDTO;
import com.infosense.ibilling.server.mediation.Record;
import com.infosense.ibilling.server.order.OrderBL;
import com.infosense.ibilling.server.order.db.OrderDAS;
import com.infosense.ibilling.server.order.db.OrderDTO;
import com.infosense.ibilling.server.order.db.OrderLineDAS;
import com.infosense.ibilling.server.order.db.OrderLineDTO;
import com.infosense.ibilling.server.order.db.OrderStatusDAS;
import com.infosense.ibilling.server.pluggableTask.TaskException;
import com.infosense.ibilling.server.user.ContactBL;
import com.infosense.ibilling.server.user.ContactDTOEx;
import com.infosense.ibilling.server.user.UserDTOEx;
import com.infosense.ibilling.server.user.contact.db.ContactFieldDTO;
import com.infosense.ibilling.server.util.DTOFactory;
import com.infosense.ibilling.server.util.db.CurrencyDAS;

import java.util.ArrayList;
import java.util.List;

public class RulesItemManager extends BasicItemManager {

    private static final Logger LOG = Logger.getLogger(RulesItemManager.class);
    protected OrderManager helperOrder = null;
    private List<Record> records;

    public void addItem(Integer itemID, BigDecimal quantity, Integer language,
                        Integer userId, Integer entityId, Integer currencyId,
                        OrderDTO order, List<Record> records) throws TaskException {       
        super.addItem(itemID, quantity, language, userId, entityId, currencyId, order, records);
        this.records = records;
        helperOrder = new OrderManager(order, language, userId, entityId, currencyId);
        processRules(order);
    }

    protected void processRules(OrderDTO newOrder) throws TaskException {
        // now we have the line with good defaults, the order and the item
        // These have to be visible to the rules
        KnowledgeBase knowledgeBase;
        try {
            knowledgeBase = readKnowledgeBase();
        } catch (Exception e) {
            throw new TaskException(e);
        }
        session = knowledgeBase.newStatefulKnowledgeSession();
        List<Object> rulesMemoryContext = new ArrayList<Object>();
        rulesMemoryContext.add(helperOrder);

        // add OrderDTO to rules memory context
        newOrder.setCurrency(new CurrencyDAS().find(newOrder.getCurrency().getId()));
        if (newOrder.getCreateDate() == null) {
            newOrder.setCreateDate(new Date());
        }
        rulesMemoryContext.add(newOrder);

        for (OrderLineDTO line : newOrder.getLines()) {
            if (line.getItem() != null) {
                ItemBL item = new ItemBL(line.getItemId());
                rulesMemoryContext.add(item.getDTO(helperOrder.getLanguage(), helperOrder.getUserId(),
                        helperOrder.getEntityId(), helperOrder.getCurrencyId()));
            }
            rulesMemoryContext.add(line);
        }

        if (newOrder.getPricingFields() != null && newOrder.getPricingFields().size() > 0) {
            for (PricingField pf : newOrder.getPricingFields()) {
                rulesMemoryContext.add(pf);
            }
        }
        try {
            Integer userId = newOrder.getBaseUserByUserId().getId();
            UserDTOEx user = DTOFactory.getUserDTOEx(userId);
            rulesMemoryContext.add(user);
            ContactBL contact = new ContactBL();
            contact.set(userId);
            ContactDTOEx contactDTO = contact.getDTO();
            rulesMemoryContext.add(contactDTO);
            for (ContactFieldDTO field : (Collection<ContactFieldDTO>) contactDTO.getFieldsTable().values()) {
                rulesMemoryContext.add(field);
            }


            // Add the subscriptions
            OrderBL order = new OrderBL();
            for (OrderDTO myOrder : order.getActiveRecurringByUser(userId)) {
                for (OrderLineDTO myLine : myOrder.getLines()) {
                    rulesMemoryContext.add(new Subscription(myLine));
                }
            }
        } catch (Exception e) {
            throw new TaskException(e);
        }
        session.setGlobal("order", helperOrder);
        // then execute the rules


        executeStatefulRules(session, rulesMemoryContext);
    }

    public class OrderManager {

        private OrderDTO order = null;
        private Integer language = null;
        private Integer userId = null;
        private Integer entityId = null;
        private Integer currencyId = null;

        public OrderManager(OrderDTO order, Integer language,
                Integer userId, Integer entityId, Integer currencyId) {
            this.order = order;
            this.language = language;
            this.userId = userId;
            this.entityId = entityId;
            this.currencyId = currencyId;
        }

        public OrderDTO getOrder() {
            return order;
        }

        public void setOrder(OrderDTO order) {
            this.order = order;
        }

        public OrderLineDTO addItem(Integer itemID, Integer quantity)
                throws TaskException {
            return addItem(itemID, new BigDecimal(quantity));
        }

        public OrderLineDTO addItem(Integer itemID, BigDecimal quantity)
                throws TaskException {
            LOG.debug("Adding item " + itemID + " q: " + quantity);

            BasicItemManager helper = new BasicItemManager();
            OrderLineDTO oldLine = order.getLine(itemID);
            FactHandle h = null;
            if (oldLine != null) {
                h = handlers.get(oldLine);
            }
            helper.addItem(itemID, quantity, language, userId, entityId, currencyId, order, records);
            OrderLineDTO retValue = helper.getLatestLine();
            if (h != null) {
                LOG.debug("updating");
                session.update(h, retValue);
            } else {
                LOG.debug("inserting");
                handlers.put(retValue, session.insert(retValue));
            }

            LOG.debug("Now order line is " + retValue + " hash: " + retValue.hashCode());
            return retValue;
        }

        public OrderLineDTO addItem(Integer itemId) throws TaskException {
            return addItem(itemId, 1);
        }

        public void removeItem(Integer itemId) {
            removeObject(order.getLine(itemId));
            order.removeLine(itemId);
        }

        /**
         * Adds or updates an order line. It will calculate the amount to add based on the
         * price of the itemBase, using the price as a percentage of the itemId
         * @param itemId
         * @param itemBase
         * @return
         * @throws TaskException
         */
        public OrderLineDTO percentageIncrease(Integer itemId, Integer itemBase)
                throws TaskException {
            OrderLineDTO updateLine = order.getLine(itemId);
            if (updateLine == null) {
                // no luck, create a new one
                updateLine = addItem(itemId);
                updateLine.setAmount(BigDecimal.ZERO); // starts from 0
                updateLine.setTotalReadOnly(true);
            }

            // now add the amount based on the itemBase
            ItemBL item = new ItemBL(itemBase);
            ItemDTO itemDto = item.getDTO(language, userId, entityId, currencyId);
            BigDecimal percentage = updateLine.getItem().getPercentage();
            BigDecimal base = itemDto.getPrice();
            BigDecimal result = base.divide(new BigDecimal("100"), Constants.BIGDECIMAL_SCALE, Constants.BIGDECIMAL_ROUND)
                    .multiply(percentage).add(updateLine.getAmount());
            updateLine.setAmount(result);

            return updateLine;
        }

        /**
         * Adds or updates an order line. Calculates a percentage item order
         * line amount based on the amount of another order line. This is added
         * to the existing percentage order line's amount. 
         */
        public OrderLineDTO percentageOnOrderLine(Integer percentageItemId, 
                OrderLineDTO line) throws TaskException {
            // try to get percentage item order line
            OrderLineDTO percentageLine = order.getLine(percentageItemId);
            if (percentageLine == null) {
                // add percentage item
                percentageLine = addItem(percentageItemId);
                percentageLine.setAmount(BigDecimal.ZERO);
                percentageLine.setTotalReadOnly(true);
            }

            // now add the percentage amount based on the order line item amount
            BigDecimal percentage = percentageLine.getItem().getPercentage();
            BigDecimal base = line.getPrice().multiply(line.getQuantity());
            BigDecimal result = base.divide(new BigDecimal("100"), Constants.BIGDECIMAL_SCALE,
                    Constants.BIGDECIMAL_ROUND).multiply(percentage).add(percentageLine.getAmount());
            percentageLine.setAmount(result);

            return percentageLine;
        }

        public OrderDTO createOrder(Integer itemId, Double quantity) throws TaskException {
            BigDecimal quant = new BigDecimal(quantity).setScale(Constants.BIGDECIMAL_SCALE, Constants.BIGDECIMAL_ROUND);
            return createOrder(itemId, quant);
        }

        public OrderDTO createOrder(Integer itemId, BigDecimal quantity)
                throws TaskException {
            // copy the current order
            OrderDTO newOrder = new OrderDTO(order);
            newOrder.setId(0);
            newOrder.setVersionNum(null);
            // the period needs to be in the session
            newOrder.setOrderPeriodId(order.getOrderPeriod().getId());
            // the status should be active
            newOrder.setOrderStatus(new OrderStatusDAS().find(Constants.ORDER_STATUS_ACTIVE));
            // but without the lines
            newOrder.getLines().clear();
            // but do get the new line in
            OrderManager helper = new OrderManager(newOrder, language, userId, entityId, currencyId);
            OrderLineDTO newLine = helper.addItem(itemId, quantity);
            newLine.setPurchaseOrder(newOrder);
            newLine.setDefaults();
            
            return new OrderDAS().save(newOrder);
        }

        public void removeOrder(Integer itemId) {
            List<OrderLineDTO> list = new OrderLineDAS().findByUserItem(
                    order.getBaseUserByUserId().getId(), itemId);

            for (OrderLineDTO line : list) {
                LOG.debug("Deleting order " + line.getPurchaseOrder().getId());

                // who is the executor? we'll use the owner.. she is cancelling
                new OrderBL(line.getPurchaseOrder()).delete(order.getBaseUserByUserId().getId());
            }
        }

        public Integer getCurrencyId() {
            return currencyId;
        }

        public Integer getEntityId() {
            return entityId;
        }

        public Integer getLanguage() {
            return language;
        }

        public Integer getUserId() {
            return userId;
        }
    }
}
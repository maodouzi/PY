package com.inforstack.openstack.order.sub;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inforstack.openstack.billing.invoice.Invoice;
import com.inforstack.openstack.billing.invoice.InvoiceCount;
import com.inforstack.openstack.billing.invoice.InvoiceService;
import com.inforstack.openstack.billing.process.BillingProcess;
import com.inforstack.openstack.instance.InstanceService;
import com.inforstack.openstack.item.ItemService;
import com.inforstack.openstack.item.ItemSpecification;
import com.inforstack.openstack.log.Logger;
import com.inforstack.openstack.order.Order;
import com.inforstack.openstack.order.OrderService;
import com.inforstack.openstack.order.period.OrderPeriod;
import com.inforstack.openstack.order.period.OrderPeriodService;
import com.inforstack.openstack.payment.PaymentService;
import com.inforstack.openstack.utils.CollectionUtil;
import com.inforstack.openstack.utils.Constants;
import com.inforstack.openstack.utils.NumberUtil;
import com.inforstack.openstack.virt.domain.usage.DomainUsage;
import com.inforstack.openstack.virt.domain.usage.DomainUsageService;

@Service("subOrderService")
@Transactional
public class SubOrderServiceImpl implements SubOrderService {

	private static final Logger log = new Logger(SubOrderServiceImpl.class);
	@Autowired
	private SubOrderDao subOrderDao;
	@Autowired
	private ItemService itemService;
	@Autowired
	private OrderService orderService;
	@Autowired
	private OrderPeriodService orderPeriodService;
	@Autowired
	private InvoiceService invoiceService;
	@Autowired
	private PaymentService paymentService;
	@Autowired
	private InstanceService instanceService;
	@Autowired
	private DomainUsageService domainUsageService;
	
	@Override
	public SubOrder createSubOrder(int itemId, int orderId,
			int periodId) {
		log.debug("Create sub order for order : " + orderId);
		ItemSpecification item = itemService.getItemSpecification(itemId);
		if(item == null){
			log.info("Create sub order failed for no item found by id : " + itemId);
			return null;
		}
		
		Order order = orderService.findOrderById(orderId);
		if(order == null){
			log.info("Create sub order failed for no order found by id : " + orderId);
			return null;
		}
		
		OrderPeriod period = orderPeriodService.findPeriodById(periodId);
		if(period == null){
			log.info("Create sub order failed for no period found by id : " + periodId);
			return null;
		}
		
		SubOrder so = new SubOrder();
		so.setItem(item);
		so.setOrder(order);
		so.setOrderPeriod(period);
		so.setStatus(Constants.SUBORDER_STATUS_NEW);
		subOrderDao.persist(so);
		
		log.debug("Create sub order successfully");
		return so;
	}

	@Override
	public List<SubOrder> findSubOrders(Integer orderId, List<Integer> statuses, List<Integer> orderPeriods) {
		log.debug("Find all sub orders by order id : " + orderId + ", status : " + statuses + ", periods : " + orderPeriods);
		List<SubOrder> subOrders = subOrderDao.find(orderId, statuses, orderPeriods);
		if(CollectionUtil.isNullOrEmpty(subOrders)){
			log.debug("No instance found");
			return new ArrayList<SubOrder>();
		}else{
			log.debug("Find successfully");
			return subOrders;
		}
	}
	
	@Override
	public SubOrder findSubOrderById(int subOrderId){
		log.debug("Find sub order by id : " + subOrderId);
		SubOrder suborder = subOrderDao.findById(subOrderId);
		if(suborder == null){
			log.info("Find sub order failed");
		}else{
			log.debug("Find sub order successfully");
		}
		
		return suborder;
	}

	@Override
	public SubOrder deleteSubOrder(int subOrderId) {
		log.debug("Delete order ");
		SubOrder subOrder = subOrderDao.findById(subOrderId);
		if(subOrder == null){
			log.info("delete sub order failed for no instance found by sub order id : " + subOrderId);
			return null;
		}
		subOrder.setStatus(Constants.SUBORDER_STATUS_DELETED);
		
		log.debug("Delete sub order successfully");
		return subOrder;
	}

	@Override
	public List<Period> calcPeriod(SubOrder subOrder, Date billingDate, Date endLimit) {
		log.debug("Calculate period for sub order : " + subOrder.getId());
		List<Period> periodLst = new ArrayList<Period>();
		Date nextBillingDate = subOrder.getNextBillingDate();
		if(nextBillingDate.after(billingDate)){
			return periodLst;
		}
		
		Calendar calendar = Calendar.getInstance();
		OrderPeriod op = subOrder.getOrderPeriod();
		int pType = op.getPeriodType();
		int pQquantity = op.getPeriodQuantity();
		
		if(subOrder.getType() == Constants.SUBORDER_TYPE_PREPAID){
			do {
				Period period = new Period();
				period.setStart(calendar.getTime());
				calendar.add(pType, pQquantity);
				
				nextBillingDate = calendar.getTime();
				
				calendar.add(Calendar.SECOND, -1);
				Date periodEnd = calendar.getTime();
				period.setPeriodEnd(periodEnd);
				if(endLimit != null && endLimit.before(periodEnd)){
					periodEnd = endLimit;
				}
				period.setEnd(periodEnd);
				
				periodLst.add(period);
			} while (billingDate.after(nextBillingDate));
		}else if(subOrder.getType() == Constants.SUBORDER_TYPE_POSTPAID){
			do {
				Period period = new Period();
				calendar.add(pType, -pQquantity);
				period.setStart(calendar.getTime());
				
				calendar.setTime(nextBillingDate);
				calendar.add(Calendar.SECOND, -1);
				Date periodEnd = calendar.getTime();
				period.setPeriodEnd(periodEnd);
				if(endLimit != null && endLimit.before(periodEnd)){
					periodEnd = endLimit;
				}
				period.setEnd(periodEnd);
				
				calendar.setTime(nextBillingDate);
				calendar.add(pType, pQquantity);
				nextBillingDate = calendar.getTime();
				
				periodLst.add(period);
			} while (billingDate.after(nextBillingDate));
		}
		
		log.debug(periodLst.size() + " period(s) found");
		
		return periodLst;
	}

	@Override
	public InvoiceCount billingProcessSubOrder(SubOrder subOrder, Boolean autoPay, Date billingDate, BillingProcess billingProcess) {
		log.debug("Pay sub order : " + subOrder.getId() + " with bill date : " + billingDate + 
				", billing process : " + billingProcess==null?null:billingProcess.getId());
		
		InvoiceCount ic = new InvoiceCount();
		Order order = subOrder.getOrder();
		List<Period> periods = this.calcPeriod(subOrder,billingDate, order.getActiveEnd());
		
		boolean doPay = order.getAutoPay();
		if(autoPay != null){
			doPay = autoPay.booleanValue();
		}
		for(int i=0, size=periods.size();i<size;){
			Period period = periods.get(i);
			BigDecimal price = this.getPrice(subOrder, period);
			Invoice invoice = invoiceService.createInvoice(period.getStart(), period.getEnd(), price, order.getTenant(), subOrder, order, billingProcess);
			if(doPay){
				paymentService.applyPayment(invoice, Constants.PAYMENT_TYPE_SUBORDER, subOrder.getOrderPeriod().getPayAsYouGo());
			}
			ic.addInvoiceTotal(invoice.getAmount());
			ic.addBalance(invoice.getBalance());
			
			i++;
			if(i == size){
				if(order.getActiveEnd() != null && !period.getEnd().before(order.getActiveEnd())){
					subOrder.setStatus(Constants.SUBORDER_STATUS_END);
				}else{
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(subOrder.getNextBillingDate());
					calendar.add(subOrder.getOrderPeriod().getPeriodType(), subOrder.getOrderPeriod().getPeriodType());
					subOrder.setNextBillingDate(calendar.getTime());
				}
				
				subOrder.setLastBillingTime(new Date());
			}
		}
		
		return ic;
	}

	@Override
	public BigDecimal getPrice(SubOrder subOrder, Period period) {
		BigDecimal price = BigDecimal.ZERO;
		if(subOrder.getOrderPeriod().getPayAsYouGo()){
			Map<String, Float> priceMap = new HashMap<String, Float>();
			if(subOrder.getUuid() != null){
				priceMap = instanceService.getUsagePrice(subOrder.getUuid());
			}
			DomainUsage usage = domainUsageService.sumUsage(subOrder.getUuid(), period.getStart(), period.getEnd());
			for(String item : priceMap.keySet()){
				if(Constants.USAGE_CPU.equals(item)){
					price = price.add(
							new BigDecimal(priceMap.get(item)).multiply(
									new BigDecimal(usage.getCpuTime())));
				}else if(Constants.USAGE_DISK.equals(item)){
					price = price.add(
							new BigDecimal(priceMap.get(item)).multiply(
									new BigDecimal(
											usage.getDiskReadBytes() +
											usage.getDiskWriteBytes())));
				}else if(Constants.USAGE_MEMORY.equals(item)){
					price = price.add(
							new BigDecimal(priceMap.get(item)).multiply(
									new BigDecimal(usage.getMemoryUsed())));
				}else if(Constants.USAGE_NETWORK.equals(item)){
					price = price.add(
							new BigDecimal(priceMap.get(item)).multiply(
									new BigDecimal(
											usage.getInterfaceReadBytes() + 
											usage.getInterfaceWriteBytes())));
				}
			}
		}else{
			price =  subOrder.getPrice();
			if(period.wholePeriod() == false){
				price = price.multiply(period.percentInPeriod());
			}
		}
		
		price.round(new MathContext(2));
		return price;
	}

	@Override
	public SubOrder findFirstSubOrderByInstanceId(int id) {
		return subOrderDao.fetchOneByInstanceId(id);
	}

}

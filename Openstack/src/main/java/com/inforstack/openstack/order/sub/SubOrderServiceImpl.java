package com.inforstack.openstack.order.sub;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inforstack.openstack.item.ItemService;
import com.inforstack.openstack.item.ItemSpecification;
import com.inforstack.openstack.order.Order;
import com.inforstack.openstack.order.OrderService;
import com.inforstack.openstack.order.period.OrderPeriod;
import com.inforstack.openstack.order.period.OrderPeriodService;
import com.inforstack.openstack.utils.CollectionUtil;
import com.inforstack.openstack.utils.Constants;
import com.inforstack.openstack.utils.StringUtil;

@Service
@Transactional
public class SubOrderServiceImpl implements SubOrderService {

	private static final Log log = LogFactory.getLog(SubOrderServiceImpl.class);
	@Autowired
	private SubOrderDao subOrderDao;
	@Autowired
	private ItemService itemService;
	@Autowired
	private OrderService orderService;
	@Autowired
	private OrderPeriodService orderPeriodService;
	
	@Override
	public SubOrder createSubOrder(int itemId, String orderId,
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
	public List<SubOrder> findSubOrders(String orderId, int status) {
		log.debug("Find all sub orders by order id : " + orderId + ", status : " + status);
		List<SubOrder> subOrders = subOrderDao.find(orderId, status);
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
	
	
}
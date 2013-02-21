package com.inforstack.openstack.order;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.inforstack.openstack.basic.BasicDaoImpl;
import com.inforstack.openstack.controller.model.PaginationModel;
import com.inforstack.openstack.log.Logger;
import com.inforstack.openstack.utils.CollectionUtil;

@Repository
public class OrderDaoImpl extends BasicDaoImpl<Order> implements OrderDao {

	private static final Logger log = new Logger(OrderDaoImpl.class);
	
	@Override
	public List<Order> find(Integer tenantId, Integer status) {
		log.debug("Find all order(s) by tenant id : " + tenantId + ", status : " + status);
		try {
			CriteriaBuilder builder = em.getCriteriaBuilder();
			CriteriaQuery<Order> criteria = builder
					.createQuery(Order.class);
			Root<Order> root = criteria.from(Order.class);
			List<Predicate> predicates = new ArrayList<Predicate>();
			if(tenantId != null){
				predicates.add(
						builder.equal(root.get("tenant.id"), tenantId)
				);
			}
			if(status != null){
				predicates.add(
						builder.equal(root.get("status"), status)
				);
			}
			if(predicates.isEmpty()){
				criteria.select(root);
			}else{
				criteria.select(root).where(
					builder.and(predicates.toArray(new Predicate[predicates.size()]))
				);
			}
			List<Order> instances = em.createQuery(criteria).getResultList();
			if(CollectionUtil.isNullOrEmpty(instances)){
				log.debug("No record found");
				return instances;
			}
			log.debug("Find successful");
			return instances;
		} catch (RuntimeException re) {
			log.error(re.getMessage(), re);
			throw re;
		}
	}

	@Override
	public PaginationModel<Order> findWithCreator(int pageIndex, int pageSize, Integer tenantId,
			Integer status) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Order> criteria = builder
				.createQuery(Order.class);
		List<Predicate> predicates = new ArrayList<Predicate>();
		Root<Order> root = criteria.from(Order.class);
		if(tenantId != null){
			predicates.add(
					builder.equal(root.get("tenant.id"), tenantId)
			);
		}
		if(status != null){
			predicates.add(
					builder.equal(root.get("status"), status)
			);
		}
		Predicate predicate =  builder.and(predicates.toArray(new Predicate[predicates.size()]));
		javax.persistence.criteria.Order[] orders = new javax.persistence.criteria.Order[]{
				builder.desc(root.get("createTime"))	
		};
		
		return this.getSelf().pagination(pageIndex, pageSize, predicate, orders);
	}

	@Override
	public PaginationModel<Order> findAllWithoutSubOrder(int pageIndex, int pageSize) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Order> criteria = builder
				.createQuery(Order.class);
		Root<Order> root = criteria.from(Order.class);
		javax.persistence.criteria.Order[] orders = new javax.persistence.criteria.Order[]{
				builder.desc(root.get("createTime"))	
		};
		
		return this.getSelf().pagination(pageIndex, pageSize, null, orders);
	}
	
}

package com.inforstack.openstack.order.period;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.inforstack.openstack.basic.BasicDaoImpl;
import com.inforstack.openstack.log.Logger;
import com.inforstack.openstack.utils.CollectionUtil;

@Repository
public class OrderPeriodDaoImpl extends BasicDaoImpl<OrderPeriod> implements OrderPeriodDao {
	
	private static final Logger log = new Logger(OrderPeriodDaoImpl.class);

	@Override
	public List<OrderPeriod> findAll(boolean includeDeleted) {
		log.debug("Find all period instance(s)  " + (includeDeleted?"include deleted":"exclude deleted"));
		try {
			CriteriaBuilder builder = em.getCriteriaBuilder();
			CriteriaQuery<OrderPeriod> criteria = builder
					.createQuery(OrderPeriod.class);
			Root<OrderPeriod> root = criteria.from(OrderPeriod.class);
			if(includeDeleted){
				criteria.select(root);
			}else{
				criteria.select(root).where(
						builder.equal(root.get("deleted"), false)
				);
			}
			List<OrderPeriod> instances = em.createQuery(criteria).getResultList();
			if(CollectionUtil.isNullOrEmpty(instances)){
				log.debug("No record found");
				return instances;
			}
			log.debug("get successful");
			return instances;
		} catch (RuntimeException re) {
			log.error(re.getMessage(), re);
			throw re;
		}
	}

}

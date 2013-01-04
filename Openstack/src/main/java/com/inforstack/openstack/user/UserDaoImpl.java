package com.inforstack.openstack.user;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;



import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.inforstack.openstack.security.auth.OpenstackUserDetailsService;


@Repository
public class UserDaoImpl implements UserDao {
	
	private static final Log log = LogFactory.getLog(UserDaoImpl.class);
	@Autowired
	private EntityManager em;

	@Override
	public User findUser(Integer userId) {
		log.debug("getting User instance with id: " + userId);
		if(userId==null) return null;
		
		try {
			User instance = em.find(User.class, userId);
			log.debug("get successful");
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	@Override
	public User findByName(String userName) {
		log.debug("getting User instance with name : " + userName);
		if(userName==null)  return null;
		
		try {
			CriteriaBuilder builder = em.getCriteriaBuilder();
			CriteriaQuery<User> criteria = builder
					.createQuery(User.class);
			Root<User> user = criteria.from(User.class);
			criteria.select(user).where(builder.equal(user.get("name"), userName));;
			List<User> instances = em.createQuery(criteria).getResultList();
			if(instances!=null && instances.size()>0){
				log.debug("get successful");
				return instances.get(0);
			}
			log.debug("No record found for name : " + userName);
			return null;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}


}
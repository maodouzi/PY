package com.inforstack.openstack.utils;

import java.util.Collection;

public class CollectionUtil {

	public static boolean isNullOrEmpty(Collection<Object> collection){
		if(collection==null || collection.isEmpty()){
			return true;
		}
		return false;
	}
}

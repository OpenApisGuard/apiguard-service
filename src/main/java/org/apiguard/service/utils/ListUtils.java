package org.apiguard.service.utils;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {
	
	public static <E> List<E> getList(Iterable<E> iter) {
	    List<E> list = new ArrayList<E>();
	    for (E item : iter) {
	        list.add(item);
	    }
	    return list;
	}

}

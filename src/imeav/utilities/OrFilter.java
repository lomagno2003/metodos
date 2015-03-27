package imeav.utilities;

import java.util.Arrays;
import java.util.List;

public class OrFilter<T> implements Filter<T> {
	private List<Filter<T>> filters;
	
	@SafeVarargs
	public OrFilter(Filter<T> ...filters) {
		this.filters = Arrays.asList(filters);
	}
	
	@Override
	public Boolean evaluate(T elem) {
		for(Filter<T> filter: filters){
			if(filter.evaluate(elem)){
				return true;
			}
		}
		
		return false;
	}
}

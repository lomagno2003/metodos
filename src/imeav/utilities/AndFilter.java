package imeav.utilities;

import java.util.Arrays;
import java.util.List;

public class AndFilter<T> implements Filter<T> {
	private List<Filter<T>> filters;
	
	@SafeVarargs
	public AndFilter(Filter<T> ...filters) {
		this.filters = Arrays.asList(filters);
	}
	
	@Override
	public Boolean evaluate(T elem) {
		for(Filter<T> filter: filters){
			if(!filter.evaluate(elem)){
				return false;
			}
		}
		
		return true;
	}
}

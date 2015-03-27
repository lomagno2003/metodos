package imeav.utilities;

public interface Filter<T> {
	public Boolean evaluate(T elem);
}

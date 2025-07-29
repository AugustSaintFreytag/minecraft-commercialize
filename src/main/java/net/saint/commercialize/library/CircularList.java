package net.saint.commercialize.library;

import java.util.List;

public class CircularList<T> {

	// Properties

	private List<T> elements;
	private int currentIndex;

	// Init

	public CircularList(List<T> elements, int currentIndex) {
		this.elements = List.copyOf(elements);
		this.currentIndex = currentIndex;
	}

	public CircularList(List<T> elements, T currentElement) {
		this(elements, validateAndGetIndex(elements, currentElement));
	}

	public CircularList(List<T> elements) {
		this(elements, 0);
	}

	public CircularList(T[] elements) {
		this(List.of(elements), 0);
	}

	// Access

	public T get() {
		return elements.get(currentIndex);
	}

	public int size() {
		return elements.size();
	}

	public boolean contains(T element) {
		return elements.contains(element);
	}

	// Mutation

	public void advance() {
		var nextIndex = (currentIndex + 1) % elements.size();
		this.currentIndex = nextIndex;
	}

	public void select(T element) {
		var index = elements.indexOf(element);

		if (index == -1) {
			throw new IllegalArgumentException("Can not construct circular list with current element not in list: " + element + ".");
		}

		this.currentIndex = index;
	}

	// Hash

	public int hashCode() {
		return elements.hashCode() ^ Integer.hashCode(currentIndex);
	}

	// Utility

	private static <T> int validateAndGetIndex(List<T> elements, T currentElement) {
		var index = elements.indexOf(currentElement);

		if (index == -1) {
			throw new IllegalArgumentException("Can not construct circular list with current element not in list: " + currentElement + ".");
		}

		return index;
	}

}

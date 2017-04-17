package io.github.robotpy.magicbot.exceptions;

public class MultipleDefaultStatesError extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public MultipleDefaultStatesError(String message) {
		super(message);
	}
}

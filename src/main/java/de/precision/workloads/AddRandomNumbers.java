package de.precision.workloads;

import java.util.Random;

/**
 * Adds random numbers
 * 
 * @author reichelt
 *
 */
public class AddRandomNumbers {
	int x = 0;

	public void addSomething() {
		x += new Random().nextInt(100);
	}

	public int getValue() {
		return x;
	}
}

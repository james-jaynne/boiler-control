package uk.co.jaynne.gpio;

import framboos.InPin;
import framboos.OutPin;

/**
 * GpioControl using the Framboos library https://github.com/jkransen/framboos 
 * @author james
 *
 */
public class GpioControlFramboos implements GpioControl{
	
	private InPin[] inpins;
	private OutPin[] outpins;
	private int noPins = 26;
	
	private GpioControlFramboos() {
		inpins = new InPin[noPins];
		outpins = new OutPin[noPins];
	}
	
	private static class SingletonHolder { 
        public static final GpioControlFramboos INSTANCE = new GpioControlFramboos();
	}

	public static GpioControlFramboos getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	@Override
	public GpioPin setAsOutput(GpioPin pin) {
		int pinNo = getPin(pin);
		//Check if this pin is assigned as an in pin
		if (isInPin(pin)) {
			close(pin);
		}
		//Check if this pin is open already
		if (!isOutPin(pin)) {
			outpins[pinNo] = new OutPin(pinNo);
		}
		return pin;
	}

	@Override
	public GpioPin setAsInput(GpioPin pin) {
		int pinNo = getPin(pin);
		//Check if this pin is assigned as an in pin
		if (isOutPin(pin)) {
			close(pin);
		}
		//Check if this pin is open already
		if (!isInPin(pin)) {
			inpins[pinNo] = new InPin(pinNo);
		}
		return pin;
	}

	@Override
	public GpioPin setValue(GpioPin pin, boolean value) {
		//Check if this pin is assigned as an out pin
		if (isInPin(pin)) {
			close(pin);
		}
		if (!isOutPin(pin)) {
			setAsOutput(pin);
		}
		outpins[getPin(pin)].setValue(value);
		return pin;
	}

	@Override
	public boolean getValue(GpioPin pin) {
		//Check if this pin is assigned as an out pin
		if (isOutPin(pin)) {
			close(pin);
		}
		//Check and set as input
		if (!isInPin(pin)) {
			setAsInput(pin);
		}
		return inpins[getPin(pin)].getValue();
	}

	@Override
	public void close(GpioPin pin) {
		//Close if outpin
		if (isOutPin(pin)) {
			outpins[getPin(pin)].close();
			outpins[getPin(pin)] = null;
		}
		if (isInPin(pin)) {
			inpins[getPin(pin)].close();
			inpins[getPin(pin)] = null;
		}
	}
	
	public boolean isInPin(GpioPin pin) {
		return (inpins[getPin(pin)] != null);
	}
	
	public boolean isOutPin(GpioPin pin) {
		return (outpins[getPin(pin)] != null);
	}
	
	/**
	 * Translates the enum to the pin numbers used by the Framboos library
	 * @param pin GpioPin
	 * @return int pin no
	 */
	private int getPin(GpioPin pin) { //TODO include all pins
		switch(pin) {
			case PIN8_GPIO14: return 15;
			case PIN10_GPIO15: return 16;
			case PIN12_GPIO18: return 1;
			case PIN13_GPIO21: return 2;
			case PIN16_GPIO23: return 4;
			case PIN18_GPIO24: return 5;
			case PIN19_GPIO10: return 12;
			case PIN22_GPIO25: return 6;
			case PIN24_GPIO8: return 10;
			case PIN26_GPIO7: return 11;
			default: return 9999;
		}
	}

}

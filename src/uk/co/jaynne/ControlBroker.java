package uk.co.jaynne;

import java.util.Calendar;

import uk.co.jaynne.dataobjects.ConfigObject;
import uk.co.jaynne.datasource.ConfigSqlSource;
import uk.co.jaynne.datasource.interfaces.ConfigSource;
import uk.co.jaynne.gpio.GpioControl;
import uk.co.jaynne.gpio.GpioControlPi4J;
import uk.co.jaynne.gpio.GpioPin;

public class ControlBroker {
	public static GpioPin RELAY2 = GpioPin.PIN18_GPIO24; //heating
	public static GpioPin RELAY1 = GpioPin.PIN23_GPIO11; //water
	public static GpioPin SWITCH1 = GpioPin.PIN18_GPIO24;
	public static GpioPin SWITCH2 = GpioPin.PIN26_GPIO7;
	private static boolean RELAY_ON = true;
	private static boolean RELAY_OFF = false;
	private ConfigSource config;
	private boolean heatingOn = false;
	private boolean heatingOnBoost = false;
	private long heatingBoostOffTime = 0;
	private boolean waterOn = false;
	private boolean waterOnBoost = false;
	private long waterBoostOffTime = 0;
	
	
	private GpioControl gpio;
	
	private ControlBroker() {
		gpio = GpioControlPi4J.getInstance();
		config = new ConfigSqlSource();
		
    	//Start with water and heating off incase they have been left in an improper state
		deactivateHeating();
		deactivateWater();
	}
	
	private static class SingletonHolder { 
        public static final ControlBroker INSTANCE = new ControlBroker();
	}

	public static ControlBroker getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public GpioControl getGpio() {
		return gpio;
	}
	
	public boolean turnHeatingOn() {
		//check if the heating is already on
		if (isHeatingOn()) {
			return true;
		} // don't turn on if this is a holiday period
		if (isHolidayPeriod()) {
			return false;
		}
		
		return activateHeating();
	}
	
	private boolean activateHeating() {
		gpio.setValue(RELAY2, RELAY_ON);
		heatingOn = true;
		System.out.println("**HEATING ON, ITS GONA GET TOASTY**");
		return true;
	}
	
	public boolean turnHeatingOff() {
		if (!isHeatingOn()) {
			return true;
		}
		if (isHeatingBoostOn()) { //don't turn off if boosted
			return false;
		}
		return deactivateHeating();
	}
	
	private boolean deactivateHeating() {
		gpio.setValue(RELAY2, RELAY_OFF);
		heatingOn = false;
		System.out.println("**HEATING OFF, BRR**");
		return true;
	}
	
	public boolean turnWaterOn() {
		//check if the water is already on
		if (isWaterOn()) {
			return true;
		} // don't turn on if this is a holiday period
		if (isHolidayPeriod()) {
			return false;
		}
		
		return activateWater();
	}
	
	private boolean activateWater() {
		gpio.setValue(RELAY1, RELAY_ON);
		waterOn = true;
		System.out.println("**WATER ON, YOU CAN SHOWER IN A BIT**");
		return true;
	}
	
	public boolean turnWaterOff() {
		if (!isWaterOn()) {
			return true;
		}
		if (isWaterBoostOn()) {
			return false;
		}
		return deactivateWater();
	}
	
	private boolean deactivateWater() {
		gpio.setValue(RELAY1, RELAY_OFF);
		waterOn = false;
		System.out.println("**WATER OFF, BETTER BE CLEAN ALREADY**");
		return true;
	}
	
	public boolean isHolidayPeriod(){
		ConfigObject holidayFrom = config.get("holidayFrom");
		ConfigObject holidayUntil = config.get("holidayUntil");
		if (holidayFrom != null && holidayUntil != null) {
			return Calendar.getInstance().getTimeInMillis() > holidayFrom.getLongValue() && 
					Calendar.getInstance().getTimeInMillis() < holidayUntil.getLongValue();
		} else {
			return false;
		}
		
	}
	
	public boolean toggleWaterBoostStatus() {
		int boostTimeInMins = config.get("boostTime").getIntValue();
		return toggleWaterBoostStatus(boostTimeInMins);
	}
	
	public boolean toggleWaterBoostStatus(int minutes) {
		if (isWaterBoostOn()) { //ON so turn off
			System.out.println("**Water BOOST TOGGLED OFF**");
			waterOnBoost = false;
			turnWaterOff();
			return isWaterOn();
		} else if (isHolidayPeriod()) {
			System.out.println("**WATER BOOST IS ON HOLIDAY**");
			turnWaterOff();
			return isWaterOn();
		} else {
			System.out.println("**WATER BOOST TOGGLED ON**");
			
			long thistime = Calendar.getInstance().getTimeInMillis();
			long boostTimeInMillis = minutes * 60 * 1000;
			waterBoostOffTime = thistime + boostTimeInMillis;
			waterOnBoost = true;
			turnWaterOn();
			return isWaterOn();
		}
	}
	
	/**
	 * Gets the water boost off time in millis since the 'epoch'
	 * @return long
	 */
	public long getWaterBoostOffTime() {
		return waterBoostOffTime;
	}
	
	public boolean isWaterBoostOn() {
		return waterOnBoost;
	}
	
	public boolean toggleHeatingBoostStatus() {
		int boostTimeInMins = config.get("boostTime").getIntValue();
		return toggleHeatingBoostStatus(boostTimeInMins);
	}
	
	public boolean toggleHeatingBoostStatus(int minutes) {
		if (isHeatingBoostOn()) { //ON so turn off
			System.out.println("**HEATING BOOST TOGGLED OFF**");
			heatingOnBoost = false;
			turnHeatingOff();
			return isHeatingOn();
		} else if (isHolidayPeriod()) {
			System.out.println("**HEATING BOOST IS ON HOLIDAY**");
			turnHeatingOff();
			return isHeatingOn();
		} else {
			System.out.println("**HEATING BOOST TOGGLED ON**");
			
			long thistime = Calendar.getInstance().getTimeInMillis();
			long boostTimeInMillis = minutes * 60 * 1000;
			heatingBoostOffTime = thistime + boostTimeInMillis;
			heatingOnBoost = true;
			turnHeatingOn();
			return isHeatingOn();
		}
	}
	
	/**
	 * Gets the water boost off time in millis since the 'epoch'
	 * @return long
	 */
	public long getHeatingBoostOffTime() {
		return heatingBoostOffTime;
	}
	
	public boolean isHeatingBoostOn() {
		return heatingOnBoost;
	}
	
	public boolean isHeatingOn() {
		return heatingOn;
	}
	
	public boolean isWaterOn() {
		return waterOn;
	}
	
	/**
	 * Open GPIO connections
	 */
	public void open() {
		gpio.setAsOutput(RELAY2);
		gpio.setAsOutput(RELAY1);
	}
	
	/**
	 * Close GPIO connections
	 */
	public void close() {
		gpio.close(RELAY2);
		gpio.close(RELAY1);
	}
}

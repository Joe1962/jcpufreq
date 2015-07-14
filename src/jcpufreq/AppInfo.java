/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcpufreq;

/**
 *
 * @author joe1962
 */
public class AppInfo {
	// App constants:
	private static final String TITLE = "JCpuFreq";
	private static final String VERSION = "2.00";
	private static final String BUILD = "150428.01";

	/**
	 * @return the TITLE
	 */
	public static String getTITLE() {
		return TITLE;
	}

	/**
	 * @return the VERSION
	 */
	public static String getVERSION() {
		return VERSION;
	}

	/**
	 * @return the BUILD
	 */
	public static String getBUILD() {
		return BUILD;
	}



	public static AppInfo getInstance() {
		return infoHolder.INSTANCE;
	}

	private AppInfo() {
	}

	private static class infoHolder {

		private static final AppInfo INSTANCE = new AppInfo();
	}
}

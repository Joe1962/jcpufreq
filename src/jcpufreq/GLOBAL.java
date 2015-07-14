/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcpufreq;

import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author joe1962
 */
public class GLOBAL {

//public static final String INIFIle = ".vcpufreq.conf"
public static final File INIFIle = new File("/etc/vcpufreq.conf");
public static final File  BasePath = new File("/sys/devices/system/cpu/");

//public static String CPUFreqPath;
public static boolean isRoot;
public static boolean LinkedFreq;

// Config. load arrays:
public static ArrayList<String> AvailableGovs = new ArrayList<>();;
public static ArrayList<String> AvailableFreqs = new ArrayList<>();;
public static ArrayList<String> AvailableFreqsPretty = new ArrayList<>();;
public static ArrayList<String> txtScalingSetSpeed = new ArrayList<>();;

// Config. load vars:
public static int vCPUCount;				// Holds count of CPUs/cores/HTs.
public static boolean vCPUMulti;			// If TRUE, need to handle CPUs separately.
public static String vScalingDriver;
public static String vScalingGov;
public static int vCPUFreq;
public static int vCPUFreqMax;
public static int vCPUFreqMin;
public static int vCPUFreqMaxLimit;
public static int vCPUFreqMinLimit;
public static String vAutoRefreshRate;
public static boolean vSaveOnExit;
public static boolean vLoadOnStart;





	public static GLOBAL getInstance() {
		return GLOBALHolder.INSTANCE;
	}

	private GLOBAL() {
	}

	private static class GLOBALHolder {

		private static final GLOBAL INSTANCE = new GLOBAL();
	}
}

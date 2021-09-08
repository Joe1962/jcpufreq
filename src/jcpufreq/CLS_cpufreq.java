/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcpufreq;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import jsoft.jutils.CONSTS;
import static jsoft.jutils.SUB_FileIO.FileExists;
import static jsoft.jutils.SUB_FileIO.FileIsReadable;
import static jsoft.jutils.SUB_FileIO.FileIsWritable;
import static jsoft.jutils.SUB_FileIO.readLinesFromFile;
import static jsoft.jutils.SUB_FileIO.writeStringToFileWithSysEnc;
import jsoft.jutils.SUB_Popups;
import types.TYP_AutoUnitsFreq;

/**
 *
 * @author joe1962
 */
public class CLS_cpufreq {

	public static int CPUCount() {
		int iCount = 0;
		int iCountNot = 0;

		// Reset CPU counter:
		GLOBAL.vCPUCount = 0;

		//File[] MyFiles = GLOBAL.BasePath.listFiles((FileFilter) new cupDirsFilter());
		File[] MyFiles = GLOBAL.BasePath.listFiles();

		for (File MyDir : MyFiles) {
			if (MyDir.isDirectory()) {
				if (MyDir.getName().startsWith("cpu")) {
					try {
						int tmpInt = Integer.parseInt(MyDir.getName().substring(MyDir.getName().length() - 1));
						iCount++;
					} catch (NumberFormatException numberFormatException) {
						iCountNot++;
					}
				}
			}
		}

		//System.out.println("CPU count YES: " + iCount);
		//System.out.println("CPU count NOT: " + iCountNot);

		return iCount;
	}

	public static Boolean CheckMultiCPUs() {
		if (GLOBAL.vCPUCount > 1) {
			// Get affected_cpus:
			String CPUFreqPath = GLOBAL.BasePath + "/cpu0" + "/cpufreq";
			String[] arrAffectedCPUs = null;
			File tmpFile = new File(CPUFreqPath + "/affected_cpus");
			if (FileExists(tmpFile)) {
				if (FileIsReadable(tmpFile)) {
					ArrayList<String> LinesRead;
					try {
						LinesRead = readLinesFromFile(tmpFile);
					} catch (FileNotFoundException ex) {
						Logger.getLogger(CLS_cpufreq.class.getName()).log(Level.SEVERE, null, ex);
						GLOBAL.vCPUMulti = false;			// Assume no need for separate handling.
						SUB_Popups.MsgErrorOK(FRM_Main.getFrames()[0],"" , "File not found...");
						return null;
					}
					if (LinesRead != null && LinesRead.size() > 0) {
						arrAffectedCPUs = LinesRead.get(0).split(",");
						//  Count AffectedCPUs:
						if (GLOBAL.vCPUCount == arrAffectedCPUs.length) {
							return false;
						} else {
							return true;
						}
					} else {
						return false;
					}
				} else {
					return null;
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public static Integer getScalingCurrFreq(int CPU) {
		String CPUFreqPath = GLOBAL.BasePath + "/cpu" + String.valueOf(CPU) + "/cpufreq";
		File tmpFile = new File(CPUFreqPath + "/scaling_cur_freq");
		if (FileExists(tmpFile)) {
			if (FileIsReadable(tmpFile)) {
				ArrayList<String> LinesRead;
				try {
					LinesRead = readLinesFromFile(tmpFile);
				} catch (FileNotFoundException ex) {
					Logger.getLogger(CLS_cpufreq.class.getName()).log(Level.SEVERE, null, ex);
					GLOBAL.vCPUMulti = false;			// Assume no need for separate handling.
					SUB_Popups.MsgErrorOK(FRM_Main.getFrames()[0],"" , "File not found...");
					return null;
				}
				if (LinesRead != null && LinesRead.size() > 0) {
					return Integer.parseInt(LinesRead.get(0));
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
		return null;
	}

	public static String getScalingDriver(int CPU) {
		String CPUFreqPath = GLOBAL.BasePath + "/cpu" + String.valueOf(CPU) + "/cpufreq";
		File tmpFile = new File(CPUFreqPath + "/scaling_driver");
		if (FileExists(tmpFile)) {
			if (FileIsReadable(tmpFile)) {
				ArrayList<String> LinesRead;
				try {
					LinesRead = readLinesFromFile(tmpFile);
				} catch (FileNotFoundException ex) {
					Logger.getLogger(CLS_cpufreq.class.getName()).log(Level.SEVERE, null, ex);
					SUB_Popups.MsgErrorOK(FRM_Main.getFrames()[0],"" , "File not found...");
					return null;
				}
				if (LinesRead != null && LinesRead.size() > 0) {
					return LinesRead.get(0);
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
		return CONSTS.EMPTY_STRING;
	}

	public static ArrayList<String> getScalingAvailableGovernors(int CPU) {
		String CPUFreqPath = GLOBAL.BasePath + "/cpu" + String.valueOf(CPU) + "/cpufreq";
		File tmpFile = new File(CPUFreqPath + "/scaling_available_governors");
		if (FileExists(tmpFile)) {
			if (FileIsReadable(tmpFile)) {
				ArrayList<String> LinesRead;
				try {
					LinesRead = readLinesFromFile(tmpFile);
				} catch (FileNotFoundException ex) {
					Logger.getLogger(CLS_cpufreq.class.getName()).log(Level.SEVERE, null, ex);
					GLOBAL.vCPUMulti = false;			// Assume no need for separate handling.
					SUB_Popups.MsgErrorOK(FRM_Main.getFrames()[0],"" , "File not found...");
					return null;
				}
				if (LinesRead != null && LinesRead.size() > 0) {
					String[] tmpArr = LinesRead.get(0).split(" ");
					LinesRead = new ArrayList<>();
					LinesRead.addAll(Arrays.asList(tmpArr));
					return LinesRead;
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
		return null;
	}

	public static String getScalingGovernor(int CPU) {
		String CPUFreqPath = GLOBAL.BasePath + "/cpu" + String.valueOf(CPU) + "/cpufreq";
		File tmpFile = new File(CPUFreqPath + "/scaling_governor");
		if (FileExists(tmpFile)) {
			if (FileIsReadable(tmpFile)) {
				ArrayList<String> LinesRead;
				try {
					LinesRead = readLinesFromFile(tmpFile);
				} catch (FileNotFoundException ex) {
					Logger.getLogger(CLS_cpufreq.class.getName()).log(Level.SEVERE, null, ex);
					SUB_Popups.MsgErrorOK(FRM_Main.getFrames()[0],"" , "File not found...");
					return null;
				}
				if (LinesRead != null && LinesRead.size() > 0) {
					return LinesRead.get(0);
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
		return CONSTS.EMPTY_STRING;
	}

	public static Integer getScalingMaxFreq(int CPU) {
		String CPUFreqPath = GLOBAL.BasePath + "/cpu" + String.valueOf(CPU) + "/cpufreq";
		File tmpFile = new File(CPUFreqPath + "/scaling_max_freq");
		if (FileExists(tmpFile)) {
			if (FileIsReadable(tmpFile)) {
				ArrayList<String> LinesRead;
				try {
					LinesRead = readLinesFromFile(tmpFile);
				} catch (FileNotFoundException ex) {
					Logger.getLogger(CLS_cpufreq.class.getName()).log(Level.SEVERE, null, ex);
					SUB_Popups.MsgErrorOK(FRM_Main.getFrames()[0],"" , "File not found...");
					return null;
				}
				if (LinesRead != null && LinesRead.size() > 0) {
					return Integer.parseInt(LinesRead.get(0));
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
		return null;
	}

	public static Integer getScalingMinFreq(int CPU) {
		String CPUFreqPath = GLOBAL.BasePath + "/cpu" + String.valueOf(CPU) + "/cpufreq";
		File tmpFile = new File(CPUFreqPath + "/scaling_min_freq");
		if (FileExists(tmpFile)) {
			if (FileIsReadable(tmpFile)) {
				ArrayList<String> LinesRead;
				try {
					LinesRead = readLinesFromFile(tmpFile);
				} catch (FileNotFoundException ex) {
					Logger.getLogger(CLS_cpufreq.class.getName()).log(Level.SEVERE, null, ex);
					SUB_Popups.MsgErrorOK(FRM_Main.getFrames()[0],"" , "File not found...");
					return null;
				}
				if (LinesRead != null && LinesRead.size() > 0) {
					return Integer.parseInt(LinesRead.get(0));
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
		return null;
	}

	public static ArrayList<String> getScalingAvailableFreqs(int CPU) {
		String CPUFreqPath = GLOBAL.BasePath + "/cpu" + String.valueOf(CPU) + "/cpufreq";
		File tmpFile = new File(CPUFreqPath + "/scaling_available_frequencies");
		if (FileExists(tmpFile)) {
			if (FileIsReadable(tmpFile)) {
				ArrayList<String> LinesRead;
				try {
					LinesRead = readLinesFromFile(tmpFile);
				} catch (FileNotFoundException ex) {
					Logger.getLogger(CLS_cpufreq.class.getName()).log(Level.SEVERE, null, ex);
					GLOBAL.vCPUMulti = false;			// Assume no need for separate handling.
					SUB_Popups.MsgErrorOK(FRM_Main.getFrames()[0],"" , "File not found...");
					return null;
				}
				if (LinesRead != null && LinesRead.size() > 0) {
					String[] tmpArr = LinesRead.get(0).split(" ");
					LinesRead = new ArrayList<>();
					LinesRead.addAll(Arrays.asList(tmpArr));
					return LinesRead;
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
		return null;
	}

	public static String getPrettyFreqString(int MyFreq) {
		return String.format("%,d", MyFreq);
	}

	public static String getUnPrettyFreqString(String MyFreq) {
		String tmpStr = MyFreq.replaceAll(",", CONSTS.EMPTY_STRING);
		return tmpStr;
	}

	public static int getUnPrettyFreqInt(String MyFreq) {
		String tmpStr = MyFreq.replaceAll(",", CONSTS.EMPTY_STRING);
		return Integer.parseInt(tmpStr);
	}

	public static TYP_AutoUnitsFreq AutoUnitsFreq(int clockfreq, boolean NoDiv) {
		return AutoUnitsFreqHelper(clockfreq, NoDiv);
	}

	public static TYP_AutoUnitsFreq AutoUnitsFreq(String clockfreq, boolean NoDiv) {
		return AutoUnitsFreqHelper(Integer.parseInt(clockfreq), NoDiv);
	}

	private static TYP_AutoUnitsFreq AutoUnitsFreqHelper(int clockfreq, boolean NoDiv) {
		// Return drive/partition size in pretty format as "value;unit" string:
		TYP_AutoUnitsFreq retObj = new TYP_AutoUnitsFreq();
		if (NoDiv) {
			retObj.setMyFreq(null);
			retObj.setMyFreqFormatted(String.format("%,d", clockfreq));
			retObj.setMyUnit("MHz");
		} else {
			//' TODO: complete this with KHz, Hz, etc.
			if (clockfreq < 1000000) {
				// MegaHertz:
				double tmpDouble = (clockfreq / 1000.000);
				retObj.setMyFreq(tmpDouble);
				retObj.setMyFreqFormatted(String.format("%,.2f", tmpDouble));
				retObj.setMyUnit("MHz");
			} else {
				// GigaHertz:
				double tmpDouble = (clockfreq / 1000000.000);
				retObj.setMyFreq(tmpDouble);
				retObj.setMyFreqFormatted(String.format("%,.2f", tmpDouble));
				retObj.setMyUnit("GHz");
			}
		}
		return retObj;
	}

	public static int AutoUnitsFreqRev(String clockfreqPretty) {
		//Convert pretty format back to drive/partition size
		//
		//' TODO: complete this with KHz, Hz, etc.
		//IF InStr(clockfreqPretty, "GHz") > 0 THEN
		//   RETURN Val(clockfreqPretty) * 1000000
		//ELSE IF InStr(clockfreqPretty, "MHz") > 0 THEN
		//   RETURN Val(clockfreqPretty) * 1000
	//ENDIF

		return 0;
	}

	public static boolean FileEchoLine(File MyFile, String sLine) {
		try {
			writeStringToFileWithSysEnc(MyFile, sLine);
		} catch (IOException ex) {
			Logger.getLogger(CLS_cpufreq.class.getName()).log(Level.SEVERE, null, ex);
		}

		return false;
	}

	public static Object ReadIni(String IniKey, Object vDEFAULT) {
		// Read from ini file primitive:
		//private hSettings AS Settings
		//private aVariant AS Variant
		//
		//hSettings = NEW Settings(GLOBAL.INIFIle)
		//aVariant = hSettings[IniKey, vDEFAULT]
		//'aVariant = hSettings [ IniKey ]
	//RETURN aVariant

		return false;
	}

	public static void WriteIni(String IniKey, Object  aVariant) {
		// Write to ini file primitive:
		//private hSettings AS Settings
		//
		//hSettings = NEW Settings(GLOBAL.INIFIle)
		//hSettings[IniKey] = aVariant
	}

	public static void WriteConfig() {
		// Write config to ini file:

		//WriteIni("Setting.ScalingGov", FMain.cmbScalingGovernor.Text)
		//WriteIni("Setting.CPUFreq", Utils.AutoUnitsFreqRev(FMain.cmbScalingSetSpeed.Text))
		//WriteIni("Setting.CPUFreqMaxLimit", Utils.AutoUnitsFreqRev(FMain.cmbScalingMaxFreq.Text))
		//WriteIni("Setting.CPUFreqMinLimit", Utils.AutoUnitsFreqRev(FMain.cmbScalingMinFreq.Text))
		//WriteIni("Setting.AutoRefreshRate", FMain.cmbAutoRefresh.Text)
		//IF FMain.cmbLoadOnBoot.index = 0 THEN
		//   WriteIni("Setting.LoadOnStart", TRUE)
		//ELSE
		//   WriteIni("Setting.LoadOnStart", FALSE)
		//ENDIF
	}

	public static void ReadConfig() {
		// Read config from ini file:

		//GLOBAL.vScalingGov = ReadIni("Setting.ScalingGov", "userspace")
		//GLOBAL.vCPUFreq = ReadIni("Setting.CPUFreq", 0)
		//GLOBAL.vCPUFreqMaxLimit = ReadIni("Setting.CPUFreqMaxLimit", 0)
		//GLOBAL.vCPUFreqMinLimit = ReadIni("Setting.CPUFreqMinLimit", 0)
		//GLOBAL.vAutoRefreshRate = ReadIni("Setting.AutoRefreshRate", "manual")
		//GLOBAL.vSaveOnExit = ReadIni("Setting.SaveOnExit", 0)
		//GLOBAL.vLoadOnStart = ReadIni("Setting.LoadOnStart", 0)
	}

	public static Boolean setScalingGovernor(int CPU, String gov) {
		String CPUFreqPath = GLOBAL.BasePath + "/cpu" + String.valueOf(CPU) + "/cpufreq";
		File tmpFile = new File(CPUFreqPath + "/scaling_governor");
		if (FileExists(tmpFile)) {
			if (FileIsWritable(tmpFile)) {
				try {
					writeStringToFileWithSysEnc(tmpFile, gov);
				} catch (IOException ex) {
					Logger.getLogger(CLS_cpufreq.class.getName()).log(Level.SEVERE, null, ex);
					return false;
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
		return true;
	}

	public static Boolean setScalingSetSpeed(int CPU, String freq) {
		String CPUFreqPath = GLOBAL.BasePath + "/cpu" + String.valueOf(CPU) + "/cpufreq";
		File tmpFile = new File(CPUFreqPath + "/scaling_setspeed");
		if (FileExists(tmpFile)) {
			if (FileIsWritable(tmpFile)) {
				try {
					writeStringToFileWithSysEnc(tmpFile, freq);
				} catch (IOException ex) {
					Logger.getLogger(CLS_cpufreq.class.getName()).log(Level.SEVERE, null, ex);
					return false;
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
		return true;
	}

	public static Boolean setScalingMaxFreq(int CPU, String freq) {
		String CPUFreqPath = GLOBAL.BasePath + "/cpu" + String.valueOf(CPU) + "/cpufreq";
		File tmpFile = new File(CPUFreqPath + "/scaling_max_freq");
		if (FileExists(tmpFile)) {
			if (FileIsWritable(tmpFile)) {
				try {
					writeStringToFileWithSysEnc(tmpFile, freq);
				} catch (IOException ex) {
					Logger.getLogger(CLS_cpufreq.class.getName()).log(Level.SEVERE, null, ex);
					return false;
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
		return true;
	}

	public static Boolean setScalingMinFreq(int CPU, String freq) {
		String CPUFreqPath = GLOBAL.BasePath + "/cpu" + String.valueOf(CPU) + "/cpufreq";
		File tmpFile = new File(CPUFreqPath + "/scaling_min_freq");
		if (FileExists(tmpFile)) {
			if (FileIsWritable(tmpFile)) {
				try {
					writeStringToFileWithSysEnc(tmpFile, freq);
				} catch (IOException ex) {
					Logger.getLogger(CLS_cpufreq.class.getName()).log(Level.SEVERE, null, ex);
					return false;
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
		return true;
	}

	public static void GetConfig(String whichCPU) {
		// Get config from hardware:
		int n;
		String strtemp;
		String CPUFreqPath;

		// Check if cpufreq enabled for this CPU:
		//CPUFreqPath = GLOBAL.BasePath &/ whichCPU &/ "cpufreq"
		//IF IsDir(CPUFreqPath) THEN
		//   // Get scaling_driver:
		//   TRY GLOBAL.vScalingDriver = Utils.FileReadLine(CPUFreqPath &/ "scaling_driver")
		//   IF ERROR THEN ERROR_exit(0, TRUE, "", CPUFreqPath &/ "scaling_driver")
		//   // Get scaling_governor:
		//   TRY GLOBAL.vScalingGov = Utils.FileReadLine(CPUFreqPath &/ "scaling_governor")
		//   IF ERROR THEN ERROR_exit(0, TRUE, "", CPUFreqPath &/ "scaling_governor")
		//   // Get scaling_cur_freq:
		//   TRY GLOBAL.vCPUFreq = Utils.FileReadLine(CPUFreqPath &/ "scaling_cur_freq")
		//   IF ERROR THEN ERROR_exit(0, TRUE, "", CPUFreqPath &/ "scaling_cur_freq")
		//   // Get cpuinfo_max_freq:
		//   TRY GLOBAL.vCPUFreqMax = Utils.FileReadLine(CPUFreqPath &/ "cpuinfo_max_freq")
		//   IF ERROR THEN ERROR_exit(0, TRUE, "", CPUFreqPath &/ "cpuinfo_max_freq")
		//   // Get cpuinfo_min_freq:
		//   TRY GLOBAL.vCPUFreqMin = Utils.FileReadLine(CPUFreqPath &/ "cpuinfo_min_freq")
		//   IF ERROR THEN ERROR_exit(0, TRUE, "", CPUFreqPath &/ "cpuinfo_min_freq")
		//   // Get scaling_max_freq:
		//   TRY GLOBAL.vCPUFreqMaxLimit = Utils.FileReadLine(CPUFreqPath &/ "scaling_max_freq")
		//   IF ERROR THEN ERROR_exit(0, TRUE, "", CPUFreqPath &/ "scaling_max_freq")
		//   // Get scaling_min_freq:
		//   TRY GLOBAL.vCPUFreqMinLimit = Utils.FileReadLine(CPUFreqPath &/ "scaling_min_freq")
		//   IF ERROR THEN ERROR_exit(0, TRUE, "", CPUFreqPath &/ "scaling_min_freq")
		//   // Get scaling_available_frequencies
		//   GLOBAL.AvailableFreqs.Clear
		//   IF Exist(CPUFreqPath &/ "scaling_available_frequencies") THEN
		//      TRY GLOBAL.AvailableFreqs = Split(Utils.FileReadLine(CPUFreqPath &/ "scaling_available_frequencies"), " ", "")
		//      IF ERROR THEN ERROR_exit(0, TRUE, "", CPUFreqPath &/ "scaling_available_frequencies")
		//   ELSE
		//      GLOBAL.AvailableFreqs.Add(GLOBAL.vCPUFreqMin)
		//      GLOBAL.AvailableFreqs.Add(GLOBAL.vCPUFreqMax)
		//   ENDIF
		//
		//   // START: Need to clean up empty positions due to gambas1 split()...
		//   FOR n = 0 TO GLOBAL.AvailableFreqs.Max
		//      IF GLOBAL.AvailableFreqs[n] = "" THEN
		//         GLOBAL.AvailableFreqs.Remove(n)
		//      ELSE
		//         IF IsInteger(Val(GLOBAL.AvailableFreqs[n])) THEN
		//         ELSE
		//            strtemp = "Unexpected contents found in:"
		//            strtemp = strtemp & gb.NewLine
		//            strtemp = strtemp & CPUFreqPath &/ "scaling_available_frequencies"
		//            strtemp = strtemp & gb.NewLine
		//            strtemp = strtemp & "If possible post the contents of this file when reporting the error."
		//            ERROR_exit(255, TRUE, strtemp)
		//         ENDIF
		//      ENDIF
		//   NEXT
		//   //END: Need to clean up empty positions due to gambas1 split()...
	//
		//   GLOBAL.AvailableFreqs.Reverse
		//ELSE
		//   ' No scaling_driver:
		//   GLOBAL.vScalingDriver = "UNAVAILABLE"
		//ENDIF
	}

	public static void ERROR_exit(byte errtype, boolean err_crit, String errmesg, String filename) {	//OPTIONAL String filename) {

		//SELECT CASE errtype
		//CASE 0   'File read error
		//   IF err_crit THEN
		//      Message.Error("Error reading: " & filename & gb.NewLine & "Nothing else to do here, so I'll just exit...")
		//   ELSE
		//      Message.Error("Error reading: " & filename & gb.NewLine)
		//   ENDIF
		//CASE 255 'Misc. error, supply own message
		//   IF err_crit THEN
		//      Message.Error(errmesg & " " & filename & gb.NewLine & gb.NewLine & "Nothing else to do here, so I'll just exit...")
		//   ELSE
		//      Message.Error(errmesg)
		//   ENDIF
		//END SELECT
		//
		//IF err_crit THEN
		//   FMain.butExit_Click
		//   QUIT
		//ENDIF
	}

	public static void CPUFreq_Refresh() {
		int n;
		String CPUFreqPath;

//		GLOBAL.txtScalingSetSpeed.Clear
//		FOR n = 0 TO GLOBAL.vCPUCount - 1
//			CPUFreqPath = GLOBAL.BasePath &/ "cpu" & n &/ "cpufreq"
//			' refresh scaling_cur_freq:
//			GLOBAL.vCPUFreq = Utils.FileReadLine(CPUFreqPath &/ "scaling_cur_freq")
//			GLOBAL.txtScalingSetSpeed.Add(Utils.AutoUnitsFreq(GLOBAL.vCPUFreq))
//			FMain.GridView1[n + 1, 0].Alignment = Align.Right
//			FMain.GridView1[n + 1, 0].Text = n
//			FMain.GridView1[n + 1, 1].Alignment = Align.Right
//			FMain.GridView1[n + 1, 1].Text = GLOBAL.txtScalingSetSpeed[n]
//			'IF GLOBAL.vCPUMulti = FALSE THEN BREAK
//		NEXT 'n
	}




	private static class cupDirsFilter extends javax.swing.filechooser.FileFilter {

		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) {
				if (f.getName().startsWith("cpu")) {
					return true;
				}
			}

			return false;
		}

		@Override
		public String getDescription() {
			return "cpu* directories filter";
		}
	}
}

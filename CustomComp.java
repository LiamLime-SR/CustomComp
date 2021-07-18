import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class CustomComp {

	public static String readData = "";
	public static String writeData = "";
	public static String readName = "";
	public static String writeName = "";
	public static String compName = "";

	public static int index = 0;
	public static boolean debug = false;

	public static long pb = 0;
	public static String pbTime = "";
	public static boolean readPb = true;
	public static long sob = 0;
	public static ArrayList<Long> golds = new ArrayList<Long>();
	public static String[] pbExit = new String[0];
	public static boolean[] wasSkipped = new boolean[0];

	public static final int timeValueLength = 12;
	public static final String timeIndicator = "<RealTime>";
	public static final String goldIndicator = "<BestSegmentTime>";
	public static final String splitEndIndicator = "</SplitTime>";
	public static final String customCompStartInsert = "\n        <SplitTime name=\"";
	public static final String customCompMidInsert = "\">\n          <RealTime>";
	public static final String customCompEndInsert = "</RealTime>\n        </SplitTime>";
	public static final String customCompSkipInsert = "\" />";
	public static final String customCompStartIndicator = "<SplitTime name=\"";
	public static final String customCompMidIndicator = "\">";
	public static final String customCompEndIndicator = "</SplitTime>";
	public static final String customCompSkipIndicator = "\" />";
	public static final String pbIndicator = customCompStartIndicator + "Personal Best" + customCompMidIndicator;
	public static final String skipIndicator = customCompStartIndicator + "Personal Best" + customCompSkipIndicator;

	public static void main(String[] args) throws IOException {
		processArgs(args);
		readIO();
		readSplits();
		processSplits();
		writeSplits();
		writeIO();
		System.out.println("export custom comp: success");
	}

	public static void readIO() throws IOException {
		File readFile = new File(readName);
		FileInputStream readStream = new FileInputStream(readFile);
		byte[] rawData = new byte[(int)readFile.length()];
		readStream.read(rawData);
		readStream.close();
		readData = new String(rawData);
		printCategory("read size");
		printData(readData.length() + "");
	}

	public static void writeIO() throws IOException {
		FileOutputStream writeStream = new FileOutputStream(writeName);
		writeStream.write(writeData.getBytes());
		writeStream.close();
		printCategory("write size");
		printData(writeData.length() + "");
	}

	public static void generateWriteName() {
		int dot = readName.lastIndexOf('.');
		writeName = readName.substring(0, dot) + "_CustomComp" + readName.substring(dot);
	}

	public static void generateCompName() {
		compName = "Custom Comp";
	}

	public static void getWriteName(String arg) {
		if (arg.equals("_generate_")) {generateWriteName();
		} else if (arg.equals("_overwrite_")) {writeName = readName;
		} else {writeName = arg;}
	}

	public static void getCompName(String arg) {
		if (arg.equals("_generate_")) {generateCompName();
		} else {compName = arg;}
	}

	public static void getCustomPb(String arg) {
		readPb = false;
		pbTime = arg;
		pb = getMillis(pbTime);
	}

	public static void processArgs(String[] args) {
		readName = args[0];
		int length = args.length;
		if (args[length - 1].equals("_debug_")) {
			debug = true;
			length--;
		} if (length > 1) {getCustomPb(args[1]);}
		if (length > 2) {getCompName(args[2]);
		} else {generateCompName();}
		if (length > 3) {getWriteName(args[3]);
		} else {generateWriteName();}
		printCategory("read file");
		printData(readName);
		printCategory("write file");
		printData(writeName);
		printCategory("comp name");
		printData(compName);
	}

	public static void readSplits() {
		printCategory("best segment");
		for (int i = 0; true; i++) {
			copyToNextInstanceOf(goldIndicator);
			copyToNextInstanceOf(timeIndicator);
			if (index >= readData.length()) {break;}
			String gold = copyCharacters(timeValueLength);
			long millis = getMillis(gold);
			golds.add(millis);
			sob += millis;
			printSubCat("split " + (i + 1));
			printData(gold + " (" + millis + ")");
		} printCategory("sum of best");
		printData(getTime(sob) + " (" + sob + ")");
		index = 0;
		wasSkipped = new boolean[golds.size()];
		printCategory("was skipped");
		for (int i = 0; i < golds.size(); i++) {
			int lastIndex = index;
			copyToNextInstanceOf(pbIndicator);
			int pbIndex = index;
			index = lastIndex;
			copyToNextInstanceClamped(skipIndicator, pbIndex);
			if (pbIndex > index) {
				wasSkipped[i] = true;
			} else {
				index = pbIndex;
			} printSubCat("split " + (i + 1));
			printData(wasSkipped[i] + "");
		} if (readPb) {
			copyToNextInstanceOf(timeIndicator);
			pbTime = copyCharacters(timeValueLength);
			pb = getMillis(pbTime);
		} printCategory("personal best");
		printData(pbTime + " (" + pb + ")");
		index = 0;
	}

	public static void writeSplits() {
		for (int i = 0; i < pbExit.length; i++) {
			if (wasSkipped[i]) {
				writeData += copyToNextInstanceOf(skipIndicator);
			} else {
				writeData += copyToNextInstanceOf(pbIndicator);
				writeData += copyToNextInstanceOf(splitEndIndicator);
			} String customPbInsert = customCompStartInsert + compName + customCompMidIndicator;
			String customSkipInsert = customCompStartInsert + compName + customCompSkipInsert;
			String customPbIndicator = customCompStartIndicator + compName + customCompMidIndicator;
			String customSkipIndicator = customCompStartIndicator + compName + customCompSkipIndicator;
			int lastIndex = index;
			copyToNextInstanceOf(pbIndicator);
			int pbIndex = index;
			index = lastIndex;
			copyToNextInstanceClamped(customPbIndicator, pbIndex);
			int customPbIndex = index;
			index = lastIndex;
			copyToNextInstanceClamped(customSkipIndicator, pbIndex);
			int customSkipIndex = index;
			index = lastIndex;
			if (pbIndex > customPbIndex) {
				writeData += copyCharacters(customPbIndex - index - customPbInsert.length());
				index = customPbIndex;
				copyToNextInstanceOf(customCompEndIndicator);
			} else if (pbIndex > customSkipIndex) {
				writeData += copyCharacters(customSkipIndex - index - customSkipInsert.length());
				index = customSkipIndex;
			} writeData += customCompStartInsert + compName + customCompMidInsert + pbExit[i] + customCompEndInsert;
		} writeData += copyToEnd();
	}

	public static void processSplits() {
		pbExit = new String[golds.size()];
		long totalTimesave = pb - sob;
		printCategory("total possible timesave");
		printData(getTime(totalTimesave) + " (" + totalTimesave + ")");
		long[] timesave = new long[pbExit.length];
		printCategory("per split possible timesave");
		for (int i = 0; i < pbExit.length; i++) {
			timesave[i] = (long)((double)totalTimesave * (double)golds.get(i) / (double)sob);
			printSubCat("split " + (i + 1));
			printData(getTime(timesave[i]) + " (" + timesave[i] + ")");
		} long[] pbSegment = new long[pbExit.length];
		printCategory("custom segment");
		for (int i = 0; i < pbExit.length; i++) {
			pbSegment[i] = golds.get(i) + timesave[i];
			printSubCat("split " + (i + 1));
			printData(getTime(pbSegment[i]) + " (" + pbSegment[i] + ")");
		} long lastExit = 0;
		printCategory("custom exit");
		for (int i = 0; i < pbExit.length - 1; i++) {
			lastExit += pbSegment[i];
			pbExit[i] = getTime(lastExit);
			printSubCat("split " + (i + 1));
			printData(pbExit[i] + " (" + lastExit + ")");
		} pbExit[pbExit.length - 1] = pbTime;
		printSubCat("split " + pbExit.length);
		printData(pbTime + " (" + pb + ")");
	}

	public static String copyToEnd() {
		String segment = readData.substring(index);
		index = readData.length();
		return segment;
	}

	public static String copyCharacters(int amount) {
		if (amount + index > readData.length()) {
			String segment = readData.substring(index);
			index = readData.length();
			return segment;
		} else {
			String segment = readData.substring(index, index + amount);
			index += amount;
			return segment;
		}
	}

	public static String copyToNextInstanceOf(String clip) {
		int start = readData.indexOf(clip, index);
		if (start == -1) {
			String segment = readData.substring(index);
			index = readData.length();
			return segment;
		} else {
			String segment = readData.substring(index, start + clip.length());
			index = start + clip.length();
			return segment;
		}
	}

	public static String copyToNextInstanceClamped(String clip, int max) {
		int start = readData.substring(0, max).indexOf(clip, index);
		if (start == -1) {
			String segment = readData.substring(index, max);
			index = max;
			return segment;
		} else {
			String segment = readData.substring(index, start + clip.length());
			index = start + clip.length();
			return segment;
		}
	}

	public static long getMillis(String time) {
		int minuteIndex = time.indexOf(':') + 1;
		int secondIndex = time.indexOf(':', minuteIndex) + 1;
		int milliIndex = time.indexOf('.', secondIndex) + 1;
		long hours = Long.parseLong(time.substring(0, 2));
		long minutes = Long.parseLong(time.substring(minuteIndex, minuteIndex + 2));
		long seconds = Long.parseLong(time.substring(secondIndex, secondIndex + 2));
		long millis = Long.parseLong(time.substring(milliIndex, milliIndex + 3));
		return ((hours * 60 + minutes) * 60 + seconds) * 1000 + millis;
	}

	public static String getTime(long millisTime) {
		long millis = millisTime % 1000;
		long seconds = (millisTime / 1000) % 60;
		long minutes = (millisTime / (60 * 1000)) % 60;
		long hours = millisTime / (60 * 60 * 1000);
		String hoursStr = setLength(hours + "", 2, '0');
		String minutesStr = setLength(minutes + "", 2, '0');
		String secondsStr = setLength(seconds + "", 2, '0');
		String millisStr = setLength(millis + "", 3, '0');
		return hoursStr + ":" + minutesStr + ":" + secondsStr + "." + millisStr;
	}

	public static String setLength(String original, int length, char buffer) {
		String clamped = original.substring(Math.max(original.length() - length, 0), original.length());
		return new String(new char[length - clamped.length()]).replace((char)0, buffer) + clamped;
	}

	public static void printCategory(String label) {
		if (debug) {
			System.out.println(label + ":");
		}
	}

	public static void printSubCat(String label) {
		if (debug) {
			System.out.println("\t" + label + ":");
		}
	}

	public static void printData(String label) {
		if (debug) {
			System.out.println("\t\t" + label);
		}
	}
}

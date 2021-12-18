
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class datalist {
	ArrayList<val> vals = new ArrayList<val>();
	
	public void addVal(String str, String val) {
		vals.add(new val(str, val));
	}
	
	public void sort() {
		vals.sort((v1, v2) -> v1.time.compareTo(v2.time));
	}
	
	public String getLatestVal(long t) {
		this.sort();
		for(int i = vals.size() - 1; i >= 0;i--) {
			if(vals.get(i).time.toInstant().isBefore(Instant.ofEpochSecond(t))) {
				return vals.get(i).val;
			}
		}
		return "";
	}
}

class val {
	ZonedDateTime time;
	String val;
	
	public val(String str, String val) {
		this.val = val;
		str = str.replaceAll("\\?", "");
		str = str.replaceAll(":..", "");
		str = str.replaceAll("  ", " 0");
		str = str.replaceAll("Jan", "01");
		str = str.replaceAll("Feb", "02");
		str = str.replaceAll("Mar", "03");
		str = str.replaceAll("Apr", "04");
		str = str.replaceAll("May", "05");
		str = str.replaceAll("Jun", "06");
		str = str.replaceAll("Jul", "07");
		str = str.replaceAll("Aug", "08");
		str = str.replaceAll("Sep", "09");
		str = str.replaceAll("Oct", "10");
		str = str.replaceAll("Nov", "11");
		str = str.replaceAll("Dec", "12") + "Z";
		time = ZonedDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy MM dd HHmmX"));
	}
	
	
}


class UpDown {
	String up;
	String down;
	
	public UpDown(String up, String down) {
		this.up = up;
		this.down = down;
	}
}

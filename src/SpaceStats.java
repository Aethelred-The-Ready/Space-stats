import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;

public class SpaceStats {

	public static void main(String[] args) {
		
		getTSV("https://planet4589.org/space/astro/lists/missions.tsv");
	}
	
	public static String[][] getTSV(String url){
		try {
			
			BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
			byte[] bytes = in.readAllBytes();
			
			int r = 0, c = 0, mc = 0;
			
			for(int i = 0; i < bytes.length;i++) {
				if(bytes[i] == '\n') {
					r++;
					if(c > mc) {
						mc = c;
					}
					c = 0;
				} else if (bytes[i] == '\t') {
					c++;
				}
			}
			
			String[][] tr = new String[r][c];
			r = 0;
			c = 0;
			
			for(int i = 0; i < bytes.length;i++) {
				if(bytes[i] == '\n') {
					r++;
					c = 0;
					tr[r][c] = "";
				} else if (bytes[i] == '\t') {
					c++;
					tr[r][c] = "";
				} else {
					tr[r][c] += bytes[i];
				}
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}

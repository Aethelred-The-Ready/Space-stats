import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.text.AttributeSet.ColorAttribute;

public class SpaceStats {
	static ArrayList<val> people = new ArrayList<val>();
	static ArrayList<val> maxPeople = new ArrayList<val>();
	static ArrayList<val> peopleAtTime = new ArrayList<val>();
	static datalist curPeopleList = new datalist();
	private static int mouseX = 0;
	private static int mouseY = 0;
	private static boolean toolTipVisible = false;
	private static long toolTipTime = 0;
	private static JPanel j;
	private static String name;
	final private static boolean fetch = true;
	final private static int elemPerPerson = 3;
	
	final static long absMinTime = ZonedDateTime.of(1960, 1, 1, 0, 0, 0, 0, ZoneId.of("Z")).toEpochSecond();
	final static long absMaxTime = ZonedDateTime.now().toEpochSecond();
	static long minTime = absMinTime;
	static long maxTime = absMaxTime;
	static int yScale = 50;
	static int zoom = 0;
	
	private static KeyListener k = new KeyListener() {
		@Override
		public void keyTyped(KeyEvent e) {
			// TODO Auto-generated method stub
			
			j.repaint();
		}

		@Override
		public void keyPressed(KeyEvent e) {
			// TODO Auto-generated method stub
			
			long d = (maxTime - minTime) / 10;

			if(e.getKeyCode() == KeyEvent.VK_D) {
				maxTime += d;
				minTime += d;
			}else if(e.getKeyCode() == KeyEvent.VK_A) {
				maxTime -= d;
				minTime -= d;
			}else if(e.getKeyCode() == KeyEvent.VK_R) {
				yScale *= 2;
			}else if(e.getKeyCode() == KeyEvent.VK_F) {
				yScale /= 2;
			}
			
			if(minTime < absMinTime) {
				minTime = absMinTime;
				maxTime += d;
			}
			if(maxTime > absMaxTime) {
				maxTime = absMaxTime;
				minTime -= d;
			}

			j.repaint();
		}

		@Override
		public void keyReleased(KeyEvent e) {
			// TODO Auto-generated method stub

			j.repaint();
		}
	};

	private static MouseListener m = new MouseListener() {
		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub

			mouseX = e.getX() - 7;
			mouseY = e.getY() - 30;
			
			toolTipVisible = true;
			toolTipTime = timeFromPos(mouseX);
			

			j.repaint();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub

			j.repaint();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub

			j.repaint();
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
			j.repaint();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub

			j.repaint();
		}
		
	};
	
	private static MouseWheelListener mw = new MouseWheelListener() {

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {

			toolTipVisible = false;
			
			mouseX = e.getX() - 7;
			mouseY = e.getY() - 30;
			
			long time = timeFromPos(mouseX);
			
			int amt = e.getWheelRotation();
			
			if(amt < 0) {
				for(int i = 0; i > amt; i--) {
					minTime += (time - minTime) / 2;
					maxTime -= (maxTime - time) / 2;
					zoom++;
				}
			} else {
				for(int i = 0; i < amt; i++) {
					minTime -= time - minTime;
					maxTime += maxTime - time;
					zoom--;
				}
			}
			if(zoom < 0) {
				zoom = 0;
			}
			if(minTime < absMinTime) {
				minTime = absMinTime;
			}
			if(maxTime > absMaxTime) {
				maxTime = absMaxTime;
			}

			j.repaint();
		}
		
	};

	public static void main(String[] args) {
		
		String[][] missions = getTSV("https://planet4589.org/space/astro/tsv/missions.tsv");
		Hashtable<String, UpDown> missionTimes = new Hashtable<String, UpDown>(missions.length);
		
		datalist d = new datalist();
		String now = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy MM dd HHmm"));
		for (int i = 2; i < missions.length; i++) {
		    if(missions[i][13].charAt(0) == 'O') {
				d.addVal(missions[i][5], (missions[i][8].substring(3)));
				if(missions[i][6].contains("-")) {
					d.addVal(now, "-" + missions[i][8].substring(3));
				    missionTimes.put(missions[i][0], new UpDown(missions[i][5],now));
				}else {
					d.addVal(missions[i][6], "-" + missions[i][8].substring(3));
				    missionTimes.put(missions[i][0], new UpDown(missions[i][5],missions[i][6]));
				}
			}
		}
		
		d.sort();
		
		String[][] rideTSV = getTSV("https://planet4589.org/space/astro/tsv/rides.tsv");
		datalist peopleList = new datalist();
		
		for(int i = 2; i < rideTSV.length; i++) {
			if(rideTSV[i][5] == null) { continue; }
			UpDown ud = missionTimes.get(rideTSV[i][5]);
			if(ud != null) {
				peopleList.addVal(rideTSV[i][12], "up;" + rideTSV[i][0].trim() + ";" + rideTSV[i][7].trim() + ";" + rideTSV[i][10].trim() + ";");
				
				ZonedDateTime up = peopleList.vals.get(peopleList.vals.size() - 1).time;
				
				String downTime = now;
				
				String duration = rideTSV[i][13].trim();
				duration = duration.replaceAll("\\?", "");
				if(duration.indexOf('*') == -1) {
					String[] bits = duration.split(":");
					up = up.plusDays(Integer.parseInt(bits[0])).plusHours(Integer.parseInt(bits[1])).plusMinutes(Integer.parseInt(bits[2]));
					if(bits.length == 4) {
						up = up.plusSeconds(Integer.parseInt(bits[3]));
					}
					downTime = up.format(DateTimeFormatter.ofPattern("yyyy MM dd HHmm"));
				}
				
				peopleList.addVal(downTime, "down;" + rideTSV[i][0].trim() + ";");
			}
		}
		
		peopleList.sort();
		String curPeople = "";
		
		long timeFromLast = Integer.MAX_VALUE;
		long minTimeFromLast = Integer.MAX_VALUE;
		Instant lastTime = ZonedDateTime.of(1960, 1, 1, 0, 0, 1, 0, ZoneId.of("Z")).toInstant();
		
		curPeopleList.addVal("1960 Jan 01 0000", "");
		for(int i = 0; i < peopleList.vals.size();i++) {
			ZonedDateTime time = peopleList.vals.get(i).time;
			String t = time.format(DateTimeFormatter.ofPattern("yyyy MM dd HHmm"));
			String[] stuff = peopleList.vals.get(i).val.split(";");
			if(stuff[0].equals("up") && i != peopleList.vals.size() - 1&& peopleList.vals.get(i + 1).time.equals(peopleList.vals.get(i).time)) {
				curPeople+=stuff[1] + ";" + stuff[2] + ";" + stuff[3] + ";";
			} else if (stuff[0].equals("up")) {
				curPeople+=stuff[1] + ";" + stuff[2] + ";" + stuff[3] + ";";
				curPeopleList.addVal(t, curPeople);	
				
				/*timeFromLast = time.toInstant().toEpochMilli() - lastTime.toEpochMilli();
				lastTime = time.toInstant();
				if(minTimeFromLast > timeFromLast) {
					minTimeFromLast = timeFromLast;
					System.out.println(time.toString());
					System.out.println(timeFromLast);
				}*/

			} else if (i != peopleList.vals.size() - 1 && peopleList.vals.get(i + 1).time.equals(peopleList.vals.get(i).time)) {
				int ind = curPeople.indexOf(stuff[1] + ";");
				int lastInd = curPeople.indexOf(";", curPeople.indexOf(";", curPeople.indexOf(";", ind + 1) + 1) + 1);
				curPeople = curPeople.substring(0, ind) + curPeople.substring(lastInd + 1);
			} else {
				int ind = curPeople.indexOf(stuff[1] + ";");
				int lastInd = curPeople.indexOf(";", curPeople.indexOf(";", curPeople.indexOf(";", ind + 1) + 1) + 1);
				curPeople = curPeople.substring(0, ind) + curPeople.substring(lastInd + 1);
				curPeopleList.addVal(t, curPeople);
			}
		}
		curPeopleList.sort();
		
		datalist peopleInSpace = new datalist();
		datalist maxPeopleInSpace = new datalist();
		
		int cur = 0;
		int curMax = 0;
		
		for(int i = 0; i < curPeopleList.vals.size(); i++) {
			String t = curPeopleList.vals.get(i).time.format(DateTimeFormatter.ofPattern("yyyy MM dd HHmm"));
			cur = curPeopleList.vals.get(i).val.split(";").length / elemPerPerson;
			if(cur > curMax) {
				maxPeopleInSpace.addVal(t, "" + cur);
				curMax = cur;
			}
			peopleInSpace.addVal(t, "" + cur);
		}
		maxPeopleInSpace.addVal(now, "" + curMax);
		
		people = peopleInSpace.vals;
		maxPeople = maxPeopleInSpace.vals;
		
		render();
		 
	}
	
	public static String[][] getTSV(String url){
		try {
			
			HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();	
			con.setRequestMethod("GET");
			
			BufferedInputStream in;
			
			File backup = new File(url.substring(url.lastIndexOf('/') + 1));
			try {
				if(!fetch) {
					throw new UnknownHostException();
				}
				con.connect();
				in = new BufferedInputStream(con.getInputStream());
				
			} catch(UnknownHostException e) {
				FileInputStream fis = new FileInputStream(backup);
				in = new BufferedInputStream(fis);
			}
			
			byte[] bytes = in.readAllBytes();
			in.close();
			
			FileOutputStream fos = new FileOutputStream(backup);
			fos.write(bytes);
			fos.close();
			
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
			mc++;
			r++;
			
			String[][] tr = new String[r][mc];
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
					tr[r][c] += (char)bytes[i];
				}
			}
			
			String[][] realTR = new String[r][mc];
			for (int i = 0; i < realTR.length; i++) {
			    for (int j = 0; j < realTR[i].length; j++) {
			    	realTR[i][j] = tr[i][j];
			    }
			}
			
			return realTR;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private static void render(){
		JFrame frame = new JFrame(name);
		
		j = new JPanel(){
			public void paint(Graphics p) {	
				
				int yFloor = 710;
				
				int prevXPos = 20;
				int prevYVal = 0;
				p.setColor(Color.WHITE);
				p.fillRect(0, 0, 2000, 2000);
				p.setColor(Color.BLACK);
				p.drawLine(0, yFloor, 2000, yFloor);
				p.drawLine(20, 0, 20, 2000);
				
				for(int i = 1; i < curPeopleList.vals.size();i ++) {
					/*long t = people.get(i).time.toEpochSecond();
					int xPos = posFromTime(t);
					String[] curPeopleData = curPeopleList.getLatestVal(t - 100).split(";");
					Color[] agencies = getAgencies(curPeopleData);
					//p.setColor(new Color(0, 0, 255, 120));
					for(int j = 0; j < agencies.length;j++) {
						p.setColor(agencies[j]);
						p.fillRect(prevXPos, 700 - (j + 1) * yScale, xPos - prevXPos, yScale);
					}
					//p.fillRect(prevXPos, 700 - prevYVal * yScale, xPos - prevXPos, prevYVal * yScale);
					p.setColor(Color.BLACK);
					/*p.drawLine(
							prevXPos, 	700 - prevYVal * yScale,
							xPos, 		700 - prevYVal * yScale);
					p.drawLine(
							xPos, 		700 - prevYVal * yScale,
							xPos, 		700 - Integer.parseInt(people.get(i).val) * yScale);
					prevXPos = xPos;
					prevYVal = Integer.parseInt(people.get(i).val);
					*/
					long t = curPeopleList.vals.get(i).time.toEpochSecond();
					int xPos = posFromTime(t);
					if(xPos < -1000 && prevXPos < -1000) {
						continue;
					}else if(xPos > 8000 && prevXPos > 8000) {
						break;
					}
					String vals = curPeopleList.vals.get(i - 1).val;
					String[] curPeopleData = vals.split(";");
					Color[] agencies = getAgencies(curPeopleData);
					for(int j = 0; j < agencies.length;j++) {
						String name = curPeopleData[elemPerPerson * j + 1];
						if(name.indexOf(',') != -1) {
							name = name.substring(0, name.indexOf(','));
						}
						p.setColor(agencies[j]);
						p.fillRect(prevXPos, yFloor - (j + 1) * yScale, xPos - prevXPos, yScale);
						p.setColor(Color.BLACK);
						p.drawLine(prevXPos, yFloor - (j + 1) * yScale, xPos, yFloor - (j + 1) * yScale);
						p.drawLine(prevXPos, yFloor - j * yScale, xPos, yFloor - j * yScale);
						if(!curPeopleList.vals.get(i).val.contains(name)) {
							p.drawLine(xPos, yFloor - (j + 1) * yScale, xPos, yFloor - j * yScale);
						}
					}
					p.setColor(Color.BLACK);
					prevXPos = xPos;
					prevYVal = agencies.length;
					
				}
				prevXPos = 20;
				prevYVal = 0;
				p.setColor(Color.BLACK);
				for(int i = 0; i < maxPeople.size();i ++) {
					long t = maxPeople.get(i).time.toEpochSecond();
					int xPos = posFromTime(t);
					p.drawLine(
							prevXPos, 	yFloor - prevYVal * yScale,
							xPos, 		yFloor - prevYVal * yScale);
					p.drawLine(
							xPos, 		yFloor - prevYVal * yScale,
							xPos, 		yFloor - Integer.parseInt(maxPeople.get(i).val) * yScale);
					prevXPos = xPos;
					prevYVal = Integer.parseInt(maxPeople.get(i).val);
				}
				double gap = 2;
				if(zoom == 1) {
					gap = 1;
				}else if(zoom == 2 || zoom == 3) {
					gap = 0.5;
				}else if(zoom == 4) {
					gap = 0.25;
				}else if(zoom == 5) {
					gap = 0.16;
				}else if(zoom == 6) {
					gap = 0.08;
				}else if(zoom >= 7) {
					gap = 0.08;
				}

				for(double i = 1960 + gap; i < 2030; i += gap) {
					ZonedDateTime temp = ZonedDateTime.of((int)i, (int) (12 * (i - (int)i) + 1), 1, 0, 0, 0, 0, ZoneId.of("Z"));
					long t = temp.toEpochSecond();
					int xPos = posFromTime(t);
					if(gap >= 0.5) {
						p.drawString(i + "", xPos - 15, yFloor + 15);
					} else {
						p.drawString(temp.format(DateTimeFormatter.ofPattern("YYYY MMM")), xPos - 15, yFloor + 15);
					}
					p.drawLine(xPos, yFloor, xPos, yFloor + 5);
				}
				for(int i = 0; i < 2000 / yScale; i ++) {
					p.drawString(i + "", 3, yFloor + 3 - i * yScale);
					p.drawLine(15, yFloor - i * yScale, 20, yFloor - i * yScale);
				}
				
				if(toolTipVisible) {
					String[] peopleData = curPeopleList.getLatestVal(toolTipTime).split(";");
					int x = posFromTime(toolTipTime);
					int noPeople = (int)(peopleData.length/elemPerPerson);
					p.setColor(Color.WHITE);
					p.fillRect(x, mouseY, 300, 25 + 10 * noPeople);
					p.setColor(Color.BLACK);
					p.drawRect(x, mouseY, 300, 25 + 10 * noPeople);
					p.drawString(ZonedDateTime.ofInstant(Instant.ofEpochSecond(toolTipTime), ZoneId.of("Z")).format(DateTimeFormatter.ofPattern("YYYY MMM dd HH:mm:ss")), x + 5, mouseY + 15);
					p.drawString("" + noPeople,  x + 280, mouseY + 13);
					for(int i = 0; i < noPeople;i++) {
						int personNo = (noPeople - i - 1);
						String name = peopleData[elemPerPerson * personNo + 1];
						if(name.indexOf(',') != -1) {
							name = name.substring(0, name.indexOf(','));
						}
						p.drawString(name, x + 5, mouseY + 25 + 10 * i);
						p.drawString(peopleData[elemPerPerson * personNo + 2], x + 200, mouseY + 25 + 10 * i);
					}
				}

			}
		};
			
		frame.addKeyListener(k);
		frame.addMouseListener(m);
		frame.addMouseWheelListener(mw);
		frame.add(j);
		frame.setLocation(10,10);
		frame.setSize(1000, 1000);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public static int posFromTime(long t) {
		return (int) (((t - minTime) * 1000 )/ (maxTime - minTime)) + 20;
	}
	
	public static long timeFromPos(int p) {
		return ((p - 20) * (maxTime - minTime)) / 1000 + minTime;
	}
	
	private static void drawAgencies(String[] data) {
		ArrayList<String> agencies = new ArrayList<String>();
		for(int i = 0; i < data.length / elemPerPerson;i++) {
			Color ta = Color.BLACK;
			switch (data[i * elemPerPerson + 2].strip()) {
			case "NASA":
			case "USAF":
			case "USAIC":
			case "MDAC":
				agencies.add("NASA");
				break;
			case "SPX":
				agencies.add("SPX");
				break;
			case "SPAD":
				agencies.add("SPAD");
			break;
			case "TSPK":
			case "TSKPR":
			case "TSPKR":
			case "RKKE":
			case "NPOE":
			case "IMBP":
			case "OKB1":
			case "TSKPR/PERVK":
				agencies.add("ROS");
				break;
			case "HYD":
				agencies.add("HYD");
				break;
			case "ESA":
			case "CNES":
			case "DLR":
			case "DFVLR":
			case "ASI":
				agencies.add("ESA");
				break;
			case "JAXA":
			case "NASDA":
				agencies.add("JAXA");
				break;
			case "KARI":
				agencies.add("KARI");
				break;
			case "CSA":
				agencies.add("CSA");
				break;
			}
		}
	}

	private static Color[] getAgencies(String[] data) {
		ArrayList<Color> colors = new ArrayList<Color>();
		for(int i = 0; i < data.length / elemPerPerson;i++) {
			Color ta = Color.BLACK;
			switch (data[i * elemPerPerson + 2].strip()) {
			case "NASA":
			case "USAF":
			case "USAIC":
			case "MDAC":
				ta = Color.BLUE;
				break;
			case "SPX":
			case "SPAD":
				ta = new Color(0, 120, 255);
			break;
			case "TSPK":
			case "TSKPR":
			case "TSPKR":
			case "RKKE":
			case "NPOE":
			case "IMBP":
			case "OKB1":
			case "TSKPR/PERVK":
				ta = new Color(150, 32, 32);
				break;
			case "HYD":
				ta = Color.RED;
				break;
			case "ESA":
			case "CNES":
			case "DLR":
			case "DFVLR":
			case "ASI":
				ta = Color.ORANGE;
				break;
			case "JAXA":
			case "NASDA":
				ta = Color.GREEN;
				break;
			case "KARI":
				ta = Color.GRAY;
				break;
			case "CSA":
				ta = Color.MAGENTA;
				break;
			}
			ta = new Color(ta.getRed(), ta.getGreen(), ta.getBlue(), 200);
			colors.add(ta);
		}
		return colors.toArray(new Color[colors.size()]);
	}

	public static int cmpCol(Color v1, Color v2) {
		if(v1.getRed() == v2.getRed() && v1.getGreen() == v2.getGreen()) {
			return v1.getBlue() - v2.getBlue();
		}

		if(v1.getRed() == v2.getRed()) {
			return v1.getGreen() - v2.getGreen();
		}
		return v2.getRed() - v1.getRed();
	}
	
}

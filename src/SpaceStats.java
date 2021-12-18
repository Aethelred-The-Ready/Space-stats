import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class SpaceStats {
	static ArrayList<val> people = new ArrayList<val>();
	static ArrayList<val> maxPeople = new ArrayList<val>();
	static ArrayList<val> peopleAtTime = new ArrayList<val>();
	private static int mouseX = 0;
	private static int mouseY = 0;
	private static boolean toolTipVisible = false;
	private static long toolTipTime = 0;
	private static JPanel j;
	private static String name;
	
	final static long absMinTime = ZonedDateTime.of(1960, 1, 1, 0, 0, 0, 0, ZoneId.of("Z")).toEpochSecond();
	final static long absMaxTime = ZonedDateTime.now().toEpochSecond();
	static long minTime = absMinTime;
	static long maxTime = absMaxTime;
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
		
		datalist d = new datalist();
		String now = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy MM dd HHmm"));
		for (int i = 2; i < missions.length; i++) {
		    if(missions[i][13].charAt(0) == 'O') {
				d.addVal(missions[i][5], (missions[i][8].substring(3)));
				if(missions[i][6].contains("-")) {
					d.addVal(now, "-" + missions[i][8].substring(3));
				}else {
					d.addVal(missions[i][6], "-" + missions[i][8].substring(3));
				}
			}
		}
		
		d.sort();
		
		String[][] rides = getTSV("https://planet4589.org/space/astro/tsv/rides.tsv");
		datalist 
		
		
		datalist peopleInSpace = new datalist();
		datalist maxPeopleInSpace = new datalist();
		
		int cur = 0;
		int curMax = 0;
		
		for(int i = 0; i < d.vals.size(); i++) {
			String t = d.vals.get(i).time.format(DateTimeFormatter.ofPattern("yyyy MM dd HHmm"));
			cur += Integer.parseInt(d.vals.get(i).val);
			if(cur > curMax) {
				maxPeopleInSpace.addVal(t, "" + cur);
				curMax = cur;
			}
			peopleInSpace.addVal(t, "" + cur);
		}
		maxPeopleInSpace.addVal(now, "" + curMax);
		
		for(int i = 0; i < peopleInSpace.vals.size(); i++) {
			String t = peopleInSpace.vals.get(i).time.format(DateTimeFormatter.ofPattern("yyyy MM dd HHmm"));
		}
		
		people = peopleInSpace.vals;
		maxPeople = maxPeopleInSpace.vals;
		
		render();
		 
	}
	
	public static String[][] getTSV(String url){
		try {
			
			HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
			con.setRequestMethod("GET");
			con.connect();
			BufferedInputStream in = new BufferedInputStream(con.getInputStream());


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
				
				int yScale = 50;
				
				int prevXPos = 20;
				int prevYVal = 0;
				p.setColor(Color.WHITE);
				p.fillRect(0, 0, 2000, 2000);
				p.setColor(Color.BLACK);
				p.drawLine(0, 800, 2000, 800);
				p.drawLine(20, 0, 20, 2000);
				for(int i = 0; i < people.size();i ++) {
					long t = people.get(i).time.toEpochSecond();
					int xPos = posFromTime(t);
					p.setColor(new Color(0, 0, 255, 120));
					p.fillRect(prevXPos, 800 - prevYVal * yScale, xPos - prevXPos, prevYVal * yScale);
					p.setColor(Color.BLACK);
					p.drawLine(
							prevXPos, 	800 - prevYVal * yScale,
							xPos, 		800 - prevYVal * yScale);
					p.drawLine(
							xPos, 		800 - prevYVal * yScale,
							xPos, 		800 - Integer.parseInt(people.get(i).val) * yScale);
					prevXPos = xPos;
					prevYVal = Integer.parseInt(people.get(i).val);
				}
				prevXPos = 20;
				prevYVal = 0;
				p.setColor(Color.BLACK);
				for(int i = 0; i < maxPeople.size();i ++) {
					long t = maxPeople.get(i).time.toEpochSecond();
					int xPos = posFromTime(t);
					p.drawLine(
							prevXPos, 	800 - prevYVal * yScale,
							xPos, 		800 - prevYVal * yScale);
					p.drawLine(
							xPos, 		800 - prevYVal * yScale,
							xPos, 		800 - Integer.parseInt(maxPeople.get(i).val) * yScale);
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
						p.drawString(i + "", xPos - 15, 815);
					} else {
						p.drawString(temp.format(DateTimeFormatter.ofPattern("YYYY MMM")), xPos - 15, 815);
					}
					p.drawLine(xPos, 800, xPos, 805);
				}
				for(int i = 0; i < 30; i ++) {
					p.drawString(i + "", 3, 803 - i * yScale);
					p.drawLine(15, 800 - i * yScale, 20, 800 - i * yScale);
				}
				
				if(toolTipVisible) {
					p.setColor(Color.WHITE);
					p.fillRect(mouseX, mouseY, 300, 50);
					p.setColor(Color.BLACK);
					p.drawRect(mouseX, mouseY, 300, 50);
					p.drawString(ZonedDateTime.ofInstant(Instant.ofEpochSecond(toolTipTime), ZoneId.of("Z")).format(DateTimeFormatter.ofPattern("YYYY MMM dd")), mouseX + 5, mouseY + 15);
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
		return (int) (((t - minTime) * 1400 )/ (maxTime - minTime)) + 20;
	}
	
	public static long timeFromPos(int p) {
		return ((p - 20) * (maxTime - minTime)) / 1400 + minTime;
	}

}

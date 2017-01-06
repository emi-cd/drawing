import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;

class MyJPanel extends JPanel {
	Line2D line;
	MyJPanel(){
		line = new Line2D.Double();
		setBackground(new Color(0,0,0));
	}
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.setStroke(new BasicStroke(4.0f));
		g2d.setPaint(Color.PINK);
		g2d.draw(line);
	}
	public void drawLine(Point p) {
		line.setLine(new Point2D.Double(0,0),p);
		repaint();
	}
}

class MyJFrame extends JFrame implements MouseListener,MouseMotionListener {
	JLabel label;
	MyJPanel myJPanel;
	 // 座標値データ
	int x1,y1;
	int x2,y2;
	MyJFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(200,200,800,600);
		setVisible(true);
		label = new JLabel();
		addMouseMotionListener(this);
		addMouseListener(this);
		myJPanel = new MyJPanel();
	}
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}
	@Override public void mousePressed(MouseEvent e) {
		e.consume();
		x1 = x2 = e.getX();
		y1 = y2 = e.getY();
		paint(getGraphics());
	}
	@Override public void mouseReleased(MouseEvent e) {}
	@Override public void mouseClicked(MouseEvent e) {}
	@Override public void mouseDragged(MouseEvent e) {
		e.consume();

		x2 = e.getX();
		y2 = e.getY();
		paint(getGraphics());

		x1 = e.getX();    // これが新たな始点になる
		y1 = e.getY();
  }
	@Override public void mouseMoved(MouseEvent e) {}
	public void paint(Graphics g) {
      g.drawLine(x1,y1,x2,y2);
  }
}

public class Drawing {
	public static void main(String[] args) {
		MyJFrame frame = new MyJFrame();
		MyJPanel myJPanel = new MyJPanel();
		frame.getContentPane().add(myJPanel);

		//frame.setVisible(true);
	}
}
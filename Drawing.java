import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.util.*;

class MyJPanel extends JPanel implements MouseListener,MouseMotionListener {
	ArrayList<Line2D> layers = new ArrayList<Line2D>();
	int x1,y1;
	MyJPanel(){
		addMouseMotionListener(this);
		addMouseListener(this);
	}
	@Override public void paintComponent(Graphics g) {
		super.paintComponent(g);
		repaint();
	}
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}
	@Override public void mousePressed(MouseEvent e) {
		e.consume();

		x1 = e.getX();
		y1 = e.getY();
		layers.add(new Line2D.Double(x1,y1,x1,y1));
		paint(getGraphics());
	}
	@Override public void mouseReleased(MouseEvent e) {}
	@Override public void mouseClicked(MouseEvent e) {}
	@Override public void mouseDragged(MouseEvent e) {
		e.consume();

		layers.add(new Line2D.Double(x1,y1,e.getX(),e.getY()));
		paint(getGraphics());
		x1 = e.getX();    // これが新たな始点になる
		y1 = e.getY();
	}
	@Override public void mouseMoved(MouseEvent e) {}
	public void paint(Graphics g) {
		for(int i = 0; i < layers.size() ;i++ ) {
    		Line2D data = (Line2D)layers.get(i);
      		g.drawLine((int)data.getX1(),(int)data.getY1(),(int)data.getX2(),(int)data.getY2());
      		System.out.println(layers.size());
    	}
  }
}

class MyJFrame extends JFrame {
	JLabel label;
	MyJPanel myJPanel;
	 // 座標値データ
	MyJFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(200,200,800,600);
		setVisible(true);
	}

}

public class Drawing {
	public static void main(String[] args) {
		MyJFrame frame = new MyJFrame();
		MyJPanel myJPanel = new MyJPanel();
		myJPanel.setPreferredSize(new Dimension(600, 600));
		JPanel buttonPanel = new JPanel();
		buttonPanel.setPreferredSize(new Dimension(200, 600));
		buttonPanel.setBackground(Color.PINK);

		//JButtonクラスのオブジェクトを作成
		JButton button1 = new JButton("button1");
		JButton button2 = new JButton("button2");
		
		buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.Y_AXIS));
		buttonPanel.add(button1);
		buttonPanel.add(button2);

		Container container = frame.getContentPane();
		container.add(myJPanel,BorderLayout.WEST);
		container.add(buttonPanel,BorderLayout.EAST);

		frame.setVisible(true);



	}
}
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.util.*;
import java.applet.Applet;

class DrawPanel extends JPanel implements MouseListener,MouseMotionListener {
    // ここに描いた軌跡を保存していく
    ArrayList<Line2D> layers = new ArrayList<Line2D>();
    // モード
    public static final int FREE = 0;
    public static final int LINE = 1;
    int mode = FREE;
    int x1,y1;
    int x2,y2;    // 終点
    int x3,y3;    // 前の終点
    // コンストラクタ
    DrawPanel(){
	addMouseMotionListener(this);
	addMouseListener(this);
    }
    // モードの選択
    public void setDrawMode(int mode) {
	switch(mode) {
	case FREE:
	case LINE:
	    this.mode = mode;
	    break;
	default:
	    break;
	}
    }
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {
	e.consume();
	switch(mode) {
	case FREE:
	    x1 = e.getX();
	    y1 = e.getY();
	    layers.add(new Line2D.Double(x1,y1,x1,y1));
	    paint(getGraphics());
	    break;
	case LINE:
	    x1 = e.getX();
	    y1 = e.getY();
	    x2 = -1;      // フラグ代わり
	    break;        // ラバーバンド描画処理を通るように
	default:
	    break;
	}
    }  
    @Override public void mouseReleased(MouseEvent e) {
	switch(mode) {
	case LINE:
	    layers.add(new Line2D.Double(x1,y1,e.getX(),e.getY()));
	    x2 = x3 = -1;   // ラバーバンド描画消去処理を通らないように
	    break;
	case FREE:
	default:
	    break;
	}
	paint(getGraphics());
    }
    @Override public void mouseDragged(MouseEvent e) {
	e.consume();
	
	switch(mode) {
	case FREE:  
	    layers.add(new Line2D.Double(x1,y1,e.getX(),e.getY()));
	    x1 = e.getX();    // これが新たな始点
	    y1 = e.getY();
	    break;
	case LINE:
	    x3 = x2;
	    y3 = y2;
	    x2 = e.getX();  // 終点
	    y2 = e.getY();
	    break;
	default:
	    break;
	}
	paint(getGraphics());
    }
    public void paint(Graphics g) {
	for(int i = 0; i < layers.size() ;i++ ) {
	    Line2D data = (Line2D)layers.get(i);
	    g.drawLine((int)data.getX1(),(int)data.getY1(),(int)data.getX2(),(int)data.getY2());
    	}
	if (mode == LINE) {
	    g.setColor(getBackground());
	    if (x3 != -1) {               // ドラッグの時のみ通る
		g.drawLine(x1,y1,x3,y3);    // 前に描いた線を消す
	    }
	    g.setColor(getForeground());
	    if (x2 != -1) {               // ボタン押下・ドラッグのときに通る
		g.drawLine(x1,y1,x2,y2);  
	    }
	}
    }
}


class MyJFrame extends JFrame {
    JLabel label;
    DrawPanel drawPanel;
    // 座標値データ
    MyJFrame() {
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	setBounds(200,200,800,600);
	setVisible(true);
    }
    
}

class OperationPanel extends JPanel implements ActionListener {
    DrawPanel drawPanel;
    OperationPanel() {
	MyJFrame frame = new MyJFrame();
        drawPanel = new DrawPanel();
	drawPanel.setBackground(Color.PINK);
	drawPanel.setPreferredSize(new Dimension(600, 600));

	// モードの選択
	JRadioButton free = new JRadioButton("FREE",true);
	JRadioButton line = new JRadioButton("LINE");
	free.addActionListener(this);
	line.addActionListener(this);

	ButtonGroup group = new ButtonGroup();
	group.add(free);
	group.add(line);

	this.setLayout(new GridLayout(2,1));
	this.add(free);
	this.add(line);

	frame.getContentPane().add(drawPanel,BorderLayout.WEST);
	frame.getContentPane().add(this,BorderLayout.EAST);
	frame.setVisible(true);	
    }
    public void actionPerformed(ActionEvent e) {
	if(e.getActionCommand() == "FREE"){
	    drawPanel.setDrawMode(0);
	}else
	    drawPanel.setDrawMode(1);
    }
}

public class Drawing {
    public static void main(String[] args) {
	OperationPanel operationPanel = new  OperationPanel();
    }
}

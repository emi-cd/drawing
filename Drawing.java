import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.util.*;
//import java.applet.Applet;
import javax.swing.event.*;

class LineInfo {
    private Point start;
    private Point end;
    private Color color;
    private float stroke;
    
    LineInfo(int x1, int y1, int x2, int y2,Color c,float f) {
	start = new Point();
	end = new Point();
	color = new Color(c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha());
	
	start.x = x1;
	start.y = y1;
	end.x = x2;
	end.y = y2;

	stroke = f;
    }
    public int getStartX() {
	return start.x;
    }
    public int getStartY() {
	return start.y;
    }
    public int getEndX() {
	return end.x;
    }
    public int getEndY() {
	return end.y;
    }
    public Color getColor() {
	return color;
    }	
    public float getStroke() {
	return stroke;
    }
}

class DrawPanel extends JPanel implements MouseListener,MouseMotionListener {
    // ここに描いた軌跡を保存していく
    ArrayList<LineInfo> layers = new ArrayList<LineInfo>();
    // モード
    public static final int FREE = 0;
    public static final int LINE = 1;
    int mode = FREE;
    int x1,y1;
    int x2,y2;    // 終点
    int x3,y3;    // 前の終点
    float stroke;
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
    public void setForegroundColor(Color color){
	setForeground(color);
    }
    public void setStroke(float f){
	stroke = f;
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
	    layers.add(new LineInfo(x1,y1,x1,y1,getForeground(),stroke));
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
	    layers.add(new LineInfo(x1,y1,e.getX(),e.getY(),getForeground(),stroke));
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
	    layers.add(new LineInfo(x1,y1,e.getX(),e.getY(),getForeground(),stroke));
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
	super.paintComponent(g);
	Graphics2D g2d = (Graphics2D)g;
	for(int i = 0; i < layers.size() ;i++ ) {
	    LineInfo data = (LineInfo)layers.get(i);
	    g2d.setColor(data.getColor());
	    g2d.setStroke(new BasicStroke(data.getStroke(),1,1));
	    g2d.drawLine(data.getStartX(), data.getStartY(), data.getEndX(), data.getEndY());
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
    MyJFrame() {
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	setBounds(200,200,800,600);
	setVisible(true);
    }
  
}

// 主に操作パネル。
class OperationPanel extends JPanel implements ActionListener,ChangeListener {
    JButton button;
    DrawPanel drawPanel;
    JSlider slider;
    OperationPanel() {
	// 色選択ボタン
	button = new JButton("COLOR");
	// モードの選択
	JRadioButton free = new JRadioButton("FREE",true);
	JRadioButton line = new JRadioButton("LINE");
	// 太さの変更
	slider = new JSlider(1,50,1);
	// グループの作成
	ButtonGroup group = new ButtonGroup();
	group.add(free);
	group.add(line);
	// ボタンの反応をつける
	button.addActionListener(this);
	free.addActionListener(this);
	line.addActionListener(this);
	slider.addChangeListener(this);
	
	MyJFrame frame = new MyJFrame();
        drawPanel = new DrawPanel();
	drawPanel.setBackground(Color.PINK);
	drawPanel.setPreferredSize(new Dimension(600, 600));
	
	this.setLayout(new GridLayout(4,1));
	this.add(free);
	this.add(line);
	this.add(button);
	this.add(slider);
	
	frame.getContentPane().add(drawPanel,BorderLayout.WEST);
	frame.getContentPane().add(this,BorderLayout.EAST);
	frame.setVisible(true);	
    }
    @Override public void actionPerformed(ActionEvent e) {
	if(e.getActionCommand() == "FREE"){        // FREE描画モード
	    drawPanel.setDrawMode(0);
	}else if(e.getActionCommand() == "LINE"){  // LINE描画モード
	    drawPanel.setDrawMode(1);
	}else if(e.getActionCommand() == "COLOR"){ // 色を選ぶとき
	    JColorChooser colorchooser = new JColorChooser();
	    Color color = colorchooser.showDialog(this,"Choose a color!",Color.black);
	    drawPanel.setForegroundColor(color);
        }
    }
    @Override public void stateChanged(ChangeEvent e) {
        drawPanel.setStroke(slider.getValue());
    }
}

public class Drawing {
    public static void main(String[] args) {
	OperationPanel operationPanel = new  OperationPanel();
    }
}

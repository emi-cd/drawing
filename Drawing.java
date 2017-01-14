import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.event.*;
import java.awt.image.*;

import java.io.*;
import javax.imageio.ImageIO;

class LineInfo {
    private Point start;
    private Point end;
    private Color color;
    private float stroke;
    private boolean flag;
    
    LineInfo(int x1, int y1, int x2, int y2,Color c,float s,boolean f) {
	start = new Point();
	end = new Point();
	color = new Color(c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha());
	flag = f;
	
	start.x = x1;
	start.y = y1;
	end.x = x2;
	end.y = y2;

	stroke = s;
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
    public boolean getFlag() {
	return flag;
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
    private BufferedImage image;
    // コンストラクタ
    DrawPanel(){
	addMouseMotionListener(this);
	addMouseListener(this);

        try {
            this.image = ImageIO.read(getClass().getResource("test.jpg"));
        } catch (IOException ex) {
            ex.printStackTrace();
            this.image = null;
        }
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
    public void setForegroundColor(Color color) {
	setForeground(color);
    }
    public void setStroke(float f){
	stroke = f;
    }
    public void undo() {
	if(layers.size() > 0){
	    layers.remove(layers.size()-1);
	    for(int i = layers.size()-1; i >= 0; i--){
		LineInfo data = (LineInfo)layers.get(i);
		layers.remove(i);
		if(data.getFlag()) {
		    break;
		}
	    }
	    paint(getGraphics());
	}
	System.out.println(layers.size());
    }
    @Override public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
	
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        double panelWidth = this.getWidth();
        double panelHeight = this.getHeight();
	
        // 画像がコンポーネントの何倍の大きさか計算
        double sx = (panelWidth / imageWidth);
        double sy = (panelHeight / imageHeight);
	
        // スケーリング
        AffineTransform af = AffineTransform.getScaleInstance(sx, sy);
        g2d.drawImage(image, af, this);
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
	    layers.add(new LineInfo(x1,y1,x1,y1,getForeground(),stroke,true));
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
	    layers.add(new LineInfo(x1,y1,e.getX(),e.getY(),getForeground(),stroke,true));
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
	    layers.add(new LineInfo(x1,y1,e.getX(),e.getY(),getForeground(),stroke,false));
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
	Image img = getToolkit().getImage("test.jpg");
	this.paintComponent(g);
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

class ImagePanel extends JPanel {
    // 描画する画像
    private BufferedImage image;
    
    public ImagePanel(String path) {
        try {
            this.image = ImageIO.read(getClass().getResource(path));
        } catch (IOException ex) {
            ex.printStackTrace();
            this.image = null;
        }
    }
    @Override public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
	
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        double panelWidth = this.getWidth();
        double panelHeight = this.getHeight();
	
        // 画像がコンポーネントの何倍の大きさか計算
        double sx = (panelWidth / imageWidth);
        double sy = (panelHeight / imageHeight);
	
        // スケーリング
        AffineTransform af = AffineTransform.getScaleInstance(sx, sy);
        g2d.drawImage(image, af, this);
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
    JButton color;
    JButton undo;
    DrawPanel drawPanel;
    JSlider slider;
    OperationPanel() {
	// 色選択ボタン
	color = new JButton("COLOR");
	//undoボタン
	undo= new JButton("UNDO");
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
	color.addActionListener(this);
	undo.addActionListener(this);
	free.addActionListener(this);
	line.addActionListener(this);
	slider.addChangeListener(this);
	
	MyJFrame frame = new MyJFrame();
        drawPanel = new DrawPanel();
	drawPanel.setBackground(Color.PINK);
	drawPanel.setPreferredSize(new Dimension(600, 600));
	
	this.setLayout(new GridLayout(5,1));
	this.add(free);
	this.add(line);
	this.add(color);
	this.add(slider);
	this.add(undo);
	
	frame.getContentPane().add(drawPanel,BorderLayout.WEST);
	frame.getContentPane().add(this,BorderLayout.EAST);
	frame.setVisible(true);	
    }
    @Override public void actionPerformed(ActionEvent e) {
	if(e.getActionCommand() == "FREE") {        // FREE描画モード
	    drawPanel.setDrawMode(0);
	}else if(e.getActionCommand() == "LINE") {  // LINE描画モード
	    drawPanel.setDrawMode(1);
	}else if(e.getActionCommand() == "COLOR") { // 色を選ぶとき
	    JColorChooser colorchooser = new JColorChooser();
	    Color color = colorchooser.showDialog(this,"Choose a color!",Color.black);
	    drawPanel.setForegroundColor(color);
        }else if(e.getActionCommand() == "UNDO") { // undo機能
	    drawPanel.undo();
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

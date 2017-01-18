import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.imageio.stream.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.math.BigDecimal;

// 線の情報を持つクラス
class LineInfo {
	private Point start;
	private Point end;
	private Color color;
	private float stroke;
	private boolean flag;			// 線の始点終点を判断する
	
	// コンストラクタ
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
	// 以下、getシリーズ
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

// 絵を描くパネル
class DrawPanel extends JPanel implements MouseListener,MouseMotionListener {
	// ここに描いた軌跡を保存していく
	ArrayList<LineInfo> layers = new ArrayList<LineInfo>();
	// モード
	public static final int FREE = 0;
	public static final int LINE = 1;
	int mode = FREE;

	int x1,y1;
	int x2,y2;		// 終点
	int x3,y3;		// 前の終点
	float stroke;	// 線の太さ

	private BufferedImage image;
	File f;
	JFrame frame;

	// コンストラクタ
	DrawPanel(MyJFrame f){
		addMouseMotionListener(this);
		addMouseListener(this);
		this.image = null;
		frame = f;
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

	// 文字の色の設定
	public void setForegroundColor(Color color) {
		setForeground(color);
	}

	// 文字の太さの設定
	public void setStroke(float f){
		stroke = f;
	}

	// undo機能
	public void undo() {
		if(layers.size() > 0){
			for(int i = layers.size()-1; i >= 0; i--){
				LineInfo data = (LineInfo)layers.get(i);
				layers.remove(i);
				if(data.getFlag()) {
					break;
				}
			}
			repaint();
		}
	}

	// clear機能
	public void clear() {
		layers.clear();
		repaint();
	}

	public BufferedImage reSize(BufferedImage image, int maxWidth, int maxHeight) {
		// 縮小比率の計算
		BigDecimal bdW = new BigDecimal(maxWidth);
		bdW = bdW.divide(new BigDecimal(image.getWidth()), 8, BigDecimal.ROUND_HALF_UP);
		BigDecimal bdH = new BigDecimal(maxHeight);
		bdH = bdH.divide(new BigDecimal(image.getHeight()), 8, BigDecimal.ROUND_HALF_UP);
		// 縦横比は変えずに最大幅、最大高さを超えないように比率を指定する
		if (bdH.compareTo(bdW) < 0) {
			maxHeight = -1;
		}
		else {
			maxWidth = -1;
		}

		// Image -> BufferedImageの変換
		BufferedImage thumb = new BufferedImage(getWidth(), getHeight(), image.getType());
		thumb.getGraphics().drawImage(image.getScaledInstance(maxWidth, maxHeight, Image.SCALE_SMOOTH), 0, 0, null);
		return thumb;
	}


	// 画像を保存
	public void saveImage(){
		BufferedImage readImage = null;
		boolean flag = false;

		if(image != null){
			try {
				// readImage = ImageIO.read(new File(f.getPath()));
				readImage = reSize(ImageIO.read(new File(f.getPath())),getWidth(),getHeight());
			} catch (Exception e) {
				e.printStackTrace();
				readImage = null;
			}
		}

		if (readImage == null){
			readImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_BGR);
			flag = true;
		}
		
		Graphics2D off = readImage.createGraphics();
		if(flag){
			off.setBackground(Color.WHITE);
			off.clearRect(0, 0, getWidth(), getHeight());
		}

		for(int i = 0; i < layers.size() ;i++ ) {
			LineInfo data = (LineInfo)layers.get(i);
			off.setColor(data.getColor());
			off.setStroke(new BasicStroke(data.getStroke(),1,1));
			off.drawLine(data.getStartX(), data.getStartY(), data.getEndX(), data.getEndY());
		}

		String path = null;  
		FileDialog fd = new FileDialog(frame , "名前を付けて保存" , FileDialog.SAVE);  
		try{  
			fd.setVisible(true);  
			if (fd.getFile() != null) {  
				path = fd.getDirectory() + fd.getFile();  
			}  
		}finally{  
			fd.dispose();  
		}  
		if (path != null){  
			try {
				boolean result = ImageIO.write(readImage, "jpg", new File(path));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}  		
	}

	// 写真の読み込み
	public void openImage() {
		JFileChooser fc = new JFileChooser();
		// 画像ファイルの拡張子を設定
		fc.setFileFilter(new FileNameExtensionFilter("画像ファイル", "png", "jpg","Jpeg", "GIF", "bmp"));
		// ファイル選択ダイアログを表示、戻り値がAPPROVE_OPTIONの場合、画像ファイルを開く
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			f = fc.getSelectedFile();
			try {
				this.image = ImageIO.read(new File(f.getPath()));
			} catch (IOException ex) {
				ex.printStackTrace();
				this.image = null;
			}
		}
		repaint();
	}

	// 読み込んだ写真を消す
	public void noopen() {
		this.image = null;
		repaint();
	}

	@Override public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setBackground(Color.WHITE);

		if(image != null){
			int imageWidth = image.getWidth();
			int imageHeight = image.getHeight();
			int panelWidth = this.getWidth();
			int panelHeight = this.getHeight();
		
			// 画像がコンポーネントの何倍の大きさか計算
			int iH = (panelWidth * imageHeight / imageWidth);
			int iW = (panelHeight * imageWidth / imageHeight);

			if(panelWidth < panelHeight) {
				if(panelHeight > imageHeight || imageWidth > panelWidth) {
					g2d.drawImage(image, 0, 0, iW, panelHeight, this);
				} else {
					g2d.drawImage(image, 0, 0, panelWidth, iH, this);
				}
			}else{
				if(panelWidth > imageWidth || panelHeight < imageHeight){
					g2d.drawImage(image, 0, 0, panelWidth, iH, this);
				}else{
					g2d.drawImage(image, 0, 0, iW, panelHeight, this);
				}
			}
		}

	}

	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseClicked(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}
	@Override public void mouseMoved(MouseEvent e) {}

	// マウスが押された時
	@Override public void mousePressed(MouseEvent e) {
	e.consume();
	switch(mode) {
		case FREE:
			x1 = e.getX();
			y1 = e.getY();
			layers.add(new LineInfo(x1,y1,x1,y1,getForeground(),stroke,true));
			repaint();
			break;
		case LINE:
			x1 = e.getX();
			y1 = e.getY();
			x2 = -1;							// フラグ代わり
			break;								// ラバーバンド描画処理を通るように
		default:
			break;
		}
	}

	// マウスが離れた時
	@Override public void mouseReleased(MouseEvent e) {
		switch(mode) {
			case LINE:
				layers.add(new LineInfo(x1,y1,e.getX(),e.getY(),getForeground(),stroke,true));
				x2 = x3 = -1;					// ラバーバンド描画消去処理を通らないように
				break;
			case FREE:
			default:
				break;
		}
		repaint();
	}

	// マウスがドラッグされている時
	@Override public void mouseDragged(MouseEvent e) {
		e.consume();	
		switch(mode) {
			case FREE:
				layers.add(new LineInfo(x1,y1,e.getX(),e.getY(),getForeground(),stroke,false));
				x1 = e.getX();					// これが新たな始点
				y1 = e.getY();
				break;
			case LINE:
				x3 = x2;
				y3 = y2;
				x2 = e.getX();					// 終点
				y2 = e.getY();
				break;
			default:
				break;
		}
		repaint();
	}

	// ペイント
	@Override public void paint(Graphics g) {
		this.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;

		for(int i = 0; i < layers.size() ;i++ ) {
			LineInfo data = (LineInfo)layers.get(i);
			g2d.setColor(data.getColor());
			g2d.setStroke(new BasicStroke(data.getStroke(),1,1));
			g2d.drawLine(data.getStartX(), data.getStartY(), data.getEndX(), data.getEndY());
		}
		if (mode == LINE) {
			g2d.setColor(getBackground());
			g2d.setStroke(new BasicStroke(stroke,1,1));
			if (x3 != -1) {						// ドラッグの時のみ通る
				g2d.drawLine(x1,y1,x3,y3);		// 前に描いた線を消す
			}
			g.setColor(getForeground());
			if (x2 != -1) {						// ボタン押下・ドラッグのときに通る
				g2d.drawLine(x1,y1,x2,y2);
			}
		}
	}

}

// フレーム
class MyJFrame extends JFrame {
	MyJFrame() {
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	setBounds(200,200,800,600);
	setVisible(true);
	}   
}

// 主に操作パネル。
class OperationPanel extends JPanel implements ActionListener,ChangeListener {
	DrawPanel drawPanel;
	JSlider slider;

	// コンストラクタ
	OperationPanel(DrawPanel panel) {
		drawPanel = panel;

		// 色選択ボタン
		JButton color = new JButton("COLOR");

		// undoボタン
		JButton undo = new JButton("UNDO");
		undo.setPreferredSize(new Dimension(80, 100));
		JButton clear = new JButton("CLEAR");
		clear.setPreferredSize(new Dimension(80, 100));

		// saveボタン
		JButton save = new JButton("SAVE");

		// 画像選択
		JButton photo = new JButton("PHOTO");
		photo.setPreferredSize(new Dimension(80, 100));
		JButton nophoto = new JButton("NOPHOTO");
		nophoto.setPreferredSize(new Dimension(80, 100));

		// モードの選択
		JRadioButton free = new JRadioButton("FREE",true);
		free.setMargin(new Insets(50, 0, 0, 0));
		JRadioButton line = new JRadioButton("LINE");
		line.setMargin(new Insets(10, 0, 20, 0));
		// グループの作成
		ButtonGroup group = new ButtonGroup();
		group.add(free);
		group.add(line);

		// 太さの変更
		slider = new JSlider(1,50,1);

		// ボタンの反応をつける
		color.addActionListener(this);
		undo.addActionListener(this);
		clear.addActionListener(this);
		save.addActionListener(this);
		photo.addActionListener(this);
		nophoto.addActionListener(this);
		free.addActionListener(this);
		line.addActionListener(this);
		slider.addChangeListener(this);
	
		JPanel panel1 = new JPanel();
		JPanel panel2 = new JPanel();
		JPanel panel3 = new JPanel();
		JPanel panel4 = new JPanel();

		panel1.setLayout(new GridLayout(4,1));
		panel1.add(free);
		panel1.add(line);
		panel1.add(color);
		panel1.add(slider);

		panel2.setLayout(new FlowLayout());
		panel2.add(photo);
		panel2.add(nophoto);

		panel3.setLayout(new FlowLayout());
		panel3.add(undo);
		panel3.add(clear);

		panel4.setLayout(new GridLayout(1,1));
		panel4.add(save);

		this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		this.add(panel1);
		this.add(panel2);
		this.add(panel3);
		this.add(panel4);
	}
	// 反応をつける
	@Override public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand() == "FREE") {			// FREE描画モード
			drawPanel.setDrawMode(0);
		}else if(e.getActionCommand() == "LINE") {		// LINE描画モード
			drawPanel.setDrawMode(1);
		}else if(e.getActionCommand() == "COLOR") {		// 色を選ぶとき
			JColorChooser colorchooser = new JColorChooser();
			Color color = colorchooser.showDialog(this,"Choose a color!",Color.black);
			drawPanel.setForegroundColor(color);	
		}else if(e.getActionCommand() == "UNDO") {		// undo機能
			drawPanel.undo();
		}else if(e.getActionCommand() == "PHOTO") {		// 写真選択
			drawPanel.openImage();
		}else if(e.getActionCommand() == "NOPHOTO") {
			drawPanel.noopen();
		}else if(e.getActionCommand() == "SAVE") {
			drawPanel.saveImage();
		}else if(e.getActionCommand() == "CLEAR") {
			int option = JOptionPane.showConfirmDialog(this, "clearすると元に戻りません。","Clear", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (option == JOptionPane.YES_OPTION){
				drawPanel.clear();
			}
		}	
	}
	@Override public void stateChanged(ChangeEvent e) {
		drawPanel.setStroke(slider.getValue());
	}

}

// メインのクラス
public class Drawing {
	public static void main(String[] args) {
		MyJFrame frame = new MyJFrame();
		DrawPanel drawPanel = new DrawPanel(frame);
		OperationPanel operationPanel = new OperationPanel(drawPanel);

		frame.getContentPane().add(drawPanel,BorderLayout.CENTER);
		frame.getContentPane().add(operationPanel,BorderLayout.EAST);
		frame.setVisible(true);	
	}

}

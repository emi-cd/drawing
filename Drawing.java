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
	private Point start = new Point();
	private Point end = new Point();
	private Color color;
	private float stroke;
	private boolean flag;			// 線の始点終点を判断する
	private String text = null;
	private String font = null;
	private int fontMode;
	private BufferedImage stamp = null;
	
	// コンストラクタ
	// 線
	LineInfo(int x1, int y1, int x2, int y2,Color c,float s, boolean f) {
		color = new Color(c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha());
		flag = f;
	
		start.x = x1;
		start.y = y1;
		end.x = x2;
		end.y = y2;

		stroke = s + 10.0f;
	}
	// 文字
	LineInfo(int x1, int y1, int x2, int y2, Color c, String str, String fo, int fm, float s, boolean f){
		color = new Color(c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha());
		start.x = x1;
		start.y = y1;
		end.x = x2;
		end.y = y2;

		text = str;
		font = fo;
		fontMode = fm;
		stroke = s + 10.0f;
		flag = f;
	}
	// スタンプ
	LineInfo(int x1, int y1, int x2, int y2, String path, boolean f){
		start.x = x1;
		start.y = y1;
		end.x = x2;
		end.y = y2;
		try {
			stamp = ImageIO.read(new File(path));
		} catch (IOException ex) {
			ex.printStackTrace();
			stamp = null;
		}

		flag = f;
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
	public String getText(){
		return text;
	}
	public BufferedImage getStamp(){
		return stamp;
	}
	public String getFont(){
		return font;
	}
	public int getFontMode(){
		return fontMode;
	}

}

// 絵を描くパネル
class DrawPanel extends JPanel implements MouseListener,MouseMotionListener,ComponentListener {
	// ここに描いた軌跡を保存していく
	ArrayList<LineInfo> layers = new ArrayList<LineInfo>();
	// モード
	public static final int FREE = 0;
	public static final int LINE = 1;
	public static final int TEXT = 2;
	public static final int STAMP = 3;
	int mode = FREE;

	int x1,y1;
	int x2,y2;		// 終点
	int x3,y3;		// 前の終点

	float stroke;	// 線の太さ

	String text;
	String font;
	int fontMode;
	String stamp;			// スタンプ画像のpathを収納
	boolean flag = false;	// 文字入力

	private BufferedImage image;
	private Color bgColor = new Color(255,255,255);
	File f;
	JFrame frame;

	// コンストラクタ
	DrawPanel(MyJFrame f){
		addMouseMotionListener(this);
		addMouseListener(this);
		addComponentListener(this);
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
			case TEXT:
				this.mode = mode;
			case STAMP:
				this.mode = mode;
			default:
				break;
		}
	}

	// スタンプのパスをセット
	public void setStamp(String str) {
		stamp = "stamp/stamp" + str + ".png";
	}

	// 書く文字をセット
	public void setText(String str) {
		text = str;
	}

	// fontをセット
	public void setFont(String str){
		font = str;
	}

	// 文字の色の設定
	public void setForegroundColor(Color color) {
		setForeground(color);
	}

	// fontmodeの設定
	public void setFontMode(int num){
		fontMode = num;
	}

	// 背景色の設定
	public void setBGColor(Color color) {
		bgColor = color;
		repaint();
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
				if(data.getFlag() || layers.size() == 0) {
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

	// 写真サイズの変更
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
			off.setBackground(bgColor);
			off.clearRect(0, 0, getWidth(), getHeight());
		}

		for(int i = 0; i < layers.size() ;i++ ) {
			LineInfo data = (LineInfo)layers.get(i);
			off.setColor(data.getColor());
			if(data.getStamp() != null) {
				off.drawImage(data.getStamp(),data.getStartX(), data.getStartY(), this);
			} else if(data.getText() == null){
				off.setStroke(new BasicStroke(data.getStroke(),1,1));
				off.drawLine(data.getStartX(), data.getStartY(), data.getEndX(), data.getEndY());
			} else if(data.getText() != null){
				off.setFont(new Font(data.getFont(), data.getFontMode(), (int)data.getStroke()));
				off.drawString(data.getText(), data.getStartX(), data.getStartY());
			}
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

	// 読み込んだ写真を消す
	public void noopen() {
		this.image = null;
		repaint();
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
		case TEXT:
			x1 = e.getX();
			y1 = e.getY();
			layers.add(new LineInfo(x1, y1, x1, y1, getForeground(), text, font, fontMode, stroke, true));
			flag = true;
			repaint();
			break;
		case STAMP:
			x1 = e.getX();
			y1 = e.getY();
			layers.add(new LineInfo(x1, y1, x1, y1, stamp, true));
			flag = true;
			repaint();
			break;
		default:
			break;
		}
	}

	// マウスが離れた時
	@Override public void mouseReleased(MouseEvent e) {
		switch(mode) {
			case LINE:
				layers.add(new LineInfo(x1, y1, e.getX(), e.getY(), getForeground(), stroke, true));
				x2 = x3 = -1;					// ラバーバンド描画消去処理を通らないように
				break;
			case TEXT:
				layers.add(new LineInfo(x1, y1, e.getX(), e.getY(), getForeground(), text, font, fontMode, stroke, true));
				flag = false;
				break;
			case STAMP:
				layers.add(new LineInfo(x1, y1, e.getX(), e.getY(), stamp, true));
				flag = false;
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
			case TEXT:
				layers.add(new LineInfo(x1,y1,e.getX(),e.getY(), getForeground(), text, font, fontMode, stroke, false));
				flag = true;
				x1 = e.getX();					// これが新たな始点
				y1 = e.getY();
				break;
			case STAMP:
				layers.add(new LineInfo(x1,y1,e.getX(),e.getY(), stamp, false));
				flag = true;
				x1 = e.getX();					// これが新たな始点
				y1 = e.getY();
				break;
			default:
				break;
		}
		repaint();
	}

	@Override public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		if(image != null){
			int maxWidth = getWidth();
			int maxHeight = getHeight();

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

			g2d.drawImage(image.getScaledInstance(maxWidth, maxHeight, Image.SCALE_SMOOTH), 0, 0, null);

		}
	}

	// ペイント
	@Override public void paint(Graphics g) {
		this.paintComponent(g);
		this.setBackground(bgColor);
		Graphics2D g2d = (Graphics2D)g;

		for(int i = 0; i < layers.size() ;i++ ) {
			LineInfo data = (LineInfo)layers.get(i);
			g2d.setColor(data.getColor());
			if(data.getStamp() != null) {
				g2d.drawImage(data.getStamp(),data.getStartX(), data.getStartY(), this);
			} else if(data.getText() == null){
				g2d.setStroke(new BasicStroke(data.getStroke(),1,1));
				g2d.drawLine(data.getStartX(), data.getStartY(), data.getEndX(), data.getEndY());
			} else if(data.getText() != null){
				g2d.setFont(new Font(data.getFont(), data.getFontMode(), (int)data.getStroke()));
				g2d.drawString(data.getText(), data.getStartX(), data.getStartY());
			}
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
		} else if (mode == TEXT || mode == STAMP) {
			if(flag){
				layers.remove(layers.size()-1);
			}
		}
	}
	@Override public void componentResized(ComponentEvent e) {
		repaint();
	}
	@Override public void componentMoved(ComponentEvent e) {}
	@Override public void componentHidden(ComponentEvent e) {}
	@Override public void componentShown(ComponentEvent e) {}
}

// フレーム
class MyJFrame extends JFrame implements ActionListener {
	DrawPanel drawPanel;

	void setDrawPanel(DrawPanel dp){
		drawPanel = dp;
	}

	MyJFrame() {
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	setBounds(200,200,800,600);
	setVisible(true);


	JMenuBar menubar = new JMenuBar();
    JMenu file = new JMenu("FILE");
    JMenu mode = new JMenu("MODE");
    JMenu photo = new JMenu("PHOTO");

	menubar.add(file);
    JMenuItem newFile = new JMenuItem("New Page");
    newFile.setMnemonic(KeyEvent.VK_N);
    newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
    JMenuItem save = new JMenuItem("SAVE");
    save.setMnemonic(KeyEvent.VK_S);
    save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
    JMenuItem undo = new JMenuItem("UNDO");
    undo.setMnemonic(KeyEvent.VK_Z);
    undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
    JMenuItem clear = new JMenuItem("CLEAR");
    file.add(newFile);
    file.add(save);
    file.add(undo);
    file.add(clear);

	menubar.add(mode);
    JRadioButtonMenuItem free = new JRadioButtonMenuItem("FREE");
    JRadioButtonMenuItem line = new JRadioButtonMenuItem("LINE");
    JRadioButtonMenuItem text = new JRadioButtonMenuItem("TEXT");
    free.setSelected(true);
    ButtonGroup group = new ButtonGroup();
    group.add(free);
    group.add(line);
    group.add(text);
    mode.add(free);
    mode.add(line);
    mode.add(text);

    menubar.add(photo);
    JMenuItem yesPhoto = new JMenuItem("PHOTO");
    JMenuItem nophoto = new JMenuItem("NO PHOTO");
    photo.add(yesPhoto);
    photo.add(nophoto);

    setJMenuBar(menubar);

    newFile.addActionListener(this);
	save.addActionListener(this);
	undo.addActionListener(this);
	clear.addActionListener(this);
	free.addActionListener(this);
	line.addActionListener(this);
	text.addActionListener(this);
	yesPhoto.addActionListener(this);
	nophoto.addActionListener(this);
	}   

	@Override public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand() == "New Page"){
			int option = JOptionPane.showConfirmDialog(this, "新しいページを作ります。その前に保存しますか?","New Page", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(option == JOptionPane.YES_OPTION){
				drawPanel.saveImage();
			}
			option = JOptionPane.showConfirmDialog(this, "このページのデータは消えますがよろしいですか?","New Page", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(option == JOptionPane.YES_OPTION){
			drawPanel.setBGColor(Color.WHITE);
			drawPanel.clear();
			drawPanel.noopen();
			}
		}else if(e.getActionCommand() == "SAVE") {
			drawPanel.saveImage();
		}else if(e.getActionCommand() == "UNDO") {		// undo機能
			drawPanel.undo();
		}else if(e.getActionCommand() == "CLEAR") {
			int option = JOptionPane.showConfirmDialog(this, "clearすると元に戻りません。","Clear", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (option == JOptionPane.YES_OPTION){
				drawPanel.clear();
			}
		}else if(e.getActionCommand() == "FREE") {		// FREE描画モード
			drawPanel.setDrawMode(0);
		}else if(e.getActionCommand() == "LINE") {		// LINE描画モード
			drawPanel.setDrawMode(1);
		}else if(e.getActionCommand() == "TEXT") {		// LINE描画モード
			drawPanel.setDrawMode(2);
		}else if(e.getActionCommand() == "PHOTO") {		// 写真選択
			drawPanel.openImage();
		}else if(e.getActionCommand() == "NO PHOTO") {
			drawPanel.noopen();
		}
	}

}


class SamplePanel extends JPanel {
	Color color;
	float stroke;

	SamplePanel(){
		color = Color.BLACK;
		stroke = 1;
		setPreferredSize(new Dimension(80, 200));
	}
	public void setColor(Color c){
		color = c;
		repaint();
	}
	public void setStroke(float f){
		stroke = f;
		repaint();
	}
	public void paintComponent(Graphics g){
		Graphics2D g2 = (Graphics2D)g;

		g2.setPaint(color);
		g2.setStroke(new BasicStroke(stroke+10));
		g2.draw(new Line2D.Double(50.0d, 40.0d, 100.0d, 40.0d));
		g2.drawString("A",150,80);
		g2.setStroke(new BasicStroke(1));
		g2.setColor(Color.BLACK);
		g2.draw(new Line2D.Double(145.0d, 20.0d, 145.0d, 80.0d));
	}

}

// 主に操作パネル。
class OperationPanel extends JPanel implements ActionListener,ChangeListener {
	DrawPanel drawPanel;
	JSlider slider;
	SamplePanel samplePanel;
	JTextField text;
	JComboBox font;
	boolean flag = false;							// falseでPLANE,trueでITALIC
	boolean boldFlag = false;						// trueでBOLD

	// コンストラクタ
	OperationPanel(DrawPanel panel) {
		drawPanel = panel;

		// モードの選択
		JRadioButton free = new JRadioButton("FREE",true);
		free.setMargin(new Insets(10, 0, 0, 0));
		JRadioButton line = new JRadioButton("LINE");
		line.setMargin(new Insets(10, 0, 0, 0));
		JRadioButton letter = new JRadioButton("TEXT");
		letter.setMargin(new Insets(10, 0, 0, 0));

		// グループの作成
		ButtonGroup group = new ButtonGroup();
		group.add(free);
		group.add(line);
		group.add(letter);

		// 色選択ボタン
		JButton color = new JButton("COLOR");

		// undoボタン
		JButton undo = new JButton("UNDO");
		undo.setPreferredSize(new Dimension(80, 50));
		
		// clearボタン
		JButton clear = new JButton("CLEAR");
		clear.setPreferredSize(new Dimension(70, 50));

		// saveボタン
		JButton save = new JButton("SAVE");
		save.setPreferredSize(new Dimension(100, 40));

		// 文字入力
		JButton textButton = new JButton("GO");
		text = new JTextField("");

		// Fontの設定
		String fontNames[] = {"Serif","SansSerif","Monospaced"};		// エラー
		// 物理フォントの一覧を取得する
		//GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		//Font fonts[] = ge.getAllFonts();
		//Vector<String> fontNames = new Vector<String>();
		//for (int i = 0; i < fonts.length; i++ ){
		//fontNames.addElement(fonts[i].getName());
		//}
		font = new JComboBox(fontNames);

		// fontスタイルの設定
		JRadioButton plane = new JRadioButton("PLANE",true);
		plane.setMargin(new Insets(10, 0, 0, 0));
		JRadioButton italic = new JRadioButton("ITALIC");
		italic.setMargin(new Insets(10, 0, 0, 0));
		JRadioButton bold = new JRadioButton("BOLD");
		bold.setMargin(new Insets(10, 0, 0, 0));

		// グループの作成
		ButtonGroup group2 = new ButtonGroup();
		group2.add(plane);
		group2.add(italic);

		// 太さの変更
		slider = new JSlider(1,80,1);

		// ボタンの反応をつける
		free.addActionListener(this);
		line.addActionListener(this);
		letter.addActionListener(this);
		font.addActionListener(this);
		plane.addActionListener(this);
		italic.addActionListener(this);
		bold.addActionListener(this);
		color.addActionListener(this);
		undo.addActionListener(this);
		clear.addActionListener(this);
		save.addActionListener(this);
		textButton.addActionListener(this);
		slider.addChangeListener(this);
	
		JPanel panel1 = new JPanel();
		samplePanel = new SamplePanel();
		JPanel panel2 = new JPanel();
		JPanel panel3 = new JPanel();
		JPanel panel4 = new JPanel();
		JPanel fontMode = new JPanel();

		panel1.setLayout(new GridLayout(1,3));
		panel1.add(free);
		panel1.add(line);
		panel1.add(letter);

		fontMode.setLayout(new GridLayout(1,3));
		fontMode.add(plane);
		fontMode.add(italic);
		fontMode.add(bold);

		panel2.setLayout(new GridLayout(6,1));
		panel2.add(text);
		panel2.add(textButton);
		panel2.add(font);
		panel2.add(fontMode);
		panel2.add(color);
		panel2.add(slider);

		panel3.setLayout(new FlowLayout());
		panel3.add(undo);
		panel3.add(clear);

		panel4.setLayout(new FlowLayout());
		panel4.add(save);

		this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		this.add(panel1);
		this.add(panel2);
		this.add(samplePanel);
		this.add(panel3);
		this.add(panel4);

	}
	// 反応をつける
	@Override public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand() == "FREE") {			// FREE描画モード
			drawPanel.setDrawMode(0);
		}else if(e.getActionCommand() == "LINE") {		// LINE描画モード
			drawPanel.setDrawMode(1);
		}else if(e.getActionCommand() == "TEXT") {		// LINE描画モード
			drawPanel.setFont((String)font.getSelectedItem());
			drawPanel.setText(text.getText());
			drawPanel.setDrawMode(2);
		}else if(e.getActionCommand() == "COLOR") {		// 色を選ぶとき
			JColorChooser colorchooser = new JColorChooser();
			Color color = colorchooser.showDialog(this,"Choose a color!",Color.black);
			drawPanel.setForegroundColor(color);
			samplePanel.setColor(color);
		}else if(e.getActionCommand() == "GO"){
			drawPanel.setDrawMode(2);
			drawPanel.setText(text.getText());
		}else if(e.getActionCommand() == "UNDO") {		// undo機能
			drawPanel.undo();
		}else if(e.getActionCommand() == "CLEAR") {
			int option = JOptionPane.showConfirmDialog(this, "clearすると元に戻りません。","Clear", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (option == JOptionPane.YES_OPTION){
				drawPanel.clear();
			}
		}else if(e.getActionCommand() == "SAVE") {
			drawPanel.saveImage();
		}else if(e.getActionCommand() == "PLANE") {
			flag = false;
		}else if(e.getActionCommand() == "ITALIC") {
			flag = true;
		}else if(e.getActionCommand() == "BOLD") {
			boldFlag = !boldFlag;
		}
		drawPanel.setFont((String)font.getSelectedItem());
		if(flag){
			if (boldFlag) {							// ITALICでBOLDの場合	
				samplePanel.setFont(new Font((String)font.getSelectedItem(), Font.BOLD | Font.ITALIC, (int)slider.getValue()+10));
				drawPanel.setFontMode(Font.BOLD | Font.ITALIC);
			}else {
				samplePanel.setFont(new Font((String)font.getSelectedItem(), Font.ITALIC, (int)slider.getValue()+10));
				drawPanel.setFontMode(Font.ITALIC);
			}
		}else{
			if(boldFlag){							// PLANEでBOLD
				samplePanel.setFont(new Font((String)font.getSelectedItem(), Font.BOLD, (int)slider.getValue()+10));
				drawPanel.setFontMode(Font.BOLD);
			}else {
				samplePanel.setFont(new Font((String)font.getSelectedItem(), Font.PLAIN, (int)slider.getValue()+10));
				drawPanel.setFontMode(Font.PLAIN);
			}
		}
		samplePanel.repaint();

	}

	@Override public void stateChanged(ChangeEvent e) {
		drawPanel.setStroke(slider.getValue());
		samplePanel.setStroke(slider.getValue());
		samplePanel.setFont(new Font((String)font.getSelectedItem(), Font.BOLD, (int)slider.getValue()+10));
	}

}

class SecondOperationPanel extends JPanel implements ActionListener {
	DrawPanel drawPanel;
	public static final int NUM = 15;			// スタンプの数

	// コンストラクタ
	SecondOperationPanel(DrawPanel panel) {
		drawPanel = panel;
		JButton[] button = new JButton[NUM];
		ImageIcon[] icon = new ImageIcon[NUM];
		JPanel panel1 = new JPanel();

		panel1.setLayout(new GridLayout(5,3));
		// スタンプ
		for (int i=0; i<NUM; i++) {
			icon[i] = new ImageIcon("stamp/stamp" + (i+1) + ".png");
			button[i] = new JButton(String.valueOf(i+1),icon[i]);
			button[i].setVerticalTextPosition(JButton.BOTTOM);
			panel1.add(button[i]);
			button[i].addActionListener(this);
		}

		// undoボタン
		JButton undo = new JButton("UNDO");
		undo.setPreferredSize(new Dimension(80, 50));

		// clearボタン
		JButton clear = new JButton("CLEAR");
		clear.setPreferredSize(new Dimension(70, 50));

		// saveボタン
		JButton save = new JButton("SAVE");
		save.setPreferredSize(new Dimension(100, 40));


		// ボタンの反応をつける
		undo.addActionListener(this);
		clear.addActionListener(this);
		save.addActionListener(this);	

		JPanel panel2 = new JPanel();
		JPanel panel3 = new JPanel();
		
		panel2.setLayout(new FlowLayout());
		panel2.add(undo);
		panel2.add(clear);

		panel3.setLayout(new FlowLayout());
		panel3.add(save);

		this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		this.add(panel1);
		this.add(panel2);
		this.add(panel3);

	}
	// 反応をつける
	@Override public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand() == "UNDO") {		// undo機能
			drawPanel.undo();
		}else if(e.getActionCommand() == "CLEAR") {
			int option = JOptionPane.showConfirmDialog(this, "clearすると元に戻りません。","Clear", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (option == JOptionPane.YES_OPTION){
				drawPanel.clear();
			}
		}else if(e.getActionCommand() == "SAVE") {
			drawPanel.saveImage();
		}else {										// 数字だった場合
			drawPanel.setDrawMode(3);
			drawPanel.setStamp(e.getActionCommand());
		}
	}

}

class ThirdOperationPanel extends JPanel implements ActionListener {
	DrawPanel drawPanel;

	// コンストラクタ
	ThirdOperationPanel(DrawPanel panel) {
		drawPanel = panel;

		// 背景色
		JButton color = new JButton("BG COLOR");

		// 画像選択
		JButton photo = new JButton("PHOTO");
		photo.setPreferredSize(new Dimension(90, 80));
		JButton nophoto = new JButton("NOPHOTO");
		nophoto.setPreferredSize(new Dimension(90, 80));

		// 新しいページ
		JButton newPage = new JButton("New Page");
		newPage.setPreferredSize(new Dimension(70, 100));

		// clearボタン
		JButton clear = new JButton("CLEAR");
		clear.setPreferredSize(new Dimension(70, 50));

		// saveボタン
		JButton save = new JButton("SAVE");
		save.setPreferredSize(new Dimension(70, 50));

		// ボタンの反応をつける
		color.addActionListener(this);
		photo.addActionListener(this);
		nophoto.addActionListener(this);
		newPage.addActionListener(this);
		clear.addActionListener(this);
		save.addActionListener(this);

		JPanel panel1 = new JPanel();
		JPanel panel2 = new JPanel();
		JPanel panel3 = new JPanel();

		panel1.setLayout(new GridLayout(2,1));
		panel1.add(color);
		panel1.add(newPage);
		
		panel2.setLayout(new FlowLayout());
		panel2.add(photo);
		panel2.add(nophoto);

		panel3.setLayout(new FlowLayout());
		panel3.add(clear);
		panel3.add(save);

		this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		this.add(panel1);
		this.add(panel2);
		this.add(panel3);

	}
	// 反応をつける
	@Override public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand() == "BG COLOR") {		// 色を選ぶとき
			JColorChooser colorchooser = new JColorChooser();
			Color color = colorchooser.showDialog(this,"Choose a color!",Color.WHITE);
			drawPanel.setBGColor(color);
		}else if(e.getActionCommand() == "PHOTO") {		// 写真選択
			drawPanel.openImage();
		}else if(e.getActionCommand() == "NO PHOTO") {
			drawPanel.noopen();						// 写真をなくす
		}else if(e.getActionCommand() == "New Page"){
			int option = JOptionPane.showConfirmDialog(this, "新しいページを作ります。その前に保存しますか?","New Page", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(option == JOptionPane.YES_OPTION){
				drawPanel.saveImage();
			}
			option = JOptionPane.showConfirmDialog(this, "このページのデータは消えますがよろしいですか?","New Page", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(option == JOptionPane.YES_OPTION){
			drawPanel.setBGColor(Color.WHITE);
			drawPanel.clear();
			drawPanel.noopen();
			}
		}else if(e.getActionCommand() == "CLEAR") {
			int option = JOptionPane.showConfirmDialog(this, "clearすると元に戻りません。","Clear", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (option == JOptionPane.YES_OPTION){
				drawPanel.clear();
			}
		}else if(e.getActionCommand() == "SAVE") {
			drawPanel.saveImage();
		}
	}

}


// メインのクラス
public class Drawing {
	public static void main(String[] args) {
		MyJFrame frame = new MyJFrame();
		JTabbedPane tabbedpane = new JTabbedPane();

		DrawPanel drawPanel = new DrawPanel(frame);
		frame.setDrawPanel(drawPanel);

		OperationPanel operationPanel = new OperationPanel(drawPanel);
		SecondOperationPanel secondOperationPanel = new SecondOperationPanel(drawPanel);
		ThirdOperationPanel thirdOperationPanel = new ThirdOperationPanel(drawPanel);

		tabbedpane.addTab("1",operationPanel);
		tabbedpane.addTab("2",secondOperationPanel);
		tabbedpane.addTab("3",thirdOperationPanel);

		frame.getContentPane().add(drawPanel,BorderLayout.CENTER);
		frame.getContentPane().add(tabbedpane,BorderLayout.EAST);
		frame.setVisible(true);	
	}

}

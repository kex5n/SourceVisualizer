package view;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.Collections;

import model.domain.Class;
import model.domain.Package;
import model.domain.Property;
import model.domain.Method;
import model.domain.Attribute;
import model.domain.InternalDependency;
import model.domain.ExternalDependency;
import model.service.DependencyResolver;
import model.service.Player;
import model.service.StringSort;
import model.service.log.Log;
import model.service.log.NormalLog;
import model.service.log.AddLog;
import model.service.log.MoveLog;

import view.components.Point;
import view.components.Box;
import view.components.ClassBox;
import view.components.PropertyBox;
import view.components.MethodBox;
import view.components.DependencyVector;
import view.components.MoveHistoryVector;
import view.components.ClassViewComposer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.badlogic.gdx.utils.Null;

class ClassStartPointGenerator {
	private int counter;
	private int SPAN = 100;
	private int HEIGHT = 100;
	private int CLASS_BOX_WIDTH = 600;
	public ClassStartPointGenerator() {
		this.counter = 0;
	}
	public Point getCurrentStartPoint() {
		int x = SPAN + (CLASS_BOX_WIDTH + SPAN) * counter;
		this.counter++;
		return new Point(x, HEIGHT);
	}
	public void reset() {
		this.counter = 0;
	}
}

public class Drawer {
//	static final int LEFT_CLASS_CENTER_WIDTH = 150;
//	static final int LEFT_CLASS_CENTER_HEIGHT = 100;
//	static final int RIGHT_CLASS_CENTER_WIDTH = 950;
//	static final int RIGHT_CLASS_CENTER_HEIGHT = 100;
	private ClassStartPointGenerator startPointGenerator;
	private Package p;
	private Package pDefault;
//	private ArrayList<Class> classArray;
	private HashMap<String, Method> aloneMethods;
//	private Class leftClass;
//	private Class rightClass;
//	private Class leftDefaultClass;
//	private Class rightDefaultClass;

//	private AttributeTransferer attributeTransferer;
	private Player player;
	
//	private Point leftClassBoxStartPoint;
//	private Point rightClassBoxStartPoint;
//	private ClassBox leftClassBox;
//	private ClassBox rightClassBox;
//	private HashMap<String, MethodBox> leftMethodBoxMap;
//	private HashMap<String, MethodBox> rightMethodBoxMap;
//	private HashMap<String, PropertyBox> leftPropertyBoxMap;
//	private HashMap<String, PropertyBox> rightPropertyBoxMap;
//	private ArrayList<DependencyVector> leftInternalDependencyVectorArray;
//	private ArrayList<DependencyVector> rightInternalDependencyVectorArray;
//	private ArrayList<DependencyVector> leftExternalDependencyVectorArray;
//	private ArrayList<DependencyVector> rightExternalDependencyVectorArray;
	private ArrayList<ClassViewComposer> classViewComposerArray;
	private ArrayList<MoveHistoryVector> moveHistoryVectorArray;

	private TextButton forwardButton;
	private TextButton rollbackButton;

	private Stage stage;
	private DragAndDrop dragAndDrop;

	public Drawer(Package p, Stage stage) {
		this.startPointGenerator = new ClassStartPointGenerator();
		this.p = p;
		this.pDefault = p.clone();
		this.stage = stage;
//		this.classArray = new ArrayList<Class>();
		this.classViewComposerArray = new ArrayList<ClassViewComposer>();
		this.aloneMethods = new HashMap<String, Method>();
		this.player = new Player();
		this.moveHistoryVectorArray = new ArrayList<MoveHistoryVector>();
//		this.attributeTransferer = new AttributeTransferer(p.getClasses().get(0), p.getClasses().get(1));
		initButton();
		stage.addActor(forwardButton);
		stage.addActor(rollbackButton);
		load();
	}

	public void add(String elementType, String name) {
		if (elementType.equals("class")) {
			addClass(name);
		}
		if (elementType.equals("method")) {
			addMethods(name);
		}
	}

	public void addClass(String name) {
		Class c = new Class(name);
		this.p.setClass(c);
		this.pDefault.setClass(c);
	}

	public void addMethods(String name) {
		Method m = new Method(name);
		this.aloneMethods.put(name, m);
	}

	private Log forward() {
		return player.foward();
	}
	private ArrayList<Log> rollback() {
		return player.rollback();
	}

	public void move(
		MoveLog moveLog
	) {
		processMove(moveLog);
		player.recordMoveLog(moveLog);
		load();
	}

	public void processMove(MoveLog moveLog) {
		String srcAttributeName = moveLog.getName();
		Class srcClass = p.getClass(moveLog.getSrcClassName());
		Class dstClass = p.getClass(moveLog.getDstClassName());

		Attribute a = srcClass.getAttribute(srcAttributeName);
		ArrayList<InternalDependency> srcInternalDependencyArray;

		// 1. 内部依存関係のうち、移動するAttributeがsrcであるとき、dstも合わせて移す。
		HashSet<Attribute> moveAttribute = new HashSet<Attribute>();  // 移動するAttribute
		HashSet<InternalDependency> moveInternalDependency = new HashSet<InternalDependency>();  // 移動する内部依存関係
		HashSet<String> visitedAttributeName = new HashSet<String>();
		Queue<Attribute> candidates = new ArrayDeque<Attribute>();  // 依存関係を調べるAttributeの候補
		candidates.add(a);
		while (candidates.size() > 0) {  // 候補がなくなり、探索が完了するまで繰り返す
			Attribute currentAttribute = candidates.poll();
			moveAttribute.add(currentAttribute);
			visitedAttributeName.add(a.getName());
			srcInternalDependencyArray = srcClass.getSrcInternalDependencies(currentAttribute);
			moveInternalDependency.addAll(srcInternalDependencyArray);
			for (InternalDependency d: srcInternalDependencyArray) {
				String dstName = d.getDstName();
				if (!visitedAttributeName.contains(dstName)) {
					candidates.add(srcClass.getAttribute(dstName));
				}
			}
			moveAttribute.add(currentAttribute);
		}
		// Attributeを移す
		for (Attribute moveA: moveAttribute) {
			srcClass.removeAttribute(moveA.getName());
			dstClass.setAttribute(moveA);
		}
		// 内部依存関係を移す
		for (InternalDependency d: moveInternalDependency) {
			srcClass.removeInternalDependency(d);
			try {
				dstClass.setInternalDependencies(d.getSrcName(), d.getDstName());
			} catch(Exception e) {
				System.exit(1);
			}
		}

		// 2. 元のクラスのうち、外部依存関係だったものが移動により内部依存関係になった場合、外部依存関係を削除し、内部依存関係に追加する。
		ArrayList<ExternalDependency> srcCurrentExternalDependencies = (ArrayList<ExternalDependency>) srcClass.getExternalDependencies().clone();
		for (ExternalDependency externalDependency: srcCurrentExternalDependencies) {
			if (srcClass.has(externalDependency.getSrcName()) & srcClass.has(externalDependency.getDstName())) {
				srcClass.removeExternalDependency(externalDependency);
				try {
					srcClass.setInternalDependencies(externalDependency.getSrcName(), externalDependency.getDstName());
				} catch(Exception e) {
					System.exit(1);
				}
			}
			if (dstClass.has(externalDependency.getSrcName()) & dstClass.has(externalDependency.getDstName())) {
				srcClass.removeExternalDependency(externalDependency);
				try {
					dstClass.setInternalDependencies(externalDependency.getSrcName(), externalDependency.getDstName());
				} catch(Exception e) {
					System.exit(1);
				}
			}
		}

		// 3. 移動先のクラスのうち、外部依存関係だったものが移動により内部依存関係になった場合、外部依存関係を削除し、内部依存関係に追加する。
		ArrayList<ExternalDependency> dstCurrentExternalDependencies = (ArrayList<ExternalDependency>) dstClass.getExternalDependencies().clone();
		for (ExternalDependency externalDependency: dstCurrentExternalDependencies) {
			if (dstClass.has(externalDependency.getSrcName()) & dstClass.has(externalDependency.getDstName())) {
				dstClass.removeExternalDependency(externalDependency);
				try {
					dstClass.setInternalDependencies(externalDependency.getSrcName(), externalDependency.getDstName());
				} catch(Exception e) {
					System.exit(1);
				}
			}
			if (srcClass.has(externalDependency.getSrcName()) & srcClass.has(externalDependency.getDstName())) {
				dstClass.removeExternalDependency(externalDependency);
				try {
					srcClass.setInternalDependencies(externalDependency.getSrcName(), externalDependency.getDstName());
				} catch(Exception e) {
					System.exit(1);
				}
			}
		}

		// 4. 移ったAttributeのうち、元のクラスに残ったAttributeに依存されている場合、外部依存関係とする。
		HashSet<ExternalDependency> externalDependencies = new HashSet<ExternalDependency>();
		ArrayList<InternalDependency> srcCurrentInternalDependencies = (ArrayList<InternalDependency>) srcClass.getInternalDependencies().clone();
		for (InternalDependency internalDependency: srcCurrentInternalDependencies) {
			if (internalDependency.getDstName().equals(a.getName())) {
				srcClass.removeInternalDependency(internalDependency);
				try {
					srcClass.setExternalDependencies(internalDependency.getSrcName(), dstClass, internalDependency.getDstName());
					externalDependencies.add(new ExternalDependency(internalDependency.getSrcName(), dstClass.getName(), internalDependency.getDstName()));
				} catch(Exception e) {
					System.exit(1);
				}
			}
		}
	}

	public void process(Log logElement) {
		if (logElement.getType().equals("normal")) {
			NormalLog normalLogElement = (NormalLog) logElement;
			if (normalLogElement.getActionType().equals("move")) {
				MoveLog moveLogElement = (MoveLog) normalLogElement;
				processMove(moveLogElement);
			} else {
				AddLog addLogElement = (AddLog) normalLogElement;
				String elementType = addLogElement.getElementType();
				String name = addLogElement.getName();
				add(elementType, name);
			}
		}
	}

	private static JSONObject readJson(String filepath) {
		Object ob = new Object();
		try {
			ob = new JSONParser().parse(new FileReader(filepath));
		} catch (FileNotFoundException e) {
			System.out.println("File isn't found.");
		} catch (ParseException e) {
			System.out.println("Error parse.");
		} catch (IOException e) {
			System.out.println("IOException occurred.");
		}
		JSONObject jo = (JSONObject) ob;
		return jo;
	}

	public JSONArray loadLog() {
		JSONObject logJsonObject = readJson("/home/kentaroishii/eclipse-workspace/sample/core/data/log2.json");
		return (JSONArray) logJsonObject.get("log");
	}

//	public void processAlt(JSONObject record) {
//		JSONArray options = (JSONArray) record.get("contents");
//		JSONObject selectedRecord = (JSONObject) options.get(0);
//		process(selectedRecord);
//	}
//
//	public void processPar(JSONObject record) {
//		JSONArray options = (JSONArray) record.get("contents");
//		for(Object recordObj: options) {
//			JSONObject tmpRecord = (JSONObject) recordObj;
//			process(tmpRecord);
//		}
//	}

	public void load() {
		stage.clear();
		startPointGenerator.reset();
		classViewComposerArray.clear();
		stage.addActor(forwardButton);
		stage.addActor(rollbackButton);
		dragAndDrop = new DragAndDrop();
		ArrayList<Class> classArray = p.getClasses();
		ArrayList<Class> defaultClassArray = pDefault.getClasses();

		// setup for each class
		for (int i=0; i<classArray.size();i++) {
			Class currentClass = classArray.get(i);
			Class currentClassDefault = defaultClassArray.get(i);
			Point startPoint = startPointGenerator.getCurrentStartPoint();

			// setup class view
			ClassBox classBox = new ClassBox(startPoint, currentClass.getName());
			stage.addActor(classBox);

			// setup method view
			HashMap<String, MethodBox> methodBoxMap = createMethodBoxMap(classBox, currentClass, currentClassDefault);
			for (final MethodBox mb: methodBoxMap.values()) {
				stage.addActor(mb);
				if (!mb.getIsRemoved()) {
					setDragAndDropFunction(mb, classBox);
				}
			}

			// setup property view
			HashMap<String, PropertyBox> propertyBoxMap = createPropertyBoxMap(classBox, currentClass, currentClassDefault);
			for (final PropertyBox pb: propertyBoxMap.values()) {
				stage.addActor(pb);
			}

			// setup internal dependencies
			ArrayList<DependencyVector> internalDependencyVectorArray = createInternalDependencyVectorArray(
					classBox, propertyBoxMap, methodBoxMap, currentClass
			);

			ClassViewComposer classViewComposer = new ClassViewComposer(classBox, methodBoxMap, propertyBoxMap, internalDependencyVectorArray);
			classViewComposerArray.add(classViewComposer);
		}
//		leftClass = p.getClasses().get(0);
//		rightClass = p.getClasses().get(1);
//		if (leftDefaultClass == null) {
//			leftDefaultClass = leftClass.clone();
//		}
//		if (rightDefaultClass == null) {
//			rightDefaultClass = rightClass.clone();
//		}

//		leftClassBoxStartPoint = new Point(LEFT_CLASS_CENTER_WIDTH, LEFT_CLASS_CENTER_HEIGHT);
//		rightClassBoxStartPoint = new Point(RIGHT_CLASS_CENTER_WIDTH, RIGHT_CLASS_CENTER_HEIGHT);
//		leftClassBox = new ClassBox(leftClassBoxStartPoint, leftClass.getName());
//		rightClassBox = new ClassBox(rightClassBoxStartPoint, rightClass.getName());

//		leftMethodBoxMap = createMethodBoxMap(leftClassBox, leftClass, leftDefaultClass);
//		rightMethodBoxMap = createMethodBoxMap(rightClassBox, rightClass, rightDefaultClass);
	
//		leftPropertyBoxMap = createPropertyBoxMap(leftClassBox, leftClass, leftDefaultClass);
//		rightPropertyBoxMap = createPropertyBoxMap(rightClassBox, rightClass, rightDefaultClass);

//		leftInternalDependencyVectorArray = createInternalDependencyVectorArray(leftClassBox, leftPropertyBoxMap, leftMethodBoxMap, leftClass);
//		rightInternalDependencyVectorArray = createInternalDependencyVectorArray(rightClassBox, rightPropertyBoxMap, rightMethodBoxMap, rightClass);
//		leftExternalDependencyVectorArray = createExternalDependencyVectorArray(
//				leftPropertyBoxMap, leftMethodBoxMap, rightPropertyBoxMap, rightMethodBoxMap, leftClass.getExternalDependencies()
//		);
//		rightExternalDependencyVectorArray = createExternalDependencyVectorArray(
//				leftPropertyBoxMap, leftMethodBoxMap, rightPropertyBoxMap, rightMethodBoxMap, rightClass.getExternalDependencies()
//		);

		// set listener
//		stage.addActor(leftClassBox);
//		stage.addActor(rightClassBox);

//		for (final MethodBox mb: leftMethodBoxMap.values()) {
//			stage.addActor(mb);
//			if (!mb.getIsRemoved()) {
//				setDragAndDropFunction(mb, rightClassBox);
//			}
//		}
//		for (final MethodBox mb: rightMethodBoxMap.values()) {
//			stage.addActor(mb);
//			if (!mb.getIsRemoved()) {
//				setDragAndDropFunction(mb, leftClassBox);
//			}
//		}
//		for (final PropertyBox pb: leftPropertyBoxMap.values()) {
//			stage.addActor(pb);
//		}
//		for (final PropertyBox pb: rightPropertyBoxMap.values()) {
//			stage.addActor(pb);
//		}
//		moveHistoryVectorArray = new ArrayList<MoveHistoryVector>();
//		ArrayList<Log> currentValidLogArray = player.getCurrentValidLogArray();
//		for (Log currentLog: currentValidLogArray) {
//			if (currentLog instanceof MoveLog) {
//				MoveLog currentMoveLog = (MoveLog) currentLog;
//				String mainMoveSrcClassName = currentMoveLog.getSrcClassName();
//				String mainMoveAttributeName = currentMoveLog.getName();
//				if (mainMoveSrcClassName.equals(leftClass.getName())) {
//					MethodBox movedBox = leftMethodBoxMap.get(mainMoveAttributeName);
//					MethodBox toBox = rightMethodBoxMap.get(mainMoveAttributeName);
//					MoveHistoryVector moveHistoryVector = new MoveHistoryVector(
//							movedBox.getRightConnectionPoints(1).get(0), toBox.getLeftConnectionPoints(1).get(0)
//					);
//					moveHistoryVectorArray.add(moveHistoryVector);
//					for (MoveLog autoMovedHistory: currentMoveLog.getAutoMoveArray()) {
//						String autoMovedAttributeName = autoMovedHistory.getName();
//						Boolean isMethod = leftMethodBoxMap.containsKey(autoMovedAttributeName);
//						Box autoMovedBox;
//						Box autoToBox;
//						if (isMethod) {
//							autoMovedBox = leftMethodBoxMap.get(autoMovedAttributeName);
//							autoToBox = rightMethodBoxMap.get(autoMovedAttributeName);
//						} else {
//							autoMovedBox = leftPropertyBoxMap.get(autoMovedAttributeName);
//							autoToBox = rightPropertyBoxMap.get(autoMovedAttributeName);
//						}
//						MoveHistoryVector autoMoveHistoryVector = new MoveHistoryVector(
//								autoMovedBox.getRightConnectionPoints(1).get(0), autoToBox.getLeftConnectionPoints(1).get(0)
//						);
//						moveHistoryVectorArray.add(autoMoveHistoryVector);
//					}
//				} else {
//					MethodBox movedBox = rightMethodBoxMap.get(mainMoveAttributeName);
//					MethodBox toBox = leftMethodBoxMap.get(mainMoveAttributeName);
//					MoveHistoryVector moveHistoryVector = new MoveHistoryVector(
//							movedBox.getLeftConnectionPoints(1).get(0), toBox.getRightConnectionPoints(1).get(0)
//					);
//					moveHistoryVectorArray.add(moveHistoryVector);
//					for (MoveLog autoMovedHistory: currentMoveLog.getAutoMoveArray()) {
//						String autoMovedAttributeName = autoMovedHistory.getName();
//						Boolean isMethod = rightMethodBoxMap.containsKey(autoMovedAttributeName);
//						Box autoMovedBox;
//						Box autoToBox;
//						if (isMethod) {
//							autoMovedBox = rightMethodBoxMap.get(autoMovedAttributeName);
//							autoToBox = leftMethodBoxMap.get(autoMovedAttributeName);
//						} else {
//							autoMovedBox = rightPropertyBoxMap.get(autoMovedAttributeName);
//							autoToBox = leftPropertyBoxMap.get(autoMovedAttributeName);
//						}
//						MoveHistoryVector autoMoveHistoryVector = new MoveHistoryVector(
//								autoMovedBox.getLeftConnectionPoints(1).get(0), autoToBox.getRightConnectionPoints(1).get(0)
//						);
//						moveHistoryVectorArray.add(autoMoveHistoryVector);
//					}
//				}
//			}
//		}
	}

	public String getLogText() {
		return player.getLogText();
	}

	private void initButton() {
		Skin skin = new Skin();

		// Generate a 1x1 white texture and store it in the skin named "white".
		Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		skin.add("white", new Texture(pixmap));

		// Store the default libGDX font under the name "default".
		skin.add("default", new BitmapFont());

		// Configure a TextButtonStyle and name it "default". Skin resources are stored by type, so this doesn't overwrite the font.
		TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
		textButtonStyle.up = skin.newDrawable("white", Color.DARK_GRAY);
		textButtonStyle.down = skin.newDrawable("white", Color.DARK_GRAY);
//		textButtonStyle.checked = skin.newDrawable("white", Color.BLUE);
		textButtonStyle.over = skin.newDrawable("white", Color.LIGHT_GRAY);
		textButtonStyle.font = skin.getFont("default");
		textButtonStyle.font.getData().setScale(10.0f);
		skin.add("default", textButtonStyle);
		forwardButton = new TextButton("forward", skin);
		forwardButton.setSize(700, 150);
		forwardButton.setPosition(10, 10);
		forwardButton.addListener(new ClickListener() {
			@Override
            public void clicked(InputEvent event, float x, float y) {
				// Log logElement = forward();
				// process(logElement);
				addClass("test");
				load();
            }
		});

		rollbackButton = new TextButton("rollback", skin);
		rollbackButton.setSize(700, 150);
		rollbackButton.setPosition(750, 10);
		rollbackButton.addListener(new ClickListener() {
			@Override
            public void clicked(InputEvent event, float x, float y) {
				p = pDefault.clone();
				ArrayList<Log> logArray = rollback();
				for (Log logElement: logArray) {
					process(logElement);
				}
				load();
            }
		});
	}

	private void setDragAndDropFunction(Box b, ClassBox targetClassBox) {
		Source s = new Source(b) {
			@Null
			public Payload dragStart (InputEvent event, float x, float y, int pointer) {
				Payload payload = new Payload();
				payload.setDragActor(getActor());
				return payload;
			}
			public void drag(InputEvent event, float x, float y, int pointer) {
				Actor b = getActor();
				b.moveBy(x, y);
		    }
			public void dragStop(InputEvent event,
                    float x,
                    float y,
                    int pointer,
                    @Null
                    DragAndDrop.Payload payload,
                    DragAndDrop.Target target) {
				Stage s = event.getStage();

				Box b = (Box) getActor();
				b.setX(b.getDefaultX());
				b.setY(b.getDefaultY());
			}
		};
		Target t = new Target(targetClassBox) {
			public void drop (Source source, Payload payload, float x, float y, int pointer) {
				String srcName = source.getActor().getName();

				// define srcClass and dstClass
				Class srcClass;
				Class dstClass;
//				if (attributeTransferer.leftClassHas(srcName)) {
//					srcClass = attributeTransferer.getLeftClass();
//					dstClass = attributeTransferer.getRightClass();
//				} else {
//					srcClass = attributeTransferer.getRightClass();
//					dstClass = attributeTransferer.getLeftClass();
//				}

				// move related attributes
//				MoveLog moveLog = DependencyResolver.resolve(srcClass, dstClass, srcName);
//				move(moveLog);
				load();
			}

			@Override
			public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
				return true;
			}
		};
		dragAndDrop.addSource(s);
		dragAndDrop.addTarget(t);
	}

	public void draw(ShapeRenderer shapeRenderer, Batch batch) {
		for (ClassViewComposer classViewComposer: classViewComposerArray) {
			for (DependencyVector v: classViewComposer.getInternalDependencyVectorArray()) {
				drawDependencyVector(shapeRenderer, v);
			}
//			for (DependencyVector v: classViewComposer.getExternalDependencyVectorArray()) {
//				drawDependencyVector(shapeRenderer, v);
//			}
		}

//		drawMoveHistorVector(shapeRenderer);

		// draw name
		batch.begin();
		for (ClassViewComposer classViewComposer: classViewComposerArray) {
			classViewComposer.getClassBox().drawName(batch);
			for (PropertyBox p: classViewComposer.getPropertyBoxMap().values()) {
				p.drawName(batch);
			}
			for (MethodBox m: classViewComposer.getMethodBoxMap().values()) {
				m.drawName(batch);
			}
		}
		batch.end();
	}

	private void drawExternalDependencyVector(ShapeRenderer shapeRenderer, DependencyVector dependencyVector) {
		int lineWidth = 8;
		Point startPoint = dependencyVector.getStartPoint();
		Point endPoint = dependencyVector.getEndPoint();

		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(Color.BLUE);
		
		// draw triangle
		int height = 24;
		int width = 9;
		double diagonal = Math.sqrt(Math.pow(startPoint.x - endPoint.x, 2) + Math.pow(startPoint.y - endPoint.y, 2));
		double cos = - (endPoint.x - startPoint.x) / diagonal;
		double sin = - (endPoint.y - startPoint.y) / diagonal;
		float p1X = (float) (cos * height - sin * width);
		float p1Y = (float) (sin * height + cos * width);
		float p2X = (float) (cos * height + sin * width);
		float p2Y = (float) (sin * height - cos * width);
		shapeRenderer.triangle(
				endPoint.x,
				endPoint.y,
				endPoint.x + p1X,
				endPoint.y + p1Y,
				endPoint.x + p2X,
				endPoint.y + p2Y
		);

		// draw main line
		shapeRenderer.rectLine(
				startPoint.x,
				startPoint.y,
				endPoint.x + (p1X + p2X) / 2,
				endPoint.y + (p1Y + p2Y) / 2,
				lineWidth
		);
		shapeRenderer.end();
	}
	
	private void drawDependencyVector(ShapeRenderer shapeRenderer, DependencyVector dependencyVector) {
		int lineWidth = 4;
		Point startPoint = dependencyVector.getStartPoint();
		Point endPoint = dependencyVector.getEndPoint();
		int distance = dependencyVector.getDistance();

		shapeRenderer.begin(ShapeType.Filled);
		if (dependencyVector.getIsMethodDst()) {
			shapeRenderer.setColor(Color.BLUE);
		} else {
			shapeRenderer.setColor(Color.FOREST);
		}

		// draw main line
		shapeRenderer.rectLine(
				startPoint.x,
				startPoint.y,
				startPoint.x + distance,
				startPoint.y,
				lineWidth
		);
		shapeRenderer.rectLine(
				startPoint.x + distance,
				startPoint.y,
				startPoint.x + distance,
				endPoint.y,
				lineWidth
		);
		shapeRenderer.rectLine(
				endPoint.x,
				endPoint.y,
				startPoint.x + distance,
				endPoint.y,
				lineWidth
		);

		// draw triangle
		shapeRenderer.triangle(
				endPoint.x,
				endPoint.y,
				endPoint.x + Math.signum(distance) * 24,
				endPoint.y + 9,
				endPoint.x + Math.signum(distance) * 24,
				endPoint.y - 9
		);
		shapeRenderer.end();
	}

	private void drawMoveHistorVector(ShapeRenderer shapeRenderer) {
		int lineWidth = 16;
		// draw triangle
		int height = 48;
		int width = 18;
		for (MoveHistoryVector moveHistoryVector: this.moveHistoryVectorArray) {
			Point startPoint = moveHistoryVector.getStartPoint();
			Point endPoint = moveHistoryVector.getEndPoint();
	
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(Color.GRAY);
			
			
			double diagonal = Math.sqrt(Math.pow(startPoint.x - endPoint.x, 2) + Math.pow(startPoint.y - endPoint.y, 2));
			double cos = - (endPoint.x - startPoint.x) / diagonal;
			double sin = - (endPoint.y - startPoint.y) / diagonal;
			float p1X = (float) (cos * height - sin * width);
			float p1Y = (float) (sin * height + cos * width);
			float p2X = (float) (cos * height + sin * width);
			float p2Y = (float) (sin * height - cos * width);
			shapeRenderer.triangle(
					endPoint.x,
					endPoint.y,
					endPoint.x + p1X,
					endPoint.y + p1Y,
					endPoint.x + p2X,
					endPoint.y + p2Y
			);
	
			// draw main line
			shapeRenderer.rectLine(
					startPoint.x,
					startPoint.y,
					endPoint.x + (p1X + p2X) / 2,
					endPoint.y + (p1Y + p2Y) / 2,
					lineWidth
			);
			shapeRenderer.end();
		}
	}
	private HashMap<String, MethodBox> createMethodBoxMap(Box baseBox, Class c, Class defaultClass) {
		HashSet<Method> wholeMethodSet = c.getMethods();
		ArrayList<String> methodNames = new ArrayList<String>();
		for (Method m: wholeMethodSet) {
			methodNames.add(m.getName());
		}
		for (Method m: defaultClass.getMethods()) {
			if (!methodNames.contains(m.getName())) {
				wholeMethodSet.add(m);
			}
		}
		int numMethods = wholeMethodSet.size();

		Box methodRegion = new Box(baseBox.getLeftBottomPoint(), baseBox.getWidth(), baseBox.getHeight() / 2);
		float methodBoxXValue = methodRegion.getLeftBottomPoint().x + (methodRegion.getWidth() - MethodBox.METHOD_BOX_WIDTH) / 2;
		float interval = methodRegion.getHeight() / (numMethods + 1);

		HashMap<String, MethodBox> methodBoxMap = new HashMap<String, MethodBox>();
		int count = 0;
		ArrayList<Method> tempMethodArray = new ArrayList<Method>();
		for (Method m: wholeMethodSet) {
			tempMethodArray.add(m);
		}
		Collections.sort(tempMethodArray);
		for (Method m: tempMethodArray) {
			String name = m.getName();
			Point startPoint = new Point(
					methodBoxXValue,
					methodRegion.getLeftBottomPoint().y + methodRegion.getHeight() - interval * (count + 1) - MethodBox.METHOD_BOX_HEIGHT / 2);
			MethodBox methodBox = new MethodBox(startPoint, m.getName(), !c.has(m.getName()));
			methodBoxMap.put(name, methodBox);
			count++;
		}
		return methodBoxMap;
	}

	private HashMap<String, PropertyBox> createPropertyBoxMap(Box baseBox, Class c, Class defaultClass) {
		HashSet<Property> wholePropertySet = c.getProperties();
		ArrayList<String> propertyNames = new ArrayList<String>();
		for (Property p: wholePropertySet) {
			propertyNames.add(p.getName());
		}
		for (Property p: defaultClass.getProperties()) {
			if (!propertyNames.contains(p.getName())) {
				wholePropertySet.add(p);
			}
		}
		int numProperties = wholePropertySet.size();

		Point baseBoxStartPoint = baseBox.getLeftBottomPoint();
		Point propertyRegionStartPoint = new Point(baseBoxStartPoint.x, baseBoxStartPoint.y + Math.round(baseBox.getHeight()) / 2);
		Box propertyRegion = new Box(propertyRegionStartPoint, Math.round(baseBox.getWidth()), Math.round(baseBox.getHeight()) / 2);
		float propertyBoxXValue = propertyRegion.getLeftBottomPoint().x + (propertyRegion.getWidth() - PropertyBox.PROPERTY_BOX_WIDTH) / 2;
		float interval = propertyRegion.getHeight() / (numProperties + 1);

		HashMap<String, PropertyBox> propertyBoxMap = new HashMap<String, PropertyBox>();
		int count = 0;
		ArrayList<Property> tempPropertyArray = new ArrayList<Property>();
		for (Property p: wholePropertySet) {
			tempPropertyArray.add(p);
		}
		Collections.sort(tempPropertyArray);
		for (Property p: tempPropertyArray) {
			String name = p.getName();
			Point startPoint = new Point(
					propertyBoxXValue,
					propertyRegion.getLeftBottomPoint().y + propertyRegion.getHeight() - interval * (count + 1) - PropertyBox.PROPERTY_BOX_HEIGHT / 2);
			final PropertyBox propertyBox = new PropertyBox(startPoint, p.getName(), !c.has(p.getName()));
			propertyBoxMap.put(name, propertyBox);
			count++;
		}
		return propertyBoxMap;
	}

	private ArrayList<DependencyVector> createInternalDependencyVectorArray(Box baseBox, HashMap<String, PropertyBox> propertyBoxMap, HashMap<String, MethodBox> methodBoxMap, Class c) {
		int vectorDistance = 30;
		int currentMethodVectorDistance = vectorDistance;
		int currentPropertyVectorDistance = -vectorDistance;

		ArrayList<DependencyVector> dependencyVectorArray = new ArrayList<DependencyVector>();
		ArrayList<InternalDependency> dependencyArray = c.getInternalDependencies();
		for (int i = 0; i < dependencyArray.size(); i++) {
			InternalDependency d = dependencyArray.get(i);
			Attribute src = c.getAttribute(d.getSrcName());
			Attribute dst = c.getAttribute(d.getDstName());
			boolean isMethodDst = dst instanceof Method;
			Box srcBox = methodBoxMap.get(src.getName());
			Box dstBox;
			DependencyVector v;
			if (isMethodDst) {
				dstBox = methodBoxMap.get(dst.getName());
				if (dstBox == null) {
					continue;
				}
				Point startPoint = srcBox.getRightConnectionPoints(1).get(0);
				Point endPoint = dstBox.getRightConnectionPoints(1).get(0);
				v = new DependencyVector(startPoint, endPoint, currentMethodVectorDistance, isMethodDst);
				dependencyVectorArray.add(v);
				currentMethodVectorDistance += vectorDistance;
			} else {
				dstBox = propertyBoxMap.get(dst.getName());
				Point startPoint = srcBox.getLeftConnectionPoints(1).get(0);
				Point endPoint = dstBox.getLeftConnectionPoints(1).get(0);
				v = new DependencyVector(startPoint, endPoint, currentPropertyVectorDistance, isMethodDst);
				dependencyVectorArray.add(v);
				currentPropertyVectorDistance -= vectorDistance;
			}
		}
		return dependencyVectorArray;
	}

//	private ArrayList<DependencyVector> createExternalDependencyVectorArray(
//			HashMap<String, PropertyBox> leftPropertyBoxMap,
//			HashMap<String, MethodBox> leftMethodBoxMap,
//			HashMap<String, PropertyBox> rightPropertyBoxMap,
//			HashMap<String, MethodBox> rightMethodBoxMap,
//			ArrayList<ExternalDependency> externalDependencies
//	) {
//		ArrayList<DependencyVector> externalDependencyVector = new ArrayList<DependencyVector>();
//		for (ExternalDependency d: externalDependencies) {
//			Box srcBox;
//			Box dstBox;
//			Point srcPoint;
//			Point dstPoint;
//			String srcName = d.getSrcName();
//			String dstName = d.getDstName();
//
//			if (leftClass.has(srcName)) {
//				if (leftClass.getAttribute(srcName) instanceof Method) {
//					srcBox = leftMethodBoxMap.get(srcName);
//				} else {
//					srcBox = leftPropertyBoxMap.get(srcName);
//				}
//				if (rightClass.getAttribute(dstName) instanceof Method) {
//					dstBox = rightMethodBoxMap.get(dstName);
//				} else {
//					dstBox = rightPropertyBoxMap.get(dstName);
//				}
//				srcPoint = srcBox.getRightConnectionPoints(1).get(0);
//				dstPoint = dstBox.getLeftConnectionPoints(1).get(0);
//			} else {
//				if (rightClass.getAttribute(srcName) instanceof Method) {
//					srcBox = rightMethodBoxMap.get(srcName);
//				} else {
//					srcBox = rightPropertyBoxMap.get(srcName);
//				}
//				if (leftClass.getAttribute(dstName) instanceof Method) {
//					dstBox = leftMethodBoxMap.get(dstName);
//				} else {
//					dstBox = leftPropertyBoxMap.get(dstName);
//				}
//				srcPoint = srcBox.getLeftConnectionPoints(1).get(0);
//				if (dstBox == null) {
//					System.out.println(dstName);
//					System.out.println(srcName);
//				}
//				dstPoint = dstBox.getRightConnectionPoints(1).get(0);
//			}
//			externalDependencyVector.add(new DependencyVector(srcPoint, dstPoint, 0, false));
//		}
//		return externalDependencyVector;
//	}
}

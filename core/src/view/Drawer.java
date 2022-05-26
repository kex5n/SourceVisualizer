package view;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;

import model.domain.Class;
import model.domain.Package;
import model.domain.Property;
import model.domain.Method;
import model.domain.Attribute;
import model.domain.InternalDependency;
import model.domain.ExternalDependency;
import model.service.AttributeTransferer;
import model.service.DependencyResolver;
import model.service.LogManager;

import view.components.Point;
import view.components.Box;
import view.components.ClassBox;
import view.components.PropertyBox;
import view.components.MethodBox;
import view.components.DependencyVector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.badlogic.gdx.utils.Null;

public class Drawer {
	static final int LEFT_CLASS_CENTER_WIDTH = 150;
	static final int LEFT_CLASS_CENTER_HEIGHT = 100;
	static final int RIGHT_CLASS_CENTER_WIDTH = 950;
	static final int RIGHT_CLASS_CENTER_HEIGHT = 100;

	private Package p;
	private ArrayList<Class> classArray;
	private HashMap<String, Method> aloneMethods;
	private Class leftClass;
	private Class rightClass;
	private Class leftDefaultClass;
	private Class rightDefaultClass;

	private AttributeTransferer attributeTransferer;
	private LogManager logManager;
	
	private Point leftClassBoxStartPoint;
	private Point rightClassBoxStartPoint;
	private ClassBox leftClassBox;
	private ClassBox rightClassBox;
	private HashMap<String, MethodBox> leftMethodBoxMap;
	private HashMap<String, MethodBox> rightMethodBoxMap;
	private HashMap<String, PropertyBox> leftPropertyBoxMap;
	private HashMap<String, PropertyBox> rightPropertyBoxMap;
	private ArrayList<DependencyVector> leftInternalDependencyVectorArray;
	private ArrayList<DependencyVector> rightInternalDependencyVectorArray;
	private ArrayList<DependencyVector> leftExternalDependencyVectorArray;
	private ArrayList<DependencyVector> rightExternalDependencyVectorArray;
	
	private Stage stage;
	private DragAndDrop dragAndDrop;

	public Drawer(Package p, Stage stage) {
		this.p = p;
		this.stage = stage;
		this.classArray = new ArrayList<Class>();
		this.aloneMethods = new HashMap<String, Method>();
		this.logManager = new LogManager();
		this.attributeTransferer = new AttributeTransferer(p.getClasses().get(0), p.getClasses().get(1));
		load();
		play();
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
	}

	public void addMethods(String name) {
		Method m = new Method(name);
		this.aloneMethods.put(name, m);
	}

	public void move(String elementType, String name, String srcClassName, String dstClassName) {
		// define srcClass and dstClass
		if (srcClassName.equals("")) {
			Method m = aloneMethods.get(name);
			Class dstClass = p.getClass(dstClassName);
			dstClass.setAttribute(m);
		} else {
			Class srcClass = p.getClass(srcClassName);
			Class dstClass = p.getClass(dstClassName);

			// move related attributes
			DependencyResolver.resolve(srcClass, dstClass, name, logManager);
		}
		load();
	}

	public void process(JSONObject record) {
		String actionType = (String) record.get("actionType");
		String elementType = (String) record.get("elementType");
		String name = (String) record.get("name");
		if (actionType.equals("add")) {
			add(elementType, name);
		} else {
			String srcClassName = (String) record.get("srcClass");
			String dstClassName = (String) record.get("dstClass");
			move(elementType, name, srcClassName, dstClassName);
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

	public void play() {
		JSONArray log = loadLog();
		for (int i=0; i<log.size(); i++) {
			JSONObject record = (JSONObject) log.get(i);
			String type = (String) record.get("type");
			if (type.equals("normal")) {
				process(record);
			} else if (type.equals("alt")) {
				processAlt(record);
			} else if (type.equals("par")) {
				processPar(record);
			}
		}
	}

	public void processAlt(JSONObject record) {
		JSONArray options = (JSONArray) record.get("contents");
		JSONObject selectedRecord = (JSONObject) options.get(0);
		process(selectedRecord);
	}

	public void processPar(JSONObject record) {
		JSONArray options = (JSONArray) record.get("contents");
		for(Object recordObj: options) {
			JSONObject tmpRecord = (JSONObject) recordObj;
			process(tmpRecord);
		}
	}

	public void load() {
		stage.clear();
		leftClass = p.getClasses().get(0);
		rightClass = p.getClasses().get(1);
		if (leftDefaultClass == null) {
			leftDefaultClass = leftClass.clone();
		}
		if (rightDefaultClass == null) {
			rightDefaultClass = rightClass.clone();
		}

		leftClassBoxStartPoint = new Point(LEFT_CLASS_CENTER_WIDTH, LEFT_CLASS_CENTER_HEIGHT);
		rightClassBoxStartPoint = new Point(RIGHT_CLASS_CENTER_WIDTH, RIGHT_CLASS_CENTER_HEIGHT);
		leftClassBox = new ClassBox(leftClassBoxStartPoint, leftClass.getName());
		rightClassBox = new ClassBox(rightClassBoxStartPoint, rightClass.getName());

		leftMethodBoxMap = createMethodBoxMap(leftClassBox, leftClass, leftDefaultClass);
		rightMethodBoxMap = createMethodBoxMap(rightClassBox, rightClass, rightDefaultClass);
	
		leftPropertyBoxMap = createPropertyBoxMap(leftClassBox, leftClass, leftDefaultClass);
		rightPropertyBoxMap = createPropertyBoxMap(rightClassBox, rightClass, rightDefaultClass);

		leftInternalDependencyVectorArray = createInternalDependencyVectorArray(leftClassBox, leftPropertyBoxMap, leftMethodBoxMap, leftClass);
		rightInternalDependencyVectorArray = createInternalDependencyVectorArray(rightClassBox, rightPropertyBoxMap, rightMethodBoxMap, rightClass);
		leftExternalDependencyVectorArray = createExternalDependencyVectorArray(
				leftPropertyBoxMap, leftMethodBoxMap, rightPropertyBoxMap, rightMethodBoxMap, leftClass.getExternalDependencies()
		);
		rightExternalDependencyVectorArray = createExternalDependencyVectorArray(
				leftPropertyBoxMap, leftMethodBoxMap, rightPropertyBoxMap, rightMethodBoxMap, rightClass.getExternalDependencies()
		);

		// set listener
		stage.addActor(leftClassBox);
		stage.addActor(rightClassBox);

		dragAndDrop = new DragAndDrop();

		for (final MethodBox mb: leftMethodBoxMap.values()) {
			stage.addActor(mb);
			if (!mb.getIsRemoved()) {
				setDragAndDropFunction(mb, rightClassBox);
			}
		}
		for (final MethodBox mb: rightMethodBoxMap.values()) {
			stage.addActor(mb);
			if (!mb.getIsRemoved()) {
				setDragAndDropFunction(mb, leftClassBox);
			}
		}
		for (final PropertyBox pb: leftPropertyBoxMap.values()) {
			stage.addActor(pb);
			if (!pb.getIsRemoved()) {
				setDragAndDropFunction(pb, rightClassBox);
			}
		}
		for (final PropertyBox pb: rightPropertyBoxMap.values()) {
			stage.addActor(pb);
			if (!pb.getIsRemoved()) {
				setDragAndDropFunction(pb, leftClassBox);
			}
		}
	}

	public String getLogText() {
		return logManager.getLogText();
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
				if (attributeTransferer.leftClassHas(srcName)) {
					srcClass = attributeTransferer.getLeftClass();
					dstClass = attributeTransferer.getRightClass();
				} else {
					srcClass = attributeTransferer.getRightClass();
					dstClass = attributeTransferer.getLeftClass();
				}
				// move related attributes
				DependencyResolver.resolve(srcClass, dstClass, srcName, logManager);
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
		for (DependencyVector v: leftInternalDependencyVectorArray) {
			drawDependencyVector(shapeRenderer, v);
		}
		for (DependencyVector v: rightInternalDependencyVectorArray) {
			drawDependencyVector(shapeRenderer, v);
		}
		for (DependencyVector v: leftExternalDependencyVectorArray) {
			drawExternalDependencyVector(shapeRenderer, v);
		}
		for (DependencyVector v: rightExternalDependencyVectorArray) {
			drawExternalDependencyVector(shapeRenderer, v);
		}

		// draw name
		batch.begin();
		leftClassBox.drawName(batch);
		rightClassBox.drawName(batch);
		for (PropertyBox p: leftPropertyBoxMap.values()) {
			p.drawName(batch);
		}
		for (PropertyBox p: rightPropertyBoxMap.values()) {
			p.drawName(batch);
		}
		for (MethodBox m: leftMethodBoxMap.values()) {
			m.drawName(batch);
		}
		for (MethodBox m: rightMethodBoxMap.values()) {
			m.drawName(batch);
		}
		batch.end();
	}

	private void drawExternalDependencyVector(ShapeRenderer shapeRenderer, DependencyVector dependencyVector) {
		int lineWidth = 40;
		Point startPoint = dependencyVector.getStartPoint();
		Point endPoint = dependencyVector.getEndPoint();

		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(Color.PURPLE);
		
		// draw triangle
		int height = 120;
		int width = 56;
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
	
	private HashMap<String, MethodBox> createMethodBoxMap(Box baseBox, Class c, Class defaultClass) {
		HashSet<Method> wholeMethodSet = c.getMethods();
		wholeMethodSet.addAll(defaultClass.getMethods());
		int numMethods = wholeMethodSet.size();

		Box methodRegion = new Box(baseBox.getLeftBottomPoint(), baseBox.getWidth(), baseBox.getHeight() / 2);
		float methodBoxXValue = methodRegion.getLeftBottomPoint().x + (methodRegion.getWidth() - MethodBox.METHOD_BOX_WIDTH) / 2;
		float interval = methodRegion.getHeight() / (numMethods + 1);

		HashMap<String, MethodBox> methodBoxMap = new HashMap<String, MethodBox>();
		int count = 0;
		for (Method m: wholeMethodSet) {
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
		wholePropertySet.addAll(defaultClass.getProperties());
		int numProperties = wholePropertySet.size();

		Point baseBoxStartPoint = baseBox.getLeftBottomPoint();
		Point propertyRegionStartPoint = new Point(baseBoxStartPoint.x, baseBoxStartPoint.y + Math.round(baseBox.getHeight()) / 2);
		Box propertyRegion = new Box(propertyRegionStartPoint, Math.round(baseBox.getWidth()), Math.round(baseBox.getHeight()) / 2);
		float propertyBoxXValue = propertyRegion.getLeftBottomPoint().x + (propertyRegion.getWidth() - PropertyBox.PROPERTY_BOX_WIDTH) / 2;
		float interval = propertyRegion.getHeight() / (numProperties + 1);

		HashMap<String, PropertyBox> propertyBoxMap = new HashMap<String, PropertyBox>();
		int count = 0;
		for (Property p: wholePropertySet) {
			String name = p.getName();
			Point startPoint = new Point(
					propertyBoxXValue,
					propertyRegion.getLeftBottomPoint().y + propertyRegion.getHeight() - interval * (count + 1) - PropertyBox.PROPERTY_BOX_HEIGHT / 2);
			final PropertyBox propertyBox = new PropertyBox(startPoint, p.getName(), !c.has(p.getName()));
			propertyBox.addListener(new DragListener() {
				public void drag(InputEvent event, float x, float y, int pointer) {
					propertyBox.moveBy(x - propertyBox.getWidth() / 2, y - propertyBox.getHeight() / 2);
				}
				});
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

	private ArrayList<DependencyVector> createExternalDependencyVectorArray(
			HashMap<String, PropertyBox> leftPropertyBoxMap,
			HashMap<String, MethodBox> leftMethodBoxMap,
			HashMap<String, PropertyBox> rightPropertyBoxMap,
			HashMap<String, MethodBox> rightMethodBoxMap,
			ArrayList<ExternalDependency> externalDependencies
	) {
		ArrayList<DependencyVector> externalDependencyVector = new ArrayList<DependencyVector>();
		for (ExternalDependency d: externalDependencies) {
			Box srcBox;
			Box dstBox;
			Point srcPoint;
			Point dstPoint;
			String srcName = d.getSrcName();
			String dstName = d.getDstName();

			if (leftClass.has(srcName)) {
				if (leftClass.getAttribute(srcName) instanceof Method) {
					srcBox = leftMethodBoxMap.get(srcName);
				} else {
					srcBox = leftPropertyBoxMap.get(srcName);
				}
				if (rightClass.getAttribute(dstName) instanceof Method) {
					dstBox = rightMethodBoxMap.get(dstName);
				} else {
					dstBox = rightPropertyBoxMap.get(dstName);
				}
				srcPoint = srcBox.getRightConnectionPoints(1).get(0);
				dstPoint = dstBox.getLeftConnectionPoints(1).get(0);
			} else {
				if (rightClass.getAttribute(srcName) instanceof Method) {
					srcBox = rightMethodBoxMap.get(srcName);
				} else {
					srcBox = rightPropertyBoxMap.get(srcName);
				}
				if (leftClass.getAttribute(dstName) instanceof Method) {
					dstBox = leftMethodBoxMap.get(dstName);
				} else {
					dstBox = leftPropertyBoxMap.get(dstName);
				}
				srcPoint = srcBox.getLeftConnectionPoints(1).get(0);
				dstPoint = dstBox.getRightConnectionPoints(1).get(0);
			}
			externalDependencyVector.add(new DependencyVector(srcPoint, dstPoint, 0, false));
		}
		return externalDependencyVector;
	}

	private boolean inDefaultClass(Attribute a) {
		if (leftClass.has(a.getName()) & leftDefaultClass.has(a.getName())) {
			return true;
		}
		if (rightClass.has(a.getName()) & rightDefaultClass.has(a.getName())) {
			return true;
		}
		return false;
	}
}

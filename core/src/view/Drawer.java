package view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;

import model.domain.Class;
import model.domain.Package;
import model.domain.Property;
import model.domain.Method;
import model.domain.Dependency;
import model.domain.Attribute;
import model.service.AttributeTransferer;
import model.service.DependencyResolver;

import view.components.Point;
import view.components.Box;
import view.components.ClassBox;
import view.components.PropertyBox;
import view.components.MethodBox;
import view.components.DependencyVector;

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
	private Class leftClass;
	private Class rightClass;

	private AttributeTransferer attributeTransferer;
	
	private Point leftClassBoxStartPoint;
	private Point rightClassBoxStartPoint;
	private ClassBox leftClassBox;
	private ClassBox rightClassBox;
	private HashMap<String, MethodBox> leftMethodBoxMap;
	private HashMap<String, MethodBox> rightMethodBoxMap;
	private HashMap<String, PropertyBox> leftPropertyBoxMap;
	private HashMap<String, PropertyBox> rightPropertyBoxMap;
	private ArrayList<DependencyVector> leftDependencyVectorArray;
	private ArrayList<DependencyVector> rightDependencyVectorArray;

	private Stage stage;
	private DragAndDrop dragAndDrop;

	public Drawer(Package p, Stage stage) {
		this.p = p;
		this.stage = stage;
		load();
		attributeTransferer = new AttributeTransferer(p.getClasses().get(0), p.getClasses().get(1));
	}

	public void load() {
		stage.clear();
		leftClass = p.getClasses().get(0);
		rightClass = p.getClasses().get(1);

		leftClassBoxStartPoint = new Point(LEFT_CLASS_CENTER_WIDTH, LEFT_CLASS_CENTER_HEIGHT);
		rightClassBoxStartPoint = new Point(RIGHT_CLASS_CENTER_WIDTH, RIGHT_CLASS_CENTER_HEIGHT);
		leftClassBox = new ClassBox(leftClassBoxStartPoint, leftClass.getName());
		rightClassBox = new ClassBox(rightClassBoxStartPoint, rightClass.getName());

		leftMethodBoxMap = createMethodBoxMap(leftClassBox, leftClass);
		rightMethodBoxMap = createMethodBoxMap(rightClassBox, rightClass);
	
		leftPropertyBoxMap = createPropertyBoxMap(leftClassBox, leftClass);
		rightPropertyBoxMap = createPropertyBoxMap(rightClassBox, rightClass);

		leftDependencyVectorArray = createDependencyVectorArray(leftClassBox, leftPropertyBoxMap, leftMethodBoxMap, leftClass);
		rightDependencyVectorArray = createDependencyVectorArray(rightClassBox, rightPropertyBoxMap, rightMethodBoxMap, rightClass);

		// set listener
		stage.addActor(leftClassBox);
		stage.addActor(rightClassBox);

		dragAndDrop = new DragAndDrop();

		for (final MethodBox mb: leftMethodBoxMap.values()) {
			stage.addActor(mb);
			setDragAndDropFunction(mb, rightClassBox);
		}
		for (final MethodBox mb: rightMethodBoxMap.values()) {
			stage.addActor(mb);
			setDragAndDropFunction(mb, leftClassBox);
		}
		for (final PropertyBox pb: leftPropertyBoxMap.values()) {
			stage.addActor(pb);
			setDragAndDropFunction(pb, rightClassBox);
		}
		for (final PropertyBox pb: rightPropertyBoxMap.values()) {
			stage.addActor(pb);
			setDragAndDropFunction(pb, leftClassBox);
		}
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
				Class srcClass;
				Class dstClass;
				if (attributeTransferer.leftClassHas(srcName)) {
					srcClass = attributeTransferer.getLeftClass();
					dstClass = attributeTransferer.getRightClass();
				} else {
					srcClass = attributeTransferer.getRightClass();
					dstClass = attributeTransferer.getLeftClass();
				}
				HashMap<String, Object> dependencyInfo = DependencyResolver.resolve(srcClass, srcName);
				HashSet<Attribute> relatedAttributes = (HashSet<Attribute>) dependencyInfo.get("attribute");
				for (Attribute a: relatedAttributes) {
					attributeTransferer.transferAttribute(a.getName());
				}
				HashSet<Dependency> relatedDependencies = (HashSet<Dependency>) dependencyInfo.get("dependencies");
				for (Dependency d: relatedDependencies) {
					srcClass.removeDependency(d);
					try {
						dstClass.setDependencies(d.getSrc().getName(), d.getDst().getName());
					} catch (Exception e) {
						System.exit(1);;
					}
				}
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
		for (DependencyVector v: leftDependencyVectorArray) {
			drawDependencyVector(shapeRenderer, v);
		}
		for (DependencyVector v: rightDependencyVectorArray) {
			drawDependencyVector(shapeRenderer, v);
		}
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
		shapeRenderer.end();
	}
	
	private HashMap<String, MethodBox> createMethodBoxMap(Box baseBox, Class c) {
		ArrayList<Method> methodArray = c.getMethods();
		int numMethods = methodArray.size();

		Box methodRegion = new Box(baseBox.getLeftBottomPoint(), baseBox.getWidth(), baseBox.getHeight() / 2);
		float methodBoxXValue = methodRegion.getLeftBottomPoint().x + (methodRegion.getWidth() - MethodBox.METHOD_BOX_WIDTH) / 2;
		float interval = methodRegion.getHeight() / (numMethods + 1);

		HashMap<String, MethodBox> methodBoxMap = new HashMap<String, MethodBox>();
		for (int i = 0; i < methodArray.size(); i++) {
			Method m = methodArray.get(i);
			String name = m.getName();
			Point startPoint = new Point(
					methodBoxXValue,
					methodRegion.getLeftBottomPoint().y + methodRegion.getHeight() - interval * (i + 1) - MethodBox.METHOD_BOX_HEIGHT / 2);
			MethodBox methodBox = new MethodBox(startPoint, m.getName());
			methodBoxMap.put(name, methodBox);
		}
		return methodBoxMap;
	}

	private HashMap<String, PropertyBox> createPropertyBoxMap(Box baseBox, Class c) {
		ArrayList<Property> propertyArray = c.getProperties();
		int numProperties = propertyArray.size();

		Point baseBoxStartPoint = baseBox.getLeftBottomPoint();
		Point propertyRegionStartPoint = new Point(baseBoxStartPoint.x, baseBoxStartPoint.y + Math.round(baseBox.getHeight()) / 2);
		Box propertyRegion = new Box(propertyRegionStartPoint, Math.round(baseBox.getWidth()), Math.round(baseBox.getHeight()) / 2);
		float propertyBoxXValue = propertyRegion.getLeftBottomPoint().x + (propertyRegion.getWidth() - PropertyBox.PROPERTY_BOX_WIDTH) / 2;
		float interval = propertyRegion.getHeight() / (numProperties + 1);

		HashMap<String, PropertyBox> propertyBoxMap = new HashMap<String, PropertyBox>();
		for (int i = 0; i < propertyArray.size(); i++) {
			Property p = propertyArray.get(i);
			String name = p.getName();
			Point startPoint = new Point(
					propertyBoxXValue,
					propertyRegion.getLeftBottomPoint().y + propertyRegion.getHeight() - interval * (i + 1) - PropertyBox.PROPERTY_BOX_HEIGHT / 2);
			final PropertyBox propertyBox = new PropertyBox(startPoint, p.getName());
			propertyBox.addListener(new DragListener() {
				public void drag(InputEvent event, float x, float y, int pointer) {
					propertyBox.moveBy(x - propertyBox.getWidth() / 2, y - propertyBox.getHeight() / 2);
				}
				});
			propertyBoxMap.put(name, propertyBox);
		}
		return propertyBoxMap;
	}

	private ArrayList<DependencyVector> createDependencyVectorArray(Box baseBox, HashMap<String, PropertyBox> propertyBoxMap, HashMap<String, MethodBox> methodBoxMap, Class c) {
		int vectorDistance = 10;
		int currentMethodVectorDistance = vectorDistance;
		int currentPropertyVectorDistance = -vectorDistance;

		ArrayList<DependencyVector> dependencyVectorArray = new ArrayList<DependencyVector>();
		ArrayList<Dependency> dependencyArray = c.getDependencies();
		for (int i = 0; i < dependencyArray.size(); i++) {
			Dependency d = dependencyArray.get(i);
			Attribute src = d.getSrc();
			Attribute dst = d.getDst();
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
}

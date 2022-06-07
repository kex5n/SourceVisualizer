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
import model.service.log.AddClassLog;
import model.service.log.AddDependencyLog;
import model.service.log.AddMethodLog;
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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
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
	private ClassStartPointGenerator startPointGenerator;
	private Package p;
	private Package pDefault;
	private Package pDefaultBuckup;
	private HashMap<String, Method> aloneMethods;
	private Player player;
	private ArrayList<ClassViewComposer> classViewComposerArray;
	private ArrayList<MoveHistoryVector> moveHistoryVectorArray;

	// add class
	private String addClassText;
	private TextField addClassTextField;
	private TextButton addClassButton;

	// add method
	private String addMethodTargetClassText;
	private String addMethodText;
	private TextField addMethodTargetClassTextField;
	private TextField addMethodTextField;
	private TextButton addMethodButton;

	// add dependency
	private String addDependencySrcClassText;
	private String addDependencySrcMethodText;
	private String addDependencyDstClassText;
	private String addDependencyDstAttributeText;
	private TextField addDependencySrcClassTextField;
	private TextField addDependencySrcMethodTextField;
	private TextField addDependencyDstClassTextField;
	private TextField addDependencyDstAttributeTextField;
	private TextButton addDependencyButton;
	
	// add forward and rollback button
	private TextButton forwardButton;
	private TextButton rollbackButton;

	private Stage stage;
	private DragAndDrop dragAndDrop;

	public Drawer(Package p, Stage stage) {
		this.startPointGenerator = new ClassStartPointGenerator();
		this.p = p;
		this.pDefault = p.clone();
		this.pDefaultBuckup = p.clone();
		this.stage = stage;
		this.classViewComposerArray = new ArrayList<ClassViewComposer>();
		this.aloneMethods = new HashMap<String, Method>();
		this.player = new Player();
		this.moveHistoryVectorArray = new ArrayList<MoveHistoryVector>();
		initButton();
		this.addClassText = "";
		load();
	}

	private Log forward() {
		return player.foward();
	}
	private ArrayList<Log> rollback() {
		return player.rollback();
	}

	public void move(MoveLog moveLog) {
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
		for (Attribute moveA: moveAttribute) {
			for (InternalDependency internalDependency: srcCurrentInternalDependencies) {
				if (internalDependency.getDstName().equals(moveA.getName())) {
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
	}

	public void add(AddLog addLog) {
		processAdd(addLog);
		player.recordAddLog(addLog);
		load();
	}

	public void processAdd(AddLog addLog) {
		if (addLog.getElementType().equals("class")) {
			addClass(addLog.getName());
		} else if (addLog.getElementType().equals("method")) {
			AddMethodLog addMethodLog = (AddMethodLog) addLog;
			addMethods(addMethodLog.getDstClassName(), addMethodLog.getName());
		} else {
			AddDependencyLog addDependencyLog = (AddDependencyLog) addLog;
			addDependencies(
				addDependencyLog.getSrcClassName(),
				addDependencyLog.getName(),
				addDependencyLog.getDstClassName(),
				addDependencyLog.getDstAttributeName()
			);
		}
	}

	public void addClass(String name) {
		Class c = new Class(name);
		this.p.setClass(c);
		this.pDefault.setClass(c);
	}

	public void addMethods(String targetClassName, String methodName) {
		Method m = new Method(methodName);
		Class c = p.getClass(targetClassName);
		c.setAttribute(m);
		Class cDefault = pDefault.getClass(targetClassName);
		cDefault.setAttribute(m);
	}

	public void addDependencies(
		String srcClassName,
		String srcMethodName,
		String dstClassName,
		String dstAttributeName
	) {
		if (srcClassName.equals(dstClassName)) {
			Class srcClass = p.getClass(srcClassName);
			try {
				srcClass.setInternalDependencies(srcMethodName, dstAttributeName);
			} catch (Exception e) {
				System.exit(1);;
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
				processAdd(addLogElement);
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

		// add class
		stage.addActor(addClassButton);
		stage.addActor(addClassTextField);

		// add method
		stage.addActor(addMethodButton);
		stage.addActor(addMethodTargetClassTextField);
		stage.addActor(addMethodTextField);
	
		// add dependency
		stage.addActor(addDependencySrcClassTextField);
		stage.addActor(addDependencySrcMethodTextField);
		stage.addActor(addDependencyDstClassTextField);
		stage.addActor(addDependencyDstAttributeTextField);
		stage.addActor(addDependencyButton);

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

			ClassViewComposer classViewComposer = new ClassViewComposer(i, classBox, methodBoxMap, propertyBoxMap, internalDependencyVectorArray);
			classViewComposerArray.add(classViewComposer);
		}

		// set drag-and-drop function
		for (ClassViewComposer tmpClassViewComposer: classViewComposerArray) {
			for (ClassViewComposer anotherClassViewComposer: classViewComposerArray) {
				if (tmpClassViewComposer.getId() == anotherClassViewComposer.getId()) {
					continue;
				} else {
					for (final MethodBox mb: tmpClassViewComposer.getMethodBoxMap().values()) {
						if (!mb.getIsRemoved()) {
							setDragAndDropFunction(mb, anotherClassViewComposer.getClassBox());
						}
					}
				}
			}
		}

		for (ClassViewComposer tmpClassViewComposer: classViewComposerArray) {
			for (ClassViewComposer anotherClassViewComposer: classViewComposerArray) {
				if (tmpClassViewComposer.getId() == anotherClassViewComposer.getId()) {
					continue;
				} else {
					ArrayList<DependencyVector> externalDependencyVectorArray = createExternalDependencyVectorArray(
							tmpClassViewComposer,
							anotherClassViewComposer,
							p.getClass(tmpClassViewComposer.getClassBox().getName()).getExternalDependencies()
					);
					tmpClassViewComposer.setExternalDependencyVectorArray(externalDependencyVectorArray);
				}
			}
		}
		moveHistoryVectorArray = createMoveHistoryVectorArray();
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

		skin.add("default", new BitmapFont());

		TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
		textButtonStyle.up = skin.newDrawable("white", Color.DARK_GRAY);
		textButtonStyle.down = skin.newDrawable("white", Color.DARK_GRAY);
		textButtonStyle.over = skin.newDrawable("white", Color.LIGHT_GRAY);

		FileHandle file = Gdx.files.local("/home/kentaroishii/eclipse-workspace/sample/core/data/NuNimonade-M2.otf");
		FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(file);
       FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
       param.size = 35;
       param.color = Color.WHITE;
		textButtonStyle.font = fontGenerator.generateFont(param);
		skin.add("default", textButtonStyle);
		forwardButton = new TextButton("forward", skin);
		forwardButton.setSize(400, 75);
		forwardButton.setPosition(1500, 260);
		forwardButton.addListener(new ClickListener() {
			@Override
            public void clicked(InputEvent event, float x, float y) {
				 Log logElement = forward();
				 process(logElement);
				 load();
            }
		});

		rollbackButton = new TextButton("rollback", skin);
		rollbackButton.setSize(400, 75);
		rollbackButton.setPosition(1500, 160);
		rollbackButton.addListener(new ClickListener() {
			@Override
            public void clicked(InputEvent event, float x, float y) {
				p = pDefaultBuckup.clone();
				pDefault = pDefaultBuckup.clone();
				ArrayList<Log> logArray = rollback();
				for (Log logElement: logArray) {
					process(logElement);
				}
				load();
            }
		});

		TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
		textFieldStyle.font = fontGenerator.generateFont(param);
		textFieldStyle.fontColor = Color.BLACK;
		skin.add("default", textFieldStyle);

		// add class
		addClassButton = new TextButton("add", skin);
		addClassButton.setSize(80, 50);
		addClassButton.setPosition(1820, 1000);
		addClassButton.addListener(new ClickListener() {
			@Override
            public void clicked(InputEvent event, float x, float y) {
				AddClassLog addLog = new AddClassLog(addClassText);
				add(addLog);
				addClassText = "class name";
				addClassTextField.setText(addClassText);
            }
		});
		addClassTextField = new TextField("class name", skin);
		addClassTextField.setSize(300, 50);
		addClassTextField.setPosition(1500, 1000);
		addClassTextField.setColor(Color.BLACK);
		addClassTextField.setDebug(true);
		addClassTextField.setTextFieldListener(new TextField.TextFieldListener() {
	        @Override
	        public void keyTyped(TextField textField, char key) {
	            addClassText = textField.getText();
	         }
	       }
		);

		// add method
		addMethodButton = new TextButton("add", skin);
		addMethodButton.setSize(80, 50);
		addMethodButton.setPosition(1820, 800);
		addMethodButton.addListener(new ClickListener() {
			@Override
            public void clicked(InputEvent event, float x, float y) {
				AddMethodLog addLog = new AddMethodLog(addMethodText, addMethodTargetClassText);
				add(addLog);
				addMethodText = "method name";
				addMethodTextField.setText(addMethodText);
				addMethodTargetClassText = "class to add";
				addMethodTargetClassTextField.setText(addMethodTargetClassText);
            }
		});
		addMethodTextField = new TextField("method name", skin);
		addMethodTextField.setSize(300, 50);
		addMethodTextField.setPosition(1500, 800);
		addMethodTextField.setColor(Color.BLACK);
		addMethodTextField.setDebug(true);
		addMethodTextField.setTextFieldListener(new TextField.TextFieldListener() {
	        @Override
	        public void keyTyped(TextField textField, char key) {
	            addMethodText = textField.getText();
	         }
	       }
		);
		addMethodTargetClassTextField = new TextField("class to add", skin);
		addMethodTargetClassTextField.setSize(300, 50);
		addMethodTargetClassTextField.setPosition(1500, 870);
		addMethodTargetClassTextField.setColor(Color.BLACK);
		addMethodTargetClassTextField.setDebug(true);
		addMethodTargetClassTextField.setTextFieldListener(new TextField.TextFieldListener() {
	        @Override
	        public void keyTyped(TextField textField, char key) {
	            addMethodTargetClassText = textField.getText();
	         }
	       }
		);

		// add dependency
		addDependencyButton = new TextButton("add", skin);
		addDependencyButton.setSize(80, 50);
		addDependencyButton.setPosition(1820, 460);
		addDependencyButton.addListener(new ClickListener() {
			@Override
            public void clicked(InputEvent event, float x, float y) {
				AddDependencyLog addLog = new AddDependencyLog(
					addDependencySrcClassText,
					addDependencySrcMethodText,
					addDependencyDstClassText,
					addDependencyDstAttributeText
				);
				add(addLog);
				addDependencySrcClassText = "source class";
				addDependencySrcClassTextField.setText(addDependencySrcClassText);
				addDependencySrcMethodText = "source method";
				addDependencySrcMethodTextField.setText(addDependencySrcMethodText);
				addDependencyDstClassText = "dst class";
				addDependencyDstClassTextField.setText(addDependencyDstClassText);
				addDependencyDstAttributeText = "dst attribute";
				addDependencyDstAttributeTextField.setText(addDependencyDstAttributeText);
            }
		});
		addDependencySrcClassTextField = new TextField("source class", skin);
		addDependencySrcClassTextField.setSize(300, 50);
		addDependencySrcClassTextField.setPosition(1500, 670);
		addDependencySrcClassTextField.setColor(Color.BLACK);
		addDependencySrcClassTextField.setTextFieldListener(new TextField.TextFieldListener() {
	        @Override
	        public void keyTyped(TextField textField, char key) {
	        	addDependencySrcClassText = textField.getText();
	         }
	       }
		);
		addDependencySrcMethodTextField = new TextField("source method", skin);
		addDependencySrcMethodTextField.setSize(300, 50);
		addDependencySrcMethodTextField.setPosition(1500, 600);
		addDependencySrcMethodTextField.setColor(Color.BLACK);
		addDependencySrcMethodTextField.setTextFieldListener(new TextField.TextFieldListener() {
	        @Override
	        public void keyTyped(TextField textField, char key) {
	        	addDependencySrcMethodText = textField.getText();
	         }
	       }
		);
		addDependencyDstClassTextField = new TextField("dst class", skin);
		addDependencyDstClassTextField.setSize(300, 50);
		addDependencyDstClassTextField.setPosition(1500, 530);
		addDependencyDstClassTextField.setColor(Color.BLACK);
		addDependencyDstClassTextField.setTextFieldListener(new TextField.TextFieldListener() {
	        @Override
	        public void keyTyped(TextField textField, char key) {
	        	addDependencyDstClassText = textField.getText();
	         }
	       }
		);
		addDependencyDstAttributeTextField = new TextField("dst attribute", skin);
		addDependencyDstAttributeTextField.setSize(300, 50);
		addDependencyDstAttributeTextField.setPosition(1500, 460);
		addDependencyDstAttributeTextField.setColor(Color.BLACK);
		addDependencyDstAttributeTextField.setTextFieldListener(new TextField.TextFieldListener() {
	        @Override
	        public void keyTyped(TextField textField, char key) {
	        	addDependencyDstAttributeText = textField.getText();
	         }
	       }
		);
	}

	private void setDragAndDropFunction(Box b, final ClassBox targetClassBox) {
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
				String targetClassName = targetClassBox.getName();
				Class dstClass = p.getClass(targetClassName);

				// define srcClass
				Class srcClass = null;
				String methodName = source.getActor().getName();
				for (Class tmpClass: p.getClasses()) {
					if (tmpClass.has(methodName)) {
						srcClass = tmpClass;
					}
				}
				if (!srcClass.getName().equals(dstClass.getName())) {
					MoveLog moveLog = DependencyResolver.resolve(srcClass, dstClass, methodName);
					move(moveLog);
					load();
				}
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
		// draw base element
		batch.begin();
		forwardButton.draw(batch, 1.0f);
		rollbackButton.draw(batch, 1.0f);
		addClassButton.draw(batch, 1.0f);
		addClassTextField.draw(batch, 1.0f);
		addClassTextField.getStyle().font.draw(
		   batch,
		   "- add Class",
		   1500f,
		   1100f
	    );
		addMethodButton.draw(batch, 1.0f);
		addMethodTextField.draw(batch, 1.0f);
		addMethodTargetClassTextField.draw(batch, 1.0f);
		addMethodTextField.getStyle().font.draw(
		   batch,
		   "- add Method",
		   1500f,
		   970f
	    );
		addDependencyButton.draw(batch, 1.0f);
		addDependencySrcClassTextField.draw(batch, 1.0f);
		addDependencySrcMethodTextField.draw(batch, 1.0f);
		addDependencyDstClassTextField.draw(batch, 1.0f);
		addDependencyDstAttributeTextField.draw(batch, 1.0f);
		addDependencySrcClassTextField.getStyle().font.draw(
		   batch,
		   "- add Dependency",
		   1500f,
		   770f
	    );
		batch.end();
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(Color.BLACK);
		shapeRenderer.rect(
			addClassTextField.getX(),
			addClassTextField.getY(),
			addClassTextField.getWidth(),
			addClassTextField.getHeight()
		);
		shapeRenderer.rect(
			addMethodTargetClassTextField.getX(),
			addMethodTargetClassTextField.getY(),
			addMethodTargetClassTextField.getWidth(),
			addMethodTargetClassTextField.getHeight()
		);
		shapeRenderer.rect(
			addMethodTextField.getX(),
			addMethodTextField.getY(),
			addMethodTextField.getWidth(),
			addMethodTextField.getHeight()
		);
		shapeRenderer.rect(
			addDependencySrcClassTextField.getX(),
			addDependencySrcClassTextField.getY(),
			addDependencySrcClassTextField.getWidth(),
			addDependencySrcClassTextField.getHeight()
		);
		shapeRenderer.rect(
			addDependencySrcMethodTextField.getX(),
			addDependencySrcMethodTextField.getY(),
			addDependencySrcMethodTextField.getWidth(),
			addDependencySrcMethodTextField.getHeight()
		);
		shapeRenderer.rect(
			addDependencyDstClassTextField.getX(),
			addDependencyDstClassTextField.getY(),
			addDependencyDstClassTextField.getWidth(),
			addDependencyDstClassTextField.getHeight()
		);
		shapeRenderer.rect(
			addDependencyDstAttributeTextField.getX(),
			addDependencyDstAttributeTextField.getY(),
			addDependencyDstAttributeTextField.getWidth(),
			addDependencyDstAttributeTextField.getHeight()
		);
		shapeRenderer.end();
		for (ClassViewComposer classViewComposer: classViewComposerArray) {
			classViewComposer.getClassBox().draw(batch, 1.0f);
		}
		for (ClassViewComposer classViewComposer: classViewComposerArray) {
			for (PropertyBox p: classViewComposer.getPropertyBoxMap().values()) {
				p.draw(batch, 1.0f);
			}
			for (MethodBox m: classViewComposer.getMethodBoxMap().values()) {
				m.draw(batch, 1.0f);
			}
		}

		// draw vector
		for (ClassViewComposer classViewComposer: classViewComposerArray) {
			for (DependencyVector v: classViewComposer.getInternalDependencyVectorArray()) {
				drawDependencyVector(shapeRenderer, v);
			}
			for (DependencyVector v: classViewComposer.getExternalDependencyVectorArray()) {
				drawExternalDependencyVector(shapeRenderer, v);
			}
		}

		drawMoveHistorVector(shapeRenderer);
	}

	private void drawExternalDependencyVector(ShapeRenderer shapeRenderer, DependencyVector dependencyVector) {
		int lineWidth = 8;
		Point startPoint = dependencyVector.getStartPoint();
		Point endPoint = dependencyVector.getEndPoint();

		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(30/255f, 144/255f, 255/255f, 1.0f);
		
		// draw triangle
		int height = 36;
		int width = 14;
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
			shapeRenderer.setColor(30/255f, 144/255f, 255/255f, 1.0f);
		} else {
			shapeRenderer.setColor(50/255f, 205/255f, 50/255f, 1.0f);
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
			shapeRenderer.setColor(192/255f, 192/255f, 192/255f, 1.0f);
			
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
			int spanX = 25;
			int num = (int) Math.abs(endPoint.x - startPoint.x) / spanX;
			float spanY = Math.abs(endPoint.y - startPoint.y) / num;
			for (int i=0; i<num-1; i++) {
				if (i % 2 == 0) {
					shapeRenderer.rectLine(
						startPoint.x + i*spanX,
						startPoint.y + i*spanY,
						startPoint.x + (i+1)*spanX,
						startPoint.y + (i+1)*spanY,
						lineWidth
					);
				}
			}
//			shapeRenderer.rectLine(
//					startPoint.x,
//					startPoint.y,
//					endPoint.x + (p1X + p2X) / 2,
//					endPoint.y + (p1Y + p2Y) / 2,
//					lineWidth
//			);
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
					methodRegion.getLeftBottomPoint().y + methodRegion.getHeight() - interval * (count + 1) - MethodBox.METHOD_BOX_HEIGHT / 2
			);
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

	private ArrayList<DependencyVector> createExternalDependencyVectorArray(
			ClassViewComposer srcClassViewComposer,
			ClassViewComposer dstClassViewComposer,
			ArrayList<ExternalDependency> externalDependencies
	) {
		ArrayList<DependencyVector> externalDependencyVector = new ArrayList<DependencyVector>();
		for (ExternalDependency d: externalDependencies) {
			if (
				!(dstClassViewComposer.getMethodBoxMap().containsKey(d.getDstName()))
				& !(dstClassViewComposer.getPropertyBoxMap().containsKey(d.getDstName()))
			) {
				continue;
			}
			Box srcBox;
			Box dstBox;
			Point srcPoint;
			Point dstPoint;
			String srcName = d.getSrcName();
			String dstName = d.getDstName();

			srcBox = srcClassViewComposer.getMethodBoxMap().get(srcName);
			if (dstClassViewComposer.getMethodBoxMap().containsKey(dstName)) {
				dstBox = dstClassViewComposer.getMethodBoxMap().get(dstName);
			} else {
				dstBox = dstClassViewComposer.getPropertyBoxMap().get(dstName);
			}
			if (srcClassViewComposer.getId() < dstClassViewComposer.getId()) {
				srcPoint = srcBox.getRightConnectionPoints(1).get(0);
				dstPoint = dstBox.getLeftConnectionPoints(1).get(0);
			} else {
				srcPoint = srcBox.getLeftConnectionPoints(1).get(0);
				dstPoint = dstBox.getRightConnectionPoints(1).get(0);
			}
			externalDependencyVector.add(new DependencyVector(srcPoint, dstPoint, 0, false));
		}
		return externalDependencyVector;
	}

	private ArrayList<MoveHistoryVector> createMoveHistoryVectorArray(){
		moveHistoryVectorArray = new ArrayList<MoveHistoryVector>();
		ArrayList<Log> currentValidLogArray = player.getCurrentValidLogArray();
		for (Log currentLog: currentValidLogArray) {
			if (!(currentLog instanceof MoveLog)) {
				continue;
			} else {
				MoveLog currentMoveLog = (MoveLog) currentLog;
				String mainMoveSrcClassName = currentMoveLog.getSrcClassName();
				String mainMoveDstClassName = currentMoveLog.getDstClassName();
				String mainMoveAttributeName = currentMoveLog.getName();

				ClassViewComposer srcClassViewComposer = null;
				ClassViewComposer dstClassViewComposer = null;
				MethodBox movedBox = null;
				MethodBox toBox = null;
				for (ClassViewComposer classViewComposer: classViewComposerArray) {
					if (classViewComposer.getClassBox().getName().equals(mainMoveSrcClassName)) {
						movedBox = classViewComposer.getMethodBoxMap().get(mainMoveAttributeName);
						srcClassViewComposer = classViewComposer;
					}
					if (classViewComposer.getClassBox().getName().equals(mainMoveDstClassName)) {
						toBox = classViewComposer.getMethodBoxMap().get(mainMoveAttributeName);
						dstClassViewComposer = classViewComposer;
					}
				}
				MoveHistoryVector moveHistoryVector;
				if (srcClassViewComposer.getId() < dstClassViewComposer.getId()) {
					System.out.println(mainMoveSrcClassName);
					System.out.println(mainMoveDstClassName);
					System.out.println(mainMoveAttributeName);
					moveHistoryVector = new MoveHistoryVector(
						movedBox.getRightConnectionPoints(1).get(0), toBox.getLeftConnectionPoints(1).get(0)
					);
				} else {
					moveHistoryVector = new MoveHistoryVector(
						movedBox.getLeftConnectionPoints(1).get(0), toBox.getRightConnectionPoints(1).get(0)
					);
				};
				moveHistoryVectorArray.add(moveHistoryVector);

				// dependency move
				for (MoveLog autoMovedHistory: currentMoveLog.getAutoMoveArray()) {
					String autoMovedAttributeName = autoMovedHistory.getName();
					Boolean isMethod = srcClassViewComposer.getMethodBoxMap().containsKey(autoMovedAttributeName);
					Box autoMovedBox;
					Box autoToBox;
					MoveHistoryVector autoMoveHistoryVector;
					if (isMethod) {
						autoMovedBox = srcClassViewComposer.getMethodBoxMap().get(autoMovedAttributeName);
						autoToBox = dstClassViewComposer.getMethodBoxMap().get(autoMovedAttributeName);
					} else {
						autoMovedBox = srcClassViewComposer.getPropertyBoxMap().get(autoMovedAttributeName);
						autoToBox = dstClassViewComposer.getPropertyBoxMap().get(autoMovedAttributeName);
					}
					if (srcClassViewComposer.getId() < dstClassViewComposer.getId()) {
						autoMoveHistoryVector = new MoveHistoryVector(
							autoMovedBox.getRightConnectionPoints(1).get(0), autoToBox.getLeftConnectionPoints(1).get(0)
						);
					} else {
						autoMoveHistoryVector = new MoveHistoryVector(
							autoMovedBox.getLeftConnectionPoints(1).get(0), autoToBox.getRightConnectionPoints(1).get(0)
						);
					}
					moveHistoryVectorArray.add(autoMoveHistoryVector);
				}
			}
		}
		return moveHistoryVectorArray;
	}
}

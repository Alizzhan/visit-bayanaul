package ngp.visit;

import static java.lang.Math.max;
import static java.lang.Math.min;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.XmlReader;

import java.util.HashMap;

public class MainScreen extends NgpScreen {
    private final TourApp app;
    private final Stage stage;
    private TextButton title, aboutBn;
    private Image map;
    private Group languages;
    private Image selector;
    private Button en,kz,ru;
    private final Vector2 del3 = new Vector2();
    private final Vector2 del2 = new Vector2();
    private final Vector2 del1 = new Vector2();
    private final Array<LocationData> locations = new Array<>();
    private final HashMap<LocationData, Button> locButtons = new HashMap<>();
    private boolean dragging = false;
    private final float map_init_x = -2300;
    private final float map_init_y = -550;

    public MainScreen(TourApp app) {
        this.app = app;
        stage = new Stage();
        initLocs();
        initUI();
        Gdx.input.setInputProcessor(stage);
    }

    private void initLocs() {
        XmlReader reader = new XmlReader();
        XmlReader.Element root = reader.parse(Gdx.files.getFileHandle("locations.xml", Files.FileType.Internal));
        for (int i = 0; i < root.getChildCount(); i++){
            initLoc(root.getChild(i));
        }
    }

    public void initLoc(XmlReader.Element child){
        if (child.getName().equalsIgnoreCase("location")) {
            try{String folder = child.getAttribute("images");
                try{String url = child.getAttribute("mapUrl");
                    try{String icon = child.getAttribute("icon");
                        try{float x = child.getFloatAttribute("x");
                            try{float y = child.getFloatAttribute("y");
                                IntMap<String> names = new IntMap<>();
                                IntMap<String> texts = new IntMap<>();
                                for (int j = 0; j < child.getChildCount(); j++){
                                    XmlReader.Element grandchild = child.getChild(j);
                                    try{String lang = grandchild.get("language");
                                        int langCode = Style.languages.get(lang);
                                        names.put(langCode,grandchild.getAttribute("name"));
                                        texts.put(langCode,grandchild.getText());
                                    }catch (Exception e){}
                                }
                                LocationData locData = new LocationData(folder, icon, new Vector2(x, y), url, names, texts);locations.add(locData);
                            }catch (Exception e){}
                        }catch (Exception e){}
                    }catch (Exception e){}
                }catch (Exception e){}
            }catch (Exception e){}
        }
    }

    public void initUI() {
        title = new TextButton("Visit Bayanaul!", Style.styleTextLarge);
        aboutBn = new TextButton("About", Style.styleTextLarge);
        stage.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
            }
        });
        Texture mapTex = new Texture("images/map.jpg");
        int sW = Gdx.graphics.getWidth();
        int sH = Gdx.graphics.getHeight();
        TextureRegionDrawable mapTexD = new TextureRegionDrawable(mapTex);
        map = new Image(mapTexD, Scaling.fill, Align.center);
        DragListener mapDrag = new DragListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                dragging=true;
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);
                panMap(this.getDeltaX(), this.getDeltaY());
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                dragging=false;
                super.touchUp(event, x, y, pointer, button);
            }
        };
        map.addListener(mapDrag);
        stage.addActor(map);
        for (int i = 0; i < locations.size; i++){
            Button b = new Button(new TextureRegionDrawable(new Texture(locations.get(i).icon+".png")));
            int finalI = i;
            b.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    app.setScreen(new PlaceScreen(app, locations.get(finalI)));
                }
            });
            locButtons.put(locations.get(finalI),b);
            stage.addActor(b);
        }
        stage.addActor(title);
        languages = new Group();
        en = new Button(new TextureRegionDrawable(new Texture("images/ui/en.png")));
        kz = new Button(new TextureRegionDrawable(new Texture("images/ui/KZ.png")));
        ru = new Button(new TextureRegionDrawable(new Texture("images/ui/RU.png")));
        en.addListener(new ClickListener(){@Override public void clicked(InputEvent event, float x, float y) {
            super.clicked(event, x, y);
            if (app.language != Style.ENGLISH) {app.setLanguage(Style.ENGLISH); selector.setPosition(0,0);}
        }
        });
        kz.addListener(new ClickListener(){@Override public void clicked(InputEvent event, float x, float y) {
            super.clicked(event, x, y);
            if (app.language != Style.KAZAKH) {app.setLanguage(Style.KAZAKH); selector.setPosition(200,0);}
        }
        });
        ru.addListener(new ClickListener(){@Override public void clicked(InputEvent event, float x, float y) {
            super.clicked(event, x, y);
            if (app.language != Style.RUSSIAN) {app.setLanguage(Style.RUSSIAN); selector.setPosition(400,0);}
        }
        });
        Pixmap pix = new Pixmap(148,84, Pixmap.Format.RGB888);
        pix.setColor(Color.WHITE);
        selector = new Image(new Texture(pix));
        selector.setSize(148,84);
        languages.addActor(selector);
        languages.addActor(en);
        languages.addActor(kz);
        languages.addActor(ru);
        selector.setPosition(0,0);
        stage.addActor(languages);
        stage.addActor(aboutBn);
        resize(sW,sH);
    }

    private void panMap(float x, float y){
        float deltax = 0.75f*x+0.65f*del3.x+0.45f*del2.x+0.25f*del1.x;
        float deltay = 0.75f*y+0.65f*del3.y+0.45f*del2.y+0.25f*del1.y;
        float xAct = min(0,max(map.getX()+deltax,-4096+Gdx.graphics.getWidth()));
        float yAct = min(0,max(map.getY()+deltay,-2560+Gdx.graphics.getHeight()));
        map.setPosition(xAct,yAct);
        for(int i = 0; i < locations.size; i++){
            LocationData l = locations.get(i);
            xAct = l.coordinates.x-map_init_x+map.getX();
            yAct = l.coordinates.y-map_init_y+map.getY();
            if (locButtons.containsKey(l)) {
                locButtons.get(l).setPosition(xAct, yAct);
            }
        }
        del1.x = del2.x; del1.y = del2.y;
        del2.x = del3.x; del2.y = del3.y;
        del3.x = x; del3.y = y;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(Style.back.r, Style.back.g, Style.back.b, Style.back.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act();
        stage.draw();
        Vector2 scrC = stage.screenToStageCoordinates(new Vector2(Gdx.input.getX(0),Gdx.input.getY(0)));
        if (!dragging) del1.x=del1.y=del2.x=del2.y=del3.x=del3.y=0;
    }

    @Override
    public void resize(int width, int height) {
        title.setBounds(0.1f*width,height-0.1f*width-128,0.8f*width,128);
        map.setPosition(map_init_x,map_init_y);
        for(int i = 0; i < locations.size; i++){
            LocationData l = locations.get(i);
            if (locButtons.containsKey(l)) locButtons.get(l).setBounds(l.coordinates.x, l.coordinates.y, 64, 64);
        }
        en.setPosition(10,10); kz.setPosition(210,10); ru.setPosition(410,10);
        languages.setPosition(448, 32);
        aboutBn.setBounds(64, 32, 320,148);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void refreshText() {
        String about = (app.language==Style.ENGLISH)?"About Us":(app.language==Style.KAZAKH)?"(kz)About Us":"(ru)About us";
        aboutBn.setText(about);
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
        for(int i = 0; i < locations.size; i++){
            locations.get(i).dispose();
        }
    }
}

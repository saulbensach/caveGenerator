package com.bensach.saul;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;


import java.util.*;

/**
 * Created by saul- on 09/02/2016.
 *
 * El mundo recibe tales parámetros como
 * width ancho del mundo
 * height alto del mundo
 * numRooms el número de habitaciones que debe de tener
 * roomMaxWidth ancho máximo de la habitacion
 * roomMaxHeight alto máximo de la habitación
 * roomMinWidth mínimo ancho de la habitación
 * roomMinHeight alto mínimo de la habitación
 */

public class Level{

    /*Debug*/
    public boolean debug = false;
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private Box2DDebugRenderer debugRenderer;

    /*Generador*/
    private ArrayList<Point> points = new ArrayList<Point>();
    private ArrayList<Edge> edges = new ArrayList<Edge>();
    private ArrayList<Edge> mstEdges = new ArrayList<Edge>();
    private int pointNumber = 0;

    /*Map*/
    public World world;
    private TiledMap map;
    public Vector2 start = new Vector2(), end = new Vector2();
    public OrthogonalTiledMapRenderer renderer;
    private ArrayList<Rectangle> walls;
    private int width, height, numRooms, roomMaxWidth, roomMinWidth, roomMaxHeight, roomMinHeight;
    public CellType[][] cells;
    private ArrayList<Room> rooms;
    boolean firstRoom = false, endRoom = false;

    /*Light*/
    public RayHandler rayHandler;
    public float r = 1,g = 1,b = 0.8f,a = 0.020f;

    /*Texturas*/
    private TextureRegion grassTexture = new TextureRegion(new Texture(Gdx.files.internal("texturaCesped.png")));
    private TextureRegion dirtTexture = new TextureRegion(new Texture(Gdx.files.internal("dirt.png")));
    private TextureRegion stoneTexture = new TextureRegion(new Texture(Gdx.files.internal("stone.png")));


    public Level(int width, int height, int numRooms, int roomMaxWidth, int roomMinWidth, int roomMaxHeight, int roomMinHeight) {
        this.width = width;
        this.height = height;
        this.numRooms = numRooms;
        this.roomMaxWidth = roomMaxWidth;
        this.roomMinWidth = roomMinWidth;
        this.roomMaxHeight = roomMaxHeight;
        this.roomMinHeight = roomMinHeight;
        cells = new CellType[width][height];
        rooms = new ArrayList<Room>();
        walls = new ArrayList<Rectangle>();
        map = new TiledMap();
        renderer = new OrthogonalTiledMapRenderer(map);
        world = new World(new Vector2(0, 0), true);
        rayHandler = new RayHandler(world);
        debugRenderer = new Box2DDebugRenderer();
        executeMethods();
    }

    public void generateNewWorld(){
        map = new TiledMap();
        renderer = new OrthogonalTiledMapRenderer(map);
        firstRoom = false;endRoom = false;
        pointNumber = 0;
        rooms.clear();
        walls.clear();
        mstEdges.clear();
        edges.clear();
        points.clear();
        world = new World(new Vector2(0, 0f), true);
        rayHandler = new RayHandler(world);
        debugRenderer = new Box2DDebugRenderer();
        executeMethods();
    }

    private void executeMethods(){
        blankWorld();
        generateRooms();
        getDelaunayVertex();
        mst();
        generateCorridors();
        createLights();
        generateHitbox();
        toTiled();
        mapToBox2D();
    }

    public void draw(OrthographicCamera camera){
        rayHandler.setCombinedMatrix(camera);
        rayHandler.update();
        renderer.setView(camera);
        renderer.render();
        rayHandler.render();
        if(debug){
            RayHandler.useDiffuseLight(true);
            debugRenderer.render(world,camera.combined);
            shapeRenderer.setProjectionMatrix(camera.combined);
            //Test triangulacion Delaunay
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.WHITE);
            for(Edge e : edges){
                shapeRenderer.rectLine(e.getP1().getX()*16, e.getP1().getY()*16, e.getP2().getX()*16, e.getP2().getY()*16,2);
            }
            shapeRenderer.setColor(Color.RED);
            for(Edge e : mstEdges){
                shapeRenderer.circle(e.getP1().getX() * 16, e.getP1().getY() * 16, 20);
                shapeRenderer.circle(e.getP2().getX() * 16, e.getP2().getY() * 16, 20);
                shapeRenderer.rectLine(e.getP1().getX()*16, e.getP1().getY()*16, e.getP2().getX()*16, e.getP2().getY()*16,2);
            }
            for(Rectangle r : walls){
                shapeRenderer.rect(r.getX()*16, r.getY()*16, r.getWidth()*16, r.getHeight() * 16);
            }
            shapeRenderer.end();
        }else{
            RayHandler.useDiffuseLight(false);
        }

        world.step(1/60f, 6, 2);
    }

    private void mapToBox2D(){
        for(Rectangle w : walls){
            BodyDef bodyDef = new BodyDef();
            bodyDef.position.set((w.getX() * 16) + 8 , (w.getY() * 16) + 8);
            Body body = world.createBody(bodyDef);
            PolygonShape polygonShape = new PolygonShape();
            polygonShape.setAsBox(8,8);
            body.createFixture(polygonShape, 0.0f);
            polygonShape.dispose();
        }
    }

    //Saca el minimum spanning tree
    public void mst(){
        Collections.sort(edges);
        Graph graph = new Graph(points.size(), edges.size());

        for(int i = 0; i < edges.size(); i++){
            graph.edge[i].src = edges.get(i).getP1().getValue();
            graph.edge[i].dest = edges.get(i).getP2().getValue();
            graph.edge[i].weight = (int) edges.get(i).getWeight();
        }

        //Las aristas q devuelve result se compara el peso con las que tengo y las que son igual son las que sirven
        Edge[] result = graph.KruskalMST();
        for(int i = 0; i < result.length; i++){
            for(int j = 0; j < edges.size(); j++){
                if(edges.get(j).weight == result[i].weight){
                    mstEdges.add(edges.get(j));
                }
            }
        }
    }

    //Triangulamos el mapa con las condiciones de delaunay
    private void getDelaunayVertex(){
        for (int i = 0; i < points.size(); i++) {
            for (int j = i+1; j < points.size(); j++) {
                for (int k = j+1; k < points.size(); k++) {
                    boolean isTriangle = true;
                    for (int a = 0; a < points.size(); a++) {
                        if (a == i || a == j || a == k) continue;
                        if (points.get(a).inside(points.get(i), points.get(j), points.get(k))) {
                            isTriangle = false;
                            break;
                        }
                    }
                    if (isTriangle) {
                        edges.add(new Edge(points.get(i), points.get(j)));
                        edges.add(new Edge(points.get(i), points.get(k)));
                        edges.add(new Edge(points.get(j), points.get(k)));
                    }
                }
            }
        }
    }

    private void createLights(){
        rayHandler.setAmbientLight(r,g,b,a);
        for(Room r : rooms){
           //new PointLight(rayHandler, 200, new Color(1,1,0.8f,0.3f), 800f, (r.getX() + r.getWidth() / 2)*16, (r.getY() + r.getHeight() / 2)*16);
        }
    }

    //Convierte la matriz a un mapa tiled
    private void toTiled(){
        MapLayers layers = map.getLayers();
        TiledMapTileLayer layer = new TiledMapTileLayer(width,height,16,16);
        TiledMapTileLayer emptyLayer = new TiledMapTileLayer(width, height, 16, 16);
        for(int y = 0; y < layer.getHeight(); y++){
            for(int x = 0; x < layer.getWidth(); x++){
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                switch (cells[x][y]){
                    case Empty: cell.setTile(new StaticTiledMapTile(dirtTexture));emptyLayer.setCell(x,y,cell);break;
                    case Floor: cell.setTile(new StaticTiledMapTile(grassTexture));layer.setCell(x,y,cell);break;
                    case Pasillo: cell.setTile(new StaticTiledMapTile(stoneTexture));layer.setCell(x,y,cell);break;
                    case Wall: cell.setTile(new StaticTiledMapTile(dirtTexture));layer.setCell(x,y,cell);break;
                    case Start: cell.setTile(new StaticTiledMapTile(dirtTexture));layer.setCell(x,y,cell);break;
                    case End: cell.setTile(new StaticTiledMapTile(dirtTexture));layer.setCell(x,y,cell);break;
                }
            }
        }
        emptyLayer.setOpacity(0);
        layers.add(emptyLayer);
        layers.add(layer);
    }

    //Genera el total de salas indicando
    private void generateRooms(){
        int generatedRooms = 0,roomWidth, roomHeight, px, py;

        while(generatedRooms < numRooms){
            //Se queda pegando vueltas hasta que salga un valor correcto!
            while(true){
                roomWidth = generateBetween(roomMaxWidth, roomMinWidth);
                roomHeight = generateBetween(roomMaxHeight, roomMinHeight);
                px = generateBetween(width, 0);
                py = generateBetween(height, 0);

                //Comprobamos los valores
                if(checkValues(px,py,roomWidth,roomHeight)){
                    if(!overlapRoom(px, py, roomWidth, roomHeight))break;
                }
            }
            //System.out.println("X: "+px+" Y: "+py+" Ancho: "+roomWidth+" Alto: "+roomHeight);
            placeRoom(px, py, roomWidth, roomHeight);
            rooms.add(new Room(px,roomHeight,roomWidth,py));
            points.add(new Point(px + roomWidth / 2, py + roomHeight / 2, pointNumber++));
            generatedRooms++;
        }
    }

    //Genera los pasillos A*
    private void generateCorridors(){
        Pathfinder finder = new Pathfinder(width, height, 1);
        for (Edge e : mstEdges){
            finder.SetGridNode((int)e.getP1().getX(), (int)e.getP1().getY(), CellType.Start);
            finder.SetGridNode((int)e.getP2().getX(), (int)e.getP2().getY(), CellType.End);

            finder.findPath();

            Array<GridNode> nodes = finder.GetPath();
            //Hace que los pasillos sean mas anchos
            for(GridNode n : nodes){
                for(int i = 0; i < 3; i++){
                    cells[(int) n.X][(int) n.Y] = CellType.Pasillo;
                    cells[(int) n.X + i][(int) n.Y] = CellType.Pasillo;
                    cells[(int) n.X - i][(int) n.Y] = CellType.Pasillo;
                    cells[(int) n.X][(int) n.Y + i] = CellType.Pasillo;
                    cells[(int) n.X][(int) n.Y - i] = CellType.Pasillo;
                    cells[(int) n.X+i][(int) n.Y+i] = CellType.Pasillo;
                    cells[(int) n.X-i][(int) n.Y-i] = CellType.Pasillo;
                    cells[(int) n.X+i][(int) n.Y-i] = CellType.Pasillo;
                    cells[(int) n.X-i][(int) n.Y+i] = CellType.Pasillo;
                }

            }

            //Como los pasillos se meten dentro de las salas, se vuelven a repasar las salas para sobreescribir la celda
            for(Room r : rooms){
                for(int y = r.getY(); y < r.getHeight() + r.getY(); y++){
                    for(int x = r.getX(); x < r.getWidth() + r.getX(); x++){
                        if(y != 0 || x != 0 || x != width - 1 || y != height - 1){
                            cells[x][y] = CellType.Floor;
                        }
                    }
                }
            }
        }
    }

    //Coloca la sala en el lugar indicado
    private void placeRoom(int px, int py, int width, int height){
        for(int y = py; y < height + py; y++){
            for(int x = px; x < width + px; x++){
                cells[x][y] = CellType.Floor;
            }
        }

        //Colocamos dos puntos a las dos primeras salas una es el inicio otra el final
        if(!endRoom && firstRoom){
            cells[px + width / 2][py + height / 2] = CellType.End;
            end.set(px + width / 2,py + height / 2);
            endRoom = true;
        }

        if(!firstRoom){
            cells[px + width / 2][py + height / 2] = CellType.Start;
            start.set(px + width / 2,py + height / 2);
            firstRoom = true;
        }
    }

    //Comprueba si choca con alguna sala
    private boolean overlapRoom(int px, int py, int width, int height){
        for(int y = py; y < height + py; y++){
            for(int x = px; x < width + px; x++){
                if(cells[x][y] == CellType.Floor)return true;
            }
        }
        return false;
    }

    //Comprueba si la sala puede llegar a existir
    private boolean checkValues(int x, int y, int width, int height){
        if(x < 0 || y < 0)return false;
        if(x + width > this.width)return false;
        if(y + height > this.height)return false;
        return true;
    }

    //Genera los rectangulos que representan solidos
    private void generateHitbox(){
        for(int y = 1; y < height - 1; y++){
            for(int x = 1; x < width - 1; x++){

                //Coloca las paredes de las salas y los pasillos
                if(cells[x][y] == CellType.Empty){
                    if(
                        cells[x + 1][y] == CellType.Floor || cells[x + 1][y] == CellType.Pasillo ||
                        cells[x - 1][y] == CellType.Floor || cells[x - 1][y] == CellType.Pasillo ||
                        cells[x][y + 1] == CellType.Floor || cells[x][y + 1] == CellType.Pasillo ||
                        cells[x][y - 1] == CellType.Floor || cells[x][y - 1] == CellType.Pasillo ||
                        cells[x + 1][y + 1] == CellType.Floor || cells[x][y + 1] == CellType.Pasillo ||
                        cells[x - 1][y - 1] == CellType.Floor || cells[x][y - 1] == CellType.Pasillo ||
                        cells[x - 1][y + 1] == CellType.Floor || cells[x][y + 1] == CellType.Pasillo ||
                        cells[x + 1][y - 1] == CellType.Floor || cells[x][y - 1] == CellType.Pasillo
                    ){
                        walls.add(new Rectangle(x,y,1,1));
                        cells[x][y] = CellType.Wall;
                    }
                }

            }
        }
    }

    //Devuelve un numero entre ese rango
    private int generateBetween(int max, int min){
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

    //Crea un mundo vacio
    private void blankWorld(){
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                if(y == 0 || x == 0 || x == width - 1 || y == height - 1){
                    walls.add(new Rectangle(x,y,1,1));
                    cells[x][y] = CellType.Wall;
                }else{
                    cells[x][y] = CellType.Empty;
                }
            }
        }
    }
}

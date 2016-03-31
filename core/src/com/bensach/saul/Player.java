package com.bensach.saul;

import box2dLight.ConeLight;
import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import java.util.ArrayList;

/**
 * Created by saul- on 18/02/2016.
 */
public class Player extends Sprite {

    private boolean up,right,left,down,light, cRight, cLeft, shoot;
    private Texture bulletTexture = new Texture("bullet.png");
    private ConeLight coneLight;
    private PointLight pointLight;
    private Sprite cannon;
    public float speed = 50000f;
    private float rotVelocity = 0.05f;
    private float cannonRotVel = 200f;
    private Level level;
    private Vector2 velocity;
    private Vector2 lookAt, barrelLookAt, bulletSpawnPoint;
    private Body tanque;
    private ArrayList<Body> b_bullets;
    private ArrayList<Sprite> s_bullets;
    private ArrayList<PointLight> l_bullets;
    private float shootTime = 1;
    private float totalTime = 0;

    /**/
    private PolygonShape polygonShape = new PolygonShape();
    private FixtureDef fixtureDef = new FixtureDef();


    public Player(float x, float y, Level level){
        super(new Texture("cuerpoTanque.png"));
        this.level = level;
        cannon = new Sprite(new Texture("cannon.png"));
        b_bullets = new ArrayList<>();
        s_bullets = new ArrayList<>();
        l_bullets = new ArrayList<>();
        cannon.setScale(0.5f);
        setScale(0.5f);
        setPosition(x, y);
        cannon.setOrigin(9,10);
        pointLight = new PointLight(level.rayHandler, 100, new Color(1,1,0.8f, 0.5f), 140f, getX() + getWidth() / 2, getY() + getHeight() / 2);
        coneLight = new ConeLight(level.rayHandler, 300, new Color(1,1,0.8f,0.7f),400f, getX() + getWidth() / 2, getY() + getHeight() / 2,0,45);
        lookAt = new Vector2(0.001f,0.001f);
        barrelLookAt = new Vector2(0,0);
        cannon.setPosition((getX() + getWidth() / 2) - 9, (getY() + getHeight() / 2 - 10));
        bulletSpawnPoint = new Vector2(cannon.getX() + cannon.getWidth(),cannon.getY() + cannon.getHeight());
        velocity = new Vector2(0, 0);
        createBody();
        pointLight.attachToBody(tanque);
        pointLight.setIgnoreAttachedBody(true);
    }

    private void createBody(){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(getX() + getWidth() / 2, getY() + getWidth() / 2);
        tanque = level.world.createBody(bodyDef);
        PolygonShape polygonShape = new PolygonShape();
        polygonShape.setAsBox(getWidth() / 4, getHeight() / 4);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = polygonShape;
        fixtureDef.density = 0.01f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.01f;

        tanque.createFixture(fixtureDef);
        polygonShape.dispose();
    }

    public void update(float delta){
        /*if(shoot){
            totalTime += delta;
            if(totalTime > 0.3f){
                createBullet();
                totalTime = 0;
            }
        }*/
        if(up){
            lookAt.x = (float) Math.cos(Math.toRadians(getRotation()));
            lookAt.y = (float) Math.sin(Math.toRadians(getRotation()));
            if(lookAt.len() > 0)lookAt.nor();
            if(lookAt.len2() > 0)lookAt.nor();
            tanque.applyForce(lookAt.x * speed ,lookAt.y * speed, tanque.getPosition().x, tanque.getPosition().y, true);
        }
        if(down){
            lookAt.x = (float) Math.cos(Math.toRadians(getRotation()));
            lookAt.y = (float) Math.sin(Math.toRadians(getRotation()));
            if(lookAt.len() > 0)lookAt.nor();
            if(lookAt.len2() > 0)lookAt.nor();
            tanque.applyForce(-lookAt.x * speed ,-lookAt.y * speed, tanque.getPosition().x, tanque.getPosition().y, true);
        }
        if(right){
            //setPosition(getX() + velocity * delta, getY());
            tanque.setTransform(tanque.getPosition(), -rotVelocity);
            coneLight.setDirection(getRotation());
        }
        if(left){
            //setPosition(getX() - velocity * delta, getY());
            tanque.setTransform(tanque.getPosition(), rotVelocity);
            coneLight.setDirection(getRotation());
        }
        if(!up && !down)tanque.setLinearDamping(5);
        if(!right && !left)tanque.setTransform(tanque.getPosition(), 0);
        if(cRight){
            barrelLookAt.x = (float) Math.cos(Math.toRadians(getRotation()));
            barrelLookAt.y = (float) Math.cos(Math.toRadians(getRotation()));
            if(barrelLookAt.len() > 0)barrelLookAt.nor();
            if(barrelLookAt.len2() > 0)barrelLookAt.nor();
            cannon.rotate(-cannonRotVel * delta);
        }
        if(cLeft){
            barrelLookAt.x = (float) Math.cos(Math.toRadians(getRotation()));
            barrelLookAt.y = (float) Math.cos(Math.toRadians(getRotation()));
            if(barrelLookAt.len() > 0)barrelLookAt.nor();
            if(barrelLookAt.len2() > 0)barrelLookAt.nor();
            cannon.rotate(cannonRotVel * delta);
        }
        coneLight.setActive(light);
        rotate((float) Math.toDegrees(tanque.getAngle()));
        cannon.rotate((float) Math.toDegrees(tanque.getAngle()));
        pointLight.setPosition(getX() + getWidth() / 2, getY() + getHeight() / 2);
        coneLight.setPosition(getX() + getWidth() / 2, getY() + getHeight() / 2);
        cannon.setPosition((getX() + getWidth() / 2) - 9, (getY() + getHeight() / 2 - 10));
        setPosition(tanque.getPosition().x  - getWidth() / 2, tanque.getPosition().y - getHeight() / 2);
        bulletSpawnPoint.set(getX() + getWidth(), getY() + getHeight() / 2 - 10);
        for (int i = 0; i < s_bullets.size(); i++){
            float x = s_bullets.get(i).getX(), y = s_bullets.get(i).getY();
            x += 100; y+=100;
            s_bullets.get(i).setPosition(x,y);
            l_bullets.get(i).setPosition(x,y );
        }
    }

    private void createBullet(){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(bulletSpawnPoint.x, bulletSpawnPoint.y);
        Body b = level.world.createBody(bodyDef);
        b.setBullet(true);

        polygonShape.setAsBox(bulletTexture.getWidth() / 4, bulletTexture.getHeight() / 4);
        fixtureDef.shape = polygonShape;
        fixtureDef.density = 0.001f;
        fixtureDef.friction = 0.0f;
        fixtureDef.restitution = 0.0f;

        b.createFixture(fixtureDef);
        //b.applyLinearImpulse(barrelLookAt.x * 100000, barrelLookAt.y * 100000, b.getPosition().x, b.getPosition().y, false);
        b.setLinearVelocity(0, 1000);
        b.setTransform(b.getPosition(), (float) Math.toRadians(cannon.getRotation()));
        b_bullets.add(b);
        Sprite s = new Sprite(bulletTexture);
        s.setScale(0.5f);
        s_bullets.add(s);
        l_bullets.add(new PointLight(level.rayHandler, 100));
    }

    public void draw(Batch batch){
        super.draw(batch);
        cannon.draw(batch);
        for(int i = 0; i < b_bullets.size(); i++){
            s_bullets.get(i).draw(batch);
        }
    }

    public void setUp(boolean up) {
        this.up = up;
    }

    public void setRight(boolean right) {
        this.right = right;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public void setDown(boolean down) {
        this.down = down;
    }

    public void setLight(boolean light) {
        this.light = light;
    }

    public void setcRight(boolean cRight) {
        this.cRight = cRight;
    }

    public void setcLeft(boolean cLeft) {
        this.cLeft = cLeft;
    }

    public boolean isLight() {
        return light;
    }

    public void setShoot(boolean shoot) {
        this.shoot = shoot;
    }

    public boolean isShoot() {
        return shoot;
    }
}

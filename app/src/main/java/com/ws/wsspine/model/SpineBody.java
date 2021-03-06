package com.ws.wsspine.model;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Event;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonBounds;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonJson;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.esotericsoftware.spine.SkeletonRendererDebug;
import com.esotericsoftware.spine.attachments.BoundingBoxAttachment;
import com.esotericsoftware.spine.utils.TwoColorPolygonBatch;

public class SpineBody extends ApplicationAdapter {

    OrthographicCamera camera;
    TwoColorPolygonBatch batch;
    SkeletonRenderer renderer;
    SkeletonRendererDebug debugRenderer;

    SkeletonBounds bounds;
    TextureAtlas atlas;
    Skeleton skeleton;
    SkeletonData skeletonData;
    AnimationState state;
    SkeletonJson json;

    boolean isClickAnimation = false;

    private float width;
    private float height;

    private String defaultAnimation = "idle";

    String [] animations = new String[]{"death", "hit", "idle", "run", "jump", "shoot","walk"};
    int indexAnimation = 0;

    public SpineBody(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public void create() {
        camera = new OrthographicCamera();
        batch = new TwoColorPolygonBatch();
        renderer = new SkeletonRenderer();
        renderer.setPremultipliedAlpha(true); // PMA results in correct blending without outlines.
        debugRenderer = new SkeletonRendererDebug();
        debugRenderer.setMeshTriangles(false);
//        debugRenderer.setBoundingBoxes(false);
        debugRenderer.setRegionAttachments(false);
        debugRenderer.setMeshHull(false);

        atlas = new TextureAtlas(Gdx.files.internal("spineboy/spineboy-pma.atlas"));
        json = new SkeletonJson(atlas); // This loads skeleton JSON data, which is stateless.
        // Load the skeleton at 60% the size it was in Spine.
        Pair<Float, Float> paintSize = readSkeletonSize(Gdx.files.internal("spineboy/spineboy-ess.json")); //只读取数据， 也可由外部传入
        json.setScale(Math.min(this.width / paintSize.first, this.height / paintSize.second));
        skeletonData = json.readSkeletonData(Gdx.files.internal("spineboy/spineboy-ess.json"));

        skeleton = new Skeleton(skeletonData); // Skeleton holds skeleton state (bone positions, slot attachments, etc).

        skeleton.setPosition(this.width / 2, (this.width / paintSize.first) > (this.height / paintSize.second)? 0 : (this.height - paintSize.second * json.getScale())  / 2);
//        skeleton.setAttachment("liuhai1", "liuhai1");
        skeleton.setAttachment("head-bb", "head");

        bounds = new SkeletonBounds(); // Convenience class to do hit detection with bounding boxes.

        AnimationStateData stateData = new AnimationStateData(skeletonData); // Defines mixing (crossfading) between animations.

        state = new AnimationState(stateData); // Holds the animation state for a skeleton (current animation, time, etc).
        state.setTimeScale(1.0f); // Slow all animations down to 50% speed.

        // Queue animations on track 0.
        state.setAnimation(0, defaultAnimation, true);

        addListener();

        setSkin("default");
        skeleton.setAttachment("head-bb", "head");
    }

    private void addListener() {
        state.addListener(new AnimationState.AnimationStateListener() {
            @Override
            public void start(AnimationState.TrackEntry entry) {
                if(!TextUtils.equals(entry.getAnimation().getName(), defaultAnimation)){
                    isClickAnimation = true;
                }
            }

            @Override
            public void interrupt(AnimationState.TrackEntry entry) {
                if(!TextUtils.equals(entry.getAnimation().getName(), defaultAnimation)){
                    isClickAnimation = false;
                }
            }

            @Override
            public void end(AnimationState.TrackEntry entry) {
                if(!TextUtils.equals(entry.getAnimation().getName(), defaultAnimation)){
                    isClickAnimation = false;
                }
            }

            @Override
            public void dispose(AnimationState.TrackEntry entry) {

            }

            @Override
            public void complete(AnimationState.TrackEntry entry) {
                if(!TextUtils.equals(entry.getAnimation().getName(), defaultAnimation)){
                    isClickAnimation = false;
                }
            }

            @Override
            public void event(AnimationState.TrackEntry entry, Event event) {

            }
        });
//        Gdx.input.setInputProcessor(new InputAdapter() {
//            final Vector3 point = new Vector3();
//
//            public boolean touchDown (int screenX, int screenY, int pointer, int button) {
//                Log.d("spine_log", "screenX : " + screenX);
//                Log.d("spine_log", "screenY : " + screenX);
//                camera.unproject(point.set(screenX, screenY, 0)); // Convert window to world coordinates.
//                bounds.update(skeleton, true); // Update SkeletonBounds with current skeleton bounding box positions.
//                if (bounds.aabbContainsPoint(point.x, point.y)) { // Check if inside AABB first. This check is fast.
//                    BoundingBoxAttachment hit = bounds.containsPoint(point.x, point.y); // Check if inside a bounding box.
//                    if (hit != null) {
//                        changeAnimation();
//                    }
//                }
//                return true;
//            }
//
//            public boolean touchUp (int screenX, int screenY, int pointer, int button) {
////                if(!isClickAnimation){
////                    state.setAnimation(0, "kuajiao", false); // Set animation on track 0 to jump.
////                    state.addAnimation(0, "daiji", true, 0); // Queue run to play after jump.
////                }
//                return true;
//            }
//
//            public boolean keyDown (int keycode) {
//                switch (keycode){
//                    case 8:
//                        state.setAnimation(0, defaultAnimation, false); // Set animation on track 0 to jump.!
//                        break;
//                    case 9:
//                        state.setAnimation(0, defaultAnimation, false); // Set animation on track 0 to jump.
//                        break;
//                }
//                state.addAnimation(0, defaultAnimation, true, 0); // Queue run to play after jump.
//                return true;
//            }
//        });

    }

    public void render() {
        state.update(Gdx.graphics.getDeltaTime()); // Update the animation time.

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Gdx.gl.glClearColor(0, 0, 0, 0);

        if (state.apply(skeleton))// Poses skeleton using current animations. This sets the bones' local SRT.
        skeleton.updateWorldTransform(); // Uses the bones' local SRT to compute their world SRT.

        // Configure the camera, SpriteBatch, and SkeletonRendererDebug.
        camera.update();
        batch.getProjectionMatrix().set(camera.combined);
        debugRenderer.getShapeRenderer().setProjectionMatrix(camera.combined);

        batch.begin();
        renderer.draw(batch, skeleton); // Draw the skeleton images.
        batch.end();
//        debugRenderer.draw(skeleton); // Draw debug lines.
    }

    public void resize(int width, int height) {
        camera.setToOrtho(false); // Update camera with new size.
    }

    public void dispose() {
        atlas.dispose();
    }


    public void zoomBig() {
        camera.zoom = 0.5f;
    }

    public void zoomSmall() {
        camera.zoom = 1f;
    }


    public void setSkin(String skinName) {
        skeleton.setSkin(skinName);
        skeleton.setSlotsToSetupPose();
    }

    public void setAnimation(String animationName){
        state.setAnimation(0, animationName, false); // Set animation on track 0 to jump.
        state.addAnimation(0, defaultAnimation, true, 0); // Queue run to play after jump.
    }

    public void changeAnimation(){
        if(!isClickAnimation){
            indexAnimation = (++indexAnimation) % animations.length;
            setAnimation(animations[indexAnimation]);
        }
    }


    public void setTouchEvent(View view){
        if(view != null){
            view.setOnTouchListener(new View.OnTouchListener() {
                final Vector3 point = new Vector3();
                @Override
                public boolean onTouch(View view, MotionEvent event) {
//                    Log.d("spine_log", "getX : " + event.getX());
//                    Log.d("spine_log", "getY : " + event.getY());
                    if(event.getAction() == MotionEvent.ACTION_DOWN){
                        camera.unproject(point.set(event.getX(), event.getY(), 0)); // Convert window to world coordinates.
                        bounds.update(skeleton, true); // Update SkeletonBounds with current skeleton bounding box positions.
                        if (bounds.aabbContainsPoint(point.x, point.y)) { // Check if inside AABB first. This check is fast.
                            skeleton.findSlot("head").getColor().set(Color.RED); // Turn head red until touchUp.
                            BoundingBoxAttachment hit = bounds.containsPoint(point.x, point.y); // Check if inside a bounding box.
                            if (hit != null) {
                                changeAnimation();
                            }
                            return true;
                        }
                    }else if(event.getAction() == MotionEvent.ACTION_UP){
                        skeleton.findSlot("head").getColor().set(Color.WHITE);
                    }

                    return false;
                }
            });
        }

    }



    public Pair<Float, Float> readSkeletonSize (FileHandle file) {
        Pair<Float, Float> result = new Pair<>(1f, 1f);
        if (file == null) throw new IllegalArgumentException("file cannot be null.");
        JsonValue root = new JsonReader().parse(file);

        JsonValue skeletonMap = root.get("skeleton");
        if (skeletonMap != null) {
            result = new Pair<>(skeletonMap.getFloat("width", 1), skeletonMap.getFloat("height", 1));
        }
        return result;
    }
}

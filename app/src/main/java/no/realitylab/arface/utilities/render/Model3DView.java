package no.realitylab.arface.utilities.render;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;

public class Model3DView extends GLSurfaceView {

    private Model3DView renderer;

    public Model3DView(Context context) {
        super(context);
        init();
    }

    public Model3DView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setEGLContextClientVersion(2);
        //renderer = new Model3DRenderer();
        //setRenderer(renderer);
    }

    public void loadModel(String modelFilename) {
        try {
            InputStream inputStream = getContext().getAssets().open(modelFilename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }
}

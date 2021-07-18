package io.ycnicholas.pomoro.splashscreen;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import io.ycnicholas.pomoro.R;
import io.ycnicholas.pomoro.menu.MenuActivity;

public class SplashScreenActivity extends Activity implements Animator.AnimatorListener {
    private AnimatorSet animatorSet;
    private ImageView appIcon;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        appIcon = (ImageView) findViewById(R.id.app_icon);
        appIcon.setAlpha(0f);

        //goToMenuActivity();
        startLogoAnimation();
    }

    public void startLogoAnimation() {
        animatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.set_splashscreen);
        animatorSet.setTarget(appIcon);
        animatorSet.start();
        animatorSet.addListener(this);
    }

    public void goToMenuActivity() {
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        animatorSet.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        animatorSet.pause();
    }

    @Override
    public void onAnimationStart(Animator animation) {
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        goToMenuActivity();
    }

    @Override
    public void onAnimationCancel(Animator animation) {
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
    }
}

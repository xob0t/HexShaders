package ru.serjik.wallpaper;

import android.app.Activity;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

public abstract class BaseSettingsActivity extends Activity {
    private int savedSystemUiVisibility = 0;

    private View.OnClickListener rootClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            view.setOnClickListener(null);
            view.setClickable(false);
            showUI();
        }
    };

    protected void showUI() {
        getWindow().getDecorView().setSystemUiVisibility(this.savedSystemUiVisibility);
        getWindow().getDecorView().findViewWithTag("ui").setVisibility(View.VISIBLE);
    }

    private void sendDropContextCommand() {
        Intent intent = new Intent(this, getWallpaperServiceClass());
        intent.putExtra("cmd", "dropContext");
        startService(intent);
    }

    private void setupHideButton() {
        View hideButton = getWindow().getDecorView().findViewWithTag("button_hide_ui");
        if (hideButton != null) {
            hideButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideUI();
                }
            });
        }
    }

    private void setupSetWallpaperButton() {
        View setWallpaperButton = getWindow().getDecorView().findViewWithTag("button_set_wallpaper");
        if (setWallpaperButton != null) {
            setWallpaperButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isTaskRoot()) {
                        setAsWallpaper();
                    }
                    finish();
                }
            });
            setWallpaperButton.setVisibility(isWallpaperAlreadySet() ? View.GONE : View.VISIBLE);
        }
    }

    private boolean isWallpaperAlreadySet() {
        WallpaperManager wm = WallpaperManager.getInstance(this);
        WallpaperInfo info = wm.getWallpaperInfo();
        if (info != null) {
            if (getClass().getCanonicalName().equals(info.getSettingsActivity())) {
                wm.forgetLoadedWallpaper();
                return true;
            }
        }
        return false;
    }

    protected void setAsWallpaper() {
        try {
            ComponentName component = new ComponentName(getWallpaperServiceClass().getPackage().getName(), getWallpaperServiceClass().getCanonicalName());
            Intent intent = new Intent("android.service.wallpaper.CHANGE_LIVE_WALLPAPER");
            intent.putExtra("android.service.wallpaper.extra.LIVE_WALLPAPER_COMPONENT", component);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            try {
                startActivity(new Intent("android.service.wallpaper.LIVE_WALLPAPER_CHOOSER"));
            } catch (ActivityNotFoundException e2) {
                try {
                    Intent intent = new Intent();
                    intent.setAction("com.bn.nook.CHANGE_WALLPAPER");
                    startActivity(intent);
                } catch (ActivityNotFoundException e3) {
                    Toast.makeText(getBaseContext(), "something goes wrong", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    protected abstract Class<?> getWallpaperServiceClass();

    protected void hideUI() {
        getWindow().getDecorView().findViewWithTag("root").setClickable(true);
        getWindow().getDecorView().findViewWithTag("root").setOnClickListener(this.rootClickListener);
        getWindow().getDecorView().findViewWithTag("ui").setVisibility(View.GONE);
        this.savedSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
        getWindow().getDecorView().setSystemUiVisibility(3847);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sendDropContextCommand();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupSetWallpaperButton();
        setupHideButton();
    }
}
